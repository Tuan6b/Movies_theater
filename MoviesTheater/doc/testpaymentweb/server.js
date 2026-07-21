'use strict';

const http = require('http');
const { URL } = require('url');

const config = require('./config');
const vnpay = require('./vnpay');
const views = require('./views');

// In-memory "database" of test orders: txnRef -> order
const orders = new Map();

function readBody(req) {
  return new Promise((resolve, reject) => {
    let data = '';
    req.on('data', (chunk) => {
      data += chunk;
      if (data.length > 1e6) req.destroy();
    });
    req.on('end', () => resolve(data));
    req.on('error', reject);
  });
}

function sendHtml(res, status, html) {
  res.writeHead(status, { 'Content-Type': 'text/html; charset=utf-8' });
  res.end(html);
}

function sendJson(res, status, obj) {
  res.writeHead(status, { 'Content-Type': 'application/json; charset=utf-8' });
  res.end(JSON.stringify(obj));
}

function notFound(res) {
  sendHtml(res, 404, '<h1>404 Not Found</h1><p><a href="/">Về trang chủ</a></p>');
}

async function handleCreatePayment(req, res) {
  const bodyStr = await readBody(req);
  const form = new URLSearchParams(bodyStr);

  const amount = Number(form.get('amount'));
  const orderInfo = form.get('orderInfo') || 'Thanh toan don hang test';
  const orderType = form.get('orderType') || 'other';
  const bankCode = form.get('bankCode') || '';
  const locale = form.get('locale') || 'vn';

  if (!amount || amount < 1000) {
    return sendHtml(res, 400, '<h1>Số tiền không hợp lệ</h1><p><a href="/">Quay lại</a></p>');
  }

  const txnRef = String(Date.now());
  const ipAddr = vnpay.getClientIp(req);

  const paymentUrl = vnpay.buildPaymentUrl({
    amount,
    orderInfo,
    orderType,
    bankCode,
    locale,
    txnRef,
    ipAddr,
  });

  orders.set(txnRef, {
    txnRef,
    amount,
    orderInfo,
    orderType,
    bankCode,
    status: 'pending',
    ipnReceived: false,
    createdAt: new Date().toISOString(),
  });

  res.writeHead(302, { Location: paymentUrl });
  res.end();
}

function handleReturn(req, res, url) {
  const query = Object.fromEntries(url.searchParams.entries());
  const verify = vnpay.verifySignature(query);
  const order = orders.get(query.vnp_TxnRef);

  if (order) {
    order.returnReceived = true;
    if (verify.valid) {
      order.status = query.vnp_ResponseCode === '00' ? 'success' : 'failed';
    }
  }

  sendHtml(res, 200, views.returnPage({ query, verify, order }));
}

function handleIpn(req, res, url) {
  const query = Object.fromEntries(url.searchParams.entries());
  const verify = vnpay.verifySignature(query);

  if (!verify.valid) {
    return sendJson(res, 200, { RspCode: '97', Message: 'Invalid signature' });
  }

  const order = orders.get(query.vnp_TxnRef);
  if (!order) {
    return sendJson(res, 200, { RspCode: '01', Message: 'Order not found' });
  }

  const expectedAmount = Math.round(order.amount) * 100;
  if (Number(query.vnp_Amount) !== expectedAmount) {
    return sendJson(res, 200, { RspCode: '04', Message: 'Invalid amount' });
  }

  if (order.ipnReceived) {
    return sendJson(res, 200, { RspCode: '02', Message: 'Order already confirmed' });
  }

  order.ipnReceived = true;
  order.status = query.vnp_ResponseCode === '00' ? 'success' : 'failed';
  order.ipnResponseCode = query.vnp_ResponseCode;

  sendJson(res, 200, { RspCode: '00', Message: 'Confirm Success' });
}

const server = http.createServer(async (req, res) => {
  const url = new URL(req.url, `http://${req.headers.host}`);

  try {
    if (req.method === 'GET' && url.pathname === '/') {
      return sendHtml(res, 200, views.homePage(Array.from(orders.values())));
    }
    if (req.method === 'POST' && url.pathname === '/create_payment') {
      return await handleCreatePayment(req, res);
    }
    if (req.method === 'GET' && url.pathname === '/vnpay_return') {
      return handleReturn(req, res, url);
    }
    if ((req.method === 'GET' || req.method === 'POST') && url.pathname === '/vnpay_ipn') {
      return handleIpn(req, res, url);
    }
    if (req.method === 'GET' && url.pathname === '/api/orders') {
      return sendJson(res, 200, Array.from(orders.values()));
    }
    return notFound(res);
  } catch (err) {
    console.error(err);
    sendHtml(res, 500, `<h1>500</h1><pre>${views.escapeHtml(err.stack || String(err))}</pre>`);
  }
});

server.listen(config.port, () => {
  console.log(`VNPay test site running at http://localhost:${config.port}`);
  console.log(`vnp_ReturnUrl: ${config.vnp_ReturnUrl}`);
  console.log(`vnp_IpnUrl:    ${config.vnp_IpnUrl} (not publicly reachable from localhost)`);
});
