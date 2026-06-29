<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% request.setAttribute("activeNav", "schedules"); %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Add Schedule — CGV Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
    <style>
        .room-card {
            border:1px solid var(--cgv-border);
            border-radius:10px;
            padding:12px 16px;
            margin-bottom:12px;
            background:#fafafa;
            transition:background 0.15s;
        }
        .room-card.checked {
            background:#fff;
            border-color:var(--cgv-red);
        }
        .room-times {
            margin-top:10px;
            padding-left:32px;
        }
        .time-row {
            display:flex;
            gap:8px;
            margin-bottom:8px;
            align-items:center;
        }
        .time-row input {
            flex:1;
        }
    </style>
</head>
<body class="cgv-body">

<%@ include file="WEB-INF/manager/_sidebar.jsp" %>

<div class="cgv-main">

    <header class="cgv-header">
        <h1 class="cgv-header-title">Add Schedule</h1>
        <div class="cgv-header-right">
            <div class="cgv-header-actions">
                <a href="${pageContext.request.contextPath}/ScheduleController?movieId=${movie.movieId}"
                   class="btn--cgv-outline" style="margin-right:8px;">
                    ← Back to Schedules
                </a>
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
        <div class="cgv-list-wrap" style="max-width:720px;">

            <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:32px;">
                <div style="font-family:var(--font-cgv-ui);font-size:10px;font-weight:700;letter-spacing:2px;text-transform:uppercase;color:rgba(94,63,58,0.5);margin-bottom:24px;">
                    SCHEDULE DETAILS
                </div>

                <c:if test="${empty movie}">
                    <div style="background:#f8d7da;color:#721c24;padding:12px 16px;border-radius:8px;margin-bottom:16px;">No movie selected. Please go back and choose a movie.</div>
                    <a href="ScheduleController" class="btn--cgv">← Back to Schedules</a>
                </c:if>

                <c:if test="${not empty movie}">
                <c:set var="totalMinutes" value="${movie.duration + 15}" />
                <form action="ScheduleController" method="post">
                    <input type="hidden" name="action" value="add">
                    <input type="hidden" name="movieId" value="${movie.movieId}">
                    <input type="hidden" name="status" value="Scheduled">

                    <c:if test="${empty rooms}">
                        <div style="background:#f8d7da;color:#721c24;padding:12px 16px;border-radius:8px;margin-bottom:16px;">
                            No rooms available.
                        </div>
                    </c:if>

                    <div class="cgv-field">
                        <label class="cgv-label">Movie</label>
                        <div style="padding:8px 0;font-weight:600;">
                            ${movie.movieName}
                            <span style="font-weight:400;font-size:13px;color:rgba(94,63,58,0.6);">
                                (${movie.duration} min + 15 min cleanup = ${totalMinutes} min per show)
                            </span>
                        </div>
                    </div>

                    <div class="cgv-field">
                        <label class="cgv-label">Base Ticket Price</label>
                        <input class="cgv-input" type="number" name="baseTicketPrice"
                               step="0.01" min="0" required>
                    </div>

                    <div class="cgv-field">
                        <label class="cgv-label">Show Date</label>
                        <input class="cgv-input" type="date" name="showDate" required>
                    </div>

                    <!-- Rooms — each with its own time slots -->
                    <div class="cgv-field">
                        <label class="cgv-label">Rooms & Start Times</label>
                        <c:forEach var="r" items="${rooms}" varStatus="loop">
                            <div class="room-card" id="card_${loop.index}">
                                <label style="display:flex;align-items:center;gap:8px;cursor:pointer;">
                                    <input type="checkbox" name="roomIds" value="${r.roomId}"
                                           data-room-name="${r.roomNumber}"
                                           onchange="toggleRoom(${loop.index}, this.checked)">
                                    <span style="font-weight:600;">${r.roomNumber}</span>
                                </label>
                                <div class="room-times" id="times_${loop.index}" style="display:none;">
                                    <div id="slots_${loop.index}"></div>
                                    <button type="button" class="btn--cgv-outline"
                                            onclick="addTime(${loop.index}, ${r.roomId})"
                                            style="font-size:13px;padding:6px 14px;margin-top:4px;">
                                        + Add showtime
                                    </button>
                                </div>
                            </div>
                        </c:forEach>
                    </div>

                    <div style="display:flex;gap:12px;margin-top:24px;">
                        <button type="submit" class="btn--cgv">Add Schedules</button>
                        <a href="${pageContext.request.contextPath}/ScheduleController?movieId=${movie.movieId}"
                           class="btn--cgv-outline">Cancel</a>
                    </div>
                </form>
                </c:if>
            </div>

        </div>
    </div>
</div>

<script>
var TOTAL_MINUTES = ${not empty movie ? totalMinutes : 0};

function toggleRoom(idx, checked) {
    var card = document.getElementById('card_' + idx);
    var container = document.getElementById('times_' + idx);
    var all = container.querySelectorAll('input, button');
    if (checked) {
        card.classList.add('checked');
        container.style.display = 'block';
        all.forEach(function(el) { el.disabled = false; });
    } else {
        card.classList.remove('checked');
        container.style.display = 'none';
        all.forEach(function(el) { el.disabled = true; });
    }
}

function updateEndTime(input) {
    var preview = input.parentElement.querySelector('.end-preview');
    if (!input.value) { preview.textContent = ''; return; }
    var parts = input.value.split(':');
    if (parts.length < 2) { preview.textContent = ''; return; }
    var h = parseInt(parts[0], 10);
    var m = parseInt(parts[1], 10);
    if (isNaN(h) || isNaN(m)) { preview.textContent = ''; return; }
    var total = h * 60 + m + TOTAL_MINUTES;
    var eh = Math.floor(total / 60) % 24;
    var em = total % 60;
    preview.textContent = '→ ' + String(eh).padStart(2, '0') + ':' + String(em).padStart(2, '0');
}

function addTime(idx, roomId) {
    var container = document.getElementById('slots_' + idx);
    var row = document.createElement('div');
    row.className = 'time-row';
    row.innerHTML = '<input class="cgv-input" type="time" name="startTime_' + roomId + '" required'
                  + ' oninput="updateEndTime(this)">'
                  + '<span class="end-preview" style="font-size:13px;color:rgba(94,63,58,0.6);white-space:nowrap;min-width:80px;"></span>'
                  + '<button type="button" class="btn--cgv-outline remove-time-btn" '
                  + 'onclick="this.parentElement.remove()" '
                  + 'style="padding:6px 14px;font-size:13px;" title="Remove">✕</button>';
    container.appendChild(row);
}

/* Form validation */
document.querySelector('form').addEventListener('submit', function(e) {
    var checked = document.querySelectorAll('input[name="roomIds"]:checked');
    if (checked.length === 0) { e.preventDefault(); alert('Please select at least one room.'); return; }
    for (var i = 0; i < checked.length; i++) {
        var cb = checked[i];
        var roomId = cb.value;
        var roomName = cb.dataset.roomName || 'Room ' + roomId;
        var times = document.querySelectorAll('input[name="startTime_' + roomId + '"]');
        var hasValue = false;
        for (var j = 0; j < times.length; j++) {
            if (times[j].value && times[j].value.trim() !== '') { hasValue = true; break; }
        }
        if (!hasValue) { e.preventDefault(); alert('Please add at least one start time for ' + roomName + '.'); return; }
    }
});
</script>

</body>
</html>
