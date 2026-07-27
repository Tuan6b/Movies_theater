<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Thiết lập tài khoản — CGV Employee</title>
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
    </style>
</head>
<body>
<div class="setup-wrap">
    <div class="setup-card">
        <img class="setup-logo"
             src="${pageContext.request.contextPath}/Image/Icon/cgvlogo.png"
             alt="CGV Cinema">
        <h1 class="setup-title">Thiết lập tài khoản</h1>
        <p class="setup-sub">Chào mừng! Vui lòng cập nhật thông tin cá nhân trước khi tiếp tục.</p>

        <c:if test="${not empty error}">
            <div class="cgv-alert cgv-alert-danger" style="margin-bottom:16px;">${error}</div>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/employee/setup">
            <div class="cgv-field">
                <label class="cgv-label">Họ và tên <span style="color:var(--cgv-red)">*</span></label>
                <input class="cgv-input" type="text" name="fullName"
                       value="${not empty param.fullName ? param.fullName : sessionScope.account.fullName}"
                       placeholder="Nhập họ và tên đầy đủ" required>
            </div>

            <div class="cgv-field">
                <label class="cgv-label">Số điện thoại</label>
                <input class="cgv-input" type="text" name="phoneNumber"
                       value="${not empty param.phoneNumber ? param.phoneNumber : sessionScope.account.phoneNumber}"
                       placeholder="Nhập số điện thoại">
            </div>

            <div class="cgv-field">
                <label class="cgv-label">Ngày sinh</label>
                <input class="cgv-input" type="date" name="dateOfBirth"
                       value="${not empty param.dateOfBirth ? param.dateOfBirth : sessionScope.account.dateOfBirth}">
            </div>

            <div class="cgv-field">
                <label class="cgv-label">Địa chỉ</label>
                <input class="cgv-input" type="text" name="address"
                       value="${not empty param.address ? param.address : sessionScope.account.address}"
                       placeholder="Nhập địa chỉ">
            </div>

            <div style="border-top:1px solid var(--cgv-border);margin:20px 0;padding-top:20px;">
                <div style="font-family:var(--font-cgv-ui);font-size:11px;font-weight:700;letter-spacing:1.5px;text-transform:uppercase;color:rgba(94,63,58,.5);margin-bottom:12px;">
                    ĐỔI MẬT KHẨU (Bắt buộc)
                </div>
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

            <button type="submit" class="btn--cgv" style="width:100%;margin-top:8px;">Lưu và tiếp tục</button>
        </form>

        <div style="margin-top:16px;text-align:center;font-size:12px;color:rgba(94,63,58,.4);">
            <a href="${pageContext.request.contextPath}/Logout" style="color:rgba(94,63,58,.5);">Đăng xuất</a>
        </div>
    </div>
</div>
</body>
</html>
