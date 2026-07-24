# VNPay Sandbox Test Site

Website tối giản (Node.js thuần, không cần `npm install`) để test tạo thanh toán / QR VNPay ở môi trường sandbox.

## Chạy

```bash
node server.js
```

Mở trình duyệt: http://localhost:3000

## Luồng test

1. Ở trang chủ, nhập số tiền, nội dung, chọn phương thức (`VNPAYQR` sẽ vào thẳng màn hình quét QR).
2. Bấm **Tạo thanh toán** → được chuyển sang trang VNPay sandbox (`vnp_Url`) với chữ ký hợp lệ.
3. Test bằng thẻ/tài khoản test do VNPay cung cấp (xem trong tài liệu tích hợp sandbox hoặc trong Merchant Admin), hoặc quét QR bằng app ngân hàng test.
4. VNPay redirect trình duyệt về `/vnpay_return` — trang này tự đối soát chữ ký (HMAC SHA512) và hiển thị kết quả.
5. Vì `vnp_IpnUrl` trỏ vào `localhost` nên **VNPay sandbox thật không gọi được** IPN của bạn (không public). Trang kết quả có nút **"Giả lập gọi IPN"** để tự test logic xử lý IPN nội bộ (kiểm tra chữ ký, số tiền, chống xử lý trùng).
6. Nếu cần test IPN thật từ VNPay (ví dụ qua công cụ SIT testing bạn được cấp), hãy expose server qua `ngrok http 3000` rồi cập nhật biến môi trường `VNP_IPNURL` (và khai báo IPN URL đó trong Merchant Admin sandbox).

### Lỗi "Ngân hàng thanh toán không được hỗ trợ"

Đây là lỗi từ phía VNPay (không phải lỗi chữ ký/code): merchant sandbox (`vnp_TmnCode`) chưa được
cấu hình bật phương thức bạn chọn trong `vnp_BankCode` (ví dụ `VNPAYQR`). Không phải TmnCode test nào
cũng bật sẵn ví/QR — thường chỉ bật sẵn thẻ nội địa test (NCB). Cách xử lý:

- Để trống `vnp_BankCode` (mặc định của form) để VNPay tự hiển thị đầy đủ các phương thức **đã được bật**
  cho merchant này.
- Vào [Merchant Admin sandbox](https://sandbox.vnpayment.vn/merchantv2/) kiểm tra/bật phương thức QR/ví
  cho TmnCode `95G1N7NZ` nếu muốn test riêng luồng QR.

## Cấu hình

Mặc định lấy đúng thông tin sandbox đã cung cấp, có thể override qua biến môi trường:

- `VNP_TMNCODE`
- `VNP_HASHSECRET`
- `VNP_URL`
- `VNP_RETURNURL`
- `VNP_IPNURL`
- `PORT` (mặc định 3000)

## Cấu trúc

- `server.js` – HTTP server thuần (không dùng Express), định tuyến `/`, `/create_payment`, `/vnpay_return`, `/vnpay_ipn`, `/api/orders`.
- `vnpay.js` – build URL thanh toán, ký/xác thực chữ ký HMAC-SHA512 theo đúng quy tắc VNPay (sort key, encode value, `%20`→`+`).
- `config.js` – thông tin cấu hình (đọc từ env, fallback về giá trị sandbox đã cho).
- `views.js` – render HTML (không dùng template engine ngoài).

Đơn hàng test được lưu tạm trong bộ nhớ (mất khi restart server) — chỉ phục vụ mục đích test, không dùng cho production.
