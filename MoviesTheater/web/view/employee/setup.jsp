<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Kích hoạt tài khoản — CGV Employee</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
    <style>
        .setup-wrap {
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            background: var(--cgv-bg, #f5eeeb);
            padding: 24px;
        }
        .setup-card {
            background: #fff;
            border: 1px solid var(--cgv-border, #e2d5d0);
            border-radius: 16px;
            padding: 40px;
            width: 100%;
            max-width: 520px;
        }
        .setup-logo {
            display: block;
            height: 36px;
            margin: 0 auto 24px;
        }
        .setup-title {
            font-family: var(--font-cgv-ui, sans-serif);
            font-size: 20px;
            font-weight: 700;
            color: var(--cgv-dark, #3d2424);
            text-align: center;
            margin-bottom: 6px;
        }
        .setup-sub {
            font-size: 13px;
            color: rgba(94,63,58,.6);
            text-align: center;
            margin-bottom: 28px;
        }
        .setup-heading {
            font-family: var(--font-cgv-ui, sans-serif);
            font-size: 11px;
            font-weight: 700;
            letter-spacing: 1.5px;
            text-transform: uppercase;
            color: rgba(94,63,58,.5);
            margin-bottom: 12px;
        }
        /* Read-only recap of what the Manager already filled in at account creation */
        .setup-info {
            background: #faf6f5;
            border: 1px solid var(--cgv-border, #e2d5d0);
            border-radius: 10px;
            padding: 16px 18px;
        }
        .setup-info-row {
            display: flex;
            gap: 12px;
            font-size: 13px;
            padding: 5px 0;
        }
        .setup-info-key {
            color: rgba(94,63,58,.55);
            min-width: 120px;
            flex-shrink: 0;
        }
        .setup-info-val {
            color: var(--cgv-dark, #3d2424);
            font-weight: 600;
            word-break: break-word;
        }
        .setup-info-note {
            font-size: 12px;
            color: rgba(94,63,58,.5);
            margin-top: 10px;
        }
    </style>
</head>
<body>
<div class="setup-wrap">
    <div class="setup-card">
        <img class="setup-logo"
             src="${pageContext.request.contextPath}/Image/Icon/cgvlogo.png"
             alt="CGV Cinema">
        <h1 class="setup-title">Kích hoạt tài khoản</h1>
        <p class="setup-sub">Chào mừng! Chỉ cần đặt mật khẩu mới để bắt đầu làm việc.</p>

        <c:if test="${not empty error}">
            <div class="cgv-alert cgv-alert-danger" style="margin-bottom:16px;">${error}</div>
        </c:if>

        <%-- Hồ sơ do quản lý nhập khi tạo tài khoản (UC44) — chỉ để đối chiếu --%>
        <div class="setup-heading">THÔNG TIN CỦA BẠN</div>
        <div class="setup-info">
            <div class="setup-info-row">
                <span class="setup-info-key">Họ và tên</span>
                <span class="setup-info-val">
                    ${not empty profile.fullName ? profile.fullName : sessionScope.account.fullName}
                </span>
            </div>
            <div class="setup-info-row">
                <span class="setup-info-key">Email</span>
                <span class="setup-info-val">${sessionScope.account.email}</span>
            </div>
            <div class="setup-info-row">
                <span class="setup-info-key">Số điện thoại</span>
                <span class="setup-info-val">
                    ${not empty profile.phoneNumber ? profile.phoneNumber : '—'}
                </span>
            </div>
            <div class="setup-info-row">
                <span class="setup-info-key">Ngày sinh</span>
                <span class="setup-info-val">
                    ${not empty profile.dateOfBirth ? profile.dateOfBirth : '—'}
                </span>
            </div>
            <div class="setup-info-row">
                <span class="setup-info-key">Địa chỉ</span>
                <span class="setup-info-val">
                    ${not empty profile.address ? profile.address : '—'}
                </span>
            </div>
            <div class="setup-info-note">
                Thông tin này do quản lý nhập khi tạo tài khoản. Nếu có sai sót, vui lòng báo quản lý cập nhật.
            </div>
        </div>

        <form method="post" action="${pageContext.request.contextPath}/employee/setup">
            <div style="border-top:1px solid var(--cgv-border);margin:20px 0;padding-top:20px;">
                <div class="setup-heading">ĐỔI MẬT KHẨU (BẮT BUỘC)</div>
                <div class="cgv-field">
                    <label class="cgv-label">Mật khẩu mới <span style="color:var(--cgv-red)">*</span></label>
                    <input class="cgv-input" type="password" name="newPassword"
                           placeholder="Tối thiểu 6 ký tự, khác mật khẩu tạm"
                           minlength="6" autocomplete="new-password" required>
                </div>
                <div style="font-size:12px;color:rgba(94,63,58,.5);margin-top:8px;">
                    Mật khẩu tạm do quản lý cấp sẽ hết hiệu lực sau bước này.
                </div>
            </div>

            <button type="submit" class="btn--cgv" style="width:100%;margin-top:8px;">Kích hoạt tài khoản</button>
        </form>

        <div style="margin-top:16px;text-align:center;font-size:12px;color:rgba(94,63,58,.4);">
            <a href="${pageContext.request.contextPath}/Logout" style="color:rgba(94,63,58,.5);">Đăng xuất</a>
        </div>
    </div>
</div>
</body>
</html>
