package com.cinema.controller;

import com.cinema.dao.AccountDAO;
import com.cinema.dao.UserProfileDAO;
import com.cinema.model.Account;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.regex.Pattern;

@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,
    maxFileSize = 5 * 1024 * 1024,
    maxRequestSize = 10 * 1024 * 1024
)
public class ProfileController extends HttpServlet {

    private final AccountDAO accountDAO = new AccountDAO();
    private final UserProfileDAO userProfileDAO = new UserProfileDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("account") == null) {
            response.sendRedirect(request.getContextPath() + "/Login");
            return;
        }
        Account sessionAccount = (Account) session.getAttribute("account");
        Account fresh = accountDAO.getAccountById(sessionAccount.getAccountId());
        if (fresh != null) {
            session.setAttribute("account", fresh);
        }
        request.getRequestDispatcher("/profile.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("account") == null) {
            response.sendRedirect(request.getContextPath() + "/Login");
            return;
        }

        Account sessionAccount = (Account) session.getAttribute("account");

        String action = request.getParameter("action");

        if ("avatar".equals(action)) {
            handleAvatarUpload(request, response, session, sessionAccount);
            return;
        }

        String fullName = request.getParameter("fullName");
        String phoneNumber = request.getParameter("phoneNumber");
        String dob = request.getParameter("dob");
        String address = request.getParameter("address");

        String error = null;
        if (fullName == null || fullName.trim().isEmpty()) {
            error = "Họ tên không được để trống.";
        } else if (phoneNumber != null && !phoneNumber.isEmpty()
                && !Pattern.matches("^\\d{10,11}$", phoneNumber.trim())) {
            error = "Số điện thoại phải là 10-11 chữ số.";
        }

        if (error != null) {
            request.setAttribute("error", error);
            request.setAttribute("fullName", fullName);
            request.setAttribute("phoneNumber", phoneNumber);
            request.setAttribute("dob", dob);
            request.setAttribute("address", address);
            request.setAttribute("showModal", true);
            request.getRequestDispatcher("/profile.jsp").forward(request, response);
            return;
        }

        boolean updated = userProfileDAO.updateProfile(
                sessionAccount.getAccountId(),
                fullName.trim(),
                phoneNumber != null ? phoneNumber.trim() : null,
                dob, address
        );

        if (updated) {
            Account fresh = accountDAO.getAccountById(sessionAccount.getAccountId());
            if (fresh != null) {
                session.setAttribute("account", fresh);
            }
            request.setAttribute("success", "Cập nhật thông tin thành công.");
        } else {
            request.setAttribute("error", "Cập nhật thất bại. Vui lòng thử lại.");
            request.setAttribute("showModal", true);
        }

        request.setAttribute("fullName", fullName);
        request.setAttribute("phoneNumber", phoneNumber);
        request.setAttribute("dob", dob);
        request.setAttribute("address", address);
        request.getRequestDispatcher("/profile.jsp").forward(request, response);
    }

    private void handleAvatarUpload(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, Account account) throws ServletException, IOException {
        Part filePart = request.getPart("avatar");
        if (filePart == null || filePart.getSize() == 0) {
            request.setAttribute("error", "Vui lòng chọn ảnh.");
            request.getRequestDispatcher("/profile.jsp").forward(request, response);
            return;
        }

        String contentType = filePart.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            request.setAttribute("error", "Chỉ chấp nhận file ảnh (JPEG, PNG, GIF).");
            request.getRequestDispatcher("/profile.jsp").forward(request, response);
            return;
        }

        try {
            String uploadDir = getServletContext().getRealPath("/uploads/avatars");
            if (uploadDir == null) {
                uploadDir = getServletContext().getRealPath("/") + "uploads" + File.separator + "avatars";
            }
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String fileName = "avatar_" + account.getAccountId() + "_"
                    + System.currentTimeMillis()
                    + getExtension(Paths.get(filePart.getSubmittedFileName()).getFileName().toString());
            String filePath = uploadDir + File.separator + fileName;
            filePart.write(filePath);

            String avatarUrl = request.getContextPath() + "/uploads/avatars/" + fileName;
            userProfileDAO.updateAvatar(account.getAccountId(), avatarUrl);

            Account fresh = accountDAO.getAccountById(account.getAccountId());
            if (fresh != null) session.setAttribute("account", fresh);

            request.setAttribute("success", "Cập nhật ảnh đại diện thành công.");
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Lỗi khi upload ảnh: " + e.getMessage());
        }
        request.getRequestDispatcher("/profile.jsp").forward(request, response);
    }

    private String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot) : ".jpg";
    }
}
