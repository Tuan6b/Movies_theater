<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<% request.setAttribute("activeNav", "food"); %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Quản lý Đồ ăn — Nhân viên CGV</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
</head>
<body class="cgv-body">

<%@ include file="/WEB-INF/employee/_sidebar.jsp" %>

<div class="cgv-main">

    <header class="cgv-header">
        <h1 class="cgv-header-title">Đồ ăn &amp; Nước uống</h1>
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

        <div class="cgv-table-wrap">

            <c:if test="${not empty requestScope.flashSuccess}">
                <div class="cgv-alert cgv-alert-success">${requestScope.flashSuccess}</div>
            </c:if>
            <c:if test="${not empty requestScope.flashError}">
                <div class="cgv-alert cgv-alert-danger">${requestScope.flashError}</div>
            </c:if>

            <div class="cgv-toolbar">
                <div class="cgv-pills">
                    <a href="${pageContext.request.contextPath}/FoodController?type=retail"
                       class="cgv-pill ${currentType eq 'retail' ? 'active' : ''}">Bán lẻ</a>
                    <a href="${pageContext.request.contextPath}/FoodController?type=combo"
                       class="cgv-pill ${currentType eq 'combo' ? 'active' : ''}">Combo</a>
                </div>
                <a href="${pageContext.request.contextPath}/FoodController?action=add&amp;type=${currentType}" class="btn--cgv" style="margin-left:auto;">
                    + Thêm món
                </a>
            </div>

            <div class="cgv-data-wrap">
                <table class="cgv-dt">
                    <thead>
                        <tr>
                            <th>#</th>
                            <th>Ảnh</th>
                            <th>Tên món</th>
                            <th>Giá</th>
                            <th>Trạng thái</th>
                            <th>Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty foodList}">
                                <c:forEach var="item" items="${foodList}" varStatus="st">
                                    <tr>
                                        <td style="color:rgba(94,63,58,0.5);font-size:12px;">${st.index + 1}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty item.image}">
                                                    <img src="${item.image}" alt="${item.foodName}"
                                                         style="width:48px;height:48px;object-fit:cover;border-radius:6px;border:1px solid var(--cgv-border);">
                                                </c:when>
                                                <c:otherwise>
                                                    <span style="color:rgba(94,63,58,0.2);font-size:11px;">—</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="font-weight:600;">${item.foodName}</td>
                                        <td><fmt:formatNumber value="${item.price}" pattern="#,###"/> VNĐ</td>
                                        <td>
                                            <span class="cgv-badge ${item.isActive ? 'active' : 'inactive'}">
                                                ${item.isActive ? 'Đang bán' : 'Đã ẩn'}
                                            </span>
                                        </td>
                                        <td>
                                            <div style="display:flex;gap:8px;">
                                                <a href="${pageContext.request.contextPath}/FoodController?action=edit&amp;id=${item.foodId}&amp;type=${currentType}" class="btn--cgv-outline">
                                                    Sửa
                                                </a>
                                                <form method="post" action="${pageContext.request.contextPath}/FoodController" style="display:inline;">
                                                    <input type="hidden" name="action" value="delete">
                                                    <input type="hidden" name="id" value="${item.foodId}">
                                                    <input type="hidden" name="type" value="${currentType}">
                                                    <button type="submit" class="btn--cgv-outline"
                                                            style="color:var(--cgv-red);border-color:var(--cgv-red);"
                                                            onclick="return confirm('Xóa món ${item.foodName}?')">
                                                        Xóa
                                                    </button>
                                                </form>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="6" style="text-align:center;padding:48px;color:rgba(94,63,58,0.4);">
                                        Chưa có món ăn nào.
                                    </td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>

        <aside class="cgv-aside">
            <div class="cgv-aside-divider">
                <div class="cgv-aside-heading">SUMMARY</div>
                <div class="cgv-stats-group">
                    <div>
                        <div class="cgv-stat-num">${not empty foodList ? foodList.size() : '0'}</div>
                        <div class="cgv-stat-key">MÓN ĂN</div>
                    </div>
                    <div>
                        <div class="cgv-stat-num amber">${currentType eq 'combo' ? 'Combo' : 'Bán lẻ'}</div>
                        <div class="cgv-stat-key">LOẠI</div>
                    </div>
                </div>
            </div>
        </aside>

    </div>
</div>
</body>
</html>
