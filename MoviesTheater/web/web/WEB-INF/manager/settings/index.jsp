<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% request.setAttribute("activeNav", "settings"); %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>System Settings — CGV Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
    <style>
        .settings-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 32px; }
        .settings-section {
            background: #fff;
            border: 1px solid var(--cgv-border);
            border-radius: 12px;
            padding: 28px;
        }
        .settings-section-title {
            font-family: var(--font-cgv-ui);
            font-weight: 700;
            font-size: 10px;
            letter-spacing: 2px;
            text-transform: uppercase;
            color: rgba(94,63,58,0.5);
            margin-bottom: 24px;
        }
        .log-table-wrap { margin-top: 32px; }
    </style>
</head>
<body class="cgv-body">

<%@ include file="../_sidebar.jsp" %>

<div class="cgv-main">

    <header class="cgv-header">
        <h1 class="cgv-header-title">System Settings</h1>
        <div class="cgv-header-right">
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

            <div class="settings-grid">
                <div class="settings-section">
                    <div class="settings-section-title">CINEMA INFORMATION</div>
                    <form method="post">
                        <input type="hidden" name="action" value="saveCinema">
                        <div class="cgv-field">
                            <label class="cgv-label">Cinema Name</label>
                            <input class="cgv-input" type="text" name="cinemaName"
                                   value="${not empty settings.cinemaName ? settings.cinemaName : 'CGV Cinema'}">
                        </div>
                        <div class="cgv-field">
                            <label class="cgv-label">Address</label>
                            <input class="cgv-input" type="text" name="address"
                                   value="${settings.address}">
                        </div>
                        <div class="cgv-field">
                            <label class="cgv-label">Phone</label>
                            <input class="cgv-input" type="text" name="phone"
                                   value="${settings.phone}">
                        </div>
                        <div class="cgv-field">
                            <label class="cgv-label">Email</label>
                            <input class="cgv-input" type="email" name="email"
                                   value="${settings.email}">
                        </div>
                        <button type="submit" class="btn--cgv" style="margin-top:8px;">Save Changes</button>
                    </form>
                </div>

                <div class="settings-section">
                    <div class="settings-section-title">BOOKING RULES</div>
                    <form method="post">
                        <input type="hidden" name="action" value="saveRules">
                        <div class="cgv-field">
                            <label class="cgv-label">Max Seats per Booking</label>
                            <input class="cgv-input" type="number" name="maxSeats" min="1" max="20"
                                   value="${not empty settings.maxSeats ? settings.maxSeats : '8'}">
                        </div>
                        <div class="cgv-field">
                            <label class="cgv-label">Cancellation Deadline (hours before showtime)</label>
                            <input class="cgv-input" type="number" name="cancelHours" min="0"
                                   value="${not empty settings.cancelHours ? settings.cancelHours : '2'}">
                        </div>
                        <div class="cgv-field">
                            <label class="cgv-label">Ticket Price Base (VND)</label>
                            <input class="cgv-input" type="number" name="basePrice" min="0" step="1000"
                                   value="${not empty settings.basePrice ? settings.basePrice : '90000'}">
                        </div>
                        <div class="cgv-field">
                            <label class="cgv-label">VIP Seat Multiplier</label>
                            <input class="cgv-input" type="number" name="vipMultiplier" min="1" step="0.1"
                                   value="${not empty settings.vipMultiplier ? settings.vipMultiplier : '1.5'}">
                        </div>
                        <button type="submit" class="btn--cgv" style="margin-top:8px;">Save Rules</button>
                    </form>
                </div>
            </div>

            <div class="log-table-wrap">
                <div class="cgv-data-wrap">
                    <div class="cgv-data-toolbar" style="justify-content:space-between;">
                        <span style="font-family:var(--font-cgv-ui);font-size:10px;font-weight:700;letter-spacing:2px;text-transform:uppercase;color:rgba(94,63,58,0.5);">
                            SYSTEM LOGS
                        </span>
                        <div style="display:flex;gap:10px;">
                            <select class="cgv-select" style="height:36px;width:140px;" name="logLevel">
                                <option value="">All Levels</option>
                                <option>INFO</option>
                                <option>WARN</option>
                                <option>ERROR</option>
                            </select>
                        </div>
                    </div>
                    <table class="cgv-dt">
                        <thead>
                            <tr>
                                <th>Timestamp</th>
                                <th>Level</th>
                                <th>Actor</th>
                                <th>Action</th>
                                <th>Detail</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty systemLogs}">
                                    <c:forEach var="log" items="${systemLogs}">
                                        <tr>
                                            <td style="font-size:12px;color:rgba(94,63,58,0.6);white-space:nowrap;">${log.timestamp}</td>
                                            <td>
                                                <span class="cgv-badge ${log.level eq 'ERROR' ? 'danger' : log.level eq 'WARN' ? 'upcoming' : 'inactive'}">
                                                    ${log.level}
                                                </span>
                                            </td>
                                            <td>${log.actor}</td>
                                            <td style="font-weight:500;">${log.action}</td>
                                            <td style="color:rgba(94,63,58,0.7);font-size:13px;">${log.detail}</td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr>
                                        <td style="color:rgba(94,63,58,0.5);font-size:12px;">2026-05-26 09:14</td>
                                        <td><span class="cgv-badge inactive">INFO</span></td>
                                        <td>admin@cgv.vn</td>
                                        <td style="font-weight:500;">Login</td>
                                        <td style="color:rgba(94,63,58,0.7);font-size:13px;">Admin logged in from 192.168.1.1</td>
                                    </tr>
                                    <tr>
                                        <td style="color:rgba(94,63,58,0.5);font-size:12px;">2026-05-26 08:55</td>
                                        <td><span class="cgv-badge upcoming">WARN</span></td>
                                        <td>system</td>
                                        <td style="font-weight:500;">DB Connection</td>
                                        <td style="color:rgba(94,63,58,0.7);font-size:13px;">Slow query detected (&gt; 2s)</td>
                                    </tr>
                                    <tr>
                                        <td style="color:rgba(94,63,58,0.5);font-size:12px;">2026-05-26 08:30</td>
                                        <td><span class="cgv-badge inactive">INFO</span></td>
                                        <td>manager@cgv.vn</td>
                                        <td style="font-weight:500;">Movie Added</td>
                                        <td style="color:rgba(94,63,58,0.7);font-size:13px;">"Neon Echoes" added to catalog</td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                    <div class="cgv-pager">
                        <span>Showing ${not empty systemLogs ? systemLogs.size() : 3} log entries</span>
                        <div class="cgv-pager-pages">
                            <c:forEach begin="1" end="${not empty totalLogPages ? totalLogPages : 1}" var="p">
                                <button class="cgv-pager-btn ${p eq currentPage ? 'active' : ''}"
                                        onclick="location.href='?page=${p}'">${p}</button>
                            </c:forEach>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <aside class="cgv-aside">
            <div class="cgv-stats-section">
                <div class="cgv-aside-heading">SYSTEM STATUS</div>
                <div class="cgv-stats-group">
                    <div>
                        <div class="cgv-stat-num" style="font-size:20px;">
                            <span class="cgv-badge active" style="font-size:14px;">ONLINE</span>
                        </div>
                        <div class="cgv-stat-key">SERVICE STATUS</div>
                    </div>
                    <div>
                        <div class="cgv-stat-num amber">${not empty warnCount ? warnCount : '2'}</div>
                        <div class="cgv-stat-key">WARNINGS TODAY</div>
                    </div>
                    <div>
                        <div class="cgv-stat-num red">${not empty errorCount ? errorCount : '0'}</div>
                        <div class="cgv-stat-key">ERRORS TODAY</div>
                    </div>
                </div>
            </div>
            <div class="cgv-aside-divider">
                <div class="cgv-aside-heading">QUICK ACTIONS</div>
                <div class="cgv-events-list">
                    <div>
                        <div class="cgv-event-title">Clear Cache</div>
                        <div class="cgv-event-desc">
                            <form method="post" style="margin-top:6px;">
                                <input type="hidden" name="action" value="clearCache">
                                <button type="submit" class="btn--cgv-outline" style="width:100%;">
                                    Clear Application Cache
                                </button>
                            </form>
                        </div>
                    </div>
                    <div>
                        <div class="cgv-event-title">Export Logs</div>
                        <div class="cgv-event-desc">
                            <a href="?action=exportLogs" class="btn--cgv-outline" style="display:block;margin-top:6px;text-align:center;">
                                Download Log File
                            </a>
                        </div>
                    </div>
                </div>
            </div>
        </aside>
    </div>
</div>
</body>
</html>
