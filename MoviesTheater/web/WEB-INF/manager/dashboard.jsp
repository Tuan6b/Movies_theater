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

            <!-- Nhóm 1: Xác thực & Tài khoản -->
            <div class="section-heading">Nhóm 1 — Xác thực &amp; Tài khoản người dùng</div>
            <div class="module-grid">

                <div class="module-card">
                    <div class="module-card-header">
                        <div>
                            <div class="module-card-title">Đăng ký / Đăng nhập</div>
                            <div class="module-card-desc">UC01–UC03 · Guest &amp; User<br>Tạo tài khoản, đăng nhập, đăng xuất</div>
                        </div>
                        <div class="module-status"><span class="badge badge-pending">Chờ</span></div>
                    </div>
                    <div class="module-actions">
                        <span class="btn btn-placeholder">Đăng ký (UC01)</span>
                        <span class="btn btn-placeholder">Đăng nhập (UC02)</span>
                        <span class="btn btn-placeholder">Đăng xuất (UC03)</span>
                    </div>
                </div>

                <div class="module-card">
                    <div class="module-card-header">
                        <div>
                            <div class="module-card-title">Mật khẩu &amp; Hồ sơ</div>
                            <div class="module-card-desc">UC04–UC07 · Guest &amp; User<br>Quên mật khẩu, đổi mật khẩu, xem/sửa hồ sơ</div>
                        </div>
                        <div class="module-status"><span class="badge badge-pending">Chờ</span></div>
                    </div>
                    <div class="module-actions">
                        <span class="btn btn-placeholder">Quên mật khẩu (UC04)</span>
                        <span class="btn btn-placeholder">Đổi mật khẩu (UC05)</span>
                        <span class="btn btn-placeholder">Xem hồ sơ (UC06)</span>
                        <span class="btn btn-placeholder">Sửa hồ sơ (UC07)</span>
                    </div>
                </div>

                <div class="module-card">
                    <div class="module-card-header">
                        <div>
                            <div class="module-card-title">Quản lý tài khoản</div>
                            <div class="module-card-desc">UC08–UC10 · System Admin<br>Khóa tài khoản, phân quyền, xem danh sách</div>
                        </div>
                        <div class="module-status"><span class="badge badge-pending">Chờ</span></div>
                    </div>
                    <div class="module-actions">
                        <span class="btn btn-placeholder">Khóa / Mở khoá (UC08)</span>
                        <span class="btn btn-placeholder">Phân quyền (UC09)</span>
                        <span class="btn btn-placeholder">DS tài khoản (UC10)</span>
                    </div>
                </div>

            </div>

            <!-- Nhóm 2: Phim & Đánh giá -->
            <div class="section-heading">Nhóm 2 — Phim, Thể loại &amp; Đánh giá</div>
            <div class="module-grid">

                <div class="module-card">
                    <div class="module-card-header">
                        <div>
                            <div class="module-card-title">Danh sách &amp; Tìm kiếm phim</div>
                            <div class="module-card-desc">UC11–UC13 · Guest<br>Xem danh sách, chi tiết, tìm kiếm phim</div>
                        </div>
                        <div class="module-status"><span class="badge badge-pending">Chờ</span></div>
                    </div>
                    <div class="module-actions">
                        <span class="btn btn-placeholder">DS phim (UC11)</span>
                        <span class="btn btn-placeholder">Chi tiết phim (UC12)</span>
                        <span class="btn btn-placeholder">Tìm kiếm (UC13)</span>
                    </div>
                </div>

                <div class="module-card">
                    <div class="module-card-header">
                        <div>
                            <div class="module-card-title">Quản lý phim &amp; Thể loại</div>
                            <div class="module-card-desc">UC14–UC17 · Manager<br>Thêm, sửa, xoá phim và quản lý thể loại</div>
                        </div>
                        <div class="module-status"><span class="badge badge-pending">Chờ</span></div>
                    </div>
                    <div class="module-actions">
                        <span class="btn btn-placeholder">Thêm phim (UC14)</span>
                        <span class="btn btn-placeholder">Sửa phim (UC15)</span>
                        <span class="btn btn-placeholder">Ẩn / Xoá phim (UC16)</span>
                        <span class="btn btn-placeholder">Thể loại (UC17)</span>
                    </div>
                </div>

                <div class="module-card">
                    <div class="module-card-header">
                        <div>
                            <div class="module-card-title">Đánh giá phim</div>
                            <div class="module-card-desc">UC18–UC21 · Guest &amp; Customer<br>Xem, gửi, sửa đánh giá và thống kê</div>
                        </div>
                        <div class="module-status"><span class="badge badge-pending">Chờ</span></div>
                    </div>
                    <div class="module-actions">
                        <span class="btn btn-placeholder">DS đánh giá (UC18)</span>
                        <span class="btn btn-placeholder">Gửi đánh giá (UC19)</span>
                        <span class="btn btn-placeholder">Sửa / Xoá (UC20)</span>
                        <span class="btn btn-placeholder">Thống kê (UC21)</span>
                    </div>
                </div>

            </div>

            <!-- Nhóm 3: Đặt vé & Thanh toán -->
            <div class="section-heading">Nhóm 3 — Đặt vé &amp; Thanh toán</div>
            <div class="module-grid">

                <div class="module-card">
                    <div class="module-card-header">
                        <div>
                            <div class="module-card-title">Lịch chiếu</div>
                            <div class="module-card-desc">UC22 · Guest<br>Xem lịch chiếu theo ngày và rạp</div>
                        </div>
                        <div class="module-status"><span class="badge badge-pending">Chờ</span></div>
                    </div>
                    <div class="module-actions">
                        <span class="btn btn-placeholder">Xem lịch chiếu (UC22)</span>
                    </div>
                </div>

                <div class="module-card">
                    <div class="module-card-header">
                        <div>
                            <div class="module-card-title">Đặt vé &amp; Thanh toán</div>
                            <div class="module-card-desc">UC23–UC27 · Customer<br>Chọn ghế, đồ ăn, áp dụng mã giảm giá, thanh toán</div>
                        </div>
                        <div class="module-status"><span class="badge badge-pending">Chờ</span></div>
                    </div>
                    <div class="module-actions">
                        <span class="btn btn-placeholder">Đặt vé (UC23)</span>
                        <span class="btn btn-placeholder">Chọn ghế (UC24)</span>
                        <span class="btn btn-placeholder">Đồ ăn (UC25)</span>
                        <span class="btn btn-placeholder">Mã giảm giá (UC26)</span>
                        <span class="btn btn-placeholder">Thanh toán (UC27)</span>
                    </div>
                </div>

                <div class="module-card">
                    <div class="module-card-header">
                        <div>
                            <div class="module-card-title">Lịch sử vé &amp; Hoá đơn</div>
                            <div class="module-card-desc">UC28–UC30 · Customer<br>Xem danh sách vé đã mua và lịch sử thanh toán</div>
                        </div>
                        <div class="module-status"><span class="badge badge-pending">Chờ</span></div>
                    </div>
                    <div class="module-actions">
                        <span class="btn btn-placeholder">DS vé đã mua (UC28)</span>
                        <span class="btn btn-placeholder">Chi tiết vé / QR (UC29)</span>
                        <span class="btn btn-placeholder">Lịch sử hoá đơn (UC30)</span>
                    </div>
                </div>

            </div>

            <!-- Nhóm 4: Phòng chiếu, Lịch chiếu & Đồ ăn -->
            <div class="section-heading">Nhóm 4 — Phòng chiếu, Lịch chiếu &amp; Đồ ăn</div>
            <div class="module-grid">

                <div class="module-card">
                    <div class="module-card-header">
                        <div>
                            <div class="module-card-title">Phòng chiếu &amp; Sơ đồ ghế</div>
                            <div class="module-card-desc">UC32–UC35 · Manager<br>Thêm, sửa, xoá phòng chiếu và sắp xếp ghế</div>
                        </div>
                        <div class="module-status"><span class="badge badge-pending">Chờ</span></div>
                    </div>
                    <div class="module-actions">
                        <span class="btn btn-placeholder">Thêm phòng (UC32)</span>
                        <span class="btn btn-placeholder">Sửa phòng (UC33)</span>
                        <span class="btn btn-placeholder">Xoá phòng (UC34)</span>
                        <span class="btn btn-placeholder">Sơ đồ ghế (UC35)</span>
                    </div>
                </div>

                <div class="module-card">
                    <div class="module-card-header">
                        <div>
                            <div class="module-card-title">Quản lý lịch chiếu</div>
                            <div class="module-card-desc">UC36–UC38 · Manager<br>Tạo, sửa, huỷ suất chiếu</div>
                        </div>
                        <div class="module-status"><span class="badge badge-pending">Chờ</span></div>
                    </div>
                    <div class="module-actions">
                        <span class="btn btn-placeholder">Tạo lịch chiếu (UC36)</span>
                        <span class="btn btn-placeholder">Sửa lịch chiếu (UC37)</span>
                        <span class="btn btn-placeholder">Huỷ suất chiếu (UC38)</span>
                    </div>
                </div>

                <div class="module-card">
                    <div class="module-card-header">
                        <div>
                            <div class="module-card-title">Đồ ăn &amp; Menu</div>
                            <div class="module-card-desc">UC39–UC42 · Employee<br>Xem, thêm, sửa, ẩn/xoá món ăn trong menu</div>
                        </div>
                        <div class="module-status"><span class="badge badge-pending">Chờ</span></div>
                    </div>
                    <div class="module-actions">
                        <span class="btn btn-placeholder">DS đồ ăn (UC39)</span>
                        <span class="btn btn-placeholder">Thêm món (UC40)</span>
                        <span class="btn btn-placeholder">Sửa món (UC41)</span>
                        <span class="btn btn-placeholder">Xoá món (UC42)</span>
                    </div>
                </div>

            </div>

            <!-- Nhóm 5: Khuyến mãi, Nhân viên, Thống kê & Hệ thống -->
            <div class="section-heading">Nhóm 5 — Khuyến mãi, Nhân viên, Thống kê &amp; Hệ thống</div>
            <div class="module-grid">

                <div class="module-card">
                    <div class="module-card-header">
                        <div>
                            <div class="module-card-title">Quản lý khuyến mãi</div>
                            <div class="module-card-desc">UC43–UC44 · Manager<br>Tạo, sửa, xoá và xem danh sách mã giảm giá</div>
                        </div>
                        <div class="module-status"><span class="badge badge-done">Hoàn thành</span></div>
                    </div>
                    <div class="module-actions">
                        <a href="${pageContext.request.contextPath}/manager/promotions" class="btn btn-success">DS khuyến mãi (UC44)</a>
                        <a href="${pageContext.request.contextPath}/manager/promotions?action=add" class="btn btn-primary">Thêm mới (UC43)</a>
                    </div>
                </div>

                <div class="module-card">
                    <div class="module-card-header">
                        <div>
                            <div class="module-card-title">Quản lý nhân viên</div>
                            <div class="module-card-desc">UC45–UC46 · Manager<br>Thêm, sửa, kích hoạt/vô hiệu hoá và xem DS nhân viên</div>
                        </div>
                        <div class="module-status"><span class="badge badge-pending">Chờ</span></div>
                    </div>
                    <div class="module-actions">
                        <span class="btn btn-placeholder">DS nhân viên (UC46)</span>
                        <span class="btn btn-placeholder">Thêm nhân viên (UC45)</span>
                    </div>
                </div>

                <div class="module-card">
                    <div class="module-card-header">
                        <div>
                            <div class="module-card-title">Vé thủ công &amp; Check-in</div>
                            <div class="module-card-desc">UC47–UC49 · Employee<br>Quét QR check-in, xem danh sách vé, tạo vé thủ công</div>
                        </div>
                        <div class="module-status"><span class="badge badge-pending">Chờ</span></div>
                    </div>
                    <div class="module-actions">
                        <span class="btn btn-placeholder">Quét QR (UC47)</span>
                        <span class="btn btn-placeholder">DS vé đặt (UC48)</span>
                        <span class="btn btn-placeholder">Tạo vé thủ công (UC49)</span>
                    </div>
                </div>

                <div class="module-card">
                    <div class="module-card-header">
                        <div>
                            <div class="module-card-title">Thống kê doanh thu</div>
                            <div class="module-card-desc">UC50 · Manager<br>Báo cáo doanh thu vé và đồ ăn</div>
                        </div>
                        <div class="module-status"><span class="badge badge-pending">Chờ</span></div>
                    </div>
                    <div class="module-actions">
                        <span class="btn btn-placeholder">Xem thống kê (UC50)</span>
                    </div>
                </div>

                <div class="module-card">
                    <div class="module-card-header">
                        <div>
                            <div class="module-card-title">Cấu hình &amp; Nhật ký hệ thống</div>
                            <div class="module-card-desc">UC51–UC52 · System Admin<br>Thay đổi cấu hình và xem nhật ký thao tác</div>
                        </div>
                        <div class="module-status"><span class="badge badge-pending">Chờ</span></div>
                    </div>
                    <div class="module-actions">
                        <span class="btn btn-placeholder">Cấu hình hệ thống (UC51)</span>
                        <span class="btn btn-placeholder">Nhật ký hệ thống (UC52)</span>
                    </div>
                </div>

            </div>

        </div><!-- /page-content -->
    </div><!-- /main -->

</div><!-- /layout -->
</body>
</html>
