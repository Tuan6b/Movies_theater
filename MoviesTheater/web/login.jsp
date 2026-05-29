<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Đăng nhập </title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/login.css">
</head>
<body class="auth-body">

<div class="auth-wrap">
    <div class="auth-card">
        <a href="${pageContext.request.contextPath}/" class="auth-logo">
            <img src="${pageContext.request.contextPath}/Image/Icon/cgvlogo.png" alt="CGV">
            <span>CINEMA</span>
        </a>

        <h2 class="auth-title">Đăng nhập</h2>

        <% String error = (String) request.getAttribute("error"); %>
        <% if (error != null) { %>
            <div class="auth-error"><%= error %></div>
        <% } %>

        <form action="${pageContext.request.contextPath}/Login" method="post" class="auth-form" id="loginForm">
            <div class="auth-field">
                <label for="email">Email</label>
                <input type="email" id="email" name="email"
                       value="<%= request.getAttribute("email") != null ? request.getAttribute("email") : "" %>"
                       placeholder="example@email.com" required>
            </div>

            <div class="auth-field">
                <label for="password">Mật khẩu</label>
                <div class="auth-password-wrap">
                    <input type="password" id="password" name="password" placeholder="••••••••" required>
                    <button type="button" class="auth-toggle-pw" onclick="togglePassword()">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                            <circle cx="12" cy="12" r="3"/>
                        </svg>
                    </button>
                </div>
            </div>

            <div class="auth-row">
                <label class="auth-checkbox">
                    <input type="checkbox" name="remember">
                    <span>Ghi nhớ đăng nhập</span>
                </label>
            </div>

            <button type="submit" class="auth-btn">Đăng nhập</button>
        </form>

        <p class="auth-footer">
            Chưa có tài khoản?
            <a href="${pageContext.request.contextPath}/Register">Đăng ký ngay</a>
        </p>

        <a href="${pageContext.request.contextPath}/" class="auth-back">← Quay lại trang chủ</a>
    </div>
</div>

<script>
function togglePassword() {
    var pw = document.getElementById("password");
    pw.type = pw.type === "password" ? "text" : "password";
}

document.getElementById("loginForm").addEventListener("submit", function(e) {
    var email = document.getElementById("email").value.trim();
    var password = document.getElementById("password").value;
    if (!email || !password) {
        e.preventDefault();
        alert("Vui lòng nhập email và mật khẩu.");
    }
});
</script>

</body>
</html>
