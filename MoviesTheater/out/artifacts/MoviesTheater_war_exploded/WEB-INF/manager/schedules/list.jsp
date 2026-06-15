<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% request.setAttribute("activeNav", "schedules"); %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Showtime Schedules — CGV Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
</head>
<body class="cgv-body">

<%@ include file="../_sidebar.jsp" %>

<div class="cgv-main">

    <header class="cgv-header">
        <h1 class="cgv-header-title">Showtime Schedule</h1>
        <div class="cgv-header-right">
            <div class="cgv-search-wrap">
                <svg class="cgv-search-icon" viewBox="0 0 24 24" fill="none"
                     stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="11" cy="11" r="8"/>
                    <line x1="21" y1="21" x2="16.65" y2="16.65"/>
                </svg>
                <input class="cgv-search" type="text" placeholder="Search schedules..." value="${param.q}">
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

            <div class="cgv-toolbar">
                <div class="cgv-pills">
                    <a href="?filter=today"    class="cgv-pill ${empty param.filter || param.filter eq 'today'   ? 'active' : ''}">Today</a>
                    <a href="?filter=week"     class="cgv-pill ${param.filter eq 'week'    ? 'active' : ''}">This Week</a>
                    <a href="?filter=upcoming" class="cgv-pill ${param.filter eq 'upcoming'? 'active' : ''}">Upcoming</a>
                    <a href="?filter=all"      class="cgv-pill ${param.filter eq 'all'     ? 'active' : ''}">All</a>
                </div>
                <a href="?action=add" class="btn--cgv">
                    <svg width="10" height="10" viewBox="0 0 12 12" fill="none"
                         stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
                        <line x1="6" y1="1" x2="6" y2="11"/><line x1="1" y1="6" x2="11" y2="6"/>
                    </svg>
                    Add Showtime
                </a>
            </div>

            <div class="cgv-data-wrap">
                <div class="cgv-data-toolbar">
                    <form method="get" style="display:flex;gap:10px;align-items:center;flex-wrap:wrap;">
                        <input class="cgv-input" style="max-width:200px;height:38px;"
                               type="date" name="date" value="${param.date}">
                        <select class="cgv-select" style="max-width:200px;height:38px;" name="movieId">
                            <option value="">All Movies</option>
                            <c:forEach var="m" items="${movieList}">
                                <option value="${m.movieId}" ${param.movieId eq m.movieId ? 'selected' : ''}>${m.title}</option>
                            </c:forEach>
                        </select>
                        <select class="cgv-select" style="max-width:160px;height:38px;" name="roomId">
                            <option value="">All Rooms</option>
                            <c:forEach var="r" items="${roomList}">
                                <option value="${r.roomId}" ${param.roomId eq r.roomId ? 'selected' : ''}>${r.roomName}</option>
                            </c:forEach>
                        </select>
                        <button type="submit" class="btn--cgv-outline">Filter</button>
                    </form>
                </div>

                <table class="cgv-dt">
                    <thead>
                        <tr>
                            <th>#</th>
                            <th>Movie</th>
                            <th>Room</th>
                            <th>Date</th>
                            <th>Start Time</th>
                            <th>End Time</th>
                            <th>Seats Sold</th>
                            <th>Status</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty scheduleList}">
                                <c:forEach var="s" items="${scheduleList}" varStatus="st">
                                    <tr>
                                        <td style="color:rgba(94,63,58,0.5);font-size:12px;">${st.index + 1}</td>
                                        <td style="font-weight:500;">${s.movieTitle}</td>
                                        <td>${s.roomName}</td>
                                        <td style="font-size:13px;">${s.showDate}</td>
                                        <td style="font-weight:500;">${s.startTime}</td>
                                        <td style="color:rgba(94,63,58,0.6);font-size:13px;">${s.endTime}</td>
                                        <td>${s.seatsSold} / ${s.totalSeats}</td>
                                        <td>
                                            <span class="cgv-badge ${s.status eq 'ACTIVE' ? 'active' : s.status eq 'CANCELLED' ? 'danger' : 'inactive'}">
                                                ${s.status}
                                            </span>
                                        </td>
                                        <td>
                                            <div style="display:flex;gap:8px;">
                                                <a href="?action=edit&id=${s.scheduleId}" class="btn--cgv-outline">Edit</a>
                                                <form method="post" style="display:inline;">
                                                    <input type="hidden" name="action" value="cancel">
                                                    <input type="hidden" name="id" value="${s.scheduleId}">
                                                    <button type="submit" class="btn--cgv-outline"
                                                            onclick="return confirm('Cancel this showtime?')">Cancel</button>
                                                </form>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr><td colspan="9" style="text-align:center;padding:48px;color:rgba(94,63,58,0.4);">No showtimes found.</td></tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>

                <div class="cgv-pager">
                    <span>Showing ${not empty scheduleList ? scheduleList.size() : 0} of ${not empty totalSchedules ? totalSchedules : 0} showtimes</span>
                    <div class="cgv-pager-pages">
                        <c:forEach begin="1" end="${not empty totalPages ? totalPages : 1}" var="p">
                            <button class="cgv-pager-btn ${p eq currentPage ? 'active' : ''}"
                                    onclick="location.href='?page=${p}&movieId=${param.movieId}&roomId=${param.roomId}&date=${param.date}'">${p}</button>
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
                        <div class="cgv-stat-num">${not empty todayCount ? todayCount : '8'}</div>
                        <div class="cgv-stat-key">SHOWTIMES TODAY</div>
                    </div>
                    <div>
                        <div class="cgv-stat-num amber">${not empty weekCount ? weekCount : '42'}</div>
                        <div class="cgv-stat-key">THIS WEEK</div>
                    </div>
                    <div>
                        <div class="cgv-stat-num red">${not empty cancelledCount ? cancelledCount : '2'}</div>
                        <div class="cgv-stat-key">CANCELLED</div>
                    </div>
                </div>
            </div>
            <div class="cgv-aside-divider">
                <div class="cgv-aside-heading">UPCOMING</div>
                <div class="cgv-events-list">
                    <c:choose>
                        <c:when test="${not empty upcomingSchedules}">
                            <c:forEach var="s" items="${upcomingSchedules}">
                                <div>
                                    <div class="cgv-event-title">${s.movieTitle}</div>
                                    <div class="cgv-event-desc">${s.showDate} — ${s.startTime} · ${s.roomName}</div>
                                </div>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <div>
                                <div class="cgv-event-title">Interstellar Drift</div>
                                <div class="cgv-event-desc">Today — 19:30 · Room 1</div>
                            </div>
                            <div>
                                <div class="cgv-event-title">Neon Echoes</div>
                                <div class="cgv-event-desc">Today — 21:00 · Room 2</div>
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
