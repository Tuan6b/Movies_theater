/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cinema.util;

import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Helper for building signed VNPay sandbox payment URLs and verifying the
 * signature on the return callback. Sandbox credentials below are the
 * project's registered VNPay test merchant account.
 *
 * @author tuan6b
 */
public class VNPayUtil {

    // Sandbox merchant credentials — override via VNP_TMNCODE / VNP_HASHSECRET
    // environment variables in any environment where this repo's history is
    // not trusted; the literals below are only a local-demo fallback and
    // should be rotated with VNPay if this repository is ever made public.
    private static final String VNP_TMNCODE = envOrDefault("VNP_TMNCODE", "95G1N7NZ");
    private static final String VNP_HASHSECRET = envOrDefault("VNP_HASHSECRET", "91PWH0PP04AHDDOBMZU9PJ6ATRP3VJ9Q");

    private static String envOrDefault(String envVar, String defaultValue) {
        String value = System.getenv(envVar);
        return (value != null && !value.trim().isEmpty()) ? value : defaultValue;
    }
    private static final String VNP_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * Generates a unique transaction reference for a new payment attempt.
     */
    public static String generateTxnRef() {
        return String.valueOf(System.currentTimeMillis());
    }

    /**
     * Builds a fully-signed VNPay sandbox payment URL for the given order.
     *
     * @param txnRef unique transaction reference for this payment attempt
     * @param amountVnd order total in whole VND (not multiplied by 100 yet)
     * @param orderInfo ASCII order description shown on the VNPay page
     * @param ipAddr the customer's IP address
     * @param returnUrl absolute URL VNPay redirects back to after payment
     * @return the full redirect URL to send the customer to
     */
    public static String buildPaymentUrl(String txnRef, long amountVnd, String orderInfo,
            String ipAddr, String returnUrl) {
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", VNP_TMNCODE);
        params.put("vnp_Locale", "vn");
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_OrderInfo", orderInfo);
        params.put("vnp_OrderType", "other");
        params.put("vnp_Amount", String.valueOf(amountVnd * 100));
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpAddr", ipAddr);
        params.put("vnp_CreateDate", formatVnDate());

        String signData = buildSignData(params);
        String secureHash = hmacSHA512(VNP_HASHSECRET, signData);
        return VNP_URL + "?" + signData + "&vnp_SecureHash=" + secureHash;
    }

    /**
     * Verifies the vnp_SecureHash on a return/IPN callback against the
     * registered hash secret. Params must contain the raw (decoded) values
     * as received from the request, including vnp_SecureHash.
     */
    public static boolean verifyReturn(Map<String, String> params) {
        String receivedHash = params.get("vnp_SecureHash");
        if (receivedHash == null || receivedHash.trim().isEmpty()) {
            return false;
        }

        TreeMap<String, String> toSign = new TreeMap<>(params);
        toSign.remove("vnp_SecureHash");
        toSign.remove("vnp_SecureHashType");

        String signData = buildSignData(toSign);
        String computedHash = hmacSHA512(VNP_HASHSECRET, signData);
        return computedHash.equalsIgnoreCase(receivedHash);
    }

    private static String buildSignData(Map<String, String> sortedParams) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append('&');
            }
            sb.append(entry.getKey()).append('=').append(vnpEncode(entry.getValue()));
        }
        return sb.toString();
    }

    /**
     * Percent-encodes a value the same way JavaScript's encodeURIComponent
     * does, then replaces %20 with + — matching VNPay's required signing
     * format exactly.
     */
    private static String vnpEncode(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            int c = b & 0xFF;
            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')
                    || c == '-' || c == '_' || c == '.' || c == '!' || c == '~' || c == '*'
                    || c == '\'' || c == '(' || c == ')') {
                sb.append((char) c);
            } else {
                sb.append('%').append(String.format("%02X", c));
            }
        }
        return sb.toString().replace("%20", "+");
    }

    private static String formatVnDate() {
        return ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).format(DATE_FORMAT);
    }

    private static String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] result = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : result) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute VNPay signature", e);
        }
    }
}
