'use strict';

const crypto = require('crypto');
const config = require('./config');

// VNPay requires params sorted by key name, with each value percent-encoded
// (encodeURIComponent, then %20 -> +) before the sign string / query string
// is built. Because every VNPay param name is plain ASCII (letters, digits,
// underscore), encodeURIComponent(key) === key, so sorting the raw keys is
// equivalent to the official sample's "sort the encoded keys" approach.
function sortAndEncode(params) {
  const sortedKeys = Object.keys(params).sort();
  const encoded = {};
  for (const key of sortedKeys) {
    encoded[key] = encodeURIComponent(String(params[key])).replace(/%20/g, '+');
  }
  return encoded;
}

function stringify(sortedEncodedParams) {
  return Object.entries(sortedEncodedParams)
    .map(([k, v]) => `${k}=${v}`)
    .join('&');
}

function signParams(params, secret) {
  const signData = stringify(sortAndEncode(params));
  return crypto.createHmac('sha512', secret).update(Buffer.from(signData, 'utf-8')).digest('hex');
}

// Formats a Date as yyyyMMddHHmmss in Asia/Ho_Chi_Minh (UTC+7), independent
// of the host machine's local timezone.
function formatVnDate(date) {
  const shifted = new Date(date.getTime() + 7 * 60 * 60 * 1000);
  const pad = (n) => String(n).padStart(2, '0');
  return (
    shifted.getUTCFullYear() +
    pad(shifted.getUTCMonth() + 1) +
    pad(shifted.getUTCDate()) +
    pad(shifted.getUTCHours()) +
    pad(shifted.getUTCMinutes()) +
    pad(shifted.getUTCSeconds())
  );
}

function getClientIp(req) {
  const xff = req.headers['x-forwarded-for'];
  let ip = xff ? xff.split(',')[0].trim() : req.socket.remoteAddress;
  if (ip === '::1') ip = '127.0.0.1';
  if (ip && ip.startsWith('::ffff:')) ip = ip.slice(7);
  return ip || '127.0.0.1';
}

function buildPaymentUrl({ amount, orderInfo, orderType, bankCode, locale, txnRef, ipAddr }) {
  const now = new Date();
  const params = {
    vnp_Version: '2.1.0',
    vnp_Command: 'pay',
    vnp_TmnCode: config.vnp_TmnCode,
    vnp_Locale: locale || 'vn',
    vnp_CurrCode: 'VND',
    vnp_TxnRef: txnRef,
    vnp_OrderInfo: orderInfo,
    vnp_OrderType: orderType || 'other',
    vnp_Amount: Math.round(amount) * 100,
    vnp_ReturnUrl: config.vnp_ReturnUrl,
    vnp_IpAddr: ipAddr,
    vnp_CreateDate: formatVnDate(now),
  };
  if (bankCode) {
    params.vnp_BankCode = bankCode;
  }

  const signData = stringify(sortAndEncode(params));
  const secureHash = crypto
    .createHmac('sha512', config.vnp_HashSecret)
    .update(Buffer.from(signData, 'utf-8'))
    .digest('hex');

  return `${config.vnp_Url}?${signData}&vnp_SecureHash=${secureHash}`;
}

// Verifies the SecureHash on an incoming return/IPN query object
// (plain key -> decoded-string map, e.g. from URLSearchParams).
function verifySignature(queryParams) {
  const params = { ...queryParams };
  const receivedHash = params.vnp_SecureHash;
  delete params.vnp_SecureHash;
  delete params.vnp_SecureHashType;

  const computedHash = signParams(params, config.vnp_HashSecret);
  return {
    valid: Boolean(receivedHash) && computedHash === receivedHash,
    computedHash,
    receivedHash,
  };
}

const RESPONSE_CODES = {
  '00': 'Giao dịch thành công',
  '07': 'Trừ tiền thành công, giao dịch bị nghi ngờ gian lận',
  '09': 'Thẻ/Tài khoản chưa đăng ký Internet Banking',
  10: 'Xác thực thông tin thẻ/tài khoản không đúng quá 3 lần',
  11: 'Đã hết hạn chờ thanh toán',
  12: 'Thẻ/Tài khoản bị khóa',
  13: 'Sai mật khẩu xác thực giao dịch (OTP)',
  24: 'Khách hàng hủy giao dịch',
  51: 'Tài khoản không đủ số dư',
  65: 'Tài khoản đã vượt quá hạn mức giao dịch trong ngày',
  75: 'Ngân hàng thanh toán đang bảo trì',
  79: 'Nhập sai mật khẩu thanh toán quá số lần quy định',
  99: 'Lỗi khác',
};

function describeResponseCode(code) {
  return RESPONSE_CODES[code] || `Mã phản hồi không xác định (${code})`;
}

module.exports = {
  buildPaymentUrl,
  verifySignature,
  getClientIp,
  formatVnDate,
  describeResponseCode,
};
