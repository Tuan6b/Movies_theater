<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% request.setAttribute("activeNav", "promotions"); %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Upcoming Promotions — CGV Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
</head>
<body class="cgv-body">

<%@ include file="../_sidebar.jsp" %>

<div class="cgv-main">

    <header class="cgv-header">
        <h1 class="cgv-header-title">Upcoming Promotions</h1>
        <div class="cgv-header-right">
            <div class="cgv-header-actions">
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
        <div class="cgv-table-wrap">

            <c:if test="${not empty flashSuccess}">
                <div class="cgv-alert cgv-alert-success">${flashSuccess}</div>
            </c:if>
            <c:if test="${not empty flashError}">
                <div class="cgv-alert cgv-alert-danger">${flashError}</div>
            </c:if>

            <div class="cgv-toolbar">
                <div class="cgv-pills">
                    <a href="${pageContext.request.contextPath}/manager/promotions?view=upcoming" class="cgv-pill active">Upcoming</a>
                    <a href="${pageContext.request.contextPath}/manager/promotions?view=active" class="cgv-pill">Active</a>
                    <a href="${pageContext.request.contextPath}/manager/promotions?view=expired" class="cgv-pill">Expired</a>
                    <a href="${pageContext.request.contextPath}/manager/promotions?view=inactive" class="cgv-pill">Inactive</a>
                </div>
                <a href="${pageContext.request.contextPath}/manager/promotions?action=add" class="btn--cgv">
                    <svg width="10" height="10" viewBox="0 0 12 12" fill="none"
                         stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
                        <line x1="6" y1="1" x2="6" y2="11"/><line x1="1" y1="6" x2="11" y2="6"/>
                    </svg>
                    Add Promotion
                </a>
            </div>

            <div class="cgv-data-wrap">
                <div class="cgv-data-toolbar">
                    <form method="get" style="display:flex;gap:10px;align-items:center;flex-wrap:wrap;">
                        <input type="hidden" name="view" value="upcoming">
                        <input class="cgv-input" style="max-width:220px;height:38px;"
                               type="text" name="keyword" placeholder="Code or name…" value="${keyword}">
                        <select class="cgv-select" style="max-width:160px;height:38px;" name="type">
                            <option value="">All Types</option>
                            <option value="Percentage" ${filterType eq 'Percentage' ? 'selected' : ''}>Percentage</option>
                            <option value="FlatAmount"  ${filterType eq 'FlatAmount'  ? 'selected' : ''}>Fixed Amount</option>
                        </select>
                        <button type="submit" class="btn--cgv-outline">Filter</button>
                    </form>
                </div>

                <c:set var="vw"  value="upcoming"/>
                <c:set var="kw"  value="${keyword}"/>
                <c:set var="ft"  value="${filterType}"/>
                <c:set var="sd"  value="${sortDir}"/>
                <table class="cgv-dt">
                    <thead>
                        <tr>
                            <th>#</th>
                            <th><a class="sort-link ${sortBy eq 'code' ? 'sort-active' : ''}" href="?view=${vw}&keyword=${kw}&type=${ft}&sort=code&dir=${sortBy eq 'code' ? (sd eq 'ASC' ? 'DESC' : 'ASC') : 'ASC'}">Code <c:if test="${sortBy eq 'code'}">${sd eq 'ASC' ? '↑' : '↓'}</c:if></a></th>
                            <th>Description</th>
                            <th><a class="sort-link ${sortBy eq 'type' ? 'sort-active' : ''}" href="?view=${vw}&keyword=${kw}&type=${ft}&sort=type&dir=${sortBy eq 'type' ? (sd eq 'ASC' ? 'DESC' : 'ASC') : 'ASC'}">Type <c:if test="${sortBy eq 'type'}">${sd eq 'ASC' ? '↑' : '↓'}</c:if></a></th>
                            <th><a class="sort-link ${sortBy eq 'value' ? 'sort-active' : ''}" href="?view=${vw}&keyword=${kw}&type=${ft}&sort=value&dir=${sortBy eq 'value' ? (sd eq 'ASC' ? 'DESC' : 'ASC') : 'ASC'}">Value <c:if test="${sortBy eq 'value'}">${sd eq 'ASC' ? '↑' : '↓'}</c:if></a></th>
                            <th>Min Order</th>
                            <th><a class="sort-link ${sortBy eq 'startDate' ? 'sort-active' : ''}" href="?view=${vw}&keyword=${kw}&type=${ft}&sort=startDate&dir=${sortBy eq 'startDate' ? (sd eq 'ASC' ? 'DESC' : 'ASC') : 'ASC'}">Start Date <c:if test="${sortBy eq 'startDate'}">${sd eq 'ASC' ? '↑' : '↓'}</c:if></a></th>
                            <th><a class="sort-link ${sortBy eq 'endDate' ? 'sort-active' : ''}" href="?view=${vw}&keyword=${kw}&type=${ft}&sort=endDate&dir=${sortBy eq 'endDate' ? (sd eq 'ASC' ? 'DESC' : 'ASC') : 'ASC'}">End Date <c:if test="${sortBy eq 'endDate'}">${sd eq 'ASC' ? '↑' : '↓'}</c:if></a></th>
                            <th><a class="sort-link ${sortBy eq 'uses' ? 'sort-active' : ''}" href="?view=${vw}&keyword=${kw}&type=${ft}&sort=uses&dir=${sortBy eq 'uses' ? (sd eq 'ASC' ? 'DESC' : 'ASC') : 'ASC'}">Uses / Limit <c:if test="${sortBy eq 'uses'}">${sd eq 'ASC' ? '↑' : '↓'}</c:if></a></th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty promotions}">
                                <c:forEach var="p" items="${promotions}" varStatus="st">
                                    <tr>
                                        <td style="color:rgba(94,63,58,0.5);font-size:12px;">${st.index + 1}</td>
                                        <td>
                                            <code style="font-family:monospace;background:#f3f3f3;padding:2px 8px;border-radius:4px;font-size:12px;font-weight:600;letter-spacing:1px;">
                                                ${p.promotionCode}
                                            </code>
                                        </td>
                                        <td style="font-weight:500;">${p.description}</td>
                                        <td style="color:rgba(94,63,58,0.7);">
                                            <c:choose>
                                                <c:when test="${p.discountType eq 'Percentage' or p.discountType eq 'PERCENT'}">Percentage</c:when>
                                                <c:when test="${p.discountType eq 'FlatAmount' or p.discountType eq 'FIXED'}">Fixed Amount</c:when>
                                                <c:otherwise>${p.discountType}</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="font-weight:600;">
                                            <c:choose>
                                                <c:when test="${p.discountType eq 'Percentage' or p.discountType eq 'PERCENT'}">${p.discountValue}%</c:when>
                                                <c:otherwise>${p.discountValue} VND</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="font-size:13px;">
                                            <c:choose>
                                                <c:when test="${empty p.minOrderAmount or p.minOrderAmount == 0}">—</c:when>
                                                <c:otherwise>${p.minOrderAmount} VND</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="font-size:13px;">${p.startDateDisplay}</td>
                                        <td style="font-size:13px;">${p.endDateDisplay}</td>
                                        <td>${p.usedCount} / ${not empty p.usageLimit ? p.usageLimit : '∞'}</td>
                                        <td>
                                            <div style="display:flex;gap:6px;flex-wrap:wrap;">
                                                <a href="${pageContext.request.contextPath}/manager/promotions?action=edit&id=${p.promotionId}"
                                                   class="btn--cgv-outline">Edit</a>
                                                <form method="post" style="display:inline;">
                                                    <input type="hidden" name="action" value="activateEarly">
                                                    <input type="hidden" name="promotionId" value="${p.promotionId}">
                                                    <button type="submit" class="btn--cgv-outline"
                                                            style="color:#2e7d32;border-color:#2e7d32;"
                                                            onclick="return confirm('Activate \'${p.promotionCode}\' now?')">Activate Now</button>
                                                </form>
                                                <c:if test="${p.usedCount eq 0}">
                                                    <form method="post" style="display:inline;">
                                                        <input type="hidden" name="action" value="hardDelete">
                                                        <input type="hidden" name="returnTo" value="upcoming">
                                                        <input type="hidden" name="promotionId" value="${p.promotionId}">
                                                        <button type="submit" class="btn--cgv-outline"
                                                                style="color:var(--cgv-red);border-color:var(--cgv-red);"
                                                                onclick="return confirm('Permanently remove ${p.promotionCode}?')">Cancel</button>
                                                    </form>
                                                </c:if>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr><td colspan="10" style="text-align:center;padding:48px;color:rgba(94,63,58,0.4);">No upcoming promotions.</td></tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>

                <div class="cgv-pager">
                    <span>Showing ${not empty promotions ? promotions.size() : 0} of ${not empty totalItems ? totalItems : 0} promotions</span>
                    <div class="cgv-pager-pages">
                        <c:forEach begin="1" end="${not empty totalPages ? totalPages : 1}" var="pg">
                            <button class="cgv-pager-btn ${pg eq currentPage ? 'active' : ''}"
                                    onclick="location.href='?view=upcoming&page=${pg}&keyword=${keyword}&type=${filterType}&sort=${sortBy}&dir=${sortDir}'">${pg}</button>
                        </c:forEach>
                    </div>
                </div>
            </div>
        </div>

        <aside class="cgv-aside">
            <div class="cgv-stats-section">
                <div class="cgv-aside-heading">UPCOMING OVERVIEW</div>
                <div class="cgv-stats-group">
                    <div>
                        <div class="cgv-stat-num">${not empty totalItems ? totalItems : '0'}</div>
                        <div class="cgv-stat-key">TOTAL UPCOMING</div>
                    </div>
                    <div>
                        <div class="cgv-stat-num amber">${not empty totalPages ? totalPages : '1'}</div>
                        <div class="cgv-stat-key">PAGES</div>
                    </div>
                    <div>
                        <div class="cgv-stat-num">${not empty promotions ? promotions.size() : '0'}</div>
                        <div class="cgv-stat-key">SHOWING</div>
                    </div>
                </div>
                <div style="margin-top:20px;font-size:12px;color:rgba(94,63,58,0.6);line-height:1.6;border-top:1px solid var(--cgv-border);padding-top:16px;">
                    <strong>Activate Now</strong> — launches a promotion immediately by setting its start date to now.<br><br>
                    <strong>Cancel</strong> — permanently removes promotions that have never been used.
                </div>
            </div>
        </aside>
    </div>
</div>
</body>
</html>
