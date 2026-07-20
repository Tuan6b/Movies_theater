package com.cinema.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class VNPAYConfig {

    private static final Logger LOG = Logger.getLogger(VNPAYConfig.class.getName());

    private static final String ENV_TMN_CODE = "VNP_TMN_CODE";
    private static final String ENV_HASH_SECRET = "VNP_HASH_SECRET";
    private static final String DEFAULT_TMN_CODE = "95G1N7NZ";
    private static final String DEFAULT_HASH_SECRET = "91PWH0PP04AHDDOBMZU9PJ6ATRP3VJ9Q";

    public static final String TMN_CODE = getEnv(ENV_TMN_CODE, DEFAULT_TMN_CODE);
    public static final String HASH_SECRET = getEnv(ENV_HASH_SECRET, DEFAULT_HASH_SECRET);
    public static final String PAY_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    public static final String RETURN_URL = "/vnpay?action=return";
    public static final String IPN_URL = "/vnpay?action=ipn";

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private static String getEnv(String key, String fallback) {
        String val = System.getenv(key);
        if (val == null || val.isEmpty()) {
            LOG.warning("Environment variable " + key + " is not set. Using default value.");
            return fallback;
        }
        return val;
    }

    public static String hmacSHA512(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec spec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac.init(spec);
            byte[] hash = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("HMAC-SHA512 error", e);
        }
    }

    public static String buildSignedUrl(Map<String, String> params, String baseUrl) {
        TreeMap<String, String> sorted = new TreeMap<>(params);
        String queryString = sorted.entrySet().stream()
                .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        String hashData = sorted.entrySet().stream()
                .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
                .filter(e -> !"vnp_SecureHash".equals(e.getKey()))
                .filter(e -> !"vnp_OrderInfo".equals(e.getKey()))
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));

        String secureHash = hmacSHA512(HASH_SECRET, hashData);
        return baseUrl + "?" + queryString + "&vnp_SecureHash=" + secureHash;
    }

    public static boolean verifySignature(Map<String, String> params) {
        String receivedHash = params.get("vnp_SecureHash");
        if (receivedHash == null) return false;

        String hashData = new TreeMap<>(params).entrySet().stream()
                .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
                .filter(e -> !"vnp_SecureHash".equals(e.getKey()))
                .filter(e -> !"vnp_OrderInfo".equals(e.getKey()))
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));

        String computedHash = hmacSHA512(HASH_SECRET, hashData);
        return computedHash.equalsIgnoreCase(receivedHash);
    }

    public static String formatDate(LocalDateTime dateTime) {
        return dateTime.format(DATE_FMT);
    }
}
