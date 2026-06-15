<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Đăng ký — CGV Cinema</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/login.css">
</head>
<body class="auth-body">

<div class="auth-wrap">
    <div class="auth-card">
        <a href="${pageContext.request.contextPath}/" class="auth-logo">
            <img src="${pageContext.request.contextPath}/Image/Icon/cgvlogo.png" alt="CGV">
            <span>CGV CINEMA</span>
        </a>

        <h2 class="auth-title">Đăng ký tài khoản</h2>

        <% String error = (String) request.getAttribute("error"); %>
        <% if (error != null) { %>
            <div class="auth-error"><%= error %></div>
        <% } %>

        <form action="${pageContext.request.contextPath}/Register" method="post" class="auth-form" id="registerForm">
            <div class="auth-field">
                <label for="fullName">Họ và tên <span class="required">*</span></label>
                <input type="text" id="fullName" name="fullName"
                       value="<%= request.getAttribute("fullName") != null ? request.getAttribute("fullName") : "" %>"
                       placeholder="Nguyễn Văn A" required>
            </div>

            <div class="auth-field">
                <label for="email">Email <span class="required">*</span></label>
                <input type="email" id="email" name="email"
                       value="<%= request.getAttribute("email") != null ? request.getAttribute("email") : "" %>"
                       placeholder="example@email.com" required>
            </div>

            <div class="auth-field">
                <label for="phoneNumber">Số điện thoại</label>
                <input type="tel" id="phoneNumber" name="phoneNumber"
                       value="<%= request.getAttribute("phoneNumber") != null ? request.getAttribute("phoneNumber") : "" %>"
                       placeholder="0912345678">
            </div>

            <div class="auth-field">
                <label for="password">Mật khẩu <span class="required">*</span></label>
                <div class="auth-password-wrap">
                    <input type="password" id="password" name="password" placeholder="Ít nhất 6 ký tự" required>
                    <button type="button" class="auth-toggle-pw" onclick="togglePassword()">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                            <circle cx="12" cy="12" r="3"/>
                        </svg>
                    </button>
                </div>
            </div>

            <div class="auth-field">
                <label for="confirmPassword">Xác nhận mật khẩu <span class="required">*</span></label>
                <input type="password" id="confirmPassword" name="confirmPassword" placeholder="Nhập lại mật khẩu" required>
            </div>

            <button type="submit" class="auth-btn">Đăng ký</button>
        </form>

        <p class="auth-footer">
            Đã có tài khoản?
            <a href="${pageContext.request.contextPath}/Login">Đăng nhập</a>
        </p>

        <a href="${pageContext.request.contextPath}/" class="auth-back">← Quay lại trang chủ</a>
    </div>
</div>

<script>
function togglePassword() {
    var pw = document.getElementById("password");
    pw.type = pw.type === "password" ? "text" : "password";
}

document.getElementById("registerForm").addEventListener("submit", function(e) {
    var fullName = document.getElementById("fullName").value.trim();
    var email = document.getElementById("email").value.trim();
    var password = document.getElementById("password").value;
    var confirmPassword = document.getElementById("confirmPassword").value;
    var errors = [];

    if (!fullName) errors.push("Vui lòng nhập họ tên.");
    if (!email) errors.push("Vui lòng nhập email.");
    if (password.length < 6) errors.push("Mật khẩu phải có ít nhất 6 ký tự.");
    if (password !== confirmPassword) errors.push("Xác nhận mật khẩu không khớp.");

    if (errors.length > 0) {
        e.preventDefault();
        alert(errors.join("\n"));
    }
});
</script>

</body>
</html>
