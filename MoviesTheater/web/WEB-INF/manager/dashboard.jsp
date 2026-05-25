<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% request.setAttribute("activeNav", "dashboard"); %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Manager Dashboard</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
</head>
<body>
<div class="layout">

    <%@ include file="_sidebar.jsp" %>

    <div class="main">
        <div class="topbar">
            <div class="topbar-title">Tổng quan hệ thống</div>
            <div class="topbar-action">
                <div class="topbar-avatar">MG</div>
            </div>
        </div>

        <div class="page-content fade-in">

            <!-- KPI Summary -->
            <div style="display:grid; grid-template-columns:repeat(4,1fr); gap:14px; margin-bottom:32px;">
                <div class="metric">
                    <div class="metric-label">Doanh thu hôm nay</div>
                    <div class="metric-value">${dashRevenueToday != null ? dashRevenueToday : '—'}</div>
                </div>
                <div class="metric">
                    <div class="metric-label">Vé đã bán hôm nay</div>
                    <div class="metric-value">${dashTicketsToday != null ? dashTicketsToday : '—'}</div>
                </div>
                <div class="metric">
                    <div class="metric-label">Suất chiếu hôm nay</div>
                    <div class="metric-value">${dashScreeningsToday != null ? dashScreeningsToday : '—'}</div>
                </div>
                <div class="metric">
                    <div class="metric-label">Khuyến mãi đang chạy</div>
                    <div class="metric-value">${dashActivePromos != null ? dashActivePromos : '—'}</div>
                </div>
            </div>

            <!-- Nội dung -->
            <div class="section-heading">Nội dung</div>
            <div class="module-grid">

                <div class="module-card">
                    <div class="module-card-header">
                        <div>
                            <div class="module-card-title">Phim &amp; Thể loại</div>
                            <div class="module-card-desc">Thêm, sửa, ẩn phim và quản lý thể loại</div>
                        </div>
                    </div>
                    <div class="module-actions">
                        <a href="#" class="btn btn-outline">Danh sách phim</a>
                        <a href="#" class="btn btn-outline">Thêm phim mới</a>
                        <a href="#" class="btn btn-outline">Thể loại</a>
                    </div>
                </div>

                <div class="module-card">
                    <div class="module-card-header">
                        <div>
                            <div class="module-card-title">Đánh giá phim</div>
                            <div class="module-card-desc">Xem và kiểm duyệt đánh giá của khách hàng</div>
                        </div>
                    </div>
                    <div class="module-actions">
                        <a href="#" class="btn btn-outline">Xem đánh giá</a>
                    </div>
                </div>

            </div>

            <!-- Vận hành -->
            <div class="section-heading">Vận hành</div>
            <div class="module-grid">

                <div class="module-card">
                    <div class="module-card-header">
                        <div>
                            <div class="module-card-title">Phòng chiếu</div>
                            <div class="module-card-desc">Quản lý phòng chiếu và sơ đồ chỗ ngồi</div>
                        </div>
                    </div>
                    <div class="module-actions">
                        <a href="#" class="btn btn-outline">Danh sách phòng</a>
                        <a href="#" class="btn btn-outline">Thêm phòng mới</a>
                    </div>
                </div>

                <div class="module-card">
                    <div class="module-card-header">
                        <div>
                            <div class="module-card-title">Lịch chiếu</div>
                            <div class="module-card-desc">Tạo, sửa và huỷ các suất chiếu</div>
                        </div>
                    </div>
                    <div class="module-actions">
                        <a href="#" class="btn btn-outline">Xem lịch chiếu</a>
                        <a href="#" class="btn btn-outline">Tạo suất chiếu</a>
                    </div>
                </div>

                <div class="module-card">
                    <div class="module-card-header">
                        <div>
                            <div class="module-card-title">Đồ ăn &amp; Menu</div>
                            <div class="module-card-desc">Quản lý danh mục đồ ăn và thức uống</div>
                        </div>
                    </div>
                    <div class="module-actions">
                        <a href="#" class="btn btn-outline">Danh sách menu</a>
                        <a href="#" class="btn btn-outline">Thêm món mới</a>
                    </div>
                </div>

            </div>

            <!-- Kinh doanh -->
            <div class="section-heading">Kinh doanh</div>
            <div class="module-grid">

                <div class="module-card">
                    <div class="module-card-header">
                        <div>
                            <div class="module-card-title">Đặt vé &amp; Thanh toán</div>
                            <div class="module-card-desc">Tra cứu và quản lý các đơn đặt vé</div>
                        </div>
                    </div>
                    <div class="module-actions">
                        <a href="#" class="btn btn-outline">Danh sách đặt vé</a>
                    </div>
                </div>

                <div class="module-card">
                    <div class="module-card-header">
                        <div>
                            <div class="module-card-title">Khuyến mãi</div>
                            <div class="module-card-desc">Tạo và quản lý mã giảm giá, chương trình khuyến mãi</div>
                        </div>
                    </div>
                    <div class="module-actions">
                        <a href="${pageContext.request.contextPath}/manager/promotions" class="btn btn-success">Danh sách khuyến mãi</a>
                        <a href="${pageContext.request.contextPath}/manager/promotions?action=add" class="btn btn-primary">Thêm khuyến mãi</a>
                    </div>
                </div>

                <div class="module-card">
                    <div class="module-card-header">
                        <div>
                            <div class="module-card-title">Thống kê doanh thu</div>
                            <div class="module-card-desc">Báo cáo doanh thu vé và đồ ăn theo thời gian</div>
                        </div>
                    </div>
                    <div class="module-actions">
                        <a href="#" class="btn btn-outline">Xem báo cáo</a>
                    </div>
                </div>

            </div>

            <!-- Hệ thống -->
            <div class="section-heading">Hệ thống</div>
            <div class="module-grid">

                <div class="module-card">
                    <div class="module-card-header">
                        <div>
                            <div class="module-card-title">Nhân viên</div>
                            <div class="module-card-desc">Quản lý thông tin và trạng thái nhân viên</div>
                        </div>
                    </div>
                    <div class="module-actions">
                        <a href="#" class="btn btn-outline">Danh sách nhân viên</a>
                        <a href="#" class="btn btn-outline">Thêm nhân viên</a>
                    </div>
                </div>

                <div class="module-card">
                    <div class="module-card-header">
                        <div>
                            <div class="module-card-title">Tài khoản khách hàng</div>
                            <div class="module-card-desc">Xem, khoá và mở khoá tài khoản người dùng</div>
                        </div>
                    </div>
                    <div class="module-actions">
                        <a href="#" class="btn btn-outline">Danh sách tài khoản</a>
                    </div>
                </div>

                <div class="module-card">
                    <div class="module-card-header">
                        <div>
                            <div class="module-card-title">Cấu hình hệ thống</div>
                            <div class="module-card-desc">Thiết lập thông số và xem nhật ký vận hành</div>
                        </div>
                    </div>
                    <div class="module-actions">
                        <a href="#" class="btn btn-outline">Cấu hình</a>
                        <a href="#" class="btn btn-outline">Nhật ký hệ thống</a>
                    </div>
                </div>

            </div>

        </div><!-- /page-content -->
    </div><!-- /main -->

</div><!-- /layout -->
</body>
</html>
