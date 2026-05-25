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
<body>
<%@ include file="../_navbar.jsp" %>
<div class="main">
        <div class="topbar">
            <div>
                <div class="topbar-title">Quản lý Khuyến mãi</div>
                <div class="topbar-subtitle">
                    <a href="${pageContext.request.contextPath}/manager">Dashboard</a>
                    &rsaquo; Khuyến mãi
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

            <div class="table-wrap">

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
                            <th>Mã KM</th>
                            <th>Loại</th>
                            <th>Giá trị</th>
                            <th>Ngày hết hạn</th>
                            <th>Đã dùng / Tổng</th>
                            <th>Trạng thái</th>
                            <th></th>
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
                                                    <button type="submit" class="btn btn--danger sm">Xóa</button>
                                                </form>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>

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
