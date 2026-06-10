<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% request.setAttribute("activeNav", "rooms"); %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Room Management — CGV Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
</head>
<body class="cgv-body">

<%@ include file="WEB-INF/manager/_sidebar.jsp" %>

<div class="cgv-main">

    <header class="cgv-header">
        <h1 class="cgv-header-title">Phòng chiếu</h1>
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

            <c:if test="${param.error eq 'capacity_invalid'}">
                <div class="cgv-alert cgv-alert-danger">Capacity must be greater than 0.</div>
            </c:if>

            <c:if test="${param.error eq 'room_number_exists'}">
                <div class="cgv-alert cgv-alert-danger">Room number already exists. Please use a different room number.</div>
            </c:if>

            <div class="cgv-toolbar">
                <div class="cgv-pills">
                    <a href="RoomServlet" class="cgv-pill active">All Rooms</a>
                </div>
            </div>

            <div class="cgv-data-wrap">
                <table class="cgv-dt">
                    <thead>
                        <tr>
                            <th>#</th>
                            <th>Room Number</th>
                            <th>Type</th>
                            <th>Capacity</th>
                            <th>Status</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty roomList}">
                                <c:forEach var="room" items="${roomList}" varStatus="st">
                                    <tr>
                                        <td style="color:rgba(94,63,58,0.5);font-size:12px;">${st.index + 1}</td>
                                        <td style="font-weight:600;">${room.roomNumber}</td>
                                        <td>
                                            <span class="cgv-badge inactive">${room.roomType}</span>
                                        </td>
                                        <td>${room.capacity}</td>
                                        <td>
                                            <span class="cgv-badge ${room.active ? 'active' : 'inactive'}">
                                                ${room.active ? 'Active' : 'Inactive'}
                                            </span>
                                        </td>
                                        <td>
                                            <div style="display:flex;gap:8px;">
                                                <a href="RoomServlet?action=edit&id=${room.roomId}&page=${currentPage}"
                                                   class="btn--cgv-outline">Edit</a>
                                                <a href="RoomServlet?action=delete&id=${room.roomId}&page=${currentPage}"
                                                   class="btn--cgv-outline"
                                                   style="color:var(--cgv-red);border-color:var(--cgv-red);"
                                                   onclick="return confirm('Deactivate room ${room.roomNumber}?')">
                                                    Deactivate
                                                </a>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="6" style="text-align:center;padding:48px;color:rgba(94,63,58,0.4);">
                                        No rooms found.
                                    </td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>

                <div class="cgv-pager">
                    <span>Page ${currentPage} of ${totalPages}</span>
                    <div class="cgv-pager-pages">
                        <c:forEach begin="1" end="${totalPages}" var="p">
                            <button class="cgv-pager-btn ${p eq currentPage ? 'active' : ''}"
                                    onclick="location.href='RoomServlet?page=${p}'">${p}</button>
                        </c:forEach>
                    </div>
                </div>
            </div>
        </div>

        <aside class="cgv-aside">
            <div class="cgv-stats-section">
                <div class="cgv-aside-heading">ADD NEW ROOM</div>
                <form action="${pageContext.request.contextPath}/RoomServlet?page=${currentPage}" method="post">
                    <input type="hidden" name="action" value="add">
                    <div class="cgv-field">
                        <label class="cgv-label">Room Number</label>
                        <input class="cgv-input" type="text" name="roomNumber"
                               placeholder="e.g. R01" required>
                    </div>
                    <div class="cgv-field">
                        <label class="cgv-label">Room Type</label>
                        <select class="cgv-select" name="roomType">
                            <option value="2D">2D</option>
                            <option value="3D">3D</option>
                            <option value="IMAX">IMAX</option>
                            <option value="4DX">4DX</option>
                            <option value="VIP">VIP</option>
                        </select>
                    </div>
                    <div class="cgv-field">
                        <label class="cgv-label">Capacity</label>
                        <input class="cgv-input" type="number" name="capacity"
                               min="1" placeholder="e.g. 120" required>
                    </div>
                    <button type="submit" class="btn--cgv" style="width:100%;margin-top:8px;">
                        Add Room
                    </button>
                </form>
            </div>

            <div class="cgv-aside-divider">
                <div class="cgv-aside-heading">SUMMARY</div>
                <div class="cgv-stats-group">
                    <div>
                        <div class="cgv-stat-num">${not empty roomList ? roomList.size() : '0'}</div>
                        <div class="cgv-stat-key">ON THIS PAGE</div>
                    </div>
                    <div>
                        <div class="cgv-stat-num amber">${not empty totalPages ? totalPages : '1'}</div>
                        <div class="cgv-stat-key">TOTAL PAGES</div>
                    </div>
                </div>
            </div>
        </aside>

    </div>
</div>
</body>
</html>