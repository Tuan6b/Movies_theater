package com.cinema.controller;

import com.cinema.dao.AccountDAO;
import com.cinema.model.Account;
import com.cinema.util.GoogleOAuthConfig;
import com.cinema.util.MailUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class LoginGoogleController extends HttpServlet {

    private final AccountDAO accountDAO = new AccountDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            String googleUrl = GoogleOAuthConfig.AUTH_URL + "?"
                    + "client_id=" + GoogleOAuthConfig.CLIENT_ID
                    + "&redirect_uri=" + URLEncoder.encode(GoogleOAuthConfig.REDIRECT_URI, StandardCharsets.UTF_8)
                    + "&response_type=code"
                    + "&scope=" + URLEncoder.encode(GoogleOAuthConfig.SCOPE, StandardCharsets.UTF_8)
                    + "&access_type=offline";
            response.sendRedirect(googleUrl);

        } else if ("/callback".equals(pathInfo)) {
            String code = request.getParameter("code");
            String error = request.getParameter("error");

            if (error != null || code == null) {
                request.setAttribute("error", "Đăng nhập Google bị hủy hoặc thất bại.");
                request.getRequestDispatcher("/view/auth/login.jsp").forward(request, response);
                return;
            }

            try {
                String accessToken = exchangeCodeForToken(code);
                if (accessToken == null) {
                    request.setAttribute("error", "Không thể lấy mã xác thực từ Google. Vui lòng thử lại.");
                    request.getRequestDispatcher("/view/auth/login.jsp").forward(request, response);
                    return;
                }

                JsonObject userInfo = getUserInfo(accessToken);
                if (userInfo == null || !userInfo.has("email")) {
                    request.setAttribute("error", "Không thể lấy thông tin người dùng từ Google.");
                    request.getRequestDispatcher("/view/auth/login.jsp").forward(request, response);
                    return;
                }

                String email = userInfo.get("email").getAsString();
                String fullName = userInfo.has("name") ? userInfo.get("name").getAsString() : email.substring(0, email.indexOf('@'));

                Account account = accountDAO.getAccountByEmail(email);
                if (account == null) {
                    account = new Account();
                    account.setEmail(email);
                    account.setPassword("");
                    account.setRoleId(2);
                    account.setFullName(fullName);
                    try {
                        int id = accountDAO.register(account);
                        if (id > 0) {
                            account = accountDAO.getAccountById(id);
                            try {
                                MailUtil.sendWelcomeEmail(account.getEmail(), account.getFullName());
                            } catch (Exception mailEx) {
                                System.err.println("[GoogleLogin] Welcome email failed: " + mailEx.getMessage());
                            }
                        } else {
                            System.err.println("[GoogleLogin] register() returned -1 for email: " + email);
                            request.setAttribute("error", "Đăng nhập Google thất bại. Vui lòng kiểm tra kết nối database và thử lại.");
                            request.getRequestDispatcher("/view/auth/login.jsp").forward(request, response);
                            return;
                        }
                    } catch (Exception e) {
                        System.err.println("[GoogleLogin] Exception during register: " + e.getMessage());
                        e.printStackTrace();
                        request.setAttribute("error", "Đăng nhập Google thất bại: " + e.getMessage());
                        request.getRequestDispatcher("/view/auth/login.jsp").forward(request, response);
                        return;
                    }
                }

                HttpSession session = request.getSession();
                session.setAttribute("account", account);
                session.setMaxInactiveInterval(30 * 60);

                String redirectAfterLogin = (String) session.getAttribute("redirectAfterLogin");
                if (redirectAfterLogin != null && !redirectAfterLogin.trim().isEmpty()) {
                    session.removeAttribute("redirectAfterLogin");
                    response.sendRedirect(redirectAfterLogin);
                } else {
                    response.sendRedirect(request.getContextPath() + "/");
                }

            } catch (Exception e) {
                System.err.println("[GoogleLogin] Unhandled exception: " + e.getMessage());
                e.printStackTrace();
                request.setAttribute("error", "Đăng nhập Google thất bại: lỗi hệ thống. Vui lòng thử lại sau.");
                request.getRequestDispatcher("/view/auth/login.jsp").forward(request, response);
            }

        } else {
            response.sendRedirect(request.getContextPath() + "/Login");
        }
    }

    private String exchangeCodeForToken(String code) throws Exception {
        String body = "code=" + URLEncoder.encode(code, StandardCharsets.UTF_8)
                + "&client_id=" + GoogleOAuthConfig.CLIENT_ID
                + "&client_secret=" + GoogleOAuthConfig.CLIENT_SECRET
                + "&redirect_uri=" + URLEncoder.encode(GoogleOAuthConfig.REDIRECT_URI, StandardCharsets.UTF_8)
                + "&grant_type=authorization_code";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GoogleOAuthConfig.TOKEN_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        String json = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);

        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        return obj.has("access_token") ? obj.get("access_token").getAsString() : null;
    }

    private JsonObject getUserInfo(String accessToken) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GoogleOAuthConfig.USERINFO_URL))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        String json = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);

        return JsonParser.parseString(json).getAsJsonObject();
    }
}
