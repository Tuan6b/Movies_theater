<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% request.setAttribute("activeNav", "promotions"); %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${pageTitle}</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
</head>
<body>
<div class="layout">

    <%@ include file="../_sidebar.jsp" %>

    <div class="main">
        <div class="topbar">
            <div>
                <div class="topbar-title">${pageTitle}</div>
                <div class="topbar-subtitle">
                    <a href="${pageContext.request.contextPath}/manager">Dashboard</a>
                    &rsaquo; <a href="${pageContext.request.contextPath}/manager/promotions">Khuyến mãi</a>
                    &rsaquo; ${pageTitle}
                </div>
            </div>
            <div class="topbar-action">
                <a href="${pageContext.request.contextPath}/manager/promotions"
                   class="btn btn--secondary">&#8592; Quay lại</a>
                <div class="topbar-avatar">MG</div>
            </div>
        </div>

        <div class="page-content fade-in">
            <div class="form-page">

                <c:if test="${not empty errorMsg}">
                    <div class="alert alert-danger">${errorMsg}</div>
                </c:if>

                <div class="card">
                    <div class="card-head">
                        <h3>${pageTitle}</h3>
                    </div>
                    <div class="card-pad">

                        <form method="post" action="${pageContext.request.contextPath}/manager/promotions">

                            <input type="hidden" name="action" value="${formAction}">
                            <c:if test="${formAction eq 'update'}">
                                <input type="hidden" name="promotionId" value="${promotionId}">
                                <input type="hidden" name="usedCount" value="${promotion.usedCount}">
                            </c:if>

                            <div class="form-group">
                                <label for="promotionCode">Mã khuyến mãi <span style="color:var(--danger)">*</span></label>
                                <input type="text" id="promotionCode" name="promotionCode"
                                       value="${promotion.promotionCode}"
                                       placeholder="VD: SUMMER2025"
                                       style="text-transform:uppercase; font-family:var(--font-mono)"
                                       ${formAction eq 'update' and promotion.usedCount > 0 ? 'readonly' : ''}>
                                <c:if test="${not empty errors['promotionCode']}">
                                    <div class="form-error">${errors['promotionCode']}</div>
                                </c:if>
                                <c:if test="${formAction eq 'update' and promotion.usedCount > 0}">
                                    <div class="form-hint">Mã đã được sử dụng, không thể thay đổi.</div>
                                </c:if>
                            </div>

                            <div class="form-group">
                                <label for="description">Mô tả</label>
                                <textarea id="description" name="description" rows="2"
                                          placeholder="Mô tả ngắn về chương trình khuyến mãi...">${promotion.description}</textarea>
                            </div>

                            <div class="form-group">
                                <label for="discountType">Loại giảm giá <span style="color:var(--danger)">*</span></label>
                                <select id="discountType" name="discountType"
                                        ${formAction eq 'update' and promotion.usedCount > 0 ? 'disabled' : ''}
                                        onchange="toggleMaxDiscount(this.value)">
                                    <option value="">-- Chọn loại --</option>
                                    <option value="Percentage" ${promotion.discountType eq 'Percentage' ? 'selected' : ''}>Phần trăm (%)</option>
                                    <option value="FlatAmount"  ${promotion.discountType eq 'FlatAmount'  ? 'selected' : ''}>Số tiền cố định (VND)</option>
                                </select>
                                <c:if test="${formAction eq 'update' and promotion.usedCount > 0}">
                                    <input type="hidden" name="discountType" value="${promotion.discountType}">
                                </c:if>
                                <c:if test="${not empty errors['discountType']}">
                                    <div class="form-error">${errors['discountType']}</div>
                                </c:if>
                            </div>

                            <div class="form-group">
                                <label for="discountValue">Giá trị giảm <span style="color:var(--danger)">*</span></label>
                                <input type="number" id="discountValue" name="discountValue"
                                       step="0.01" min="0.01"
                                       value="${promotion.discountValue}"
                                       placeholder="Nhập giá trị..."
                                       ${formAction eq 'update' and promotion.usedCount > 0 ? 'readonly' : ''}>
                                <c:if test="${not empty errors['discountValue']}">
                                    <div class="form-error">${errors['discountValue']}</div>
                                </c:if>
                            </div>

                            <div class="form-group" id="maxDiscountRow"
                                 style="${promotion.discountType ne 'Percentage' ? 'display:none' : ''}">
                                <label for="maxDiscountAmount">Giảm tối đa (VND)</label>
                                <input type="number" id="maxDiscountAmount" name="maxDiscountAmount"
                                       step="0.01" min="0"
                                       value="${promotion.maxDiscountAmount}"
                                       placeholder="Để trống nếu không giới hạn">
                                <div class="form-hint">Chỉ áp dụng cho giảm giá theo phần trăm.</div>
                            </div>

                            <div class="form-group">
                                <label for="minOrderAmount">Giá trị đơn hàng tối thiểu (VND)</label>
                                <input type="number" id="minOrderAmount" name="minOrderAmount"
                                       step="0.01" min="0"
                                       value="${promotion.minOrderAmount}"
                                       placeholder="0">
                                <c:if test="${not empty errors['minOrderAmount']}">
                                    <div class="form-error">${errors['minOrderAmount']}</div>
                                </c:if>
                            </div>

                            <div class="form-row">
                                <div class="form-group">
                                    <label for="startDate">Ngày bắt đầu <span style="color:var(--danger)">*</span></label>
                                    <input type="datetime-local" id="startDate" name="startDate" value="${startDateStr}">
                                    <c:if test="${not empty errors['startDate']}">
                                        <div class="form-error">${errors['startDate']}</div>
                                    </c:if>
                                </div>
                                <div class="form-group">
                                    <label for="endDate">Ngày kết thúc <span style="color:var(--danger)">*</span></label>
                                    <input type="datetime-local" id="endDate" name="endDate" value="${endDateStr}">
                                    <c:if test="${not empty errors['endDate']}">
                                        <div class="form-error">${errors['endDate']}</div>
                                    </c:if>
                                </div>
                            </div>

                            <div class="form-group">
                                <label for="usageLimit">Giới hạn sử dụng</label>
                                <input type="number" id="usageLimit" name="usageLimit"
                                       min="1"
                                       value="${promotion.usageLimit}"
                                       placeholder="Để trống = không giới hạn">
                                <c:if test="${not empty errors['usageLimit']}">
                                    <div class="form-error">${errors['usageLimit']}</div>
                                </c:if>
                            </div>

                            <div class="form-group">
                                <label>
                                    <input type="checkbox" name="isActive" id="isActive"
                                           ${(promotion.active or empty promotion) ? 'checked' : ''}>
                                    Kích hoạt ngay
                                </label>
                            </div>

                            <div style="display:flex; gap:10px; margin-top:4px; padding-top:18px; border-top:1px solid var(--border-subtle)">
                                <button type="submit" class="btn btn--primary">Lưu khuyến mãi</button>
                                <a href="${pageContext.request.contextPath}/manager/promotions"
                                   class="btn btn--secondary">Hủy</a>
                            </div>

                        </form>

                    </div><!-- /card-pad -->
                </div><!-- /card -->

            </div><!-- /form-page -->
        </div><!-- /page-content -->
    </div><!-- /main -->

</div><!-- /layout -->

<script>
    function toggleMaxDiscount(type) {
        var row = document.getElementById('maxDiscountRow');
        if (row) row.style.display = (type === 'Percentage') ? '' : 'none';
    }
</script>
</body>
</html>
