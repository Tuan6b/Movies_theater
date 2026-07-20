package com.cinema.service;

import com.cinema.util.BarcodeUtil;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmailService {

    private static final Logger LOG = Logger.getLogger(EmailService.class.getName());

    public void sendTicketConfirmation(String toEmail, String customerName,
            String movieName, String showDate, String startTime, String roomInfo,
            List<String> seatNames, List<String> ticketCodes,
            String foodSummary, String totalAmount, String vnpayTxnRef) {

        String smtpUser = System.getenv("MAIL_USER");
        String smtpPass = System.getenv("MAIL_PASS");
        if (smtpUser == null || smtpPass == null || smtpUser.isEmpty() || smtpPass.isEmpty()) {
            LOG.warning("MAIL_USER/MAIL_PASS not set. Skipping email.");
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpUser, smtpPass);
            }
        });

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(smtpUser, "CGV Cinema"));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            msg.setSubject("CGV Cinema - Xác nhận đặt vé");

            Multipart multipart = new MimeMultipart("related");

            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html lang='vi'><head><meta charset='UTF-8'>")
                .append("<style>")
                .append("body{font-family:Arial,sans-serif;color:#333;max-width:600px;margin:0 auto;padding:20px;}")
                .append(".header{background:#d71f2b;color:#fff;padding:20px;text-align:center;border-radius:8px 8px 0 0;}")
                .append(".header h1{margin:0;font-size:24px;}")
                .append(".content{padding:20px;border:1px solid #ddd;border-top:none;}")
                .append(".ticket-card{border:1px solid #ddd;border-radius:8px;padding:15px;margin:10px 0;text-align:center;background:#f9f9f9;}")
                .append(".ticket-card img{display:block;margin:0 auto 10px;}")
                .append(".ticket-code{font-size:18px;font-weight:bold;letter-spacing:2px;color:#d71f2b;}")
                .append(".info-row{display:flex;justify-content:space-between;padding:5px 0;border-bottom:1px solid #eee;}")
                .append(".footer{text-align:center;padding:20px;color:#999;font-size:12px;}")
                .append("</style></head><body>")
                .append("<div class='header'><h1>CGV CINEMA</h1><p>Xác nhận đặt vé thành công</p></div>")
                .append("<div class='content'>")
                .append("<p>Xin chào <strong>").append(escapeHtml(customerName)).append("</strong>,</p>")
                .append("<p>Cảm ơn bạn đã đặt vé tại CGV Cinema. Thông tin chi tiết:</p>")
                .append("<div class='info-row'><span>Phim:</span><strong>").append(escapeHtml(movieName)).append("</strong></div>")
                .append("<div class='info-row'><span>Suất chiếu:</span><strong>").append(escapeHtml(showDate)).append(" - ").append(escapeHtml(startTime)).append("</strong></div>")
                .append("<div class='info-row'><span>Phòng:</span><strong>").append(escapeHtml(roomInfo)).append("</strong></div>")
                .append("<div class='info-row'><span>Ghế:</span><strong>").append(escapeHtml(String.join(", ", seatNames))).append("</strong></div>");

            if (foodSummary != null && !foodSummary.isEmpty()) {
                html.append("<div class='info-row'><span>Bắp nước:</span><strong>").append(escapeHtml(foodSummary)).append("</strong></div>");
            }

            html.append("<div class='info-row'><span>Tổng cộng:</span><strong style='color:#d71f2b;'>").append(escapeHtml(totalAmount)).append("</strong></div>")
                .append("<div class='info-row'><span>Mã giao dịch:</span><strong>").append(escapeHtml(vnpayTxnRef)).append("</strong></div>")
                .append("<h3 style='margin-top:20px;'>VÉ CỦA BẠN</h3>");

            for (int i = 0; i < ticketCodes.size(); i++) {
                String code = ticketCodes.get(i);
                String seatName = i < seatNames.size() ? seatNames.get(i) : "N/A";
                String cid = "barcode-" + i;

                try {
                    byte[] barcodeBytes = BarcodeUtil.generateBarcodeBytes(code, 300, 80);
                    MimeBodyPart imagePart = new MimeBodyPart();
                    imagePart.setContent(barcodeBytes, "image/png");
                    imagePart.setHeader("Content-ID", "<" + cid + ">");
                    imagePart.setDisposition(MimeBodyPart.INLINE);
                    multipart.addBodyPart(imagePart);

                    html.append("<div class='ticket-card'>")
                        .append("<img src='cid:").append(cid).append("' alt='").append(escapeHtml(code)).append("' style='width:300px;height:80px;'/>")
                        .append("<div class='ticket-code'>").append(escapeHtml(code)).append("</div>")
                        .append("<p style='margin:5px 0;color:#666;'>Ghế: <strong>").append(escapeHtml(seatName)).append("</strong></p>")
                        .append("</div>");
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Failed to generate barcode for " + code, e);
                    html.append("<div class='ticket-card'>")
                        .append("<div class='ticket-code'>").append(escapeHtml(code)).append("</div>")
                        .append("<p style='margin:5px 0;color:#666;'>Ghế: <strong>").append(escapeHtml(seatName)).append("</strong></p>")
                        .append("</div>");
                }
            }

            html.append("<div style='background:#fff3cd;border:1px solid #ffc107;border-radius:8px;padding:12px;margin:15px 0;text-align:center;'>")
                .append("<strong style='color:#856404;font-size:14px;'>LƯU MÃ VÉ CỦA BẠN</strong>")
                .append("<p style='color:#856404;font-size:12px;margin:5px 0 0;'>Vui lòng sao chép hoặc chụp ảnh mã vạch để sử dụng khi soát vé tại rạp.</p>")
                .append("</div>")
                .append("<p style='margin-top:15px;color:#d71f2b;font-weight:bold;'>Vui lòng xuất trình mã vạch tại quầy soát vé để vào rạp.</p>")
                .append("</div>")
                .append("<div class='footer'><p>CGV Cinema - Hệ thống đặt vé trực tuyến</p></div>")
                .append("</body></html>");

            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setContent(html.toString(), "text/html; charset=UTF-8");
            multipart.addBodyPart(textPart);

            MimeBodyPart topPart = new MimeBodyPart();
            topPart.setContent(multipart);
            MimeMultipart wrapped = new MimeMultipart("mixed");
            wrapped.addBodyPart(topPart);

            msg.setContent(wrapped);
            Transport.send(msg);
            LOG.info("Ticket confirmation email sent to " + toEmail);

        } catch (MessagingException | UnsupportedEncodingException e) {
            LOG.log(Level.SEVERE, "Failed to send email to " + toEmail, e);
        }
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;")
                   .replace(">", "&gt;").replace("\"", "&quot;");
    }
}
