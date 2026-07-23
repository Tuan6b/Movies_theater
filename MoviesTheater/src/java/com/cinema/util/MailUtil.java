/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cinema.util;

import com.cinema.model.BookingScheduleView;
import com.cinema.model.Food;
import com.cinema.model.Ticket;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Sends e-ticket confirmation emails via SMTP using Jakarta Mail.
 *
 * @author tuan6b
 */
public class MailUtil {

    // SMTP parameters — the password has no source fallback: set the
    // SMTP_APP_PASSWORD environment variable before booking a ticket.
    // Read lazily (not as a static final) so a missing var surfaces as a
    // normal MessagingException from sendTicketEmail, not a class-init Error
    // that would bypass the caller's best-effort try/catch.
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String SMTP_USERNAME = envOrDefault("SMTP_USERNAME", "");
    private static final String SENDER_NAME = "CGV Cinema";

    private static String requireEnv(String envVar) throws MessagingException {
        String value = System.getenv(envVar);
        if (value == null || value.trim().isEmpty()) {
            throw new MessagingException(envVar + " environment variable is not configured");
        }
        return value;
    }

    private static String envOrDefault(String envVar, String defaultValue) {
        String value = System.getenv(envVar);
        return (value != null && !value.trim().isEmpty()) ? value : defaultValue;
    }

    /**
     * Sends the e-ticket email for a completed booking to the customer.
     *
     * @throws MessagingException if the SMTP send fails (e.g. credentials not configured yet)
     */
    public static void sendTicketEmail(String toEmail, String toName, BookingScheduleView schedule,
            List<Ticket> tickets, List<Integer> seatIds, List<String> seatNames,
            Map<Integer, Integer> foodQuantities, Map<Integer, Food> foodMap,
            double totalAmount, String paymentMethod) throws MessagingException {

        Session session = buildSession(requireEnv("SMTP_APP_PASSWORD"));

        MimeMessage message = new MimeMessage(session);
        try {
            message.setFrom(new InternetAddress(SMTP_USERNAME, SENDER_NAME));
        } catch (UnsupportedEncodingException e) {
            message.setFrom(new InternetAddress());
        }
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("Vé xem phim CGV Cinema - " + schedule.getMovieName(), "UTF-8");
        message.setContent(buildTicketEmailHtml(toName, schedule, tickets, seatIds, seatNames,
                foodQuantities, foodMap, totalAmount, paymentMethod), "text/html; charset=UTF-8");

        Transport.send(message);
    }

    private static Session buildSession(String appPassword) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USERNAME, appPassword);
            }
        });
    }

    private static String buildTicketEmailHtml(String toName, BookingScheduleView schedule,
            List<Ticket> tickets, List<Integer> seatIds, List<String> seatNames,
            Map<Integer, Integer> foodQuantities, Map<Integer, Food> foodMap,
            double totalAmount, String paymentMethod) {

        StringBuilder sb = new StringBuilder();
        sb.append("<div style=\"font-family:Arial,sans-serif;max-width:600px;margin:0 auto;\">");
        sb.append("<div style=\"background:#151515;padding:20px;text-align:center;\">");
        sb.append("<span style=\"color:#e71a0f;font-size:24px;font-weight:bold;letter-spacing:1px;\">CGV CINEMA</span>");
        sb.append("</div>");
        sb.append("<div style=\"padding:24px;border:1px solid #e2dfcc;\">");
        sb.append("<p>Xin chào ").append(escape(toName)).append(",</p>");
        sb.append("<p>Cảm ơn bạn đã đặt vé tại CGV Cinema. Dưới đây là thông tin vé của bạn:</p>");

        sb.append("<table style=\"width:100%;border-collapse:collapse;margin:16px 0;\">");
        appendInfoRow(sb, "Phim", schedule.getMovieName());
        appendInfoRow(sb, "Suất chiếu", schedule.getStartTime() + " - " + schedule.getShowDate());
        appendInfoRow(sb, "Phòng", schedule.getRoomNumber() + " (" + schedule.getRoomType() + ")");
        appendInfoRow(sb, "Phương thức thanh toán", paymentMethod);
        sb.append("</table>");

        sb.append("<h3 style=\"color:#e71a0f;border-bottom:2px solid #151515;padding-bottom:6px;\">Mã vé của bạn</h3>");
        for (Ticket ticket : tickets) {
            String seatLabel = findSeatName(seatIds, seatNames, ticket.getSeatId());
            sb.append("<div style=\"border:1px dashed #e71a0f;border-radius:8px;padding:12px 16px;margin-bottom:10px;\">");
            sb.append("<div>Ghế: <strong>").append(escape(seatLabel)).append("</strong></div>");
            sb.append("<div>Mã vé: <strong style=\"font-family:monospace;font-size:16px;letter-spacing:1px;\">")
                    .append(escape(ticket.getCode())).append("</strong></div>");
            sb.append("</div>");
        }

        if (foodQuantities != null && !foodQuantities.isEmpty()) {
            sb.append("<h3 style=\"color:#e71a0f;border-bottom:2px solid #151515;padding-bottom:6px;\">Bắp nước</h3>");
            for (Map.Entry<Integer, Integer> entry : foodQuantities.entrySet()) {
                Food food = foodMap != null ? foodMap.get(entry.getKey()) : null;
                if (food == null) {
                    continue;
                }
                sb.append("<div>").append(entry.getValue()).append(" x ").append(escape(food.getFoodName())).append("</div>");
            }
        }

        sb.append("<p style=\"font-size:18px;font-weight:bold;margin-top:16px;\">Tổng cộng: ")
                .append(String.format("%,.0f", totalAmount)).append(" đ</p>");

        sb.append("<div style=\"background:#fff3cd;border:1px solid #ffc107;border-radius:8px;padding:12px 16px;margin-top:20px;\">");
        sb.append("<strong>Lưu ý:</strong> Vui lòng lưu lại email này hoặc chụp màn hình mã vé/QR code để xuất trình tại quầy vé khi đến rạp.");
        sb.append("</div>");

        sb.append("</div>");
        sb.append("<div style=\"background:#151515;color:rgba(255,255,255,0.5);padding:12px;text-align:center;font-size:12px;\">");
        sb.append("&copy; 2026 CGV Cinema");
        sb.append("</div>");
        sb.append("</div>");
        return sb.toString();
    }

    private static void appendInfoRow(StringBuilder sb, String label, String value) {
        sb.append("<tr><td style=\"padding:4px 8px 4px 0;color:#666;white-space:nowrap;\">")
                .append(escape(label)).append("</td><td style=\"padding:4px 0;font-weight:bold;\">")
                .append(escape(value)).append("</td></tr>");
    }

    private static String findSeatName(List<Integer> seatIds, List<String> seatNames, int seatId) {
        if (seatIds == null || seatNames == null) {
            return "";
        }
        for (int i = 0; i < seatIds.size(); i++) {
            if (seatIds.get(i) == seatId) {
                return seatNames.get(i);
            }
        }
        return "";
    }

    public static void sendWelcomeEmail(String toEmail, String toName) throws MessagingException {
        Session session = buildSession(requireEnv("SMTP_APP_PASSWORD"));
        MimeMessage message = new MimeMessage(session);
        try {
            message.setFrom(new InternetAddress(SMTP_USERNAME, SENDER_NAME));
        } catch (UnsupportedEncodingException e) {
            message.setFrom(new InternetAddress());
        }
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("Chào mừng bạn đến với CGV Cinema!", "UTF-8");

        String html = "<div style=\"font-family:Arial,sans-serif;max-width:600px;margin:0 auto;\">"
                + "<div style=\"background:#151515;padding:20px;text-align:center;\">"
                + "<span style=\"color:#e71a0f;font-size:24px;font-weight:bold;letter-spacing:1px;\">CGV CINEMA</span>"
                + "</div>"
                + "<div style=\"padding:24px;border:1px solid #e2dfcc;\">"
                + "<p>Xin chào <strong>" + escape(toName) + "</strong>,</p>"
                + "<p>Cảm ơn bạn đã đăng ký tài khoản tại CGV Cinema!</p>"
                + "<p>Bạn có thể đặt vé xem phim, chọn ghế yêu thích và nhận ưu đãi đặc biệt từ chúng tôi.</p>"
                + "<div style=\"text-align:center;margin:24px 0;\">"
                + "<a href=\"http://localhost:8080/MoviesTheater/\" "
                + "style=\"display:inline-block;background:#e71a0f;color:#fff;padding:12px 32px;"
                + "border-radius:24px;text-decoration:none;font-weight:bold;\">"
                + "ĐẶT VÉ NGAY</a></div>"
                + "<p>Trân trọng,<br><strong>CGV Cinema Team</strong></p>"
                + "</div>"
                + "<div style=\"background:#151515;color:rgba(255,255,255,0.5);padding:12px;text-align:center;font-size:12px;\">"
                + "&copy; 2026 CGV Cinema</div></div>";

        message.setContent(html, "text/html; charset=UTF-8");
        Transport.send(message);
    }

    public static void sendNotificationEmail(String toEmail, String toName, String subject, String bodyHtml)
            throws MessagingException {
        Session session = buildSession(requireEnv("SMTP_APP_PASSWORD"));
        MimeMessage message = new MimeMessage(session);
        try {
            message.setFrom(new InternetAddress(SMTP_USERNAME, SENDER_NAME));
        } catch (UnsupportedEncodingException e) {
            message.setFrom(new InternetAddress());
        }
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject(subject, "UTF-8");
        String html = "<div style=\"font-family:Arial,sans-serif;max-width:600px;margin:0 auto;\">"
                + "<div style=\"background:#151515;padding:20px;text-align:center;\">"
                + "<span style=\"color:#e71a0f;font-size:24px;font-weight:bold;letter-spacing:1px;\">CGV CINEMA</span>"
                + "</div>"
                + "<div style=\"padding:24px;border:1px solid #e2dfcc;\">"
                + bodyHtml
                + "</div>"
                + "<div style=\"background:#151515;color:rgba(255,255,255,0.5);padding:12px;text-align:center;font-size:12px;\">"
                + "&copy; 2026 CGV Cinema</div></div>";
        message.setContent(html, "text/html; charset=UTF-8");
        Transport.send(message);
    }

    public static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
