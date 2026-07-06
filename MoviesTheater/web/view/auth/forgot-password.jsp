<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Quên mật khẩu</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/login.css">
</head>
<body class="auth-body">

<div class="auth-wrap">
    <div class="auth-card">
        <a href="${pageContext.request.contextPath}/" class="auth-logo">
            <img src="${pageContext.request.contextPath}/Image/Icon/cgvlogo.png" alt="CGV">
            <span>CINEMA</span>
        </a>

        <h2 class="auth-title">Quên mật khẩu</h2>

        <% String error = (String) request.getAttribute("error"); %>
        <% if (error != null) { %>
            <div class="auth-error"><%= error %></div>
        <% } %>

        <% String message = (String) request.getAttribute("message"); %>
        <% Long expirySeconds = (Long) request.getAttribute("expirySeconds"); %>
        <% if (message != null) { %>
            <div class="auth-success"><%= message %></div>
            <div id="countdown" style="text-align:center;font-size:13px;color:var(--cgv-text-muted);margin-top:12px;"></div>
            <script>
                var totalSec = <%= expirySeconds != null ? expirySeconds : 3600 %>;
                var cd = document.getElementById("countdown");
                function updateCountdown() {
                    if (totalSec <= 0) {
                        cd.innerHTML = "Link đã hết hạn.";
                        cd.style.color = "#b91c1c";
                        return;
                    }
                    var m = Math.floor(totalSec / 60);
                    var s = totalSec % 60;
                    cd.innerHTML = "Link hết hạn sau: <strong>" +
                        String(m).padStart(2, '0') + ":" + String(s).padStart(2, '0') + "</strong>";
                    totalSec--;
                }
                updateCountdown();
                setInterval(updateCountdown, 1000);
            </script>
        <% } else { %>
        <form action="${pageContext.request.contextPath}/forgot-password" method="post" class="auth-form">
            <p style="font-size:13px;color:var(--cgv-text-muted);margin-bottom:8px;">
                Nhập email đã đăng ký, chúng tôi sẽ gửi link đặt lại mật khẩu cho bạn.
            </p>
            <div class="auth-field">
                <label for="email">Email</label>
                <input type="email" id="email" name="email" placeholder="example@email.com" required>
            </div>
            <button type="submit" class="auth-btn">Gửi yêu cầu</button>
        </form>
        <% } %>

        <p class="auth-footer">
            <a href="${pageContext.request.contextPath}/Login">← Quay lại đăng nhập</a>
        </p>
    </div>
</div>

</body>
</html>
