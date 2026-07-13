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
                    <input type="checkbox" name="remember" value="on">
                    Ghi nhớ đăng nhập
                </label>
                <a href="${pageContext.request.contextPath}/forgot-password" class="auth-forgot">Quên mật khẩu?</a>
            </div>

            <button type="submit" class="auth-btn">Đăng nhập</button>
        </form>

        <div class="auth-divider">
            <span>hoặc</span>
        </div>

        <a href="${pageContext.request.contextPath}/LoginGoogle/" class="auth-btn-google">
            <svg width="18" height="18" viewBox="0 0 48 48">
                <path fill="#EA4335" d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z"/>
                <path fill="#4285F4" d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z"/>
                <path fill="#FBBC05" d="M10.54 28.59A14.5 14.5 0 0 1 9.5 24c0-1.59.28-3.14.76-4.59l-7.98-6.19A23.99 23.99 0 0 0 0 24c0 3.77.87 7.35 2.56 10.56l7.98-5.97z"/>
                <path fill="#34A853" d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.15 1.45-4.92 2.3-8.16 2.3-6.26 0-11.57-4.22-13.47-9.91l-7.98 5.97C6.51 42.62 14.62 48 24 48z"/>
            </svg>
            <span>Đăng nhập bằng Google</span>
        </a>

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
