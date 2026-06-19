<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<% request.setAttribute("activeNav", "analytics"); %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Revenue &amp; Analytics — CGV Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
    <style>
        .rev-grid {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 24px;
            margin-bottom: 40px;
        }
        .rev-kpi {
            background: #fff;
            border: 1px solid var(--cgv-border);
            border-radius: 12px;
            padding: 24px;
        }
        .rev-kpi-val {
            font-family: var(--font-cgv-display);
            font-size: 32px;
            color: var(--cgv-dark);
            line-height: 1.1;
        }
        .rev-kpi-val.red   { color: var(--cgv-red); }
        .rev-kpi-val.amber { color: var(--cgv-amber); }
        .rev-kpi-key {
            font-family: var(--font-cgv-ui);
            font-size: 10px;
            font-weight: 600;
            letter-spacing: 1.5px;
            text-transform: uppercase;
            color: rgba(94,63,58,0.6);
            margin-top: 6px;
        }
        .rev-table-section { margin-top: 8px; }
        .sort-link {
            color: rgba(94,63,58,0.5);
            font-size: 10px;
            text-decoration: none;
            margin-left: 4px;
        }
        .sort-link:hover { color: var(--cgv-red); }
        .sort-link.active { color: var(--cgv-red); font-weight:700; }
    </style>
</head>
<body class="cgv-body">

<%@ include file="../_sidebar.jsp" %>

<div class="cgv-main">

    <header class="cgv-header">
        <h1 class="cgv-header-title">Revenue &amp; Analytics</h1>
        <div class="cgv-header-right">
            <div class="cgv-header-actions">
                <div class="cgv-header-divider"></div>
                <div class="cgv-user-wrap">
                    <div class="cgv-avatar">MG</div>
                    <span class="cgv-user-name">
                        <c:choose>
                            <c:when test="${not empty sessionScope.account}">${sessionScope.account.fullName}</c:when>
                            <c:otherwise>Manager</c:otherwise>
                        </c:choose>
                    </span>
                </div>
            </div>
        </div>
    </header>

    <div class="cgv-page">
        <div class="cgv-list-wrap">

            <%-- KPI cards --%>
            <div class="rev-grid">
                <div class="rev-kpi">
                    <div class="rev-kpi-val">${not empty grandTotal ? grandTotal : '0'}</div>
                    <div class="rev-kpi-key">TOTAL REVENUE ${selectedYear} (VND)</div>
                </div>
                <div class="rev-kpi">
                    <div class="rev-kpi-val amber">${not empty grandTickets ? grandTickets : '0'}</div>
                    <div class="rev-kpi-key">TICKETS SOLD ${selectedYear}</div>
                </div>
                <div class="rev-kpi">
                    <div class="rev-kpi-val red">${not empty newCustomers ? newCustomers : '0'}</div>
                    <div class="rev-kpi-key">NEW CUSTOMERS THIS MONTH</div>
                </div>
            </div>

            <%-- Monthly revenue table --%>
            <div class="rev-table-section">
                <div class="cgv-data-wrap">
                    <div class="cgv-data-toolbar" style="display:flex;align-items:center;justify-content:space-between;flex-wrap:wrap;gap:12px;">
                        <span style="font-family:var(--font-cgv-ui);font-size:10px;font-weight:700;letter-spacing:2px;text-transform:uppercase;color:rgba(94,63,58,0.5);">
                            MONTHLY REVENUE
                        </span>
                        <form method="get" action="${pageContext.request.contextPath}/manager/analytics"
                              style="display:flex;gap:10px;align-items:center;">
                            <select class="cgv-select" style="height:36px;width:100px;" name="year">
                                <c:forEach begin="2023" end="2026" var="y">
                                    <option value="${y}" ${selectedYear eq y ? 'selected' : ''}>${y}</option>
                                </c:forEach>
                            </select>
                            <input type="hidden" name="sortBy" value="${sortBy}">
                            <input type="hidden" name="dir" value="${sortDir}">
                            <button type="submit" class="btn--cgv-outline" style="height:36px;">Go</button>
                        </form>
                    </div>

                    <table class="cgv-dt">
                        <thead>
                            <tr>
                                <th>
                                    Month
                                    <c:set var="nextMonthDir" value="${sortBy eq 'month' && sortDir eq 'ASC' ? 'DESC' : 'ASC'}"/>
                                    <a href="?year=${selectedYear}&sortBy=month&dir=${nextMonthDir}"
                                       class="sort-link ${sortBy eq 'month' ? 'active' : ''}">
                                        ${sortBy eq 'month' && sortDir eq 'ASC' ? '↑' : '↓'}
                                    </a>
                                </th>
                                <th>Invoices</th>
                                <th>Tickets Sold</th>
                                <th>
                                    Revenue (VND)
                                    <c:set var="nextRevDir" value="${sortBy eq 'revenue' && sortDir eq 'ASC' ? 'DESC' : 'ASC'}"/>
                                    <a href="?year=${selectedYear}&sortBy=revenue&dir=${nextRevDir}"
                                       class="sort-link ${sortBy eq 'revenue' ? 'active' : ''}">
                                        ${sortBy eq 'revenue' && sortDir eq 'ASC' ? '↑' : '↓'}
                                    </a>
                                </th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty monthlyData}">
                                    <c:forEach var="mr" items="${monthlyData}">
                                        <tr>
                                            <td style="font-weight:600;">${mr.monthName} ${selectedYear}</td>
                                            <td>${mr.totalInvoices}</td>
                                            <td>${mr.ticketsSold}</td>
                                            <td style="font-weight:500;">${mr.formattedRevenue}</td>
                                        </tr>
                                    </c:forEach>
                                    <tr style="background:rgba(94,63,58,0.04);font-weight:700;">
                                        <td>TOTAL ${selectedYear}</td>
                                        <td>—</td>
                                        <td>${grandTickets}</td>
                                        <td style="color:var(--cgv-red);">${grandTotal}</td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <tr>
                                        <td colspan="4" style="text-align:center;padding:48px;color:rgba(94,63,58,0.4);">
                                            No revenue data for ${selectedYear}.
                                        </td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>

        </div>

        <aside class="cgv-aside">
            <div class="cgv-stats-section">
                <div class="cgv-aside-heading">THIS MONTH</div>
                <div class="cgv-stats-group">
                    <div>
                        <div class="cgv-stat-num">${not empty monthRevenue ? monthRevenue : '0'}</div>
                        <div class="cgv-stat-key">REVENUE (VND)</div>
                    </div>
                    <div>
                        <div class="cgv-stat-num amber">${not empty monthTickets ? monthTickets : '0'}</div>
                        <div class="cgv-stat-key">TICKETS SOLD</div>
                    </div>
                    <div>
                        <div class="cgv-stat-num red">${not empty newCustomers ? '+' : ''}${not empty newCustomers ? newCustomers : '0'}</div>
                        <div class="cgv-stat-key">NEW CUSTOMERS</div>
                    </div>
                </div>
            </div>
            <div class="cgv-aside-divider">
                <div class="cgv-aside-heading">SORT OPTIONS</div>
                <div style="display:flex;flex-direction:column;gap:6px;margin-top:8px;">
                    <a href="?year=${selectedYear}&sortBy=month&dir=ASC"
                       class="btn--cgv-outline ${sortBy eq 'month' && sortDir eq 'ASC' ? 'active' : ''}"
                       style="text-align:center;font-size:12px;">Month ↑</a>
                    <a href="?year=${selectedYear}&sortBy=month&dir=DESC"
                       class="btn--cgv-outline ${sortBy eq 'month' && sortDir eq 'DESC' ? 'active' : ''}"
                       style="text-align:center;font-size:12px;">Month ↓</a>
                    <a href="?year=${selectedYear}&sortBy=revenue&dir=DESC"
                       class="btn--cgv-outline ${sortBy eq 'revenue' && sortDir eq 'DESC' ? 'active' : ''}"
                       style="text-align:center;font-size:12px;">Revenue ↓</a>
                    <a href="?year=${selectedYear}&sortBy=revenue&dir=ASC"
                       class="btn--cgv-outline ${sortBy eq 'revenue' && sortDir eq 'ASC' ? 'active' : ''}"
                       style="text-align:center;font-size:12px;">Revenue ↑</a>
                </div>
            </div>
        </aside>
    </div>
</div>
</body>
</html>
