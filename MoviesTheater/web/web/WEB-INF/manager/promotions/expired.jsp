<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% request.setAttribute("activeNav", "promotions"); %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Expired Promotions — CGV Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
</head>
<body class="cgv-body">

<%@ include file="../_sidebar.jsp" %>

<div class="cgv-main">

    <header class="cgv-header">
        <h1 class="cgv-header-title">Expired Promotions</h1>
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
                    <a href="${pageContext.request.contextPath}/manager/promotions?view=upcoming" class="cgv-pill">Upcoming</a>
                    <a href="${pageContext.request.contextPath}/manager/promotions?view=active" class="cgv-pill">Active</a>
                    <a href="${pageContext.request.contextPath}/manager/promotions?view=expired" class="cgv-pill active">Expired</a>
                    <a href="${pageContext.request.contextPath}/manager/promotions?view=inactive" class="cgv-pill">Inactive</a>
                </div>
            </div>

            <div class="cgv-data-wrap">
                <div class="cgv-data-toolbar">
                    <form method="get" style="display:flex;gap:10px;align-items:center;flex-wrap:wrap;">
                        <input type="hidden" name="view" value="expired">
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

                <c:set var="vw"  value="expired"/>
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
                            <th><a class="sort-link ${sortBy eq 'uses' ? 'sort-active' : ''}" href="?view=${vw}&keyword=${kw}&type=${ft}&sort=uses&dir=${sortBy eq 'uses' ? (sd eq 'ASC' ? 'DESC' : 'ASC') : 'ASC'}">Total Uses <c:if test="${sortBy eq 'uses'}">${sd eq 'ASC' ? '↑' : '↓'}</c:if></a></th>
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
                                        <td style="font-size:13px;color:var(--cgv-red);">${p.endDateDisplay}</td>
                                        <td style="font-weight:600;">${p.usedCount}</td>
                                        <td>
                                            <div style="display:flex;gap:6px;flex-wrap:wrap;">
                                                <button type="button" class="btn--cgv-outline"
                                                        style="color:#2e7d32;border-color:#2e7d32;"
                                                        onclick="showExtend(${p.promotionId}, '${p.promotionCode}')">Extend</button>
                                                <form method="post" style="display:inline;">
                                                    <input type="hidden" name="action" value="hardDelete">
                                                    <input type="hidden" name="returnTo" value="expired">
                                                    <input type="hidden" name="promotionId" value="${p.promotionId}">
                                                    <button type="submit" class="btn--cgv-outline"
                                                            style="color:var(--cgv-red);border-color:var(--cgv-red);"
                                                            onclick="return confirm('Archive and permanently delete ${p.promotionCode}?')">Archive</button>
                                                </form>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr><td colspan="10" style="text-align:center;padding:48px;color:rgba(94,63,58,0.4);">No expired promotions.</td></tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>

                <div class="cgv-pager">
                    <span>Showing ${not empty promotions ? promotions.size() : 0} of ${not empty totalItems ? totalItems : 0} promotions</span>
                    <div class="cgv-pager-pages">
                        <c:forEach begin="1" end="${not empty totalPages ? totalPages : 1}" var="pg">
                            <button class="cgv-pager-btn ${pg eq currentPage ? 'active' : ''}"
                                    onclick="location.href='?view=expired&page=${pg}&keyword=${keyword}&type=${filterType}&sort=${sortBy}&dir=${sortDir}'">${pg}</button>
                        </c:forEach>
                    </div>
                </div>
            </div>
        </div>

        <aside class="cgv-aside">
            <div class="cgv-stats-section">
                <div class="cgv-aside-heading">EXPIRED OVERVIEW</div>
                <div class="cgv-stats-group">
                    <div>
                        <div class="cgv-stat-num">${not empty totalItems ? totalItems : '0'}</div>
                        <div class="cgv-stat-key">TOTAL EXPIRED</div>
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
                    <strong>Extend</strong> — sets a new end date and reactivates the promotion.<br><br>
                    <strong>Archive</strong> — permanently removes expired promotions that have no paid invoices.
                </div>
            </div>
        </aside>
    </div>
</div>

<!-- Extend modal overlay -->
<div id="extendOverlay" style="display:none;position:fixed;inset:0;background:rgba(0,0,0,0.5);z-index:1000;align-items:center;justify-content:center;">
    <div style="background:#fff;border-radius:12px;padding:32px;max-width:440px;width:100%;margin:16px;box-shadow:0 8px 32px rgba(0,0,0,0.15);">
        <div style="font-size:10px;font-weight:700;letter-spacing:2px;text-transform:uppercase;color:rgba(94,63,58,0.5);margin-bottom:16px;">EXTEND PROMOTION</div>
        <p style="font-size:14px;margin-bottom:20px;color:rgba(94,63,58,0.8);">
            Set a new end date for
            <code id="extendCode" style="background:#f3f3f3;padding:2px 8px;border-radius:4px;font-size:12px;font-weight:700;letter-spacing:1px;"></code>
        </p>
        <form method="post" action="${pageContext.request.contextPath}/manager/promotions">
            <input type="hidden" name="action" value="extend">
            <input type="hidden" name="promotionId" id="extendId">
            <div class="cgv-field">
                <label class="cgv-label">New End Date <span style="color:var(--cgv-red)">*</span></label>
                <input class="cgv-input" type="datetime-local" name="newEndDate" required>
            </div>
            <div style="display:flex;gap:12px;margin-top:20px;">
                <button type="submit" class="btn--cgv">Extend &amp; Reactivate</button>
                <button type="button" class="btn--cgv-outline" onclick="closeExtend()">Cancel</button>
            </div>
        </form>
    </div>
</div>

<script>
function showExtend(id, code) {
    document.getElementById('extendId').value = id;
    document.getElementById('extendCode').textContent = code;
    document.getElementById('extendOverlay').style.display = 'flex';
}
function closeExtend() {
    document.getElementById('extendOverlay').style.display = 'none';
}
</script>

</body>
</html>
