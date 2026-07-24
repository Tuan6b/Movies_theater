'use strict';

const port = process.env.PORT || 3000;

module.exports = {
  port,
  vnp_TmnCode: process.env.VNP_TMNCODE || '95G1N7NZ',
  vnp_HashSecret: process.env.VNP_HASHSECRET || '91PWH0PP04AHDDOBMZU9PJ6ATRP3VJ9Q',
  vnp_Url: process.env.VNP_URL || 'https://sandbox.vnpayment.vn/paymentv2/vpcpay.html',
  vnp_ReturnUrl: process.env.VNP_RETURNURL || `http://localhost:${port}/vnpay_return`,
  vnp_IpnUrl: process.env.VNP_IPNURL || `http://localhost:${port}/vnpay_ipn`,
};
