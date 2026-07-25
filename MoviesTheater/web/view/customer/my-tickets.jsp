<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Vé của tôi — CGV Cinema</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/my-tickets.css">
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    </head>
    <body>
        <header class="site-header">
            <div class="site-header-inner">
                <a href="${pageContext.request.contextPath}/HomeController" class="site-logo">
                    <img src="${pageContext.request.contextPath}/Image/Icon/cgvlogo.png" alt="CGV">
                    <span class="site-logo-text">CGV CINEMA</span>
                </a>

                <nav class="site-nav">
                    <a href="${pageContext.request.contextPath}/HomeController">Phim Đang Chiếu</a>
                    <a href="${pageContext.request.contextPath}/HomeController#upcoming-movies">Phim Sắp Chiếu</a>
                    <a href="#">Rạp &amp; Giá Vé</a>
                    <a href="${pageContext.request.contextPath}/my-tickets" class="active">Vé của tôi</a>
                </nav>

                <div class="site-header-actions">
                    <span class="customer-name">
                        Xin chào, <strong><c:out value="${sessionScope.account.fullName}"/></strong>
                    </span>
                    <a href="${pageContext.request.contextPath}/Logout" class="btn btn-ghost">Đăng xuất</a>
                </div>
            </div>
        </header>

        <main class="my-tickets-page">
            <c:choose>
                <c:when test="${viewMode == 'detail'}">
                    <section class="ticket-detail-section">
                        <div class="my-tickets-inner">
                            <a class="back-link" href="${pageContext.request.contextPath}/my-tickets">
                                <i class="fa-solid fa-arrow-left"></i> Quay lại lịch sử vé
                            </a>

                            <div class="detail-heading">
                                <div>
                                    <div class="section-eyebrow">My Tickets</div>
                                    <h1>Chi tiết vé</h1>
                                </div>
                                <c:choose>
                                    <c:when test="${booking.valid}">
                                        <span class="ticket-status status-valid">
                                            <i class="fa-solid fa-circle-check"></i> Còn hiệu lực
                                        </span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="ticket-status status-expired">
                                            <i class="fa-solid fa-clock-rotate-left"></i> Hết hiệu lực
                                        </span>
                                    </c:otherwise>
                                </c:choose>
                            </div>

                            <div class="detail-layout">
                                <article class="cinema-ticket ${booking.valid ? '' : 'cinema-ticket-expired'}">
                                    <div class="ticket-poster-panel">
                                        <c:choose>
                                            <c:when test="${not empty booking.poster}">
                                                <img src="${fn:escapeXml(booking.poster)}" alt="Poster ${fn:escapeXml(booking.movieName)}">
                                            </c:when>
                                            <c:otherwise>
                                                <img src="${pageContext.request.contextPath}/Image/default_poster.svg" alt="Poster mặc định">
                                            </c:otherwise>
                                        </c:choose>
                                        <div class="poster-shade"></div>
                                        <div class="poster-brand">CGV CINEMA</div>
                                    </div>

                                    <div class="ticket-main-panel">
                                        <div class="ticket-main-top">
                                            <div>
                                                <div class="ticket-label">Phim</div>
                                                <h2><c:out value="${booking.movieName}"/></h2>
                                            </div>
                                            <span class="age-chip"><c:out value="${booking.roomType}"/></span>
                                        </div>

                                        <div class="ticket-info-grid">
                                            <div>
                                                <span>Ngày chiếu</span>
                                                <strong><fmt:formatDate value="${booking.startTime}" pattern="dd/MM/yyyy"/></strong>
                                            </div>
                                            <div>
                                                <span>Giờ chiếu</span>
                                                <strong><fmt:formatDate value="${booking.startTime}" pattern="HH:mm"/></strong>
                                            </div>
                                            <div>
                                                <span>Phòng</span>
                                                <strong><c:out value="${booking.roomNumber}"/> · <c:out value="${booking.roomType}"/></strong>
                                            </div>
                                            <div>
                                                <span>Ghế</span>
                                                <strong><c:out value="${booking.seatNames}"/></strong>
                                            </div>
                                        </div>

                                        <div class="ticket-code-block">
                                            <span>Mã đặt vé</span>
                                            <strong><c:out value="${booking.bookingCode}"/></strong>
                                        </div>
                                    </div>

                                    <div class="ticket-qr-panel">
                                        <div class="perforation"></div>
                                        <c:choose>
                                            <c:when test="${not empty booking.qrDataUri}">
                                                <div class="qr-frame ${booking.valid ? '' : 'qr-disabled'}">
                                                    <img src="${booking.qrDataUri}" alt="Mã QR nhận vé">
                                                    <c:if test="${not booking.valid}">
                                                        <span>HẾT HIỆU LỰC</span>
                                                    </c:if>
                                                </div>
                                            </c:when>
                                            <c:otherwise>
                                                <div class="qr-fallback">
                                                    <i class="fa-solid fa-ticket"></i>
                                                    <span>Xuất trình mã đặt vé tại quầy</span>
                                                </div>
                                            </c:otherwise>
                                        </c:choose>
                                        <small>${booking.ticketCount} vé · ${booking.seatNames}</small>
                                    </div>
                                </article>

                                <aside class="invoice-card">
                                    <div class="invoice-card-header">
                                        <div>
                                            <span>Hóa đơn thanh toán</span>
                                            <strong>#${booking.invoiceId}</strong>
                                        </div>
                                        <c:choose>
                                            <c:when test="${booking.paymentStatus == 'Paid'}">
                                                <span class="payment-badge paid">Đã thanh toán</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="payment-badge refunded">Đã hoàn tiền</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>

                                    <dl class="invoice-meta">
                                        <div>
                                            <dt>Ngày giao dịch</dt>
                                            <dd><fmt:formatDate value="${booking.createdAt}" pattern="dd/MM/yyyy HH:mm"/></dd>
                                        </div>
                                        <div>
                                            <dt>Phương thức</dt>
                                            <dd><c:out value="${booking.paymentMethod}"/></dd>
                                        </div>
                                        <c:if test="${not empty booking.transactionRef}">
                                            <div>
                                                <dt>Mã giao dịch</dt>
                                                <dd><c:out value="${booking.transactionRef}"/></dd>
                                            </div>
                                        </c:if>
                                        <c:if test="${not empty booking.bankCode}">
                                            <div>
                                                <dt>Ngân hàng</dt>
                                                <dd><c:out value="${booking.bankCode}"/></dd>
                                            </div>
                                        </c:if>
                                    </dl>

                                    <div class="invoice-prices">
                                        <div>
                                            <span>Tiền vé (${booking.ticketCount})</span>
                                            <strong><fmt:formatNumber value="${booking.ticketTotal}" pattern="#,##0"/> đ</strong>
                                        </div>
                                        <c:if test="${booking.foodTotal > 0}">
                                            <div>
                                                <span>Bắp nước</span>
                                                <strong><fmt:formatNumber value="${booking.foodTotal}" pattern="#,##0"/> đ</strong>
                                            </div>
                                        </c:if>
                                        <c:if test="${booking.discountAmount > 0}">
                                            <div class="discount-line">
                                                <span>Giảm giá</span>
                                                <strong>-<fmt:formatNumber value="${booking.discountAmount}" pattern="#,##0"/> đ</strong>
                                            </div>
                                        </c:if>
                                        <div class="grand-total">
                                            <span>Tổng thanh toán</span>
                                            <strong><fmt:formatNumber value="${booking.totalAmount}" pattern="#,##0"/> đ</strong>
                                        </div>
                                    </div>
                                </aside>
                            </div>

                            <div class="history-detail-grid">
                                <section class="detail-card">
                                    <div class="detail-card-title">
                                        <i class="fa-solid fa-couch"></i>
                                        <h3>Danh sách vé và ghế</h3>
                                    </div>
                                    <div class="ticket-table-wrap">
                                        <table class="ticket-table">
                                            <thead>
                                                <tr>
                                                    <th>Ghế</th>
                                                    <th>Loại ghế</th>
                                                    <th>Mã vé</th>
                                                    <th>Giá vé</th>
                                                    <th>Trạng thái</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <c:forEach var="ticket" items="${booking.tickets}">
                                                    <tr>
                                                        <td><strong><c:out value="${ticket.seatName}"/></strong></td>
                                                        <td><c:out value="${ticket.seatType}"/></td>
                                                        <td class="mono-code"><c:out value="${ticket.code}"/></td>
                                                        <td><fmt:formatNumber value="${ticket.price}" pattern="#,##0"/> đ</td>
                                                        <td>
                                                            <c:choose>
                                                                <c:when test="${ticket.checkedIn}">
                                                                    <span class="mini-status used">Đã sử dụng</span>
                                                                </c:when>
                                                                <c:when test="${booking.valid}">
                                                                    <span class="mini-status ready">Sẵn sàng</span>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <span class="mini-status expired">Hết hạn</span>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </td>
                                                    </tr>
                                                </c:forEach>
                                            </tbody>
                                        </table>
                                    </div>
                                </section>

                                <section class="detail-card">
                                    <div class="detail-card-title">
                                        <i class="fa-solid fa-burger"></i>
                                        <h3>Đồ ăn đã mua</h3>
                                    </div>
                                    <c:choose>
                                        <c:when test="${empty booking.foods}">
                                            <div class="no-food">Đơn hàng này không có bắp nước hoặc combo.</div>
                                        </c:when>
                                        <c:otherwise>
                                            <div class="food-history-list">
                                                <c:forEach var="food" items="${booking.foods}">
                                                    <div class="food-history-item">
                                                        <div>
                                                            <strong><c:out value="${food.foodName}"/></strong>
                                                            <span>Số lượng: ${food.quantity}</span>
                                                        </div>
                                                        <strong><fmt:formatNumber value="${food.lineTotal}" pattern="#,##0"/> đ</strong>
                                                    </div>
                                                </c:forEach>
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                </section>
                            </div>

                            <div class="ticket-note ${booking.valid ? 'note-valid' : 'note-expired'}">
                                <i class="fa-solid ${booking.valid ? 'fa-circle-info' : 'fa-triangle-exclamation'}"></i>
                                <c:choose>
                                    <c:when test="${booking.valid}">
                                        <span>Vui lòng xuất trình mã QR hoặc mã đặt vé tại quầy trước giờ chiếu.</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span>Vé này đã hết hiệu lực hoặc đã được sử dụng. Mã QR không còn dùng để nhận vé.</span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </section>
                </c:when>

                <c:otherwise>
                    <section class="history-hero">
                        <div class="my-tickets-inner history-hero-inner">
                            <div>
                                <div class="section-eyebrow">Transaction History</div>
                                <h1>Vé của tôi</h1>
                                <p>Xem lại phim, vé và hóa đơn thanh toán của bạn tại CGV Cinema.</p>
                            </div>
                            <div class="history-icon"><i class="fa-solid fa-ticket"></i></div>
                        </div>
                    </section>

                    <section class="history-section">
                        <div class="my-tickets-inner">
                            <div class="history-toolbar">
                                <form class="history-search" method="get" action="${pageContext.request.contextPath}/my-tickets">
                                    <input type="hidden" name="status" value="${status}">
                                    <i class="fa-solid fa-magnifying-glass"></i>
                                    <input type="search" name="keyword" maxlength="100"
                                           value="${fn:escapeXml(keyword)}"
                                           placeholder="Tìm lịch sử theo tên phim...">
                                    <button type="submit" class="btn btn-primary">Tìm kiếm</button>
                                    <c:if test="${not empty keyword}">
                                        <a class="clear-search" href="${pageContext.request.contextPath}/my-tickets?status=${status}">Xóa</a>
                                    </c:if>
                                </form>

                                <div class="history-total">
                                    <strong>${totalItems}</strong>
                                    <span>giao dịch</span>
                                </div>
                            </div>

                            <div class="ticket-tabs">
                                <c:url var="allUrl" value="/my-tickets">
                                    <c:param name="status" value="all"/>
                                    <c:if test="${not empty keyword}"><c:param name="keyword" value="${keyword}"/></c:if>
                                </c:url>
                                <c:url var="validUrl" value="/my-tickets">
                                    <c:param name="status" value="valid"/>
                                    <c:if test="${not empty keyword}"><c:param name="keyword" value="${keyword}"/></c:if>
                                </c:url>
                                <c:url var="expiredUrl" value="/my-tickets">
                                    <c:param name="status" value="expired"/>
                                    <c:if test="${not empty keyword}"><c:param name="keyword" value="${keyword}"/></c:if>
                                </c:url>

                                <a href="${allUrl}" class="${status == 'all' ? 'active' : ''}">
                                    Tất cả <span>${allCount}</span>
                                </a>
                                <a href="${validUrl}" class="${status == 'valid' ? 'active' : ''}">
                                    Còn hiệu lực <span>${validCount}</span>
                                </a>
                                <a href="${expiredUrl}" class="${status == 'expired' ? 'active' : ''}">
                                    Hết hiệu lực <span>${expiredCount}</span>
                                </a>
                            </div>

                            <c:choose>
                                <c:when test="${empty bookings}">
                                    <div class="empty-history">
                                        <div class="empty-history-icon"><i class="fa-regular fa-calendar-xmark"></i></div>
                                        <h2>Chưa tìm thấy lịch sử vé</h2>
                                        <c:choose>
                                            <c:when test="${not empty keyword}">
                                                <p>Không có phim nào khớp với “<c:out value="${keyword}"/>”.</p>
                                                <a href="${pageContext.request.contextPath}/my-tickets" class="btn btn-primary">Xem toàn bộ lịch sử</a>
                                            </c:when>
                                            <c:otherwise>
                                                <p>Các vé đã thanh toán sẽ xuất hiện tại đây.</p>
                                                <a href="${pageContext.request.contextPath}/HomeController" class="btn btn-primary">Khám phá phim đang chiếu</a>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div class="booking-history-list">
                                        <c:forEach var="item" items="${bookings}">
                                            <article class="booking-history-card ${item.valid ? 'booking-valid' : 'booking-expired'}">
                                                <div class="history-poster">
                                                    <c:choose>
                                                        <c:when test="${not empty item.poster}">
                                                            <img src="${fn:escapeXml(item.poster)}" alt="Poster ${fn:escapeXml(item.movieName)}">
                                                        </c:when>
                                                        <c:otherwise>
                                                            <img src="${pageContext.request.contextPath}/Image/default_poster.svg" alt="Poster mặc định">
                                                        </c:otherwise>
                                                    </c:choose>
                                                </div>

                                                <div class="history-card-content">
                                                    <div class="history-card-topline">
                                                        <div class="invoice-number">
                                                            Mã hóa đơn <strong>#${item.invoiceId}</strong>
                                                            <span>· <fmt:formatDate value="${item.createdAt}" pattern="dd/MM/yyyy HH:mm"/></span>
                                                        </div>
                                                        <c:choose>
                                                            <c:when test="${item.valid}">
                                                                <span class="ticket-status status-valid">Còn hiệu lực</span>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="ticket-status status-expired">Hết hiệu lực</span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </div>

                                                    <h2><c:out value="${item.movieName}"/></h2>

                                                    <div class="history-info-grid">
                                                        <div>
                                                            <i class="fa-regular fa-calendar"></i>
                                                            <span><fmt:formatDate value="${item.startTime}" pattern="dd/MM/yyyy"/></span>
                                                        </div>
                                                        <div>
                                                            <i class="fa-regular fa-clock"></i>
                                                            <span><fmt:formatDate value="${item.startTime}" pattern="HH:mm"/> – <fmt:formatDate value="${item.endTime}" pattern="HH:mm"/></span>
                                                        </div>
                                                        <div>
                                                            <i class="fa-solid fa-door-open"></i>
                                                            <span><c:out value="${item.roomNumber}"/> · <c:out value="${item.roomType}"/></span>
                                                        </div>
                                                        <div>
                                                            <i class="fa-solid fa-couch"></i>
                                                            <span>Ghế: <strong><c:out value="${item.seatNames}"/></strong></span>
                                                        </div>
                                                    </div>

                                                    <div class="history-card-bottom">
                                                        <div class="history-payment">
                                                            <span>${item.ticketCount} vé · <c:out value="${item.paymentMethod}"/></span>
                                                            <strong><fmt:formatNumber value="${item.totalAmount}" pattern="#,##0"/> đ</strong>
                                                        </div>
                                                        <c:url var="detailUrl" value="/my-tickets">
                                                            <c:param name="action" value="detail"/>
                                                            <c:param name="invoiceId" value="${item.invoiceId}"/>
                                                        </c:url>
                                                        <a class="btn btn-primary" href="${detailUrl}">
                                                            Xem chi tiết <i class="fa-solid fa-arrow-right"></i>
                                                        </a>
                                                    </div>
                                                </div>
                                            </article>
                                        </c:forEach>
                                    </div>

                                    <c:if test="${totalPages > 1}">
                                        <nav class="history-pagination" aria-label="Phân trang lịch sử vé">
                                            <c:forEach begin="1" end="${totalPages}" var="pageNumber">
                                                <c:url var="pageUrl" value="/my-tickets">
                                                    <c:param name="status" value="${status}"/>
                                                    <c:param name="page" value="${pageNumber}"/>
                                                    <c:if test="${not empty keyword}"><c:param name="keyword" value="${keyword}"/></c:if>
                                                </c:url>
                                                <a href="${pageUrl}" class="${pageNumber == currentPage ? 'active' : ''}">${pageNumber}</a>
                                            </c:forEach>
                                        </nav>
                                    </c:if>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </section>
                </c:otherwise>
            </c:choose>
        </main>

        <footer class="site-footer">
            <div class="footer-inner">
                <a href="${pageContext.request.contextPath}/HomeController" class="footer-brand">
                    <img src="${pageContext.request.contextPath}/Image/Icon/cgvlogo.png" alt="CGV">
                    <span class="footer-brand-text">CGV CINEMA</span>
                </a>
                <p class="footer-copy">&copy; 2026 CGV Cinema. Hệ thống quản lý rạp chiếu phim.</p>
                <div class="footer-links">
                    <a href="#">Điều khoản</a>
                    <a href="#">Hỗ trợ</a>
                </div>
            </div>
        </footer>
    </body>
</html>
