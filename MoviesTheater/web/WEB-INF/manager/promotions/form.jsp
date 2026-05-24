<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${pageTitle}</title>
    <link rel="stylesheet"
          href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
</head>
<body class="bg-light">

<div class="container py-4" style="max-width: 760px;">

    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2 class="mb-0">${pageTitle}</h2>
        <a href="${pageContext.request.contextPath}/manager/promotions"
           class="btn btn-outline-secondary">&#8592; Quay lại danh sách</a>
    </div>

    <c:if test="${not empty errorMsg}">
        <div class="alert alert-danger">${errorMsg}</div>
    </c:if>

    <div class="card">
        <div class="card-body">
            <form method="post"
                  action="${pageContext.request.contextPath}/manager/promotions"
                  novalidate>

                <input type="hidden" name="action" value="${formAction}">
                <c:if test="${formAction eq 'update'}">
                    <input type="hidden" name="promotionId" value="${promotionId}">
                </c:if>

                <!-- Promotion Code -->
                <div class="mb-3">
                    <label class="form-label fw-semibold">
                        Mã khuyến mãi <span class="text-danger">*</span>
                    </label>
                    <input type="text" name="promotionCode"
                           value="${promotion.promotionCode}"
                           class="form-control <c:if test="${not empty errors['promotionCode']}">is-invalid</c:if>"
                           placeholder="VD: SUMMER2025"
                           style="text-transform: uppercase;"
                           <c:if test="${formAction eq 'update' and promotion.usedCount > 0}">readonly</c:if>>
                    <c:if test="${not empty errors['promotionCode']}">
                        <div class="invalid-feedback">${errors['promotionCode']}</div>
                    </c:if>
                    <c:if test="${formAction eq 'update' and promotion.usedCount > 0}">
                        <div class="form-text text-warning">
                            Mã đã được sử dụng, không thể thay đổi.
                        </div>
                    </c:if>
                </div>

                <!-- Description -->
                <div class="mb-3">
                    <label class="form-label fw-semibold">Mô tả</label>
                    <textarea name="description" rows="2"
                              class="form-control"
                              placeholder="Mô tả ngắn về chương trình khuyến mãi...">${promotion.description}</textarea>
                </div>

                <!-- Discount Type -->
                <div class="mb-3">
                    <label class="form-label fw-semibold">
                        Loại giảm giá <span class="text-danger">*</span>
                    </label>
                    <select name="discountType" id="discountType"
                            class="form-select <c:if test="${not empty errors['discountType']}">is-invalid</c:if>"
                            <c:if test="${formAction eq 'update' and promotion.usedCount > 0}">disabled</c:if>
                            onchange="toggleMaxDiscount(this.value)">
                        <option value="">-- Chọn loại --</option>
                        <option value="Percentage"
                            <c:if test="${promotion.discountType eq 'Percentage'}">selected</c:if>>
                            Phần trăm (%)
                        </option>
                        <option value="FlatAmount"
                            <c:if test="${promotion.discountType eq 'FlatAmount'}">selected</c:if>>
                            Số tiền cố định (VND)
                        </option>
                    </select>
                    <c:if test="${formAction eq 'update' and promotion.usedCount > 0}">
                        <input type="hidden" name="discountType"
                               value="${promotion.discountType}">
                    </c:if>
                    <c:if test="${not empty errors['discountType']}">
                        <div class="invalid-feedback">${errors['discountType']}</div>
                    </c:if>
                </div>

                <!-- Discount Value -->
                <div class="mb-3">
                    <label class="form-label fw-semibold">
                        Giá trị giảm <span class="text-danger">*</span>
                    </label>
                    <input type="number" name="discountValue" step="0.01" min="0.01"
                           value="${promotion.discountValue}"
                           class="form-control <c:if test="${not empty errors['discountValue']}">is-invalid</c:if>"
                           placeholder="Nhập giá trị..."
                           <c:if test="${formAction eq 'update' and promotion.usedCount > 0}">readonly</c:if>>
                    <c:if test="${not empty errors['discountValue']}">
                        <div class="invalid-feedback">${errors['discountValue']}</div>
                    </c:if>
                </div>

                <!-- Max Discount Amount (Percentage only) -->
                <div class="mb-3" id="maxDiscountRow"
                     style="${promotion.discountType ne 'Percentage' ? 'display:none' : ''}">
                    <label class="form-label fw-semibold">Giảm tối đa (VND)</label>
                    <input type="number" name="maxDiscountAmount" step="0.01" min="0"
                           value="${promotion.maxDiscountAmount}"
                           class="form-control"
                           placeholder="Để trống nếu không giới hạn">
                    <div class="form-text">Áp dụng cho giảm giá theo phần trăm.</div>
                </div>

                <!-- Min Order Amount -->
                <div class="mb-3">
                    <label class="form-label fw-semibold">Giá trị đơn hàng tối thiểu (VND)</label>
                    <input type="number" name="minOrderAmount" step="0.01" min="0"
                           value="${promotion.minOrderAmount}"
                           class="form-control <c:if test="${not empty errors['minOrderAmount']}">is-invalid</c:if>"
                           placeholder="0">
                    <c:if test="${not empty errors['minOrderAmount']}">
                        <div class="invalid-feedback">${errors['minOrderAmount']}</div>
                    </c:if>
                </div>

                <!-- Start Date / End Date -->
                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label class="form-label fw-semibold">
                            Ngày bắt đầu <span class="text-danger">*</span>
                        </label>
                        <input type="datetime-local" name="startDate"
                               value="${startDateStr}"
                               class="form-control <c:if test="${not empty errors['startDate']}">is-invalid</c:if>">
                        <c:if test="${not empty errors['startDate']}">
                            <div class="invalid-feedback">${errors['startDate']}</div>
                        </c:if>
                    </div>
                    <div class="col-md-6 mb-3">
                        <label class="form-label fw-semibold">
                            Ngày kết thúc <span class="text-danger">*</span>
                        </label>
                        <input type="datetime-local" name="endDate"
                               value="${endDateStr}"
                               class="form-control <c:if test="${not empty errors['endDate']}">is-invalid</c:if>">
                        <c:if test="${not empty errors['endDate']}">
                            <div class="invalid-feedback">${errors['endDate']}</div>
                        </c:if>
                    </div>
                </div>

                <!-- Usage Limit -->
                <div class="mb-3">
                    <label class="form-label fw-semibold">Giới hạn sử dụng</label>
                    <input type="number" name="usageLimit" min="1"
                           value="${promotion.usageLimit}"
                           class="form-control <c:if test="${not empty errors['usageLimit']}">is-invalid</c:if>"
                           placeholder="Để trống = không giới hạn">
                    <c:if test="${not empty errors['usageLimit']}">
                        <div class="invalid-feedback">${errors['usageLimit']}</div>
                    </c:if>
                </div>

                <!-- Is Active -->
                <div class="mb-4">
                    <div class="form-check form-switch">
                        <input class="form-check-input" type="checkbox"
                               name="isActive" id="isActive" role="switch"
                               <c:if test="${promotion.isActive or empty promotion}">checked</c:if>>
                        <label class="form-check-label" for="isActive">
                            Kích hoạt ngay
                        </label>
                    </div>
                </div>

                <!-- Buttons -->
                <div class="d-flex gap-2">
                    <button type="submit" class="btn btn-primary px-4">Lưu</button>
                    <a href="${pageContext.request.contextPath}/manager/promotions"
                       class="btn btn-outline-secondary px-4">Hủy</a>
                </div>

            </form>
        </div>
    </div>

</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
    function toggleMaxDiscount(type) {
        var row = document.getElementById('maxDiscountRow');
        if (row) {
            row.style.display = (type === 'Percentage') ? '' : 'none';
        }
    }
</script>
</body>
</html>
