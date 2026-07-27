<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<% request.setAttribute("activeNav", "food"); %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Food Management — CGV Staff</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/employee/food-list.css">
    </head>
<body class="cgv-body">

<%@ include file="/view/employee/_sidebar.jsp" %>

<div class="cgv-main">

    <header class="cgv-header">
        <h1 class="cgv-header-title">Food &amp; Drinks</h1>
        <div class="cgv-header-right">
            <div class="cgv-header-actions">
                <div class="cgv-header-divider"></div>
                <div class="cgv-user-wrap">
                    <div class="cgv-avatar">EM</div>
                    <span class="cgv-user-name">
                        <c:choose>
                            <c:when test="${not empty sessionScope.account}">${sessionScope.account.fullName}</c:when>
                            <c:otherwise>Staff</c:otherwise>
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
                       class="cgv-pill ${currentType eq 'retail' ? 'active' : ''}">Retail</a>
                    <a href="${pageContext.request.contextPath}/FoodController?type=combo"
                       class="cgv-pill ${currentType eq 'combo' ? 'active' : ''}">Combo</a>
                </div>
                <a href="${pageContext.request.contextPath}/FoodController?action=showAddForm&amp;type=${currentType}" class="btn--cgv fl-btn-right">
                    + Add Food
                </a>
            </div>

            <div class="cgv-data-wrap">
                <table class="cgv-dt">
                    <thead>
                        <tr>
                            <th>#</th>
                            <th>Image</th>
                            <th>Item Name</th>
                            <th>Price</th>
                            <th>Status</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty foodList}">
                                <c:forEach var="item" items="${foodList}" varStatus="st">
                                    <tr>
                                        <td class="fl-td-index">${st.index + 1}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty item.image}">
                                                    <img src="${item.image}" alt="${item.foodName}"
                                                         onerror="this.onerror=null; this.src='${pageContext.request.contextPath}/Image/cgv_combo.svg';" class="fl-img-preview">
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="fl-sep">—</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="fl-td-bold">${item.foodName}</td>
                                        <td><fmt:formatNumber value="${item.price}" pattern="#,###"/> VND</td>
                                        <td>
                                            <span class="cgv-badge ${item.active ? 'active' : 'inactive'}">${item.active ? 'Active' : 'Inactive'}</span>
                                        </td>
                                        <td>
                                            <div class="fl-actions-wrap">
                                                <a href="${pageContext.request.contextPath}/FoodController?action=showEditForm&amp;id=${item.foodId}&amp;type=${currentType}" class="btn--cgv-outline">
                                                    Edit
                                                </a>
                                                <c:choose>
                                                    <c:when test="${item.active}">
                                                        <form method="post" action="${pageContext.request.contextPath}/FoodController" class="fl-form-inline">
                                                            <input type="hidden" name="action" value="delete">
                                                            <input type="hidden" name="id" value="${item.foodId}">
                                                            <input type="hidden" name="type" value="${currentType}">
                                                            <button type="submit" class="btn--cgv-outline fl-btn-danger"
                                                                    onclick="return confirm('Deactivate ${item.foodName}?')">
                                                                Deactivate
                                                            </button>
                                                        </form>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <form method="post" action="${pageContext.request.contextPath}/FoodController" class="fl-form-inline">
                                                            <input type="hidden" name="action" value="restore">
                                                            <input type="hidden" name="id" value="${item.foodId}">
                                                            <input type="hidden" name="type" value="${currentType}">
                                                            <button type="submit" class="btn--cgv-outline fl-btn-success">
                                                                Restore
                                                            </button>
                                                        </form>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="6" class="fl-empty-row">
                                        No food items yet.
                                    </td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>


    </div>
</div>
</body>
</html>
