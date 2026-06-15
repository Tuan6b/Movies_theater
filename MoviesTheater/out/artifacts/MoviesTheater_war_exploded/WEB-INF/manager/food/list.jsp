<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% request.setAttribute("activeNav", "dashboard"); %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Food &amp; Menu — CGV Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
    <style>
        .food-grid {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 20px;
        }
        .food-card {
            background: #fff;
            border: 1px solid var(--cgv-border);
            border-radius: 12px;
            overflow: hidden;
            display: flex;
            flex-direction: column;
        }
        .food-thumb {
            height: 160px;
            background: #f3eeec;
            display: flex;
            align-items: center;
            justify-content: center;
            overflow: hidden;
        }
        .food-thumb img { width:100%;height:100%;object-fit:cover; }
        .food-thumb-empty { color:rgba(94,63,58,0.3);font-size:32px; }
        .food-info { padding: 16px; flex: 1; }
        .food-name {
            font-family: var(--font-cgv-ui);
            font-weight: 600;
            font-size: 15px;
            color: var(--cgv-dark);
            margin-bottom: 4px;
        }
        .food-cat {
            font-size: 12px;
            color: rgba(94,63,58,0.5);
            letter-spacing: 0.5px;
            text-transform: uppercase;
            margin-bottom: 10px;
        }
        .food-price {
            font-family: var(--font-cgv-display);
            font-size: 18px;
            color: var(--cgv-red);
            margin-bottom: 12px;
        }
        .food-actions {
            padding: 12px 16px;
            border-top: 1px solid var(--cgv-border);
            display: flex;
            gap: 8px;
        }
    </style>
</head>
<body class="cgv-body">

<%@ include file="../_sidebar.jsp" %>

<div class="cgv-main">

    <header class="cgv-header">
        <h1 class="cgv-header-title">Food &amp; Menu</h1>
        <div class="cgv-header-right">
            <div class="cgv-search-wrap">
                <svg class="cgv-search-icon" viewBox="0 0 24 24" fill="none"
                     stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="11" cy="11" r="8"/>
                    <line x1="21" y1="21" x2="16.65" y2="16.65"/>
                </svg>
                <input class="cgv-search" type="text" placeholder="Search menu items..." value="${param.q}">
            </div>
            <div class="cgv-header-actions">
                <div class="cgv-header-divider"></div>
                <div class="cgv-user-wrap">
                    <div class="cgv-avatar">
                        <c:choose>
                            <c:when test="${not empty sessionScope.LOGIN_USER}">MG</c:when>
                            <c:otherwise>MG</c:otherwise>
                        </c:choose>
                    </div>
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
        <div class="cgv-list-wrap">

            <c:if test="${not empty flashSuccess}">
                <div class="cgv-alert cgv-alert-success">${flashSuccess}</div>
            </c:if>
            <c:if test="${not empty flashError}">
                <div class="cgv-alert cgv-alert-danger">${flashError}</div>
            </c:if>

            <div class="cgv-toolbar">
                <div class="cgv-pills">
                    <a href="?cat=all"      class="cgv-pill ${empty param.cat || param.cat eq 'all'      ? 'active' : ''}">All Items</a>
                    <a href="?cat=popcorn"  class="cgv-pill ${param.cat eq 'popcorn'  ? 'active' : ''}">Popcorn</a>
                    <a href="?cat=drinks"   class="cgv-pill ${param.cat eq 'drinks'   ? 'active' : ''}">Drinks</a>
                    <a href="?cat=snacks"   class="cgv-pill ${param.cat eq 'snacks'   ? 'active' : ''}">Snacks</a>
                </div>
                <a href="?action=add" class="btn--cgv">
                    <svg width="10" height="10" viewBox="0 0 12 12" fill="none"
                         stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
                        <line x1="6" y1="1" x2="6" y2="11"/><line x1="1" y1="6" x2="11" y2="6"/>
                    </svg>
                    Add Item
                </a>
            </div>

            <div class="food-grid">
                <c:choose>
                    <c:when test="${not empty foodList}">
                        <c:forEach var="item" items="${foodList}">
                            <div class="food-card">
                                <div class="food-thumb">
                                    <c:choose>
                                        <c:when test="${not empty item.imageUrl}">
                                            <img src="${item.imageUrl}" alt="${item.name}">
                                        </c:when>
                                        <c:otherwise>
                                            <span class="food-thumb-empty">🍿</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <div class="food-info">
                                    <div class="food-name">${item.name}</div>
                                    <div class="food-cat">${item.category}</div>
                                    <div class="food-price">${item.price} VND</div>
                                    <span class="cgv-badge ${item.available ? 'active' : 'inactive'}">
                                        ${item.available ? 'Available' : 'Unavailable'}
                                    </span>
                                </div>
                                <div class="food-actions">
                                    <a href="?action=edit&id=${item.itemId}" class="btn--cgv-outline">Edit</a>
                                    <form method="post" style="display:inline;">
                                        <input type="hidden" name="action" value="${item.available ? 'hide' : 'show'}">
                                        <input type="hidden" name="id" value="${item.itemId}">
                                        <button type="submit" class="btn--cgv-outline">
                                            ${item.available ? 'Hide' : 'Show'}
                                        </button>
                                    </form>
                                </div>
                            </div>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <%-- Placeholder cards --%>
                        <div class="food-card">
                            <div class="food-thumb"><span class="food-thumb-empty">🍿</span></div>
                            <div class="food-info">
                                <div class="food-name">Large Popcorn</div>
                                <div class="food-cat">Popcorn</div>
                                <div class="food-price">69,000 VND</div>
                                <span class="cgv-badge active">Available</span>
                            </div>
                            <div class="food-actions">
                                <a href="?action=edit&id=1" class="btn--cgv-outline">Edit</a>
                            </div>
                        </div>
                        <div class="food-card">
                            <div class="food-thumb"><span class="food-thumb-empty">🥤</span></div>
                            <div class="food-info">
                                <div class="food-name">Coca-Cola L</div>
                                <div class="food-cat">Drinks</div>
                                <div class="food-price">39,000 VND</div>
                                <span class="cgv-badge active">Available</span>
                            </div>
                            <div class="food-actions">
                                <a href="?action=edit&id=2" class="btn--cgv-outline">Edit</a>
                            </div>
                        </div>
                        <div class="food-card">
                            <div class="food-thumb"><span class="food-thumb-empty">🌭</span></div>
                            <div class="food-info">
                                <div class="food-name">Hot Dog Combo</div>
                                <div class="food-cat">Snacks</div>
                                <div class="food-price">59,000 VND</div>
                                <span class="cgv-badge inactive">Unavailable</span>
                            </div>
                            <div class="food-actions">
                                <a href="?action=edit&id=3" class="btn--cgv-outline">Edit</a>
                            </div>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>

        </div>

        <aside class="cgv-aside">
            <div class="cgv-stats-section">
                <div class="cgv-aside-heading">MENU</div>
                <div class="cgv-stats-group">
                    <div>
                        <div class="cgv-stat-num">${not empty totalItems ? totalItems : '24'}</div>
                        <div class="cgv-stat-key">TOTAL ITEMS</div>
                    </div>
                    <div>
                        <div class="cgv-stat-num amber">${not empty availableItems ? availableItems : '20'}</div>
                        <div class="cgv-stat-key">AVAILABLE</div>
                    </div>
                    <div>
                        <div class="cgv-stat-num red">${not empty hiddenItems ? hiddenItems : '4'}</div>
                        <div class="cgv-stat-key">HIDDEN</div>
                    </div>
                </div>
            </div>
            <div class="cgv-aside-divider">
                <div class="cgv-aside-heading">TOP SELLERS</div>
                <div class="cgv-events-list">
                    <c:choose>
                        <c:when test="${not empty topItems}">
                            <c:forEach var="t" items="${topItems}">
                                <div>
                                    <div class="cgv-event-title">${t.name}</div>
                                    <div class="cgv-event-desc">${t.soldCount} sold this month</div>
                                </div>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <div>
                                <div class="cgv-event-title">Large Popcorn</div>
                                <div class="cgv-event-desc">1,240 sold this month</div>
                            </div>
                            <div>
                                <div class="cgv-event-title">Coca-Cola L</div>
                                <div class="cgv-event-desc">980 sold this month</div>
                            </div>
                            <div>
                                <div class="cgv-event-title">Combo Set A</div>
                                <div class="cgv-event-desc">640 sold this month</div>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </aside>
    </div>
</div>
</body>
</html>
