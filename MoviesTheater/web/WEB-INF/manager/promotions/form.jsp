<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% request.setAttribute("activeNav", "promotions"); %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${pageTitle} — CGV Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
</head>
<body class="cgv-body">

<%@ include file="../_sidebar.jsp" %>

<div class="cgv-main">

    <header class="cgv-header">
        <h1 class="cgv-header-title">${pageTitle}</h1>
        <div class="cgv-header-right">
            <div class="cgv-header-actions">
                <a href="${pageContext.request.contextPath}/manager/promotions"
                   class="btn--cgv-outline" style="margin-right:8px;">
                    ← Back to Promotions
                </a>
                <div class="cgv-header-divider"></div>
                <div class="cgv-user-wrap">
                    <div class="cgv-avatar">MG</div>
                    <span class="cgv-user-name">
                        <c:choose>
                            <c:when test="${not empty sessionScope.LOGIN_USER}">${sessionScope.LOGIN_USER.fullName}</c:when>
                            <c:otherwise>Manager</c:otherwise>
                        </c:choose>
                    </span>
                </div>
            </div>
        </div>
    </header>

    <div class="cgv-page">
        <div class="cgv-list-wrap" style="max-width:680px;">

            <c:if test="${not empty errorMsg}">
                <div class="cgv-alert cgv-alert-danger">${errorMsg}</div>
            </c:if>

            <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:32px;">
                <div style="font-family:var(--font-cgv-ui);font-size:10px;font-weight:700;letter-spacing:2px;text-transform:uppercase;color:rgba(94,63,58,0.5);margin-bottom:24px;">
                    PROMOTION DETAILS
                </div>

                <form method="post" action="${pageContext.request.contextPath}/manager/promotions">
                    <input type="hidden" name="action" value="${formAction}">
                    <c:if test="${formAction eq 'update'}">
                        <input type="hidden" name="promotionId" value="${promotionId}">
                        <input type="hidden" name="usedCount"   value="${promotion.usedCount}">
                    </c:if>

                    <%-- Promotion Code --%>
                    <div class="cgv-field">
                        <label class="cgv-label">Mã khuyến mãi</label>
                        <c:choose>
                            <c:when test="${formAction eq 'create'}">
                                <div style="padding:10px 14px;background:#f7f3f2;border:1px solid var(--cgv-border);border-radius:8px;font-family:monospace;font-size:13px;color:rgba(94,63,58,0.55);letter-spacing:1px;">
                                    Tự động tạo khi lưu (VD: KM202606001)
                                </div>
                                <div style="font-size:12px;color:rgba(94,63,58,0.5);margin-top:4px;">
                                    Mã được tạo tự động theo định dạng KM + tháng/năm + số thứ tự.
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div style="padding:10px 14px;background:#f7f3f2;border:1px solid var(--cgv-border);border-radius:8px;font-family:monospace;font-size:14px;font-weight:700;letter-spacing:2px;">
                                    ${promotion.promotionCode}
                                </div>
                                <div style="font-size:12px;color:rgba(94,63,58,0.5);margin-top:4px;">Mã khuyến mãi không thể thay đổi sau khi tạo.</div>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <%-- Description --%>
                    <div class="cgv-field">
                        <label class="cgv-label">Mô tả</label>
                        <textarea class="cgv-input" name="description" rows="2"
                                  placeholder="Mô tả ngắn về chương trình khuyến mãi..."
                                  style="height:auto;resize:vertical;">${promotion.description}</textarea>
                    </div>

                    <%-- Discount Type --%>
                    <div class="cgv-field">
                        <label class="cgv-label">
                            Loại giảm giá <span style="color:var(--cgv-red)">*</span>
                        </label>
                        <select class="cgv-select" name="discountType"
                                onchange="toggleMaxDiscount(this.value)"
                                ${formAction eq 'update' and promotion.usedCount > 0 ? 'disabled' : ''}>
                            <option value="">-- Chọn loại --</option>
                            <option value="Percentage" ${promotion.discountType eq 'Percentage' ? 'selected' : ''}>Phần trăm (%)</option>
                            <option value="FlatAmount"  ${promotion.discountType eq 'FlatAmount'  ? 'selected' : ''}>Số tiền cố định (VND)</option>
                        </select>
                        <c:if test="${formAction eq 'update' and promotion.usedCount > 0}">
                            <input type="hidden" name="discountType" value="${promotion.discountType}">
                        </c:if>
                        <c:if test="${not empty errors['discountType']}">
                            <div style="font-size:12px;color:var(--cgv-red);margin-top:4px;">${errors['discountType']}</div>
                        </c:if>
                    </div>

                    <%-- Discount Value --%>
                    <div class="cgv-field">
                        <label class="cgv-label">
                            Giá trị giảm <span style="color:var(--cgv-red)">*</span>
                        </label>
                        <input class="cgv-input" type="number" name="discountValue"
                               step="0.01" min="0.01"
                               value="${promotion.discountValue}"
                               placeholder="Nhập giá trị..."
                               ${formAction eq 'update' and promotion.usedCount > 0 ? 'readonly' : ''}>
                        <c:if test="${not empty errors['discountValue']}">
                            <div style="font-size:12px;color:var(--cgv-red);margin-top:4px;">${errors['discountValue']}</div>
                        </c:if>
                    </div>

                    <%-- Max Discount (Percentage only) --%>
                    <div class="cgv-field" id="maxDiscountRow"
                         style="${promotion.discountType ne 'Percentage' ? 'display:none' : ''}">
                        <label class="cgv-label">Giảm tối đa (VND)</label>
                        <input class="cgv-input" type="number" name="maxDiscountAmount"
                               step="0.01" min="0"
                               value="${promotion.maxDiscountAmount}"
                               placeholder="Để trống nếu không giới hạn">
                        <div style="font-size:12px;color:rgba(94,63,58,0.55);margin-top:4px;">Chỉ áp dụng cho giảm giá theo phần trăm.</div>
                    </div>

                    <%-- Min Order Amount --%>
                    <div class="cgv-field">
                        <label class="cgv-label">Giá trị đơn hàng tối thiểu (VND)</label>
                        <input class="cgv-input" type="number" name="minOrderAmount"
                               step="0.01" min="0"
                               value="${promotion.minOrderAmount}"
                               placeholder="0">
                        <c:if test="${not empty errors['minOrderAmount']}">
                            <div style="font-size:12px;color:var(--cgv-red);margin-top:4px;">${errors['minOrderAmount']}</div>
                        </c:if>
                    </div>

                    <%-- Start / End Date row --%>
                    <div style="display:grid;grid-template-columns:1fr 1fr;gap:16px;">
                        <div class="cgv-field">
                            <label class="cgv-label">
                                Ngày bắt đầu <span style="color:var(--cgv-red)">*</span>
                            </label>
                            <input class="cgv-input" type="datetime-local" name="startDate" value="${startDateStr}">
                            <c:if test="${not empty errors['startDate']}">
                                <div style="font-size:12px;color:var(--cgv-red);margin-top:4px;">${errors['startDate']}</div>
                            </c:if>
                            <c:if test="${formAction eq 'create'}">
                                <div style="font-size:12px;color:rgba(94,63,58,0.5);margin-top:4px;">Phải là thời điểm trong tương lai.</div>
                            </c:if>
                        </div>
                        <div class="cgv-field">
                            <label class="cgv-label">
                                Ngày kết thúc <span style="color:var(--cgv-red)">*</span>
                            </label>
                            <input class="cgv-input" type="datetime-local" name="endDate" value="${endDateStr}">
                            <c:if test="${not empty errors['endDate']}">
                                <div style="font-size:12px;color:var(--cgv-red);margin-top:4px;">${errors['endDate']}</div>
                            </c:if>
                        </div>
                    </div>

                    <%-- Usage Limit --%>
                    <div class="cgv-field">
                        <label class="cgv-label">Giới hạn sử dụng</label>
                        <input class="cgv-input" type="number" name="usageLimit"
                               min="1"
                               value="${promotion.usageLimit}"
                               placeholder="Để trống = không giới hạn">
                        <c:if test="${not empty errors['usageLimit']}">
                            <div style="font-size:12px;color:var(--cgv-red);margin-top:4px;">${errors['usageLimit']}</div>
                        </c:if>
                    </div>

                    <%-- Active toggle (edit only — new promotions are always upcoming) --%>
                    <c:if test="${formAction eq 'update'}">
                        <div class="cgv-field" style="flex-direction:row;align-items:center;gap:12px;">
                            <input type="checkbox" name="isActive" id="isActive"
                                   ${promotion.active ? 'checked' : ''}
                                   style="width:18px;height:18px;accent-color:var(--cgv-red);cursor:pointer;">
                            <label class="cgv-label" for="isActive" style="margin-bottom:0;cursor:pointer;">
                                Kích hoạt
                            </label>
                        </div>
                    </c:if>

                    <div style="display:flex;gap:12px;margin-top:24px;padding-top:20px;border-top:1px solid var(--cgv-border);">
                        <button type="submit" class="btn--cgv">Lưu khuyến mãi</button>
                        <a href="${pageContext.request.contextPath}/manager/promotions"
                           class="btn--cgv-outline">Hủy</a>
                    </div>

                </form>
            </div>

        </div>
    </div>
</div>

<script>
    function toggleMaxDiscount(type) {
        var row = document.getElementById('maxDiscountRow');
        if (row) row.style.display = (type === 'Percentage') ? '' : 'none';
    }
</script>
</body>
</html>
