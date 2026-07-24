<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Yêu cầu mở khóa — CGV Cinema</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/login.css">
</head>
<body class="auth-body">
<div class="auth-wrap">
    <div class="auth-card">
        <a href="${pageContext.request.contextPath}/" class="auth-logo">
            <span style="font-family: 'Anton', sans-serif; font-size: 20px; color: #e71a0f; letter-spacing: 0.06em;">CGV CINEMA</span>
        </a>
        <h2 class="auth-title">Yêu cầu mở khóa tài khoản</h2>

        <% if (request.getAttribute("success") != null) { %>
            <div style="display:flex;align-items:center;gap:8px;background:#f0fdf4;border:1px solid #86efac;border-radius:10px;padding:12px 14px;font-size:13px;color:#166534;margin-bottom:20px;">
                <%= request.getAttribute("success") %>
            </div>
        <% } %>

        <% if (request.getAttribute("error") != null) { %>
            <div class="auth-error"><%= request.getAttribute("error") %></div>
        <% } %>

        <form action="${pageContext.request.contextPath}/unlock-request" method="post" class="auth-form">
            <div class="auth-field">
                <label for="email">Email <span class="required">*</span></label>
                <input type="email" id="email" name="email" placeholder="example@email.com"
                       value="<%= request.getAttribute("email") != null ? request.getAttribute("email") : "" %>" required>
            </div>
            <div class="auth-field">
                <label for="reason">Lý do yêu cầu mở khóa <span class="required">*</span></label>
                <textarea id="reason" name="reason" rows="4" placeholder="Nhập lý do bạn muốn mở khóa tài khoản..." style="padding:12px 16px;border:2px solid var(--cgv-border);border-radius:10px;font-size:14px;font-family:var(--font-body);outline:none;resize:vertical;" required></textarea>
            </div>
            <button type="submit" class="auth-btn">Gửi yêu cầu</button>
        </form>

        <p class="auth-footer">
            <a href="${pageContext.request.contextPath}/Login">Quay lại đăng nhập</a>
        </p>
    </div>
</div>
</body>
</html>
