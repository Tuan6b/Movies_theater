<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% request.setAttribute("activeNav", "dashboard"); %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Ticket Check-in — CGV Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
    <style>
        .checkin-lookup {
            background: #fff;
            border: 1px solid var(--cgv-border);
            border-radius: 12px;
            padding: 32px;
            margin-bottom: 32px;
        }
        .checkin-lookup-title {
            font-family: var(--font-cgv-ui);
            font-weight: 700;
            font-size: 10px;
            letter-spacing: 2px;
            text-transform: uppercase;
            color: rgba(94,63,58,0.5);
            margin-bottom: 20px;
        }
        .checkin-search-row {
            display: flex;
            gap: 12px;
            align-items: center;
            flex-wrap: wrap;
        }
        .checkin-result {
            margin-top: 24px;
            background: #faf6f5;
            border: 1px solid var(--cgv-border);
            border-radius: 10px;
            padding: 20px 24px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 16px;
            flex-wrap: wrap;
        }
        .checkin-result-info { display: flex; flex-direction: column; gap: 4px; }
        .checkin-result-name { font-family: var(--font-cgv-display); font-size: 20px; color: var(--cgv-dark); }
        .checkin-result-meta { font-size: 13px; color: rgba(94,63,58,0.6); }
    </style>
</head>
<body class="cgv-body">

<%@ include file="../_sidebar.jsp" %>

<div class="cgv-main">

    <header class="cgv-header">
        <h1 class="cgv-header-title">Ticket Check-in</h1>
        <div class="cgv-header-right">
            <div class="cgv-search-wrap">
                <svg class="cgv-search-icon" viewBox="0 0 24 24" fill="none"
                     stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="11" cy="11" r="8"/>
                    <line x1="21" y1="21" x2="16.65" y2="16.65"/>
                </svg>
                <input class="cgv-search" type="text" placeholder="Search booking ID..." value="${param.q}">
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
        <div class="cgv-table-wrap">

            <c:if test="${not empty flashSuccess}">
                <div class="cgv-alert cgv-alert-success">${flashSuccess}</div>
            </c:if>
            <c:if test="${not empty flashError}">
                <div class="cgv-alert cgv-alert-danger">${flashError}</div>
            </c:if>

            <div class="checkin-lookup">
                <div class="checkin-lookup-title">LOOK UP BOOKING</div>
                <form method="get" class="checkin-search-row">
                    <input class="cgv-input" style="flex:1;max-width:320px;" type="text"
                           name="code" placeholder="Enter booking code or ticket QR…"
                           value="${param.code}">
                    <button type="submit" class="btn--cgv">Verify Ticket</button>
                </form>

                <c:if test="${not empty booking}">
                    <div class="checkin-result">
                        <div class="checkin-result-info">
                            <div class="checkin-result-name">${booking.movieTitle}</div>
                            <div class="checkin-result-meta">
                                ${booking.showDate} · ${booking.startTime} · ${booking.roomName}
                            </div>
                            <div class="checkin-result-meta" style="margin-top:4px;">
                                Customer: <strong>${booking.customerName}</strong> ·
                                Seat(s): <strong>${booking.seats}</strong>
                            </div>
                        </div>
                        <div style="display:flex;gap:12px;align-items:center;">
                            <span class="cgv-badge ${booking.checkedIn ? 'active' : 'upcoming'}">
                                ${booking.checkedIn ? 'CHECKED IN' : 'PENDING'}
                            </span>
                            <c:if test="${not booking.checkedIn}">
                                <form method="post">
                                    <input type="hidden" name="action" value="checkin">
                                    <input type="hidden" name="bookingId" value="${booking.bookingId}">
                                    <button type="submit" class="btn--cgv">Confirm Check-in</button>
                                </form>
                            </c:if>
                        </div>
                    </div>
                </c:if>
            </div>

            <div class="cgv-toolbar">
                <div class="cgv-pills">
                    <a href="?filter=today"   class="cgv-pill ${empty param.filter || param.filter eq 'today'   ? 'active' : ''}">Today's Bookings</a>
                    <a href="?filter=pending" class="cgv-pill ${param.filter eq 'pending' ? 'active' : ''}">Pending</a>
                    <a href="?filter=checked" class="cgv-pill ${param.filter eq 'checked' ? 'active' : ''}">Checked In</a>
                </div>
            </div>

            <div class="cgv-data-wrap">
                <table class="cgv-dt">
                    <thead>
                        <tr>
                            <th>Booking Code</th>
                            <th>Customer</th>
                            <th>Movie</th>
                            <th>Showtime</th>
                            <th>Seats</th>
                            <th>Amount</th>
                            <th>Status</th>
                            <th>Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty bookingList}">
                                <c:forEach var="b" items="${bookingList}">
                                    <tr>
                                        <td>
                                            <code style="font-family:monospace;background:#f3f3f3;padding:2px 8px;border-radius:4px;font-size:12px;letter-spacing:1px;">
                                                ${b.code}
                                            </code>
                                        </td>
                                        <td style="font-weight:500;">${b.customerName}</td>
                                        <td>${b.movieTitle}</td>
                                        <td style="font-size:13px;white-space:nowrap;">${b.showDate} ${b.startTime}</td>
                                        <td>${b.seats}</td>
                                        <td style="font-weight:500;">${b.totalAmount} VND</td>
                                        <td>
                                            <span class="cgv-badge ${b.checkedIn ? 'active' : 'upcoming'}">
                                                ${b.checkedIn ? 'Checked In' : 'Pending'}
                                            </span>
                                        </td>
                                        <td>
                                            <c:if test="${not b.checkedIn}">
                                                <form method="post">
                                                    <input type="hidden" name="action" value="checkin">
                                                    <input type="hidden" name="bookingId" value="${b.bookingId}">
                                                    <button type="submit" class="btn--cgv-outline">Check In</button>
                                                </form>
                                            </c:if>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr><td colspan="8" style="text-align:center;padding:48px;color:rgba(94,63,58,0.4);">No bookings for today.</td></tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
                <div class="cgv-pager">
                    <span>Showing ${not empty bookingList ? bookingList.size() : 0} of ${not empty totalBookings ? totalBookings : 0} bookings</span>
                    <div class="cgv-pager-pages">
                        <c:forEach begin="1" end="${not empty totalPages ? totalPages : 1}" var="p">
                            <button class="cgv-pager-btn ${p eq currentPage ? 'active' : ''}"
                                    onclick="location.href='?page=${p}&filter=${param.filter}'">${p}</button>
                        </c:forEach>
                    </div>
                </div>
            </div>
        </div>

        <aside class="cgv-aside">
            <div class="cgv-stats-section">
                <div class="cgv-aside-heading">TODAY</div>
                <div class="cgv-stats-group">
                    <div>
                        <div class="cgv-stat-num">${not empty todayTotal ? todayTotal : '124'}</div>
                        <div class="cgv-stat-key">TOTAL BOOKINGS</div>
                    </div>
                    <div>
                        <div class="cgv-stat-num amber">${not empty checkedInCount ? checkedInCount : '86'}</div>
                        <div class="cgv-stat-key">CHECKED IN</div>
                    </div>
                    <div>
                        <div class="cgv-stat-num red">${not empty pendingCount ? pendingCount : '38'}</div>
                        <div class="cgv-stat-key">AWAITING CHECK-IN</div>
                    </div>
                </div>
            </div>
            <div class="cgv-aside-divider">
                <div class="cgv-aside-heading">RECENT CHECK-INS</div>
                <div class="cgv-events-list">
                    <c:choose>
                        <c:when test="${not empty recentCheckins}">
                            <c:forEach var="c" items="${recentCheckins}">
                                <div>
                                    <div class="cgv-event-title">${c.customerName}</div>
                                    <div class="cgv-event-desc">${c.movieTitle} · ${c.checkinTime}</div>
                                </div>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <div>
                                <div class="cgv-event-title">Nguyen Van A</div>
                                <div class="cgv-event-desc">Interstellar Drift · 19:28</div>
                            </div>
                            <div>
                                <div class="cgv-event-title">Tran Thi B</div>
                                <div class="cgv-event-desc">Aetheria Bound · 19:25</div>
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
