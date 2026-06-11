<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% request.setAttribute("activeNav", "promotions"); %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Quản lý Khuyến mãi</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
</head>
<<<<<<< Updated upstream
<body>
<%@ include file="../_navbar.jsp" %>
<div class="main">
        <div class="topbar">
            <div>
                <div class="topbar-title">Quản lý Khuyến mãi</div>
                <div class="topbar-subtitle">
                    <a href="${pageContext.request.contextPath}/manager">Dashboard</a>
                    &rsaquo; Khuyến mãi
=======
<body class="cgv-body">

<%@ include file="../_sidebar.jsp" %>

<div class="cgv-main">

    <header class="cgv-header">
        <h1 class="cgv-header-title">Promotions</h1>
        <div class="cgv-header-right">
            <div class="cgv-header-actions">
                <div class="cgv-header-divider"></div>
                <div class="cgv-user-wrap">
                    <div class="cgv-avatar">MG</div>
                    <span class="cgv-user-name">
                        <c:choose>
                            <c:when test="${not empty sessionScope.LOGIN_USER}">${sessionScope.LOGIN_USER.fullName}</c:when>
                            <c:otherwise>Manager</c:otherwise>
                        </c:choose>
                    </span>
>>>>>>> Stashed changes
                </div>
            </div>
            <div class="topbar-action">
                <a href="${pageContext.request.contextPath}/manager/promotions?action=add"
                   class="btn btn--primary">+ Thêm mới</a>
            </div>
        </div>

        <div class="page-content fade-in">

            <c:if test="${not empty flashSuccess}">
                <div class="alert alert-success">${flashSuccess}</div>
            </c:if>
            <c:if test="${not empty flashError}">
                <div class="alert alert-danger">${flashError}</div>
            </c:if>
            <c:if test="${not empty errorMsg}">
                <div class="alert alert-danger">${errorMsg}</div>
            </c:if>

<<<<<<< Updated upstream
            <div class="table-wrap">
=======
            <div class="cgv-toolbar">
                <div class="cgv-pills">
                    <a href="${pageContext.request.contextPath}/manager/promotions"
                       class="cgv-pill active">All</a>
                    <a href="${pageContext.request.contextPath}/manager/promotions?view=upcoming"
                       class="cgv-pill">Upcoming</a>
                    <a href="${pageContext.request.contextPath}/manager/promotions?view=active"
                       class="cgv-pill">Active</a>
                    <a href="${pageContext.request.contextPath}/manager/promotions?view=expired"
                       class="cgv-pill">Expired</a>
                    <a href="${pageContext.request.contextPath}/manager/promotions?view=inactive"
                       class="cgv-pill">Inactive</a>
                </div>
                <a href="${pageContext.request.contextPath}/manager/promotions?action=add" class="btn--cgv">
                    <svg width="10" height="10" viewBox="0 0 12 12" fill="none"
                         stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
                        <line x1="6" y1="1" x2="6" y2="11"/><line x1="1" y1="6" x2="11" y2="6"/>
                    </svg>
                    Add Promotion
                </a>
            </div>
>>>>>>> Stashed changes

                <!-- Toolbar / Filter -->
                <form method="get" action="${pageContext.request.contextPath}/manager/promotions"
                      class="table-toolbar">
                    <input class="search-input" type="text" name="keyword"
                           value="${keyword}" placeholder="Tìm mã hoặc mô tả...">

                    <select class="input" name="type" style="width:160px">
                        <option value="">Tất cả loại</option>
                        <option value="Percentage" ${filterType eq 'Percentage' ? 'selected' : ''}>Phần trăm (%)</option>
                        <option value="FlatAmount"  ${filterType eq 'FlatAmount'  ? 'selected' : ''}>Số tiền cố định</option>
                    </select>

                    <select class="input" name="status" style="width:160px">
                        <option value="">Tất cả trạng thái</option>
                        <option value="active"   ${filterStatus eq 'active'   ? 'selected' : ''}>Đang hoạt động</option>
                        <option value="expired"  ${filterStatus eq 'expired'  ? 'selected' : ''}>Hết hạn</option>
                        <option value="inactive" ${filterStatus eq 'inactive' ? 'selected' : ''}>Vô hiệu</option>
                    </select>

                    <button type="submit" class="btn btn--secondary">Lọc</button>
                </form>

                <!-- Data table -->
                <table class="dt">
                    <thead>
                        <tr>
<<<<<<< Updated upstream
                            <th>Mã KM</th>
                            <th>Loại</th>
                            <th>Giá trị</th>
                            <th>Ngày hết hạn</th>
                            <th>Đã dùng / Tổng</th>
                            <th>Trạng thái</th>
                            <th></th>
=======
                            <th>#</th>
                            <th>Code</th>
                            <th>Description</th>
                            <th>Type</th>
                            <th>Value</th>
                            <th>Giá trị đơn tối thiểu (VND)</th>
                            <th>Start Date</th>
                            <th>End Date</th>
                            <th>Uses / Limit</th>
                            <th>Status</th>
                            <th>Actions</th>
>>>>>>> Stashed changes
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${empty promotions}">
                                <tr>
                                    <td colspan="7" style="text-align:center; color:var(--fg-4); padding:32px;">
                                        Không có dữ liệu
                                    </td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach items="${promotions}" var="p">
                                    <tr>
                                        <td>
                                            <strong style="font-family:var(--font-mono); color:var(--fg-1)">${p.promotionCode}</strong>
                                        </td>
<<<<<<< Updated upstream
                                        <td>
                                            <c:choose>
                                                <c:when test="${p.discountType eq 'Percentage'}">Phần trăm</c:when>
                                                <c:otherwise>Số tiền</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="font-family:var(--font-mono)">
                                            <c:choose>
                                                <c:when test="${p.discountType eq 'Percentage'}">${p.discountValue}%</c:when>
                                                <c:otherwise>${p.discountValue} VND</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="font-family:var(--font-mono); font-size:13px">${p.endDateDisplay}</td>
                                        <td style="font-family:var(--font-mono)">
                                            ${p.usedCount}&nbsp;/&nbsp;<c:choose>
                                                <c:when test="${p.usageLimit != null}">${p.usageLimit}</c:when>
                                                <c:otherwise>&#8734;</c:otherwise>
                                            </c:choose>
=======
                                        <td style="font-weight:500;">${p.description}</td>
                                        <td style="color:rgba(94,63,58,0.7);">
                                            <c:choose>
                                                <c:when test="${p.discountType eq 'Percentage' or p.discountType eq 'PERCENT'}">Percentage</c:when>
                                                <c:when test="${p.discountType eq 'FlatAmount' or p.discountType eq 'FIXED'}">Fixed Amount</c:when>
                                                <c:otherwise>${p.discountType}</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="font-weight:600;">
                                            <c:choose>
                                                <c:when test="${p.discountType eq 'Percentage' or p.discountType eq 'PERCENT'}">${p.discountValue}%</c:when>
                                                <c:otherwise>${p.discountValue} VND</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="font-size:13px;">
                                            <c:choose>
                                                <c:when test="${empty p.minOrderAmount or p.minOrderAmount == 0}">—</c:when>
                                                <c:otherwise>${p.minOrderAmount} VND</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="font-size:13px;">${p.startDateDisplay}</td>
                                        <td style="font-size:13px;">${p.endDateDisplay}</td>
                                        <td>${p.usedCount} / ${not empty p.usageLimit ? p.usageLimit : '∞'}</td>
                                        <td>
                                            <span class="cgv-badge ${p.status eq 'active' ? 'active' : p.status eq 'upcoming' ? 'upcoming' : p.status eq 'expired' ? 'danger' : 'inactive'}">
                                                ${p.status}
                                            </span>
>>>>>>> Stashed changes
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${p.status eq 'active'}">
                                                    <span class="badge badge--active dot">Hoạt động</span>
                                                </c:when>
                                                <c:when test="${p.status eq 'expired'}">
                                                    <span class="badge badge--warning dot">Hết hạn</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge badge--neutral dot">Vô hiệu</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <div style="display:flex; gap:4px">
                                                <a href="${pageContext.request.contextPath}/manager/promotions?action=edit&amp;id=${p.promotionId}"
                                                   class="btn btn--secondary sm">Sửa</a>
                                                <form method="post"
                                                      action="${pageContext.request.contextPath}/manager/promotions"
                                                      style="display:inline"
                                                      onsubmit="return confirm('Xác nhận vô hiệu hóa mã ${p.promotionCode}?')">
                                                    <input type="hidden" name="action" value="delete">
                                                    <input type="hidden" name="promotionId" value="${p.promotionId}">
<<<<<<< Updated upstream
                                                    <button type="submit" class="btn btn--danger sm">Xóa</button>
=======
                                                    <button type="submit" class="btn--cgv-outline"
                                                            style="color:var(--cgv-red);border-color:var(--cgv-red);"
                                                            onclick="return confirm('Delete promotion ${p.promotionCode}?')">deactivate</button>
>>>>>>> Stashed changes
                                                </form>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
<<<<<<< Updated upstream
=======
                            </c:when>
                            <c:otherwise>
                                <tr><td colspan="11" style="text-align:center;padding:48px;color:rgba(94,63,58,0.4);">No promotions found.</td></tr>
>>>>>>> Stashed changes
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>

<<<<<<< Updated upstream
                <!-- Pager -->
                <div class="pager">
                    <span>Trang ${currentPage} / ${totalPages} &nbsp;·&nbsp; ${totalItems} bản ghi</span>
                    <div class="pager-pages">
                        <c:if test="${currentPage > 1}">
                            <a href="${pageContext.request.contextPath}/manager/promotions?keyword=${keyword}&amp;type=${filterType}&amp;status=${filterStatus}&amp;page=${currentPage - 1}"
                               class="btn btn--ghost sm">‹</a>
                        </c:if>
                        <c:if test="${currentPage <= 1}">
                            <span class="btn btn--ghost sm" style="opacity:0.3; pointer-events:none">‹</span>
                        </c:if>
=======
                <div class="cgv-pager">
                    <span>
                        Showing ${not empty promotions ? promotions.size() : 0}
                        of ${not empty totalItems ? totalItems : 0} promotions
                    </span>
                    <div class="cgv-pager-pages">
                        <c:forEach begin="1" end="${not empty totalPages ? totalPages : 1}" var="pg">
                            <button class="cgv-pager-btn ${pg eq currentPage ? 'active' : ''}"
                                    onclick="location.href='?page=${pg}&keyword=${param.keyword}&type=${param.type}'">${pg}</button>
                        </c:forEach>
                    </div>
                </div>
            </div>
        </div>
>>>>>>> Stashed changes

                        <span style="padding:4px 8px; font-family:var(--font-mono); font-size:12px; color:var(--fg-3)">${currentPage}</span>

                        <c:if test="${currentPage < totalPages}">
                            <a href="${pageContext.request.contextPath}/manager/promotions?keyword=${keyword}&amp;type=${filterType}&amp;status=${filterStatus}&amp;page=${currentPage + 1}"
                               class="btn btn--ghost sm">›</a>
                        </c:if>
                        <c:if test="${currentPage >= totalPages}">
                            <span class="btn btn--ghost sm" style="opacity:0.3; pointer-events:none">›</span>
                        </c:if>
                    </div>
                </div>

            </div><!-- /table-wrap -->

        </div><!-- /page-content -->
    </div><!-- /main -->
</body>
</html>
