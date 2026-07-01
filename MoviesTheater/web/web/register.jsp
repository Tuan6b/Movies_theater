<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.Map" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Đăng ký — CGV Cinema</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/register.css">
</head>
<body class="auth-body">

<%
    Map<String, String> fieldErrors = (Map<String, String>) request.getAttribute("fieldErrors");
    if (fieldErrors == null) fieldErrors = new java.util.HashMap<>();
    String errFullName = fieldErrors.getOrDefault("fullName", "");
    String errEmail = fieldErrors.getOrDefault("email", "");
    String errPassword = fieldErrors.getOrDefault("password", "");
    String errConfirm = fieldErrors.getOrDefault("confirmPassword", "");
    String generalError = (String) request.getAttribute("error");
%>

<div class="auth-wrap">
    <div class="auth-card">
        <a href="${pageContext.request.contextPath}/" class="auth-logo">
            <img src="${pageContext.request.contextPath}/Image/Icon/cgvlogo.png" alt="CGV">
            <span>CGV CINEMA</span>
        </a>

        <h2 class="auth-title">Đăng ký tài khoản</h2>

        <% if (generalError != null && fieldErrors.isEmpty()) { %>
            <div class="auth-error"><%= generalError %></div>
        <% } %>

        <form action="${pageContext.request.contextPath}/Register" method="post" class="auth-form" id="registerForm" novalidate>
            <div class="auth-field<%= !errFullName.isEmpty() ? " has-error" : "" %>">
                <label for="fullName">Họ và tên <span class="required">*</span></label>
                <input type="text" id="fullName" name="fullName"
                       value="<%= request.getAttribute("fullName") != null ? request.getAttribute("fullName") : "" %>"
                       placeholder="Nguyễn Văn A" required
                       data-error="Vui lòng nhập họ tên.">
                <span class="field-error<%= !errFullName.isEmpty() ? " show" : "" %>" id="fullNameError">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
                    <span><%= errFullName %></span>
                </span>
            </div>

            <div class="auth-field<%= !errEmail.isEmpty() ? " has-error" : "" %>">
                <label for="email">Email <span class="required">*</span></label>
                <input type="email" id="email" name="email"
                       value="<%= request.getAttribute("email") != null ? request.getAttribute("email") : "" %>"
                       placeholder="example@email.com" required
                       data-error="Vui lòng nhập email.">
                <span class="field-error<%= !errEmail.isEmpty() ? " show" : "" %>" id="emailError">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
                    <span><%= errEmail %></span>
                </span>
            </div>

            <div class="auth-field">
                <label for="phoneNumber">Số điện thoại</label>
                <input type="tel" id="phoneNumber" name="phoneNumber"
                       value="<%= request.getAttribute("phoneNumber") != null ? request.getAttribute("phoneNumber") : "" %>"
                       placeholder="0912345678"
                       pattern="(0|\+84)\d{9}" data-error="Số điện thoại không hợp lệ (VD: 0912345678).">
                <span class="field-error" id="phoneError">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
                    <span>Số điện thoại không hợp lệ (VD: 0912345678).</span>
                </span>
            </div>

            <div class="auth-field<%= !errPassword.isEmpty() ? " has-error" : "" %>">
                <label for="password">Mật khẩu <span class="required">*</span></label>
                <div class="auth-password-wrap">
                    <input type="password" id="password" name="password" placeholder="Ít nhất 6 ký tự" required
                           data-error="Vui lòng nhập mật khẩu.">
                    <button type="button" class="auth-toggle-pw" onclick="toggleField('password')" tabindex="-1">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                            <circle cx="12" cy="12" r="3"/>
                        </svg>
                    </button>
                </div>
                <span class="field-error<%= !errPassword.isEmpty() ? " show" : "" %>" id="passwordError">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
                    <span><%= errPassword %></span>
                </span>
                <div class="auth-pw-strength" id="pwStrength">
                    <div class="auth-pw-strength-bar" id="pwStrengthBar"></div>
                </div>
                <div class="auth-pw-strength-text" id="pwStrengthText"></div>
            </div>

            <div class="auth-field<%= !errConfirm.isEmpty() ? " has-error" : "" %>">
                <label for="confirmPassword">Xác nhận mật khẩu <span class="required">*</span></label>
                <div class="auth-password-wrap">
                    <input type="password" id="confirmPassword" name="confirmPassword" placeholder="Nhập lại mật khẩu" required
                           data-error="Vui lòng xác nhận mật khẩu.">
                    <button type="button" class="auth-toggle-pw" onclick="toggleField('confirmPassword')" tabindex="-1">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                            <circle cx="12" cy="12" r="3"/>
                        </svg>
                    </button>
                </div>
                <span class="field-error<%= !errConfirm.isEmpty() ? " show" : "" %>" id="confirmError">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
                    <span><%= errConfirm %></span>
                </span>
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
function toggleField(id) {
    var el = document.getElementById(id);
    el.type = el.type === "password" ? "text" : "password";
}

function showError(fieldId, errorId, message) {
    var field = document.getElementById(fieldId).closest(".auth-field");
    var errEl = document.getElementById(errorId);
    var msgEl = errEl.querySelector("span");
    if (message) {
        field.classList.add("has-error");
        field.classList.remove("input-valid");
        msgEl.textContent = message;
        errEl.classList.add("show");
    } else {
        field.classList.remove("has-error");
        errEl.classList.remove("show");
    }
}

function validateField(input) {
    var fieldId = input.id;
    var val = input.value.trim();
    var errorId = fieldId + "Error";
    var isEmpty = !val;

    switch (fieldId) {
        case "fullName":
            showError(fieldId, errorId, isEmpty ? input.getAttribute("data-error") : null);
            break;
        case "email":
            if (isEmpty) {
                showError(fieldId, errorId, input.getAttribute("data-error"));
            } else if (!/^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(val)) {
                showError(fieldId, errorId, "Email không hợp lệ.");
            } else {
                showError(fieldId, errorId, null);
            }
            break;
        case "phoneNumber":
            var phoneErr = document.getElementById("phoneError");
            var phoneField = document.getElementById("phoneNumber").closest(".auth-field");
            if (val && !/^(0|\+84)\d{9}$/.test(val)) {
                phoneField.classList.add("has-error");
                phoneErr.classList.add("show");
            } else {
                phoneField.classList.remove("has-error");
                phoneErr.classList.remove("show");
            }
            break;
        case "password":
            if (isEmpty) {
                showError(fieldId, errorId, input.getAttribute("data-error"));
            } else if (val.length < 6) {
                showError(fieldId, errorId, "Mật khẩu phải có ít nhất 6 ký tự.");
            } else {
                showError(fieldId, errorId, null);
            }
            updatePasswordStrength(val);
            // Re-validate confirmPassword if it has a value
            var confirmEl = document.getElementById("confirmPassword");
            if (confirmEl.value.trim()) validateField(confirmEl);
            break;
        case "confirmPassword":
            var pw = document.getElementById("password").value;
            if (isEmpty) {
                showError(fieldId, errorId, input.getAttribute("data-error"));
            } else if (val !== pw) {
                showError(fieldId, errorId, "Xác nhận mật khẩu không khớp.");
            } else {
                showError(fieldId, errorId, null);
            }
            break;
    }
}

function updatePasswordStrength(pw) {
    var bar = document.getElementById("pwStrengthBar");
    var text = document.getElementById("pwStrengthText");
    var score = 0;

    if (pw.length >= 6) score += 1;
    if (pw.length >= 10) score += 1;
    if (/[a-z]/.test(pw) && /[A-Z]/.test(pw)) score += 1;
    if (/\d/.test(pw)) score += 1;
    if (/[^a-zA-Z0-9]/.test(pw)) score += 1;

    var pct = (score / 5) * 100;
    var colors = ["#e5e7eb", "#ef4444", "#f97316", "#eab308", "#22c55e", "#16a34a"];
    var labels = ["", "Yếu", "Trung bình", "Khá", "Mạnh", "Rất mạnh"];

    bar.style.width = pct + "%";
    bar.style.background = colors[score];
    text.textContent = pw.length > 0 ? "Độ mạnh: " + labels[score] : "";
    text.style.color = score >= 3 ? "#16a34a" : score >= 2 ? "#eab308" : "#ef4444";
}

// Real-time validation on blur
document.querySelectorAll("#registerForm input[required], #registerForm input[pattern]").forEach(function(input) {
    input.addEventListener("blur", function() { validateField(this); });
    input.addEventListener("input", function() { validateField(this); });
});

// Prevent form submission if errors
document.getElementById("registerForm").addEventListener("submit", function(e) {
    var inputs = this.querySelectorAll("input[required], input[pattern]");
    var hasError = false;
    inputs.forEach(function(input) {
        validateField(input);
        var field = input.closest(".auth-field");
        if (field && field.classList.contains("has-error")) hasError = true;
    });
    if (hasError) {
        e.preventDefault();
        // Scroll to first error
        var firstErr = this.querySelector(".has-error");
        if (firstErr) firstErr.scrollIntoView({ behavior: "smooth", block: "center" });
    }
});
</script>

</body>
</html>
