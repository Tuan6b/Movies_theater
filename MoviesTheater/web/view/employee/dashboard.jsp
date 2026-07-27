<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% request.setAttribute("activeNav", "dashboard"); %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Trang chính — CGV Employee</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
    <style>
        .emp-card {
            background: #fff;
            border: 1px solid var(--cgv-border);
            border-radius: 12px;
            padding: 24px;
            display: flex;
            flex-direction: column;
        }
        .emp-card-title {
            font-weight: 700;
            font-family: var(--font-cgv-ui);
            margin-bottom: 4px;
        }
        .emp-card-desc {
            font-size: 13px;
            color: rgba(94,63,58,0.6);
            margin-bottom: 16px;
            flex: 1;
        }
        .emp-shift-bar {
            display: flex;
            flex-wrap: wrap;
            align-items: center;
            gap: 10px;
            background: #fff;
            border: 1px solid var(--cgv-border);
            border-radius: 12px;
            padding: 16px 24px;
            margin-bottom: 32px;
            font-size: 13px;
            color: rgba(94,63,58,0.7);
        }
        .emp-shift-bar b {
            font-family: var(--font-cgv-ui);
            font-size: 15px;
            color: var(--cgv-dark);
        }
        .emp-shift-tag {
            font-family: var(--font-cgv-ui);
            font-size: 11px;
            font-weight: 700;
            padding: 2px 10px;
            border-radius: 20px;
        }
        .emp-shift-tag.on  { background: #d1fae5; color: #065f46; }
        .emp-shift-tag.off { background: #f3f4f6; color: #6b7280; }
        .section-heading {
            font-family: var(--font-cgv-ui);
            font-size: 10px;
            font-weight: 700;
            letter-spacing: 2px;
            text-transform: uppercase;
            color: rgba(94,63,58,0.5);
            margin: 0 0 12px 0;
        }
        .emp-grid {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 16px;
        }
        @media (max-width: 1100px) { .emp-grid { grid-template-columns: repeat(2, 1fr); } }
        @media (max-width: 700px)  { .emp-grid { grid-template-columns: 1fr; } }
    </style>
</head>
<body class="cgv-body">

<%@ include file="_sidebar.jsp" %>

<div class="cgv-main">

    <header class="cgv-header">
        <h1 class="cgv-header-title">Trang chính</h1>
        <div class="cgv-header-right">
            <div class="cgv-header-actions">
                <%@ include file="_notifications.jsp" %>
                <div class="cgv-header-divider"></div>
                <div class="cgv-user-wrap">
                    <div class="cgv-avatar">EM</div>
                    <span class="cgv-user-name">
                        <c:choose>
                            <c:when test="${not empty sessionScope.account}">${sessionScope.account.fullName}</c:when>
                            <c:otherwise>Employee</c:otherwise>
                        </c:choose>
                    </span>
                </div>
            </div>
        </div>
    </header>

    <div class="cgv-page">
        <div class="cgv-list-wrap">

            <c:if test="${not empty flashError}">
                <div style="background:#fee2e2;border:1px solid #ef4444;border-radius:8px;padding:12px 16px;margin-bottom:20px;font-size:13px;font-weight:600;color:#991b1b;">
                    ${flashError}
                </div>
            </c:if>

            <%-- Cảnh báo ngoài ca: giải thích vì sao bán vé và check-in bị khoá --%>
            <c:if test="${noShift}">
                <div style="background:#fef3c7;border:1px solid #f59e0b;border-radius:8px;padding:12px 16px;margin-bottom:20px;display:flex;align-items:center;gap:10px;">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#d97706" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="flex-shrink:0;">
                        <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
                        <line x1="12" y1="9" x2="12" y2="13"/>
                        <line x1="12" y1="17" x2="12.01" y2="17"/>
                    </svg>
                    <span style="font-size:13px;font-weight:600;color:#92400e;">Bạn không có ca làm việc vào lúc này. Chức năng check-in vé và xuất vé bị tạm khoá cho đến khi ca bắt đầu.</span>
                </div>
            </c:if>

            <%-- Ca của chính nhân viên: quyết định chức năng nào đang mở khoá --%>
            <div class="emp-shift-bar">
                <span>Ca hôm nay:</span>
                <b>${not empty empShiftToday ? empShiftToday : 'Không có ca'}</b>
                <span class="emp-shift-tag ${noShift ? 'off' : 'on'}">${empShiftStatus}</span>
                <a href="${pageContext.request.contextPath}/employee/my-shifts"
                   style="color:var(--cgv-red);font-weight:600;text-decoration:none;">Xem lịch ca →</a>
            </div>

            <div class="section-heading">CHỨC NĂNG</div>
            <div class="emp-grid">

                <div class="emp-card">
                    <div class="emp-card-title">Check-in vé</div>
                    <div class="emp-card-desc">Quét mã vé hoặc tra cứu để cho khách vào phòng chiếu</div>
                    <c:choose>
                        <c:when test="${noShift}">
                            <span class="btn--cgv-outline" style="opacity:.45;cursor:not-allowed;">Cần có ca làm việc</span>
                        </c:when>
                        <c:otherwise>
                            <a href="${pageContext.request.contextPath}/employee/checkin" class="btn--cgv-outline">Mở màn hình quét vé</a>
                        </c:otherwise>
                    </c:choose>
                </div>

                <div class="emp-card">
                    <div class="emp-card-title">Lịch chiếu</div>
                    <div class="emp-card-desc">Tra cứu suất chiếu theo ngày, phim hoặc phòng — nơi bắt đầu để bán vé tại quầy</div>
                    <a href="${pageContext.request.contextPath}/employee/schedules" class="btn--cgv-outline">Xem lịch chiếu</a>
                </div>

                <div class="emp-card">
                    <div class="emp-card-title">Ca làm việc của tôi</div>
                    <div class="emp-card-desc">Xem lịch ca tháng này và gửi yêu cầu chuyển ca</div>
                    <a href="${pageContext.request.contextPath}/employee/my-shifts" class="btn--cgv-outline">Mở lịch ca</a>
                </div>

                <div class="emp-card">
                    <div class="emp-card-title">Đồ ăn &amp; nước</div>
                    <div class="emp-card-desc">Xem và cập nhật danh sách bắp nước đang bán</div>
                    <a href="${pageContext.request.contextPath}/FoodController" class="btn--cgv-outline">Mở danh sách</a>
                </div>

                <div class="emp-card">
                    <div class="emp-card-title">Thông tin cá nhân</div>
                    <div class="emp-card-desc">Xem hồ sơ nhân viên do quản lý cập nhật</div>
                    <a href="${pageContext.request.contextPath}/employee/profile" class="btn--cgv-outline">Xem hồ sơ</a>
                </div>

                <div class="emp-card">
                    <div class="emp-card-title">Đổi mật khẩu</div>
                    <div class="emp-card-desc">Cập nhật mật khẩu đăng nhập của bạn</div>
                    <a href="${pageContext.request.contextPath}/change-password" class="btn--cgv-outline">Đổi mật khẩu</a>
                </div>

            </div>

        </div>
    </div>
</div>
</body>
</html>
