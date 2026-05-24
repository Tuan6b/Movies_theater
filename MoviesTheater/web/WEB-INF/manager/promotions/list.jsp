<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Quản lý Khuyến mãi</title>
    <link rel="stylesheet"
          href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
</head>
<body class="bg-light">

<div class="container-fluid py-4">

    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2 class="mb-0">Quản lý Khuyến mãi</h2>
        <a href="${pageContext.request.contextPath}/manager/promotions?action=add"
           class="btn btn-primary">+ Thêm mới</a>
    </div>

    <c:if test="${not empty flashSuccess}">
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            ${flashSuccess}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>
    <c:if test="${not empty flashError}">
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            ${flashError}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>
    <c:if test="${not empty errorMsg}">
        <div class="alert alert-danger">${errorMsg}</div>
    </c:if>

    <!-- Filter form -->
    <div class="card mb-4">
        <div class="card-body">
            <form method="get"
                  action="${pageContext.request.contextPath}/manager/promotions"
                  class="row g-3 align-items-end">
                <div class="col-md-4">
                    <label class="form-label">Tìm kiếm</label>
                    <input type="text" name="keyword" value="${keyword}"
                           class="form-control" placeholder="Mã hoặc mô tả...">
                </div>
                <div class="col-md-3">
                    <label class="form-label">Loại giảm giá</label>
                    <select name="type" class="form-select">
                        <option value="">-- Tất cả --</option>
                        <option value="Percentage"
                            <c:if test="${filterType eq 'Percentage'}">selected</c:if>>
                            Phần trăm (%)
                        </option>
                        <option value="FlatAmount"
                            <c:if test="${filterType eq 'FlatAmount'}">selected</c:if>>
                            Số tiền cố định
                        </option>
                    </select>
                </div>
                <div class="col-md-3">
                    <label class="form-label">Trạng thái</label>
                    <select name="status" class="form-select">
                        <option value="">-- Tất cả --</option>
                        <option value="active"
                            <c:if test="${filterStatus eq 'active'}">selected</c:if>>
                            Đang hoạt động
                        </option>
                        <option value="expired"
                            <c:if test="${filterStatus eq 'expired'}">selected</c:if>>
                            Hết hạn
                        </option>
                        <option value="inactive"
                            <c:if test="${filterStatus eq 'inactive'}">selected</c:if>>
                            Vô hiệu
                        </option>
                    </select>
                </div>
                <div class="col-md-2">
                    <button type="submit" class="btn btn-secondary w-100">Lọc</button>
                </div>
            </form>
        </div>
    </div>

    <!-- Promotions table -->
    <div class="card">
        <div class="card-body p-0">
            <table class="table table-bordered table-hover mb-0">
                <thead class="table-dark">
                    <tr>
                        <th>Mã KM</th>
                        <th>Loại</th>
                        <th>Giá trị</th>
                        <th>Ngày hết hạn</th>
                        <th>Đã dùng / Tổng</th>
                        <th>Trạng thái</th>
                        <th class="text-center">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${empty promotions}">
                            <tr>
                                <td colspan="7" class="text-center text-muted py-4">
                                    Không có dữ liệu
                                </td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach items="${promotions}" var="p">
                                <tr>
                                    <td class="fw-semibold">${p.promotionCode}</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${p.discountType eq 'Percentage'}">
                                                <span class="badge bg-info text-dark">Phần trăm</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge bg-warning text-dark">Số tiền</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${p.discountType eq 'Percentage'}">
                                                ${p.discountValue}%
                                            </c:when>
                                            <c:otherwise>
                                                ${p.discountValue} VND
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>${p.endDateDisplay}</td>
                                    <td>
                                        ${p.usedCount} /
                                        <c:choose>
                                            <c:when test="${p.usageLimit != null}">
                                                ${p.usageLimit}
                                            </c:when>
                                            <c:otherwise>
                                                &#8734;
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${p.isActive}">
                                                <span class="badge bg-success">Hoạt động</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge bg-secondary">Vô hiệu</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="text-center">
                                        <a href="${pageContext.request.contextPath}/manager/promotions?action=edit&amp;id=${p.promotionId}"
                                           class="btn btn-sm btn-outline-primary me-1">Sửa</a>
                                        <form method="post"
                                              action="${pageContext.request.contextPath}/manager/promotions"
                                              class="d-inline"
                                              onsubmit="return confirm('Xác nhận vô hiệu hóa mã ${p.promotionCode}?')">
                                            <input type="hidden" name="action" value="delete">
                                            <input type="hidden" name="promotionId"
                                                   value="${p.promotionId}">
                                            <button type="submit"
                                                    class="btn btn-sm btn-outline-danger">Xóa</button>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
    </div>

    <!-- Pagination -->
    <div class="d-flex justify-content-between align-items-center mt-3">
        <span class="text-muted small">
            Trang ${currentPage} / ${totalPages}
            &nbsp;|&nbsp; Tổng: ${totalItems} bản ghi
        </span>
        <nav>
            <ul class="pagination mb-0">
                <li class="page-item <c:if test="${currentPage <= 1}">disabled</c:if>">
                    <a class="page-link"
                       href="${pageContext.request.contextPath}/manager/promotions?keyword=${keyword}&amp;type=${filterType}&amp;status=${filterStatus}&amp;page=${currentPage - 1}">
                        &laquo; Trang trước
                    </a>
                </li>
                <li class="page-item <c:if test="${currentPage >= totalPages}">disabled</c:if>">
                    <a class="page-link"
                       href="${pageContext.request.contextPath}/manager/promotions?keyword=${keyword}&amp;type=${filterType}&amp;status=${filterStatus}&amp;page=${currentPage + 1}">
                        Trang sau &raquo;
                    </a>
                </li>
            </ul>
        </nav>
    </div>

</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
