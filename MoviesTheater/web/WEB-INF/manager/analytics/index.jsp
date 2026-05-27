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
        .rev-chart-box {
            background: #fff;
            border: 1px solid var(--cgv-border);
            border-radius: 12px;
            padding: 28px 24px;
            margin-bottom: 32px;
        }
        .rev-chart-title {
            font-family: var(--font-cgv-ui);
            font-weight: 700;
            font-size: 10px;
            letter-spacing: 2px;
            text-transform: uppercase;
            color: rgba(94,63,58,0.5);
            margin-bottom: 20px;
        }
        .bar-chart {
            display: flex;
            align-items: flex-end;
            gap: 12px;
            height: 160px;
        }
        .bar-col {
            display: flex;
            flex-direction: column;
            align-items: center;
            gap: 6px;
            flex: 1;
        }
        .bar {
            width: 100%;
            background: var(--cgv-red);
            border-radius: 4px 4px 0 0;
            min-height: 4px;
            opacity: 0.85;
            transition: opacity 0.15s;
        }
        .bar:hover { opacity: 1; }
        .bar-label {
            font-size: 10px;
            color: rgba(94,63,58,0.5);
            font-family: var(--font-cgv-ui);
            white-space: nowrap;
        }
        .rev-table-section { margin-top: 8px; }
    </style>
</head>
<body class="cgv-body">

<%@ include file="../_sidebar.jsp" %>

<div class="cgv-main">

    <header class="cgv-header">
        <h1 class="cgv-header-title">Revenue &amp; Analytics</h1>
        <div class="cgv-header-right">
            <div class="cgv-search-wrap">
                <svg class="cgv-search-icon" viewBox="0 0 24 24" fill="none"
                     stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="11" cy="11" r="8"/>
                    <line x1="21" y1="21" x2="16.65" y2="16.65"/>
                </svg>
                <input class="cgv-search" type="text" placeholder="Search...">
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

            <div class="cgv-toolbar">
                <div class="cgv-pills">
                    <a href="?period=today"  class="cgv-pill ${empty param.period || param.period eq 'today'  ? 'active' : ''}">Today</a>
                    <a href="?period=week"   class="cgv-pill ${param.period eq 'week'   ? 'active' : ''}">This Week</a>
                    <a href="?period=month"  class="cgv-pill ${param.period eq 'month'  ? 'active' : ''}">This Month</a>
                    <a href="?period=year"   class="cgv-pill ${param.period eq 'year'   ? 'active' : ''}">This Year</a>
                </div>
                <form method="get" style="display:flex;gap:10px;align-items:center;">
                    <input class="cgv-input" style="height:38px;width:140px;" type="date" name="from" value="${param.from}">
                    <span style="color:rgba(94,63,58,0.5);font-size:13px;">—</span>
                    <input class="cgv-input" style="height:38px;width:140px;" type="date" name="to" value="${param.to}">
                    <button type="submit" class="btn--cgv-outline">Apply</button>
                </form>
            </div>

            <div class="rev-grid">
                <div class="rev-kpi">
                    <div class="rev-kpi-val">${not empty totalRevenue ? totalRevenue : '142M'}</div>
                    <div class="rev-kpi-key">TOTAL REVENUE (VND)</div>
                </div>
                <div class="rev-kpi">
                    <div class="rev-kpi-val amber">${not empty ticketsSold ? ticketsSold : '3,480'}</div>
                    <div class="rev-kpi-key">TICKETS SOLD</div>
                </div>
                <div class="rev-kpi">
                    <div class="rev-kpi-val red">${not empty avgOccupancy ? avgOccupancy : '78%'}</div>
                    <div class="rev-kpi-key">AVG. OCCUPANCY</div>
                </div>
            </div>

            <div class="rev-chart-box">
                <div class="rev-chart-title">DAILY REVENUE (LAST 7 DAYS)</div>
                <div class="bar-chart">
                    <c:choose>
                        <c:when test="${not empty dailyRevenue}">
                            <c:forEach var="d" items="${dailyRevenue}">
                                <div class="bar-col">
                                    <div class="bar" style="height:${d.heightPct}%;"></div>
                                    <span class="bar-label">${d.label}</span>
                                </div>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <%-- Placeholder bars --%>
                            <div class="bar-col"><div class="bar" style="height:55%;"></div><span class="bar-label">Mon</span></div>
                            <div class="bar-col"><div class="bar" style="height:72%;"></div><span class="bar-label">Tue</span></div>
                            <div class="bar-col"><div class="bar" style="height:48%;"></div><span class="bar-label">Wed</span></div>
                            <div class="bar-col"><div class="bar" style="height:90%;"></div><span class="bar-label">Thu</span></div>
                            <div class="bar-col"><div class="bar" style="height:100%;"></div><span class="bar-label">Fri</span></div>
                            <div class="bar-col"><div class="bar" style="height:95%;"></div><span class="bar-label">Sat</span></div>
                            <div class="bar-col"><div class="bar" style="height:85%;"></div><span class="bar-label">Sun</span></div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <div class="rev-table-section">
                <div class="cgv-data-wrap">
                    <div class="cgv-data-toolbar">
                        <span style="font-family:var(--font-cgv-ui);font-size:10px;font-weight:700;letter-spacing:2px;text-transform:uppercase;color:rgba(94,63,58,0.5);">
                            TOP MOVIES BY REVENUE
                        </span>
                    </div>
                    <table class="cgv-dt">
                        <thead>
                            <tr>
                                <th>Rank</th>
                                <th>Movie</th>
                                <th>Genre</th>
                                <th>Tickets Sold</th>
                                <th>Revenue (VND)</th>
                                <th>Occupancy</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty topMovies}">
                                    <c:forEach var="m" items="${topMovies}" varStatus="st">
                                        <tr>
                                            <td style="font-weight:700;color:var(--cgv-red);">#${st.index + 1}</td>
                                            <td style="font-weight:500;">${m.title}</td>
                                            <td style="color:rgba(94,63,58,0.6);">${m.genre}</td>
                                            <td>${m.ticketsSold}</td>
                                            <td style="font-weight:500;">${m.revenue}</td>
                                            <td>
                                                <span class="cgv-badge ${m.occupancy >= 80 ? 'active' : m.occupancy >= 50 ? 'upcoming' : 'inactive'}">
                                                    ${m.occupancy}%
                                                </span>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr>
                                        <td style="font-weight:700;color:var(--cgv-red);">#1</td>
                                        <td style="font-weight:500;">Interstellar Drift</td>
                                        <td style="color:rgba(94,63,58,0.6);">Sci-Fi</td>
                                        <td>1,240</td>
                                        <td style="font-weight:500;">62,000,000</td>
                                        <td><span class="cgv-badge active">98%</span></td>
                                    </tr>
                                    <tr>
                                        <td style="font-weight:700;color:var(--cgv-red);">#2</td>
                                        <td style="font-weight:500;">Aetheria Bound</td>
                                        <td style="color:rgba(94,63,58,0.6);">Fantasy</td>
                                        <td>980</td>
                                        <td style="font-weight:500;">49,000,000</td>
                                        <td><span class="cgv-badge active">82%</span></td>
                                    </tr>
                                    <tr>
                                        <td style="font-weight:700;color:var(--cgv-red);">#3</td>
                                        <td style="font-weight:500;">Neon Echoes</td>
                                        <td style="color:rgba(94,63,58,0.6);">Action/Crime</td>
                                        <td>760</td>
                                        <td style="font-weight:500;">38,000,000</td>
                                        <td><span class="cgv-badge upcoming">64%</span></td>
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
                        <div class="cgv-stat-num">${not empty monthRevenue ? monthRevenue : '520M'}</div>
                        <div class="cgv-stat-key">REVENUE (VND)</div>
                    </div>
                    <div>
                        <div class="cgv-stat-num amber">${not empty monthTickets ? monthTickets : '12,400'}</div>
                        <div class="cgv-stat-key">TICKETS SOLD</div>
                    </div>
                    <div>
                        <div class="cgv-stat-num red">${not empty newCustomers ? newCustomers : '+84'}</div>
                        <div class="cgv-stat-key">NEW CUSTOMERS</div>
                    </div>
                </div>
            </div>
            <div class="cgv-aside-divider">
                <div class="cgv-aside-heading">TOP ROOM</div>
                <div class="cgv-events-list">
                    <c:choose>
                        <c:when test="${not empty topRooms}">
                            <c:forEach var="r" items="${topRooms}">
                                <div>
                                    <div class="cgv-event-title">${r.roomName}</div>
                                    <div class="cgv-event-desc">${r.occupancy}% occupancy · ${r.screenings} screenings</div>
                                </div>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <div>
                                <div class="cgv-event-title">Room 1 — IMAX</div>
                                <div class="cgv-event-desc">98% occupancy · 45 screenings</div>
                            </div>
                            <div>
                                <div class="cgv-event-title">Room 2 — Standard</div>
                                <div class="cgv-event-desc">76% occupancy · 62 screenings</div>
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
