<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="java.util.Map" %>
<% request.setAttribute("activeNav", "create-account"); %>
<%
    Map<String, String> errors = (Map<String, String>) request.getAttribute("errors");
    if (errors == null) errors = new java.util.HashMap<>();
    String flashSuccess = (String) request.getAttribute("flashSuccess");
    String flashError = (String) request.getAttribute("flashError");
    String error = (String) request.getAttribute("error");
    String captchaText = (String) request.getAttribute("captchaText");
    if (captchaText == null) captchaText = "";
    String[] captchaChars = captchaText.split("");
    java.util.Random rnd = new java.util.Random();
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Tạo tài khoản — CGV System Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
    <style>
        .form-card { background:#fff; border-radius:12px; padding:32px; max-width:560px; box-shadow:0 1px 4px rgba(0,0,0,0.08); margin:0 auto; }
        .form-card h1 { font-size:22px; margin:0 0 24px; color:var(--cgv-dark); }
        .form-group { margin-bottom:18px; }
        .form-group label { display:block; font-size:13px; font-weight:600; margin-bottom:5px; color:var(--cgv-dark); }
        .form-group label .required { color:var(--cgv-red); }
        .form-group input,
        .form-group select { width:100%; padding:10px 12px; border:1px solid var(--cgv-border); border-radius:8px; font-size:14px; outline:none; box-sizing:border-box; }
        .form-group input:focus,
        .form-group select:focus { border-color:var(--cgv-red); }
        .form-group .field-error { color:var(--cgv-red); font-size:12px; margin-top:4px; display:block; }
        .form-group.has-error input,
        .form-group.has-error select { border-color:var(--cgv-red); background:#fff5f5; }
        .flash { padding:12px 16px; border-radius:8px; margin-bottom:20px; font-size:14px; font-weight:600; }
        .flash-success { background:#e8f5e9; color:#2e7d32; }
        .flash-error { background:#ffebee; color:#c62828; }
        .btn-submit { background:var(--cgv-red); color:#fff; border:none; padding:12px 28px; border-radius:8px; font-size:15px; font-weight:700; cursor:pointer; transition:background 0.2s; width:100%; }
        .btn-submit:hover { background:#c62828; }
        .page-wrap { display:flex; gap:24px; }
        .main-content { flex:1; min-width:0; padding:24px; }
        .captcha-box { display:flex;gap:4px;align-items:center;padding:6px 12px;background:#f3f4f6;border-radius:6px;border:1px solid #d1d5db;user-select:none;font-size:24px;font-weight:bold;font-family:'Courier New',monospace;letter-spacing:4px;height:42px;box-sizing:border-box; }
        .captcha-row { display:flex;gap:10px;align-items:center;flex-wrap:wrap; }
        .captcha-row input { flex:1;min-width:100px;padding:10px 12px;text-transform:uppercase;letter-spacing:3px;font-weight:bold;border:1px solid var(--cgv-border);border-radius:6px;font-size:14px;outline:none; }
        .captcha-row input:focus { border-color:var(--cgv-red); }
        .captcha-row .refresh-btn { padding:8px 12px;border:1px solid var(--cgv-border);border-radius:6px;background:#fff;cursor:pointer;font-size:13px;white-space:nowrap; }
        .captcha-row .refresh-btn:hover { border-color:var(--cgv-red);color:var(--cgv-red); }
    </style>
</head>
<body>
<div class="page-wrap">
    <jsp:include page="/view/admin/_sidebar.jsp"/>
    <div class="main-content">
        <% if (flashSuccess != null) { %><div class="flash flash-success"><%= flashSuccess %></div><% } %>
        <% if (flashError != null) { %><div class="flash flash-error"><%= flashError %></div><% } %>
        <% if (error != null) { %><div class="flash flash-error"><%= error %></div><% } %>

        <div class="form-card">
            <h1>Tạo tài khoản mới</h1>
            <form action="${pageContext.request.contextPath}/admin/create-account" method="post" id="createAccountForm" novalidate>
                <div class="form-group<%= errors.containsKey("fullName") ? " has-error" : "" %>">
                    <label for="fullName">Họ và tên <span class="required">*</span></label>
                    <input type="text" id="fullName" name="fullName"
                           value="<%= request.getAttribute("fullName") != null ? request.getAttribute("fullName") : "" %>"
                           placeholder="Nguyễn Văn A" required data-error="Vui lòng nhập họ tên.">
                    <% if (errors.containsKey("fullName")) { %><span class="field-error"><%= errors.get("fullName") %></span><% } %>
                </div>

                <div class="form-group<%= errors.containsKey("email") ? " has-error" : "" %>">
                    <label for="email">Email <span class="required">*</span></label>
                    <input type="email" id="email" name="email"
                           value="<%= request.getAttribute("email") != null ? request.getAttribute("email") : "" %>"
                           placeholder="example@email.com" required data-error="Vui lòng nhập email.">
                    <% if (errors.containsKey("email")) { %><span class="field-error"><%= errors.get("email") %></span><% } %>
                </div>

                <div class="form-group<%= errors.containsKey("password") ? " has-error" : "" %>">
                    <label for="password">Mật khẩu <span class="required">*</span></label>
                    <input type="password" id="password" name="password" placeholder="Ít nhất 6 ký tự" required data-error="Vui lòng nhập mật khẩu.">
                    <% if (errors.containsKey("password")) { %><span class="field-error"><%= errors.get("password") %></span><% } %>
                </div>

                <div class="form-group<%= errors.containsKey("phoneNumber") ? " has-error" : "" %>">
                    <label for="phoneNumber">Số điện thoại</label>
                    <input type="text" id="phoneNumber" name="phoneNumber"
                           value="<%= request.getAttribute("phoneNumber") != null ? request.getAttribute("phoneNumber") : "" %>"
                           placeholder="0912345678" pattern="^(0[35789])([0-9]{8})$" data-error="Số điện thoại không hợp lệ (VD: 0912345678).">
                    <% if (errors.containsKey("phoneNumber")) { %><span class="field-error"><%= errors.get("phoneNumber") %></span><% } %>
                </div>

                <div class="form-group">
                    <label for="roleId">Vai trò <span class="required">*</span></label>
                    <select id="roleId" name="roleId">
                        <option value="2" <%= Integer.valueOf(2).equals(request.getAttribute("selectedRole")) ? "selected" : "" %>>Customer</option>
                        <option value="3" <%= Integer.valueOf(3).equals(request.getAttribute("selectedRole")) ? "selected" : "" %>>Employee</option>
                        <option value="4" <%= Integer.valueOf(4).equals(request.getAttribute("selectedRole")) ? "selected" : "" %>>Manager</option>
           
                    </select>
                </div>

                <div class="form-group<%= errors.containsKey("captcha") ? " has-error" : "" %>">
                    <label for="captcha">Mã xác nhận <span class="required">*</span></label>
                    <div class="captcha-row">
                        <div class="captcha-box" id="captchaBox">
                            <% for (String ch : captchaChars) {
                                int r = 50 + rnd.nextInt(150);
                                int g = 50 + rnd.nextInt(150);
                                int b = 50 + rnd.nextInt(150);
                                double rot = (rnd.nextDouble() - 0.5) * 0.3;
                            %>
                            <span style="display:inline-block;transform:rotate(<%= rot %>rad);color:rgb(<%= r %>,<%= g %>,<%= b %>);"><%= ch %></span>
                            <% } %>
                        </div>
                        <input type="text" id="captcha" name="captcha" placeholder="Nhập mã" required maxlength="5"
                               data-error="Vui lòng nhập mã xác nhận.">
                        <button type="button" class="refresh-btn" onclick="refreshCaptcha()">Làm mới</button>
                    </div>
                    <% if (errors.containsKey("captcha")) { %><span class="field-error"><%= errors.get("captcha") %></span><% } %>
                </div>

                <button type="submit" class="btn-submit">Tạo tài khoản</button>
            </form>
        </div>
    </div>
</div>

<script>
function refreshCaptcha() {
    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function() {
        if (xhr.readyState === 4 && xhr.status === 200) {
            var txt = xhr.responseText;
            var container = document.getElementById("captchaBox");
            if (container) {
                var chars = txt.split("");
                container.innerHTML = "";
                for (var i = 0; i < chars.length; i++) {
                    var r = 50 + Math.floor(Math.random() * 150);
                    var g = 50 + Math.floor(Math.random() * 150);
                    var b = 50 + Math.floor(Math.random() * 150);
                    var rot = (Math.random() - 0.5) * 0.3;
                    var span = document.createElement("span");
                    span.style.cssText = "display:inline-block;transform:rotate(" + rot + "rad);color:rgb(" + r + "," + g + "," + b + ");";
                    span.textContent = chars[i];
                    container.appendChild(span);
                }
            }
            document.getElementById("captcha").value = "";
            document.getElementById("captcha").focus();
        }
    };
    xhr.open("GET", "${pageContext.request.contextPath}/admin/create-account?refreshCaptcha=1", true);
    xhr.send();
}

function showError(fieldId, msg) {
    var field = document.getElementById(fieldId).closest(".form-group");
    if (msg) {
        field.classList.add("has-error");
        var errEl = field.querySelector(".field-error");
        if (errEl) errEl.textContent = msg;
        else {
            var span = document.createElement("span");
            span.className = "field-error";
            span.textContent = msg;
            field.appendChild(span);
        }
    } else {
        field.classList.remove("has-error");
        var errEl = field.querySelector(".field-error");
        if (errEl && !errEl.getAttribute("data-server")) errEl.remove();
    }
}

function validateField(input) {
    var val = input.value.trim();
    var id = input.id;
    if (id === "fullName") {
        showError(id, val ? null : input.getAttribute("data-error"));
    } else if (id === "email") {
        if (!val) showError(id, input.getAttribute("data-error"));
        else if (!/^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(val)) showError(id, "Email không hợp lệ.");
        else showError(id, null);
    } else if (id === "password") {
        if (!val) showError(id, input.getAttribute("data-error"));
        else if (val.length < 6) showError(id, "Mật khẩu phải có ít nhất 6 ký tự.");
        else showError(id, null);
    } else if (id === "phoneNumber") {
        if (val && !/^(0[35789])([0-9]{8})$/.test(val)) showError(id, "Số điện thoại không hợp lệ (VD: 0912345678).");
        else showError(id, null);
    } else if (id === "captcha") {
        showError(id, val ? null : input.getAttribute("data-error"));
    }
}

document.querySelectorAll("#createAccountForm input[required], #createAccountForm input[pattern]").forEach(function(input) {
    input.addEventListener("blur", function() { validateField(this); });
    input.addEventListener("input", function() { validateField(this); });
});

document.getElementById("createAccountForm").addEventListener("submit", function(e) {
    var inputs = this.querySelectorAll("input[required], input[pattern]");
    var hasError = false;
    inputs.forEach(function(input) {
        validateField(input);
        var field = input.closest(".form-group");
        if (field && field.classList.contains("has-error")) hasError = true;
    });
    if (hasError) {
        e.preventDefault();
        var firstErr = this.querySelector(".has-error");
        if (firstErr) firstErr.scrollIntoView({ behavior: "smooth", block: "center" });
    }
});
</script>
</body>
</html>
