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
                <a href="${pageContext.request.contextPath}/FoodController?action=showAddForm&amp;type=${currentType}" class="btn--cgv" style="margin-left:auto;">
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
                                        <td style="color:rgba(94,63,58,0.5);font-size:12px;">${st.index + 1}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty item.image}">
                                                    <img src="${item.image}" alt="${item.foodName}"
                                                         onerror="this.onerror=null; this.src='${pageContext.request.contextPath}/Image/cgv_combo.svg';"
                                                         style="width:48px;height:48px;object-fit:cover;border-radius:6px;border:1px solid var(--cgv-border);">
                                                </c:when>
                                                <c:otherwise>
                                                    <span style="color:rgba(94,63,58,0.2);font-size:11px;">—</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="font-weight:600;">${item.foodName}</td>
                                        <td><fmt:formatNumber value="${item.price}" pattern="#,###"/> VND</td>
                                        <td>
                                            <span class="cgv-badge ${item.active ? 'active' : 'inactive'}">${item.active ? 'Active' : 'Inactive'}</span>
                                        </td>
                                        <td>
                                            <div style="display:flex;gap:8px;">
                                                <a href="${pageContext.request.contextPath}/FoodController?action=showEditForm&amp;id=${item.foodId}&amp;type=${currentType}" class="btn--cgv-outline">
                                                    Edit
                                                </a>
                                                <c:choose>
                                                    <c:when test="${item.active}">
                                                        <form method="post" action="${pageContext.request.contextPath}/FoodController" style="display:inline;">
                                                            <input type="hidden" name="action" value="delete">
                                                            <input type="hidden" name="id" value="${item.foodId}">
                                                            <input type="hidden" name="type" value="${currentType}">
                                                            <button type="submit" class="btn--cgv-outline"
                                                                    style="color:var(--cgv-red);border-color:var(--cgv-red);"
                                                                    onclick="return confirm('Deactivate ${item.foodName}?')">
                                                                Deactivate
                                                            </button>
                                                        </form>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <form method="post" action="${pageContext.request.contextPath}/FoodController" style="display:inline;">
                                                            <input type="hidden" name="action" value="restore">
                                                            <input type="hidden" name="id" value="${item.foodId}">
                                                            <input type="hidden" name="type" value="${currentType}">
                                                            <button type="submit" class="btn--cgv-outline"
                                                                    style="color:var(--success);border-color:var(--success);">
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
                                    <td colspan="6" style="text-align:center;padding:48px;color:rgba(94,63,58,0.4);">
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
