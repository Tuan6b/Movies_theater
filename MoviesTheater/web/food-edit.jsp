<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% request.setAttribute("activeNav", "food"); %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Sửa món ăn — Nhân viên CGV</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
</head>
<body class="cgv-body">

<%@ include file="/WEB-INF/employee/_sidebar.jsp" %>

<div class="cgv-main">

    <header class="cgv-header">
        <h1 class="cgv-header-title">Sửa món ăn</h1>
        <div class="cgv-header-right">
            <div class="cgv-header-actions">
                <div class="cgv-header-divider"></div>
                <div class="cgv-user-wrap">
                    <div class="cgv-avatar">EM</div>
                    <span class="cgv-user-name">
                        <c:choose>
                            <c:when test="${not empty sessionScope.account}">${sessionScope.account.fullName}</c:when>
                            <c:otherwise>Nhân viên</c:otherwise>
                        </c:choose>
                    </span>
                </div>
            </div>
        </div>
    </header>

    <div class="cgv-page">
        <div class="cgv-list-wrap" style="max-width:640px;">

            <c:if test="${not empty requestScope.flashError}">
                <div class="cgv-alert cgv-alert-danger">${requestScope.flashError}</div>
            </c:if>

            <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:32px;">
                <div style="font-family:var(--font-cgv-ui);font-size:10px;font-weight:700;letter-spacing:2px;text-transform:uppercase;color:rgba(94,63,58,0.5);margin-bottom:24px;">
                    THÔNG TIN MÓN ĂN
                </div>

                <form action="${pageContext.request.contextPath}/FoodController" method="post">
                    <input type="hidden" name="action" value="edit">
                    <input type="hidden" name="id" value="${food.foodId}">

                    <div class="cgv-field">
                        <label class="cgv-label">Loại</label>
                        <select name="type" class="cgv-select">
                            <option value="retail" ${currentType eq 'retail' ? 'selected' : ''}>Bán lẻ</option>
                            <option value="combo" ${currentType eq 'combo' ? 'selected' : ''}>Combo</option>
                        </select>
                    </div>

                    <div class="cgv-field">
                        <label class="cgv-label">Tên món</label>
                        <input class="cgv-input" type="text" name="foodName" required
                               value="${food.foodName}">
                    </div>

                    <div class="cgv-field">
                        <label class="cgv-label">Giá (VNĐ)</label>
                        <input class="cgv-input" type="number" name="price" required min="0" step="1000"
                               value="${food.price}">
                    </div>

                    <div class="cgv-field">
                        <label class="cgv-label">URL Ảnh</label>
                        <input class="cgv-input" type="url" name="image"
                               value="${food.image}" placeholder="https://example.com/image.jpg">
                        <c:if test="${not empty food.image}">
                            <div style="margin-top:8px;">
                                <img src="${food.image}" alt="${food.foodName}"
                                     style="max-width:120px;max-height:80px;border-radius:6px;border:1px solid var(--cgv-border);">
                            </div>
                        </c:if>
                    </div>

                    <div style="display:flex;gap:12px;margin-top:24px;">
                        <button type="submit" class="btn--cgv">Cập nhật</button>
                        <a href="${pageContext.request.contextPath}/FoodController?type=${currentType}" class="btn--cgv-outline">Hủy</a>
                    </div>
                </form>
            </div>

        </div>
    </div>
</div>
</body>
</html>
