<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% request.setAttribute("activeNav", "dashboard"); %>
<c:set var="r" value="${sessionScope.account.roleId}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Dashboard — CGV Manager</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
</head>
<body class="cgv-body">

<%@ include file="_sidebar.jsp" %>

<div class="cgv-main">

    <header class="cgv-header">
        <h1 class="cgv-header-title">Dashboard</h1>
        <div class="cgv-header-right">
            <div class="cgv-header-actions">
                <div class="cgv-header-divider"></div>
                <div class="cgv-user-wrap">
                    <div class="cgv-avatar">MG</div>
                    <span class="cgv-user-name">
                        <c:choose>
                            <c:when test="${not empty sessionScope.account}">${sessionScope.account.profile.fullName}</c:when>
                            <c:otherwise>Manager</c:otherwise>
                        </c:choose>
                    </span>
                </div>
            </div>
        </div>
    </header>

    <div class="cgv-page">
        <div class="cgv-list-wrap">

            <div style="display:grid;grid-template-columns:repeat(4,1fr);gap:16px;margin-bottom:32px;">
                <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:24px;">
                    <div style="font-family:var(--font-cgv-ui);font-size:10px;font-weight:700;letter-spacing:2px;text-transform:uppercase;color:rgba(94,63,58,0.5);margin-bottom:8px;">Doanh thu hôm nay</div>
                    <div class="cgv-stat-num" style="font-size:28px;">${not empty dashRevenueToday ? dashRevenueToday : '—'}</div>
                </div>
                <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:24px;">
                    <div style="font-family:var(--font-cgv-ui);font-size:10px;font-weight:700;letter-spacing:2px;text-transform:uppercase;color:rgba(94,63,58,0.5);margin-bottom:8px;">Vé đã bán hôm nay</div>
                    <div class="cgv-stat-num amber" style="font-size:28px;">${not empty dashTicketsToday ? dashTicketsToday : '—'}</div>
                </div>
                <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:24px;">
                    <div style="font-family:var(--font-cgv-ui);font-size:10px;font-weight:700;letter-spacing:2px;text-transform:uppercase;color:rgba(94,63,58,0.5);margin-bottom:8px;">Suất chiếu hôm nay</div>
                    <div class="cgv-stat-num" style="font-size:28px;">${not empty dashScreeningsToday ? dashScreeningsToday : '—'}</div>
                </div>
                <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:24px;">
                    <div style="font-family:var(--font-cgv-ui);font-size:10px;font-weight:700;letter-spacing:2px;text-transform:uppercase;color:rgba(94,63,58,0.5);margin-bottom:8px;">Khuyến mãi đang chạy</div>
                    <div class="cgv-stat-num red" style="font-size:28px;">${not empty dashActivePromos ? dashActivePromos : '—'}</div>
                </div>
            </div>

            <c:if test="${r ge 3}">
            <div style="font-family:var(--font-cgv-ui);font-size:10px;font-weight:700;letter-spacing:2px;text-transform:uppercase;color:rgba(94,63,58,0.4);margin-bottom:16px;">NỘI DUNG</div>
            <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:16px;margin-bottom:32px;">

                <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:24px;">
                    <div style="font-weight:700;font-family:var(--font-cgv-ui);margin-bottom:4px;">Phim &amp; Thể loại</div>
                    <div style="font-size:13px;color:rgba(94,63,58,0.6);margin-bottom:16px;">Thêm, sửa, ẩn phim và quản lý thể loại</div>
                    <div style="display:flex;gap:8px;flex-wrap:wrap;">
                        <a href="${pageContext.request.contextPath}/manager/movies" class="btn--cgv-outline">Danh sách phim</a>
                        <c:if test="${r ge 4}"><a href="${pageContext.request.contextPath}/manager/genre" class="btn--cgv-outline">Thể loại</a></c:if>
                    </div>
                </div>

                <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:24px;">
                    <div style="font-weight:700;font-family:var(--font-cgv-ui);margin-bottom:4px;">Phòng chiếu</div>
                    <div style="font-size:13px;color:rgba(94,63,58,0.6);margin-bottom:16px;">Quản lý phòng chiếu và sơ đồ chỗ ngồi</div>
                    <div style="display:flex;gap:8px;flex-wrap:wrap;">
                        <a href="${pageContext.request.contextPath}/RoomServlet" class="btn--cgv-outline">Danh sách phòng</a>
                    </div>
                </div>

                <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:24px;">
                    <div style="font-weight:700;font-family:var(--font-cgv-ui);margin-bottom:4px;">Lịch chiếu</div>
                    <div style="font-size:13px;color:rgba(94,63,58,0.6);margin-bottom:16px;">Tạo, sửa và huỷ các suất chiếu</div>
                    <div style="display:flex;gap:8px;flex-wrap:wrap;">
                        <a href="${pageContext.request.contextPath}/manager/schedules" class="btn--cgv-outline">Xem lịch chiếu</a>
                    </div>
                </div>

            </div>
            </c:if>

            <c:if test="${r ge 4}">
            <div style="font-family:var(--font-cgv-ui);font-size:10px;font-weight:700;letter-spacing:2px;text-transform:uppercase;color:rgba(94,63,58,0.4);margin-bottom:16px;">KINH DOANH</div>
            <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:16px;margin-bottom:32px;">

                <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:24px;">
                    <div style="font-weight:700;font-family:var(--font-cgv-ui);margin-bottom:4px;">Khuyến mãi</div>
                    <div style="font-size:13px;color:rgba(94,63,58,0.6);margin-bottom:16px;">Tạo và quản lý mã giảm giá, chương trình khuyến mãi</div>
                    <div style="display:flex;gap:8px;flex-wrap:wrap;">
                        <a href="${pageContext.request.contextPath}/manager/promotions" class="btn--cgv-outline">Danh sách</a>
                        <a href="${pageContext.request.contextPath}/manager/promotions?action=add" class="btn--cgv">+ Thêm mới</a>
                    </div>
                </div>

                <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:24px;">
                    <div style="font-weight:700;font-family:var(--font-cgv-ui);margin-bottom:4px;">Thống kê doanh thu</div>
                    <div style="font-size:13px;color:rgba(94,63,58,0.6);margin-bottom:16px;">Báo cáo doanh thu vé và đồ ăn theo thời gian</div>
                    <div style="display:flex;gap:8px;flex-wrap:wrap;">
                        <a href="${pageContext.request.contextPath}/manager/analytics" class="btn--cgv-outline">Xem báo cáo</a>
                    </div>
                </div>

                <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:24px;">
                    <div style="font-weight:700;font-family:var(--font-cgv-ui);margin-bottom:4px;">Đồ ăn &amp; Menu</div>
                    <div style="font-size:13px;color:rgba(94,63,58,0.6);margin-bottom:16px;">Quản lý danh mục đồ ăn và thức uống</div>
                    <div style="display:flex;gap:8px;flex-wrap:wrap;">
                        <a href="${pageContext.request.contextPath}/manager/food" class="btn--cgv-outline">Danh sách menu</a>
                    </div>
                </div>

            </div>
            </c:if>

            <div style="font-family:var(--font-cgv-ui);font-size:10px;font-weight:700;letter-spacing:2px;text-transform:uppercase;color:rgba(94,63,58,0.4);margin-bottom:16px;">HỆ THỐNG</div>
            <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:16px;">

                <c:if test="${r ge 4}">
                <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:24px;">
                    <div style="font-weight:700;font-family:var(--font-cgv-ui);margin-bottom:4px;">Tài khoản người dùng</div>
                    <div style="font-size:13px;color:rgba(94,63,58,0.6);margin-bottom:16px;">Xem, khoá và mở khoá tài khoản</div>
                    <div style="display:flex;gap:8px;flex-wrap:wrap;">
                        <a href="${pageContext.request.contextPath}/manager/users" class="btn--cgv-outline">Danh sách</a>
                    </div>
                </div>
                </c:if>

                <c:if test="${r ge 3}">
                <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:24px;">
                    <div style="font-weight:700;font-family:var(--font-cgv-ui);margin-bottom:4px;">Check-in vé</div>
                    <div style="font-size:13px;color:rgba(94,63,58,0.6);margin-bottom:16px;">Tra cứu và xác nhận vé đặt chỗ</div>
                    <div style="display:flex;gap:8px;flex-wrap:wrap;">
                        <a href="${pageContext.request.contextPath}/manager/checkin" class="btn--cgv-outline">Check-in</a>
                    </div>
                </div>
                </c:if>

                <c:if test="${r ge 4}">
                <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:24px;">
                    <div style="font-weight:700;font-family:var(--font-cgv-ui);margin-bottom:4px;">Cấu hình hệ thống</div>
                    <div style="font-size:13px;color:rgba(94,63,58,0.6);margin-bottom:16px;">Thiết lập thông số và xem nhật ký vận hành</div>
                    <div style="display:flex;gap:8px;flex-wrap:wrap;">
                        <a href="${pageContext.request.contextPath}/manager/settings" class="btn--cgv-outline">Cấu hình</a>
                    </div>
                </div>
                </c:if>

            </div>
                </div>

                <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:24px;">
                    <div style="font-weight:700;font-family:var(--font-cgv-ui);margin-bottom:4px;">Phòng chiếu</div>
                    <div style="font-size:13px;color:rgba(94,63,58,0.6);margin-bottom:16px;">Quản lý phòng chiếu và sơ đồ chỗ ngồi</div>
                    <div style="display:flex;gap:8px;flex-wrap:wrap;">
                        <a href="${pageContext.request.contextPath}/RoomServlet" class="btn--cgv-outline">Danh sách phòng</a>
                    </div>
                </div>
8   
                <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:24px;">
                    <div style="font-weight:700;font-family:var(--font-cgv-ui);margin-bottom:4px;">Lịch chiếu</div>
                    <div style="font-size:13px;color:rgba(94,63,58,0.6);margin-bottom:16px;">Tạo, sửa và huỷ các suất chiếu</div>
                    <div style="display:flex;gap:8px;flex-wrap:wrap;">
                        <a href="${pageContext.request.contextPath}/manager/schedules" class="btn--cgv-outline">Xem lịch chiếu</a>
                    </div>
                </div>

            </div>

            <div style="font-family:var(--font-cgv-ui);font-size:10px;font-weight:700;letter-spacing:2px;text-transform:uppercase;color:rgba(94,63,58,0.4);margin-bottom:16px;">KINH DOANH</div>
            <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:16px;margin-bottom:32px;">

                <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:24px;">
                    <div style="font-weight:700;font-family:var(--font-cgv-ui);margin-bottom:4px;">Khuyến mãi</div>
                    <div style="font-size:13px;color:rgba(94,63,58,0.6);margin-bottom:16px;">Tạo và quản lý mã giảm giá, chương trình khuyến mãi</div>
                    <div style="display:flex;gap:8px;flex-wrap:wrap;">
                        <a href="${pageContext.request.contextPath}/manager/promotions" class="btn--cgv-outline">Danh sách</a>
                        <a href="${pageContext.request.contextPath}/manager/promotions?action=add" class="btn--cgv">+ Thêm mới</a>
                    </div>
                </div>

                <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:24px;">
                    <div style="font-weight:700;font-family:var(--font-cgv-ui);margin-bottom:4px;">Thống kê doanh thu</div>
                    <div style="font-size:13px;color:rgba(94,63,58,0.6);margin-bottom:16px;">Báo cáo doanh thu vé và đồ ăn theo thời gian</div>
                    <div style="display:flex;gap:8px;flex-wrap:wrap;">
                        <a href="${pageContext.request.contextPath}/manager/analytics" class="btn--cgv-outline">Xem báo cáo</a>
                    </div>
                </div>

                <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:24px;">
                    <div style="font-weight:700;font-family:var(--font-cgv-ui);margin-bottom:4px;">Đồ ăn &amp; Menu</div>
                    <div style="font-size:13px;color:rgba(94,63,58,0.6);margin-bottom:16px;">Quản lý danh mục đồ ăn và thức uống</div>
                    <div style="display:flex;gap:8px;flex-wrap:wrap;">
                        <a href="${pageContext.request.contextPath}/manager/food" class="btn--cgv-outline">Danh sách menu</a>
                    </div>
                </div>

            </div>

            <div style="font-family:var(--font-cgv-ui);font-size:10px;font-weight:700;letter-spacing:2px;text-transform:uppercase;color:rgba(94,63,58,0.4);margin-bottom:16px;">HỆ THỐNG</div>
            <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:16px;">

                <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:24px;">
                    <div style="font-weight:700;font-family:var(--font-cgv-ui);margin-bottom:4px;">Tài khoản người dùng</div>
                    <div style="font-size:13px;color:rgba(94,63,58,0.6);margin-bottom:16px;">Xem, khoá và mở khoá tài khoản</div>
                    <div style="display:flex;gap:8px;flex-wrap:wrap;">
                        <a href="${pageContext.request.contextPath}/manager/users" class="btn--cgv-outline">Danh sách</a>
                    </div>
                </div>

                <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:24px;">
                    <div style="font-weight:700;font-family:var(--font-cgv-ui);margin-bottom:4px;">Check-in vé</div>
                    <div style="font-size:13px;color:rgba(94,63,58,0.6);margin-bottom:16px;">Tra cứu và xác nhận vé đặt chỗ</div>
                    <div style="display:flex;gap:8px;flex-wrap:wrap;">
                        <a href="${pageContext.request.contextPath}/manager/checkin" class="btn--cgv-outline">Check-in</a>
                    </div>
                </div>

                <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:24px;">
                    <div style="font-weight:700;font-family:var(--font-cgv-ui);margin-bottom:4px;">Cấu hình hệ thống</div>
                    <div style="font-size:13px;color:rgba(94,63,58,0.6);margin-bottom:16px;">Thiết lập thông số và xem nhật ký vận hành</div>
                    <div style="display:flex;gap:8px;flex-wrap:wrap;">
                        <a href="${pageContext.request.contextPath}/manager/settings" class="btn--cgv-outline">Cấu hình</a>
                    </div>
                </div>

            </div>

        </div>
    </div>
</div>
</body>
</html>
