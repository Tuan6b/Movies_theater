<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
            margin-bottom: 32px;
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
        .section-heading {
            font-family: var(--font-cgv-ui);
            font-size: 10px;
            font-weight: 700;
            letter-spacing: 2px;
            text-transform: uppercase;
            color: rgba(94,63,58,0.5);
            margin: 32px 0 12px 0;
        }
        .two-col-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 24px;
            margin-bottom: 32px;
        }
        .sort-link {
            color: rgba(94,63,58,0.5);
            font-size: 10px;
            text-decoration: none;
            margin-left: 4px;
        }
        .sort-link:hover  { color: var(--cgv-red); }
        .sort-link.active { color: var(--cgv-red); font-weight: 700; }
        .bar-wrap {
            height: 8px;
            background: rgba(94,63,58,0.08);
            border-radius: 4px;
            overflow: hidden;
            margin-top: 4px;
        }
        .bar-fill {
            height: 100%;
            background: var(--cgv-red);
            border-radius: 4px;
        }
        @media (max-width: 900px) {
            .two-col-grid { grid-template-columns: 1fr; }
        }
    </style>
</head>
<body class="cgv-body">

<%@ include file="../_sidebar.jsp" %>

<div class="cgv-main">

    <header class="cgv-header">
        <h1 class="cgv-header-title">Revenue &amp; Analytics (UC49)</h1>
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
                    <div class="rev-kpi-key">TỔNG DOANH THU ${selectedYear} (VND)</div>
                </div>
                <div class="rev-kpi">
                    <div class="rev-kpi-val amber">${not empty grandTickets ? grandTickets : '0'}</div>
                    <div class="rev-kpi-key">VÉ ĐÃ BÁN ${selectedYear}</div>
                </div>
                <div class="rev-kpi">
                    <div class="rev-kpi-val red">${not empty newCustomers ? newCustomers : '0'}</div>
                    <div class="rev-kpi-key">KHÁCH MỚI THÁNG NÀY</div>
                </div>
            </div>

            <%-- Year filter --%>
            <div style="display:flex; justify-content:flex-end; margin-bottom:8px;">
                <form method="get" action="${pageContext.request.contextPath}/manager/analytics"
                      style="display:flex; gap:10px; align-items:center;">
                    <label style="font-size:12px; font-weight:600; color:rgba(94,63,58,0.5);">Năm:</label>
                    <select class="cgv-select" style="height:36px; width:100px;" name="year">
                        <c:forEach begin="2023" end="2026" var="y">
                            <option value="${y}" ${selectedYear eq y ? 'selected' : ''}>${y}</option>
                        </c:forEach>
                    </select>
                    <input type="hidden" name="sortBy" value="${sortBy}">
                    <input type="hidden" name="dir" value="${sortDir}">
                    <button type="submit" class="btn--cgv-outline" style="height:36px;">Xem</button>
                </form>
            </div>

            <%-- Top movies + Payment method breakdown --%>
            <div class="two-col-grid">

                <%-- Top 5 movies --%>
                <div class="cgv-data-wrap">
                    <div class="cgv-data-toolbar">
                        <span class="section-heading" style="margin:0;">TOP 5 PHIM THEO DOANH THU ${selectedYear}</span>
                    </div>
                    <table class="cgv-dt">
                        <thead>
                            <tr>
                                <th>#</th>
                                <th>Tên phim</th>
                                <th>Vé bán</th>
                                <th>Doanh thu (VND)</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty topMovies}">
                                    <c:set var="maxRev" value="${topMovies[0].revenue}" />
                                    <c:forEach var="mv" items="${topMovies}" varStatus="s">
                                        <tr>
                                            <td style="font-weight:700; color:rgba(94,63,58,0.4);">${s.index + 1}</td>
                                            <td style="font-weight:600; max-width:180px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap;">
                                                ${mv.movieName}
                                            </td>
                                            <td>${mv.totalTickets}</td>
                                            <td>
                                                <span style="font-weight:500;">${mv.formattedRevenue}</span>
                                                <c:if test="${maxRev gt 0}">
                                                    <div class="bar-wrap">
                                                        <div class="bar-fill" style="width:${mv.revenue / maxRev * 100}%;"></div>
                                                    </div>
                                                </c:if>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr>
                                        <td colspan="4" style="text-align:center; padding:32px; color:rgba(94,63,58,0.4);">
                                            Không có dữ liệu phim năm ${selectedYear}.
                                        </td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>

                <%-- Revenue by payment method --%>
                <div class="cgv-data-wrap">
                    <div class="cgv-data-toolbar">
                        <span class="section-heading" style="margin:0;">DOANH THU THEO PHƯƠNG THỨC THANH TOÁN ${selectedYear}</span>
                    </div>
                    <table class="cgv-dt">
                        <thead>
                            <tr>
                                <th>Phương thức</th>
                                <th>Hóa đơn</th>
                                <th>Doanh thu (VND)</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty paymentStats}">
                                    <c:forEach var="ps" items="${paymentStats}">
                                        <tr>
                                            <td>
                                                <span class="cgv-pill" style="font-weight:600; text-transform:uppercase;">
                                                    ${ps.method}
                                                </span>
                                            </td>
                                            <td>${ps.totalInvoices}</td>
                                            <td style="font-weight:500;">${ps.formattedRevenue}</td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr>
                                        <td colspan="3" style="text-align:center; padding:32px; color:rgba(94,63,58,0.4);">
                                            Không có dữ liệu năm ${selectedYear}.
                                        </td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>

            </div>

            <%-- Monthly revenue table --%>
            <div class="section-heading">DOANH THU THEO THÁNG ${selectedYear}</div>
            <div class="cgv-data-wrap">
                <table class="cgv-dt">
                    <thead>
                        <tr>
                            <th>
                                Tháng
                                <c:set var="nextMonthDir" value="${sortBy eq 'month' && sortDir eq 'ASC' ? 'DESC' : 'ASC'}"/>
                                <a href="?year=${selectedYear}&sortBy=month&dir=${nextMonthDir}"
                                   class="sort-link ${sortBy eq 'month' ? 'active' : ''}">
                                    ${sortBy eq 'month' && sortDir eq 'ASC' ? '↑' : '↓'}
                                </a>
                            </th>
                            <th>Hóa đơn</th>
                            <th>Vé bán</th>
                            <th>
                                Doanh thu (VND)
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
                                <tr style="background:rgba(94,63,58,0.04); font-weight:700;">
                                    <td>TỔNG ${selectedYear}</td>
                                    <td>—</td>
                                    <td>${grandTickets}</td>
                                    <td style="color:var(--cgv-red);">${grandTotal}</td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="4" style="text-align:center; padding:48px; color:rgba(94,63,58,0.4);">
                                        Không có dữ liệu doanh thu năm ${selectedYear}.
                                    </td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>

        </div>

        <aside class="cgv-aside">
            <div class="cgv-stats-section">
                <div class="cgv-aside-heading">THÁNG NÀY</div>
                <div class="cgv-stats-group">
                    <div>
                        <div class="cgv-stat-num">${not empty monthRevenue ? monthRevenue : '0'}</div>
                        <div class="cgv-stat-key">DOANH THU (VND)</div>
                    </div>
                    <div>
                        <div class="cgv-stat-num amber">${not empty monthTickets ? monthTickets : '0'}</div>
                        <div class="cgv-stat-key">VÉ ĐÃ BÁN</div>
                    </div>
                    <div>
                        <div class="cgv-stat-num red">${not empty newCustomers ? '+' : ''}${not empty newCustomers ? newCustomers : '0'}</div>
                        <div class="cgv-stat-key">KHÁCH MỚI</div>
                    </div>
                </div>
            </div>
            <div class="cgv-aside-divider">
                <div class="cgv-aside-heading">SẮP XẾP</div>
                <div style="display:flex; flex-direction:column; gap:6px; margin-top:8px;">
                    <a href="?year=${selectedYear}&sortBy=month&dir=ASC"
                       class="btn--cgv-outline ${sortBy eq 'month' && sortDir eq 'ASC' ? 'active' : ''}"
                       style="text-align:center; font-size:12px;">Tháng ↑</a>
                    <a href="?year=${selectedYear}&sortBy=month&dir=DESC"
                       class="btn--cgv-outline ${sortBy eq 'month' && sortDir eq 'DESC' ? 'active' : ''}"
                       style="text-align:center; font-size:12px;">Tháng ↓</a>
                    <a href="?year=${selectedYear}&sortBy=revenue&dir=DESC"
                       class="btn--cgv-outline ${sortBy eq 'revenue' && sortDir eq 'DESC' ? 'active' : ''}"
                       style="text-align:center; font-size:12px;">Doanh thu ↓</a>
                    <a href="?year=${selectedYear}&sortBy=revenue&dir=ASC"
                       class="btn--cgv-outline ${sortBy eq 'revenue' && sortDir eq 'ASC' ? 'active' : ''}"
                       style="text-align:center; font-size:12px;">Doanh thu ↑</a>
                </div>
            </div>
        </aside>
    </div>
</div>
</body>
</html>
