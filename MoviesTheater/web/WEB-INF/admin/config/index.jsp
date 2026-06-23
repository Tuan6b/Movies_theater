<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% request.setAttribute("activeNav", "config"); %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>System Config — CGV Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
    <style>
        .config-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 32px;
        }
        .config-card {
            background: #fff;
            border: 1px solid var(--cgv-border);
            border-radius: 12px;
            padding: 28px;
        }
        .config-card-title {
            font-family: var(--font-cgv-display);
            font-size: 18px;
            color: var(--cgv-dark);
            margin: 0 0 20px 0;
            padding-bottom: 14px;
            border-bottom: 2px solid var(--cgv-border);
        }
        .field-group {
            margin-bottom: 18px;
        }
        .field-group label {
            display: block;
            font-size: 11px;
            font-weight: 700;
            letter-spacing: 1px;
            text-transform: uppercase;
            color: rgba(94,63,58,0.55);
            margin-bottom: 6px;
        }
        .field-hint {
            font-size: 11px;
            color: rgba(94,63,58,0.4);
            margin-top: 4px;
        }
        @media (max-width: 900px) {
            .config-grid { grid-template-columns: 1fr; }
        }
    </style>
</head>
<body class="cgv-body">

<%@ include file="../_sidebar.jsp" %>

<div class="cgv-main">

    <header class="cgv-header">
        <h1 class="cgv-header-title">System Configuration (UC50)</h1>
        <div class="cgv-header-right">
            <div class="cgv-user-wrap">
                <div class="cgv-avatar">SA</div>
                <span class="cgv-user-name">
                    <c:choose>
                        <c:when test="${not empty sessionScope.account}">${sessionScope.account.fullName}</c:when>
                        <c:otherwise>Admin</c:otherwise>
                    </c:choose>
                </span>
            </div>
        </div>
    </header>

    <div class="cgv-page" style="flex-direction: column;">
        <div class="cgv-list-wrap">

            <c:if test="${not empty flashSuccess}">
                <div class="cgv-alert cgv-alert-success">${flashSuccess}</div>
            </c:if>
            <c:if test="${not empty flashError}">
                <div class="cgv-alert cgv-alert-danger">${flashError}</div>
            </c:if>

            <form method="post" action="${pageContext.request.contextPath}/admin/config">

                <div class="config-grid">

                    <%-- Cinema Information --%>
                    <div class="config-card">
                        <h3 class="config-card-title">Thông tin rạp chiếu</h3>

                        <div class="field-group">
                            <label for="cinema_name">Tên rạp</label>
                            <input class="cgv-input" type="text" id="cinema_name" name="cinema_name"
                                   value="${config['cinema_name']}" required maxlength="200">
                        </div>

                        <div class="field-group">
                            <label for="cinema_address">Địa chỉ</label>
                            <input class="cgv-input" type="text" id="cinema_address" name="cinema_address"
                                   value="${config['cinema_address']}" maxlength="500">
                        </div>

                        <div class="field-group">
                            <label for="cinema_phone">Số điện thoại</label>
                            <input class="cgv-input" type="text" id="cinema_phone" name="cinema_phone"
                                   value="${config['cinema_phone']}" maxlength="50">
                        </div>

                        <div class="field-group">
                            <label for="cinema_email">Email liên hệ</label>
                            <input class="cgv-input" type="email" id="cinema_email" name="cinema_email"
                                   value="${config['cinema_email']}" maxlength="255">
                        </div>

                        <div class="field-group">
                            <label for="banner_url">URL Banner trang chủ</label>
                            <input class="cgv-input" type="text" id="banner_url" name="banner_url"
                                   value="${config['banner_url']}" placeholder="https://..." maxlength="1000">
                            <div class="field-hint">Đường dẫn ảnh hoặc video banner hiển thị trên trang chủ.</div>
                        </div>
                    </div>

                    <%-- Booking Rules --%>
                    <div class="config-card">
                        <h3 class="config-card-title">Chính sách đặt vé</h3>

                        <div class="field-group">
                            <label for="max_seats_per_booking">Số ghế tối đa mỗi lần đặt</label>
                            <input class="cgv-input" type="number" id="max_seats_per_booking"
                                   name="max_seats_per_booking" min="1" max="20"
                                   value="${config['max_seats_per_booking']}">
                            <div class="field-hint">Mặc định: 8 ghế / giao dịch.</div>
                        </div>

                        <div class="field-group">
                            <label for="cancel_hours_before">Giờ tối thiểu để hủy vé</label>
                            <input class="cgv-input" type="number" id="cancel_hours_before"
                                   name="cancel_hours_before" min="0" max="72"
                                   value="${config['cancel_hours_before']}">
                            <div class="field-hint">Khách hàng chỉ được hủy nếu còn ít nhất X giờ trước suất chiếu.</div>
                        </div>

                        <div class="field-group">
                            <label for="base_ticket_price">Giá vé cơ bản mặc định (VND)</label>
                            <input class="cgv-input" type="number" id="base_ticket_price"
                                   name="base_ticket_price" min="0" step="1000"
                                   value="${config['base_ticket_price']}">
                            <div class="field-hint">Dùng khi tạo lịch chiếu mới mà không chỉ định giá riêng.</div>
                        </div>

                        <div style="margin-top: 32px; padding-top: 20px; border-top: 1px solid var(--cgv-border);">
                            <p style="font-size:12px; color:rgba(94,63,58,0.45); margin-bottom:16px;">
                                Thay đổi sẽ có hiệu lực ngay sau khi lưu.
                            </p>
                            <button type="submit" class="btn--cgv" style="width:100%; height:46px; font-size:14px;">
                                Lưu cấu hình
                            </button>
                        </div>
                    </div>

                </div>

            </form>

        </div>
    </div>
</div>
</body>
</html>
