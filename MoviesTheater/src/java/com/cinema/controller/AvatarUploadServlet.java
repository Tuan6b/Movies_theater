package com.cinema.controller;

import com.cinema.dao.AccountDAO;
import com.cinema.model.Account;
import com.cinema.util.SystemLogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class AvatarUploadServlet extends HttpServlet {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private final AccountDAO accountDAO = new AccountDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Account account = (Account) session.getAttribute("account");
        if (account == null) {
            response.sendRedirect(request.getContextPath() + "/Login");
            return;
        }

        String imageUrl = request.getParameter("imageUrl");

        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            handleUrlUpload(imageUrl.trim(), account, session, request, response);
        } else {
            handleFileUpload(request, account, session, response);
        }
    }

    private void handleUrlUpload(String imageUrl, Account account, HttpSession session,
            HttpServletRequest request, HttpServletResponse response) throws IOException {

        if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
            session.setAttribute("flashError", "URL không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }

        String fileName = "avatar_" + account.getAccountId() + "_" + UUID.randomUUID().toString().substring(0, 8) + ".jpg";
        String uploadDir = getServletContext().getRealPath("/Image/Avatars");
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();
        Path dest = new File(dir, fileName).toPath();

        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(imageUrl).openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(10000);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.connect();

            if (conn.getResponseCode() != 200) {
                session.setAttribute("flashError", "Không thể tải ảnh từ URL (HTTP " + conn.getResponseCode() + ").");
                response.sendRedirect(request.getContextPath() + "/profile");
                return;
            }

            String contentType = conn.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                session.setAttribute("flashError", "URL không trỏ tới file ảnh.");
                response.sendRedirect(request.getContextPath() + "/profile");
                return;
            }

            long contentLength = conn.getContentLengthLong();
            if (contentLength > MAX_FILE_SIZE) {
                session.setAttribute("flashError", "Ảnh không được quá 5MB.");
                response.sendRedirect(request.getContextPath() + "/profile");
                return;
            }

            try (InputStream in = conn.getInputStream()) {
                Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            session.setAttribute("flashError", "Lỗi khi tải ảnh từ URL: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }

        String avatarUrl = "Image/Avatars/" + fileName;
        updateAvatarInDb(avatarUrl, account, session);
        response.sendRedirect(request.getContextPath() + "/profile");
    }

    private void handleFileUpload(HttpServletRequest request, Account account, HttpSession session,
            HttpServletResponse response) throws IOException, ServletException {

        Part filePart = request.getPart("avatar");
        if (filePart == null || filePart.getSize() == 0) {
            session.setAttribute("flashError", "Vui lòng chọn ảnh.");
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }

        if (filePart.getSize() > MAX_FILE_SIZE) {
            session.setAttribute("flashError", "Ảnh không được quá 5MB.");
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }

        String submitted = filePart.getSubmittedFileName();
        if (submitted == null || !submitted.matches("(?i).+\\.(jpg|jpeg|png|gif)$")) {
            session.setAttribute("flashError", "Chỉ chấp nhận file JPG, PNG, GIF.");
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }

        String ext = submitted.substring(submitted.lastIndexOf('.'));
        String fileName = "avatar_" + account.getAccountId() + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;
        String uploadDir = getServletContext().getRealPath("/Image/Avatars");
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        Path dest = new File(dir, fileName).toPath();
        try (var input = filePart.getInputStream()) {
            Files.copy(input, dest, StandardCopyOption.REPLACE_EXISTING);
        }

        String avatarUrl = "Image/Avatars/" + fileName;
        updateAvatarInDb(avatarUrl, account, session);
        response.sendRedirect(request.getContextPath() + "/profile");
    }

    private void updateAvatarInDb(String avatarUrl, Account account, HttpSession session) {
        String sql = "UPDATE UserProfile SET AvatarURL = ? WHERE AccountID = ?";
        try (java.sql.Connection conn = com.cinema.util.DBUtils.getConnection();
                java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, avatarUrl);
            ps.setInt(2, account.getAccountId());
            ps.executeUpdate();

            Account fresh = accountDAO.getAccountById(account.getAccountId());
            if (fresh != null) session.setAttribute("account", fresh);

            SystemLogService.log(account.getAccountId(), "AVATAR_CHANGE",
                    "User changed avatar", "");
            session.setAttribute("flashSuccess", "Cập nhật ảnh đại diện thành công.");
        } catch (Exception e) {
            session.setAttribute("flashError", "Lỗi khi lưu ảnh: " + e.getMessage());
        }
    }
}
