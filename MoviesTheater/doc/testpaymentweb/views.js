'use strict';

const config = require('./config');

function escapeHtml(str) {
  return String(str).replace(/[&<>"']/g, (c) => ({
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#39;',
  }[c]));
}

function layout(title, body) {
  return `<!doctype html>
<html lang="vi">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>${escapeHtml(title)}</title>
<style>
  :root { color-scheme: light; }
  * { box-sizing: border-box; }
  body {
    font-family: -apple-system, Segoe UI, Roboto, Helvetica, Arial, sans-serif;
    background: #f2f4f7; margin: 0; padding: 32px 16px; color: #1a1f2b;
  }
  .wrap { max-width: 720px; margin: 0 auto; }
  .card {
    background: #fff; border-radius: 12px; padding: 28px 32px;
    box-shadow: 0 1px 3px rgba(0,0,0,0.08); margin-bottom: 20px;
  }
  h1 { font-size: 22px; margin: 0 0 4px; }
  h2 { font-size: 17px; margin: 0 0 16px; color: #333; }
  .sub { color: #667085; font-size: 14px; margin-bottom: 24px; }
  label { display: block; font-size: 13px; font-weight: 600; margin: 16px 0 6px; color: #344054; }
  input, select {
    width: 100%; padding: 10px 12px; border: 1px solid #d0d5dd; border-radius: 8px;
    font-size: 14px; background: #fff;
  }
  button {
    margin-top: 22px; width: 100%; padding: 12px; border: none; border-radius: 8px;
    background: #d6172c; color: #fff; font-size: 15px; font-weight: 600; cursor: pointer;
  }
  button:hover { background: #b8121f; }
  button.secondary { background: #344054; }
  button.secondary:hover { background: #1d2939; }
  table { width: 100%; border-collapse: collapse; font-size: 13px; }
  th, td { text-align: left; padding: 8px 6px; border-bottom: 1px solid #eaecf0; }
  th { color: #667085; font-weight: 600; }
  .badge { display: inline-block; padding: 2px 10px; border-radius: 999px; font-size: 12px; font-weight: 600; }
  .badge.pending { background: #fef0c7; color: #93370d; }
  .badge.success { background: #d1fadf; color: #05603a; }
  .badge.failed { background: #fee4e2; color: #912018; }
  .result-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #eaecf0; font-size: 14px; }
  .result-row span:first-child { color: #667085; }
  .valid-yes { color: #05603a; font-weight: 700; }
  .valid-no { color: #912018; font-weight: 700; }
  a { color: #d6172c; }
  pre { background: #101828; color: #d1fae5; padding: 14px; border-radius: 8px; overflow-x: auto; font-size: 12px; }
  .config { font-size: 12px; color: #667055; }
  code { background: #f2f4f7; padding: 2px 6px; border-radius: 4px; }
</style>
</head>
<body>
<div class="wrap">
${body}
</div>
</body>
</html>`;
}

function maskSecret(secret) {
  if (!secret || secret.length < 8) return '****';
  return `${secret.slice(0, 4)}${'*'.repeat(secret.length - 8)}${secret.slice(-4)}`;
}

function homePage(orders) {
  const rows = orders
    .slice()
    .reverse()
    .slice(0, 15)
    .map(
      (o) => `<tr>
        <td>${escapeHtml(o.txnRef)}</td>
        <td>${Number(o.amount).toLocaleString('vi-VN')} đ</td>
        <td>${escapeHtml(o.bankCode || '(tất cả)')}</td>
        <td><span class="badge ${o.status}">${o.status}</span></td>
        <td>${o.ipnReceived ? '✅' : '—'}</td>
      </tr>`
    )
    .join('');

  const body = `
  <div class="card">
    <h1>VNPay Sandbox – Test tạo QR &amp; thanh toán</h1>
    <div class="sub">Tạo một giao dịch test, chuyển sang cổng VNPay sandbox, quét QR / chọn phương thức test và quay lại xem kết quả đối soát chữ ký.</div>
    <form method="POST" action="/create_payment">
      <label>Số tiền (VND)</label>
      <input type="number" name="amount" min="1000" step="1000" value="100000" required>

      <label>Nội dung thanh toán</label>
      <input type="text" name="orderInfo" value="Thanh toan don hang test" required>

      <label>Loại hàng hóa (vnp_OrderType)</label>
      <select name="orderType">
        <option value="other" selected>other</option>
        <option value="topup">topup</option>
        <option value="billpayment">billpayment</option>
        <option value="fashion">fashion</option>
      </select>

      <label>Phương thức thanh toán (vnp_BankCode)</label>
      <select name="bankCode">
        <option value="" selected>(Trống) – hiển thị đầy đủ danh sách phương thức đã bật cho merchant này</option>
        <option value="VNPAYQR">VNPAYQR – vào thẳng màn hình quét QR (chỉ chạy nếu merchant đã bật ví/QR)</option>
        <option value="NCB">NCB – thẻ nội địa (test, thường bật sẵn)</option>
        <option value="INTCARD">INTCARD – thẻ quốc tế (test)</option>
      </select>

      <label>Ngôn ngữ</label>
      <select name="locale">
        <option value="vn" selected>Tiếng Việt</option>
        <option value="en">English</option>
      </select>

      <button type="submit">Tạo thanh toán →</button>
    </form>
  </div>

  <div class="card">
    <h2>Giao dịch test gần đây</h2>
    ${
      orders.length
        ? `<table>
      <thead><tr><th>Mã GD (TxnRef)</th><th>Số tiền</th><th>BankCode</th><th>Trạng thái</th><th>IPN</th></tr></thead>
      <tbody>${rows}</tbody>
    </table>`
        : '<p style="color:#667085;font-size:14px;">Chưa có giao dịch nào.</p>'
    }
  </div>

  <div class="card config">
    <strong>Cấu hình hiện tại</strong><br>
    vnp_TmnCode: <code>${escapeHtml(config.vnp_TmnCode)}</code><br>
    vnp_HashSecret: <code>${escapeHtml(maskSecret(config.vnp_HashSecret))}</code><br>
    vnp_Url: <code>${escapeHtml(config.vnp_Url)}</code><br>
    vnp_ReturnUrl: <code>${escapeHtml(config.vnp_ReturnUrl)}</code><br>
    vnp_IpnUrl: <code>${escapeHtml(config.vnp_IpnUrl)}</code>
    <br><br>
    ⚠️ vnp_IpnUrl trỏ vào localhost nên VNPay sandbox (server thật) sẽ không gọi tới được.
    Dùng nút "Giả lập gọi IPN" ở trang kết quả để tự kiểm tra logic IPN, hoặc dùng ngrok/localtunnel
    nếu cần test IPN thật từ VNPay.
    <br><br>
    ⚠️ Nếu chọn <code>VNPAYQR</code> mà báo lỗi "Ngân hàng thanh toán không được hỗ trợ": merchant
    sandbox này chưa được bật phương thức ví/QR. Hãy để trống <code>vnp_BankCode</code> (mặc định) để
    xem VNPay hiển thị đúng các phương thức đã bật, hoặc vào Merchant Admin
    (<a href="https://sandbox.vnpayment.vn/merchantv2/" target="_blank" rel="noopener">merchantv2</a>)
    kiểm tra/bật QR cho TmnCode này.
  </div>
  `;
  return layout('VNPay Sandbox Test', body);
}

function returnPage({ query, verify, order }) {
  const code = query.vnp_ResponseCode;
  const amount = query.vnp_Amount ? Number(query.vnp_Amount) / 100 : null;
  const qs = new URLSearchParams(query).toString();

  const rows = [
    ['Mã đơn hàng (vnp_TxnRef)', query.vnp_TxnRef],
    ['Số tiền', amount !== null ? `${amount.toLocaleString('vi-VN')} đ` : '—'],
    ['Ngân hàng (vnp_BankCode)', query.vnp_BankCode || '—'],
    ['Mã giao dịch VNPay (vnp_TransactionNo)', query.vnp_TransactionNo || '—'],
    ['Thời gian thanh toán (vnp_PayDate)', query.vnp_PayDate || '—'],
    ['Mã phản hồi (vnp_ResponseCode)', `${code} – ${escapeHtml(require('./vnpay').describeResponseCode(code))}`],
    ['Chữ ký hợp lệ?', verify.valid ? '<span class="valid-yes">HỢP LỆ</span>' : '<span class="valid-no">KHÔNG HỢP LỆ</span>'],
  ];

  const body = `
  <div class="card">
    <h1>${verify.valid && code === '00' ? '✅ Thanh toán thành công' : verify.valid ? '⚠️ Giao dịch không thành công' : '❌ Sai chữ ký'}</h1>
    <div class="sub">Kết quả trả về từ VNPay tại vnp_ReturnUrl (client redirect – không cần URL public).</div>
    ${rows.map(([k, v]) => `<div class="result-row"><span>${escapeHtml(k)}</span><span>${v}</span></div>`).join('')}
    ${order ? `<div class="result-row"><span>Trạng thái đơn (bộ nhớ)</span><span><span class="badge ${order.status}">${order.status}</span></span></div>` : ''}

    <button type="button" class="secondary" onclick="simulateIpn()">Giả lập gọi IPN (test nội bộ) →</button>
    <pre id="ipn-result" style="display:none;"></pre>

    <p style="margin-top:20px;"><a href="/">← Về trang tạo giao dịch</a></p>
  </div>
  <script>
    async function simulateIpn() {
      const pre = document.getElementById('ipn-result');
      pre.style.display = 'block';
      pre.textContent = 'Đang gọi /vnpay_ipn ...';
      try {
        const res = await fetch('/vnpay_ipn?${qs}');
        const data = await res.json();
        pre.textContent = 'HTTP ' + res.status + '\\n' + JSON.stringify(data, null, 2);
      } catch (e) {
        pre.textContent = 'Lỗi: ' + e;
      }
    }
  </script>
  `;
  return layout('Kết quả thanh toán', body);
}

module.exports = { homePage, returnPage, escapeHtml };
