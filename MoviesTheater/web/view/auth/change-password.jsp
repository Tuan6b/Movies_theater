<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Đổi mật khẩu</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/login.css">
</head>
<body class="auth-body">

<div class="auth-wrap">
    <div class="auth-card">
        <a href="${pageContext.request.contextPath}/" class="auth-logo">
            <img src="${pageContext.request.contextPath}/Image/Icon/cgvlogo.png" alt="CGV">
            <span>CINEMA</span>
        </a>

        <h2 class="auth-title">Đổi mật khẩu</h2>

        <% String error = (String) request.getAttribute("error"); %>
        <% if (error != null) { %>
            <div class="auth-error"><%= error %></div>
        <% } %>

        <% String message = (String) request.getAttribute("message"); %>
        <% if (message != null) { %>
            <div class="auth-success"><%= message %></div>
        <% } %>

        <form action="${pageContext.request.contextPath}/change-password" method="post" class="auth-form">
            <div class="auth-field">
                <label for="oldPassword">Mật khẩu hiện tại</label>
                <div class="auth-password-wrap">
                    <input type="password" id="oldPassword" name="oldPassword" placeholder="••••••••" required>
                    <button type="button" class="auth-toggle-pw" onclick="togglePassword('oldPassword')">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                            <circle cx="12" cy="12" r="3"/>
                        </svg>
                    </button>
                </div>
            </div>

            <div class="auth-field">
                <label for="newPassword">Mật khẩu mới</label>
                <div class="auth-password-wrap">
                    <input type="password" id="newPassword" name="newPassword" placeholder="Ít nhất 6 ký tự" required>
                    <button type="button" class="auth-toggle-pw" onclick="togglePassword('newPassword')">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                            <circle cx="12" cy="12" r="3"/>
                        </svg>
                    </button>
                </div>
            </div>

            <div class="auth-field">
                <label for="confirmPassword">Xác nhận mật khẩu mới</label>
                <div class="auth-password-wrap">
                    <input type="password" id="confirmPassword" name="confirmPassword" placeholder="Nhập lại mật khẩu" required>
                    <button type="button" class="auth-toggle-pw" onclick="togglePassword('confirmPassword')">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                            <circle cx="12" cy="12" r="3"/>
                        </svg>
                    </button>
                </div>
            </div>

            <button type="submit" class="auth-btn">Đổi mật khẩu</button>
        </form>

        <p class="auth-footer">
            <a href="${pageContext.request.contextPath}/">← Quay lại trang chủ</a>
        </p>
    </div>
</div>

<script>
function togglePassword(id) {
    var pw = document.getElementById(id);
    pw.type = pw.type === "password" ? "text" : "password";
}
</script>

</body>
</html>
