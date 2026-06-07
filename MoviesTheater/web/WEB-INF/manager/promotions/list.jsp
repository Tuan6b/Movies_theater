<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% request.setAttribute("activeNav", "promotions"); %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Promotions — CGV Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
</head>
<body class="cgv-body">

<%@ include file="../_sidebar.jsp" %>

<div class="cgv-main">

    <header class="cgv-header">
        <h1 class="cgv-header-title">Promotions</h1>
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
                    <a href="?status="        class="cgv-pill ${empty param.status ? 'active' : ''}">All</a>
                    <a href="?status=upcoming" class="cgv-pill ${param.status eq 'upcoming' ? 'active' : ''}">Upcoming</a>
                    <a href="?status=active"   class="cgv-pill ${param.status eq 'active'   ? 'active' : ''}">Active</a>
                    <a href="?status=expired"  class="cgv-pill ${param.status eq 'expired'  ? 'active' : ''}">Expired</a>
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
                        <input class="cgv-input" style="max-width:220px;height:38px;"
                               type="text" name="keyword" placeholder="Code or name…" value="${param.keyword}">
                        <select class="cgv-select" style="max-width:160px;height:38px;" name="type">
                            <option value="">All Types</option>
                            <option value="PERCENT" ${param.type eq 'PERCENT' ? 'selected' : ''}>Percentage</option>
                            <option value="FIXED"   ${param.type eq 'FIXED'   ? 'selected' : ''}>Fixed Amount</option>
                        </select>
                        <button type="submit" class="btn--cgv-outline">Filter</button>
                    </form>
                </div>

                <table class="cgv-dt">
                    <thead>
                        <tr>
                            <th>#</th>
                            <th>Code</th>
                            <th>Description</th>
                            <th>Type</th>
                            <th>Value</th>
                            <th>Start Date</th>
                            <th>End Date</th>
                            <th>Uses / Limit</th>
                            <th>Status</th>
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
                                        <td style="font-size:13px;">${p.startDateDisplay}</td>
                                        <td style="font-size:13px;">${p.endDateDisplay}</td>
                                        <td>${p.usedCount} / ${not empty p.usageLimit ? p.usageLimit : '∞'}</td>
                                        <td>
                                            <span class="cgv-badge ${p.status eq 'active' ? 'active' : p.status eq 'upcoming' ? 'upcoming' : p.status eq 'expired' ? 'danger' : 'inactive'}">
                                                ${p.status}
                                            </span>
                                        </td>
                                        <td>
                                            <div style="display:flex;gap:8px;">
                                                <a href="${pageContext.request.contextPath}/manager/promotions?action=edit&id=${p.promotionId}"
                                                   class="btn--cgv-outline">Edit</a>
                                                <form method="post" style="display:inline;">
                                                    <input type="hidden" name="action" value="delete">
                                                    <input type="hidden" name="promotionId" value="${p.promotionId}">
                                                    <button type="submit" class="btn--cgv-outline"
                                                            style="color:var(--cgv-red);border-color:var(--cgv-red);"
                                                            onclick="return confirm('Delete promotion ${p.promotionCode}?')">deactivate</button>
                                                </form>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr><td colspan="10" style="text-align:center;padding:48px;color:rgba(94,63,58,0.4);">No promotions found.</td></tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>

                <div class="cgv-pager">
                    <span>
                        Showing ${not empty promotions ? promotions.size() : 0}
                        of ${not empty totalItems ? totalItems : 0} promotions
                    </span>
                    <div class="cgv-pager-pages">
                        <c:forEach begin="1" end="${not empty totalPages ? totalPages : 1}" var="pg">
                            <button class="cgv-pager-btn ${pg eq currentPage ? 'active' : ''}"
                                    onclick="location.href='?page=${pg}&keyword=${param.keyword}&type=${param.type}&status=${param.status}'">${pg}</button>
                        </c:forEach>
                    </div>
                </div>
            </div>
        </div>

        <aside class="cgv-aside">
            <div class="cgv-stats-section">
                <div class="cgv-aside-heading">OVERVIEW</div>
                <div class="cgv-stats-group">
                    <div>
                        <div class="cgv-stat-num">${not empty totalItems ? totalItems : '0'}</div>
                        <div class="cgv-stat-key">TOTAL PROMOTIONS</div>
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
            </div>
        </aside>
    </div>
</div>
</body>
</html>
