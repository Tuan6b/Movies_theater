<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% request.setAttribute("activeNav", "food"); %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Add Food — CGV Staff</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
</head>
<body class="cgv-body">

<%@ include file="/view/employee/_sidebar.jsp" %>

<div class="cgv-main">

    <header class="cgv-header">
        <h1 class="cgv-header-title">Add New Food Item</h1>
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
        <div class="cgv-list-wrap" style="max-width:640px;">

            <c:if test="${not empty requestScope.flashError}">
                <div class="cgv-alert cgv-alert-danger">${requestScope.flashError}</div>
            </c:if>

            <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:32px;">
                <div style="font-family:var(--font-cgv-ui);font-size:10px;font-weight:700;letter-spacing:2px;text-transform:uppercase;color:rgba(94,63,58,0.5);margin-bottom:24px;">
                    FOOD ITEM DETAILS
                </div>

                <form action="${pageContext.request.contextPath}/FoodController" method="post">
                    <input type="hidden" name="action" value="add">

                    <input type="hidden" name="type" value="${currentType}">

                    <div class="cgv-field">
                        <label class="cgv-label">Item Name</label>
                        <input class="cgv-input" type="text" name="foodName" required
                               placeholder="e.g. Large Butter Popcorn">
                    </div>

                    <div class="cgv-field">
                        <label class="cgv-label">Price (VND)</label>
                        <input class="cgv-input" type="number" name="price" required min="0" step="1000"
                               placeholder="e.g. 45000">
                    </div>

                    <div class="cgv-field">
                        <label class="cgv-label">Image URL</label>
                        <input class="cgv-input" type="text" name="image"
                               placeholder="https://example.com/image.jpg">
                    </div>

                    <div style="display:flex;gap:12px;margin-top:24px;">
                        <button type="submit" class="btn--cgv">Add New</button>
                        <a href="${pageContext.request.contextPath}/FoodController?type=${currentType}" class="btn--cgv-outline">Cancel</a>
                    </div>
                </form>
            </div>

        </div>
    </div>
</div>
</body>
</html>
