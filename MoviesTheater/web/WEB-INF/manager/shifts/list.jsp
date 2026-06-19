<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% request.setAttribute("activeNav", "shifts"); %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Work Shifts — CGV Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
    <style>
        /* ── Calendar layout ─────────────────────────────────────────── */
        .shift-emp-bar {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-bottom: 20px;
        }
        .shift-emp-bar select { flex: 1; max-width: 300px; }

        /* Shift type pill selector */
        .cal-type-bar {
            display: flex;
            flex-wrap: wrap;
            gap: 8px;
            margin-bottom: 16px;
        }
        .cal-type-pill {
            display: flex;
            flex-direction: column;
            align-items: center;
            padding: 8px 14px;
            border-radius: 8px;
            border: 2px solid transparent;
            cursor: pointer;
            background: #f5f5f5;
            font-family: var(--font-cgv-ui, sans-serif);
            font-size: 12px;
            font-weight: 700;
            color: #444;
            transition: all .15s;
            user-select: none;
        }
        .cal-type-pill small {
            font-size: 10px;
            font-weight: 400;
            opacity: .75;
            margin-top: 2px;
        }
        .cal-type-pill.selected {
            color: #fff;
            border-color: transparent;
        }
        .cal-type-pill:hover { opacity: .85; }

        /* Month navigation */
        .cal-month-nav {
            display: flex;
            align-items: center;
            gap: 16px;
            margin-bottom: 12px;
        }
        .cal-month-nav a {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            width: 32px; height: 32px;
            border-radius: 6px;
            border: 1px solid var(--cgv-border, #e2d5d0);
            color: inherit;
            text-decoration: none;
            font-size: 16px;
            font-weight: 700;
            transition: background .15s;
        }
        .cal-month-nav a:hover { background: #f5eeeb; }
        .cal-month-nav span {
            font-family: var(--font-cgv-ui, sans-serif);
            font-size: 16px;
            font-weight: 700;
            color: #3d2424;
        }

        /* Calendar grid */
        .cal-weekday-row {
            display: grid;
            grid-template-columns: repeat(7, 1fr);
            gap: 4px;
            margin-bottom: 4px;
        }
        .cal-weekday {
            text-align: center;
            font-family: var(--font-cgv-ui, sans-serif);
            font-size: 11px;
            font-weight: 700;
            letter-spacing: .05em;
            color: rgba(94,63,58,.5);
            padding: 4px 0;
        }
        .cal-grid {
            display: grid;
            grid-template-columns: repeat(7, 1fr);
            gap: 4px;
        }
        .cal-day {
            min-height: 90px;
            background: #fff;
            border: 1px solid var(--cgv-border, #e2d5d0);
            border-radius: 8px;
            padding: 6px;
            cursor: pointer;
            transition: background .1s;
            position: relative;
            overflow: hidden;
        }
        .cal-day:hover { background: #fdf8f6; }
        .cal-day.empty {
            background: #fafafa;
            border-color: #eee;
            cursor: default;
        }
        .cal-day.today {
            border-color: #c8253a;
            box-shadow: 0 0 0 1px #c8253a20;
        }
        .cal-day-num {
            font-family: var(--font-cgv-ui, sans-serif);
            font-size: 12px;
            font-weight: 700;
            color: rgba(94,63,58,.55);
            margin-bottom: 4px;
        }
        .cal-day.today .cal-day-num {
            color: #c8253a;
        }

        /* Shift badge inside a day cell */
        .shift-badge {
            display: flex;
            align-items: center;
            justify-content: space-between;
            border-radius: 5px;
            padding: 3px 5px;
            margin-bottom: 3px;
            cursor: pointer;
            transition: opacity .1s;
            min-height: 22px;
        }
        .shift-badge:hover { opacity: .88; }
        .sb-time {
            font-family: var(--font-cgv-ui, sans-serif);
            font-size: 10px;
            font-weight: 700;
            color: rgba(255,255,255,.95);
            white-space: nowrap;
        }
        .sb-del {
            width: 16px; height: 16px;
            border-radius: 50%;
            border: none;
            background: rgba(0,0,0,.25);
            color: #fff;
            font-size: 11px;
            line-height: 1;
            cursor: pointer;
            padding: 0;
            flex-shrink: 0;
            display: flex;
            align-items: center;
            justify-content: center;
            transition: background .1s;
        }
        .sb-del:hover { background: rgba(0,0,0,.45); }

        /* Legend */
        .cal-legend {
            display: flex;
            flex-wrap: wrap;
            gap: 10px;
            margin-top: 16px;
        }
        .cal-legend-item {
            display: flex;
            align-items: center;
            gap: 6px;
            font-family: var(--font-cgv-ui, sans-serif);
            font-size: 11px;
            color: rgba(94,63,58,.7);
        }
        .cal-legend-dot {
            width: 10px; height: 10px;
            border-radius: 50%;
            flex-shrink: 0;
        }

        /* Empty state */
        .cal-empty-state {
            text-align: center;
            padding: 64px 24px;
            color: rgba(94,63,58,.4);
            font-family: var(--font-cgv-ui, sans-serif);
        }
        .cal-empty-state svg { margin-bottom: 16px; opacity: .3; }
        .cal-empty-state p { font-size: 14px; margin: 0; }
    </style>
</head>
<body class="cgv-body">

<%@ include file="../_sidebar.jsp" %>

<div class="cgv-main">

    <header class="cgv-header">
        <h1 class="cgv-header-title">Lịch Ca Làm Việc</h1>
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

            <c:if test="${not empty flashSuccess}">
                <div class="cgv-alert cgv-alert-success">${flashSuccess}</div>
            </c:if>
            <c:if test="${not empty flashError}">
                <div class="cgv-alert cgv-alert-danger">${flashError}</div>
            </c:if>

            <%-- Employee selector ─────────────────────────────────────── --%>
            <form method="get" action="${pageContext.request.contextPath}/manager/shifts"
                  class="shift-emp-bar">
                <label style="font-family:var(--font-cgv-ui);font-size:12px;font-weight:700;color:rgba(94,63,58,.6);">NHÂN VIÊN:</label>
                <select class="cgv-select" name="empId" style="height:36px;"
                        onchange="this.form.submit()">
                    <option value="0">— Chọn nhân viên —</option>
                    <c:forEach var="emp" items="${employees}">
                        <option value="${emp.accountId}"
                            ${selectedEmpId eq emp.accountId ? 'selected' : ''}>
                            ${emp.fullName}
                            <c:if test="${not empty emp.email}"> — ${emp.email}</c:if>
                        </option>
                    </c:forEach>
                </select>
                <input type="hidden" name="year"  value="${selectedYear}">
                <input type="hidden" name="month" value="${selectedMonth}">
            </form>

            <c:choose>
                <c:when test="${selectedEmpId > 0}">

                    <%-- Shift type pill selector ─────────────────────── --%>
                    <div class="cal-type-bar" id="typeBar">
                        <div class="cal-type-pill selected"
                             data-type="6H_SANG" data-color="#3b82f6"
                             style="background:#3b82f6;color:#fff;">
                            Ca 6h Sáng<small>08:00–14:00</small>
                        </div>
                        <div class="cal-type-pill"
                             data-type="6H_CHIEU" data-color="#10b981">
                            Ca 6h Chiều<small>14:00–20:00</small>
                        </div>
                        <div class="cal-type-pill"
                             data-type="6H_TOI" data-color="#8b5cf6">
                            Ca 6h Tối<small>20:00–23:59</small>
                        </div>
                        <div class="cal-type-pill"
                             data-type="8H_SANG" data-color="#f59e0b">
                            Ca 8h Sáng<small>08:00–17:30</small>
                        </div>
                        <div class="cal-type-pill"
                             data-type="8H_CHIEU" data-color="#f97316">
                            Ca 8h Chiều<small>13:00–22:30</small>
                        </div>
                    </div>
                    <div style="font-family:var(--font-cgv-ui);font-size:11px;color:rgba(94,63,58,.5);margin-bottom:16px;">
                        Chọn loại ca rồi click vào ngày để thêm ca làm việc.
                    </div>

                    <%-- Month navigation ─────────────────────────────── --%>
                    <div class="cal-month-nav">
                        <a href="${pageContext.request.contextPath}/manager/shifts?empId=${selectedEmpId}&year=${prevYear}&month=${prevMonth}">&#8249;</a>
                        <span>${monthName}</span>
                        <a href="${pageContext.request.contextPath}/manager/shifts?empId=${selectedEmpId}&year=${nextYear}&month=${nextMonth}">&#8250;</a>
                    </div>

                    <%-- Day of week headers ──────────────────────────── --%>
                    <div class="cal-weekday-row">
                        <div class="cal-weekday">T2</div>
                        <div class="cal-weekday">T3</div>
                        <div class="cal-weekday">T4</div>
                        <div class="cal-weekday">T5</div>
                        <div class="cal-weekday">T6</div>
                        <div class="cal-weekday">T7</div>
                        <div class="cal-weekday">CN</div>
                    </div>

                    <%-- Calendar grid (built by JS) ──────────────────── --%>
                    <div id="cal-grid" class="cal-grid"></div>

                    <%-- Legend ───────────────────────────────────────── --%>
                    <div class="cal-legend">
                        <div class="cal-legend-item">
                            <div class="cal-legend-dot" style="background:#3b82f6"></div>Ca 6h Sáng (đã lên lịch)
                        </div>
                        <div class="cal-legend-item">
                            <div class="cal-legend-dot" style="background:#1e40af"></div>Ca 6h Sáng (đã làm)
                        </div>
                        <div class="cal-legend-item">
                            <div class="cal-legend-dot" style="background:#10b981"></div>Ca 6h Chiều
                        </div>
                        <div class="cal-legend-item">
                            <div class="cal-legend-dot" style="background:#8b5cf6"></div>Ca 6h Tối
                        </div>
                        <div class="cal-legend-item">
                            <div class="cal-legend-dot" style="background:#f59e0b"></div>Ca 8h Sáng
                        </div>
                        <div class="cal-legend-item">
                            <div class="cal-legend-dot" style="background:#f97316"></div>Ca 8h Chiều
                        </div>
                        <div class="cal-legend-item">
                            <div class="cal-legend-dot" style="background:#dc2626"></div>Vắng / Chưa check-in
                        </div>
                    </div>

                </c:when>
                <c:otherwise>
                    <div class="cal-empty-state">
                        <svg width="48" height="48" viewBox="0 0 24 24" fill="none"
                             stroke="currentColor" stroke-width="1.5">
                            <rect x="3" y="4" width="18" height="18" rx="2"/>
                            <line x1="16" y1="2" x2="16" y2="6"/>
                            <line x1="8"  y1="2" x2="8"  y2="6"/>
                            <line x1="3"  y1="10" x2="21" y2="10"/>
                        </svg>
                        <p>Chọn nhân viên để xem và quản lý lịch ca làm việc.</p>
                    </div>
                </c:otherwise>
            </c:choose>

        </div>

        <%-- Aside ──────────────────────────────────────────────────── --%>
        <aside class="cgv-aside">
            <div class="cgv-stats-section">
                <div class="cgv-aside-heading">THÁNG NÀY</div>
                <div class="cgv-stats-group">
                    <div>
                        <div class="cgv-stat-num" id="stat-total">—</div>
                        <div class="cgv-stat-key">CA ĐÃ LÊN LỊCH</div>
                    </div>
                    <div>
                        <div class="cgv-stat-num" id="stat-done">—</div>
                        <div class="cgv-stat-key">ĐÃ HOÀN THÀNH</div>
                    </div>
                    <div>
                        <div class="cgv-stat-num" id="stat-absent">—</div>
                        <div class="cgv-stat-key">VẮNG</div>
                    </div>
                </div>
            </div>
            <div class="cgv-aside-divider">
                <div class="cgv-aside-heading">ĐIỀU HƯỚNG</div>
                <div style="display:flex;flex-direction:column;gap:8px;margin-top:8px;">
                    <a href="${pageContext.request.contextPath}/manager/employees"
                       class="btn--cgv-outline" style="text-align:center;">← Nhân Viên</a>
                </div>
            </div>
        </aside>
    </div>
</div>

<%-- Hidden add form ─────────────────────────────────────────────────── --%>
<form id="add-form" method="post"
      action="${pageContext.request.contextPath}/manager/shifts" style="display:none;">
    <input type="hidden" name="action"     value="create">
    <input type="hidden" name="employeeId" value="${selectedEmpId}">
    <input type="hidden" id="af-date"      name="shiftDate" value="">
    <input type="hidden" id="af-type"      name="shiftType" value="">
    <input type="hidden" name="year"       value="${selectedYear}">
    <input type="hidden" name="month"      value="${selectedMonth}">
</form>

<script>
/* ── Constants from server ─────────────────────────────────────────── */
var CTX   = '<%=request.getContextPath()%>';
var YEAR  = ${selectedYear};
var MONTH = ${selectedMonth};
var EMP_ID = ${selectedEmpId};
var TODAY = '${serverToday}';
var NOW   = '${serverTime}';

var SHIFTS = [
<c:forEach var="s" items="${shifts}" varStatus="st">
<c:if test="${!st.first}">,
</c:if>{id:${s.shiftId},date:'${s.shiftDate}',start:'${s.startTime}',end:'${s.endTime}',status:'${s.status}'}
</c:forEach>
];

/* ── Shift type definitions ─────────────────────────────────────────── */
var SHIFT_DEFS = {
    '6H_SANG':  {label:'Ca 6h Sáng',  start:'08:00', end:'14:00', color:'#3b82f6', dark:'#1e40af'},
    '6H_CHIEU': {label:'Ca 6h Chiều', start:'14:00', end:'20:00', color:'#10b981', dark:'#065f46'},
    '6H_TOI':   {label:'Ca 6h Tối',   start:'20:00', end:'23:59', color:'#8b5cf6', dark:'#4c1d95'},
    '8H_SANG':  {label:'Ca 8h Sáng',  start:'08:00', end:'17:30', color:'#f59e0b', dark:'#92400e'},
    '8H_CHIEU': {label:'Ca 8h Chiều', start:'13:00', end:'22:30', color:'#f97316', dark:'#7c2d12'}
};

function getShiftType(start, end) {
    var s = start.slice(0,5), e = end.slice(0,5);
    if (s==='08:00' && e==='14:00') return '6H_SANG';
    if (s==='14:00' && e==='20:00') return '6H_CHIEU';
    if (s==='20:00') return '6H_TOI';
    if (s==='08:00' && e==='17:30') return '8H_SANG';
    if (s==='13:00' && e==='22:30') return '8H_CHIEU';
    return null;
}

function getDisplayStatus(shift) {
    if (shift.status === 'Completed') return 'completed';
    if (shift.status === 'Absent')    return 'absent';
    if (shift.date < TODAY) return 'absent';
    if (shift.date === TODAY && shift.end.slice(0,5) <= NOW) return 'absent';
    return 'scheduled';
}

function getBadgeColor(shift) {
    var ds = getDisplayStatus(shift);
    if (ds === 'absent') return '#dc2626';
    var type = getShiftType(shift.start, shift.end);
    if (!type) return ds === 'completed' ? '#374151' : '#6b7280';
    return ds === 'completed' ? SHIFT_DEFS[type].dark : SHIFT_DEFS[type].color;
}

/* ── Selected shift type (from pills) ──────────────────────────────── */
var selectedType = '6H_SANG';

document.addEventListener('DOMContentLoaded', function() {
    var pills = document.querySelectorAll('.cal-type-pill');
    pills.forEach(function(pill) {
        pill.addEventListener('click', function(e) {
            e.stopPropagation();
            pills.forEach(function(p) {
                p.classList.remove('selected');
                p.style.background = '';
                p.style.color = '#444';
            });
            this.classList.add('selected');
            this.style.background = this.dataset.color;
            this.style.color = '#fff';
            selectedType = this.dataset.type;
        });
    });
    buildCalendar();
    updateStats();
});

/* ── Build calendar grid ────────────────────────────────────────────── */
function buildCalendar() {
    var grid = document.getElementById('cal-grid');
    if (!grid) return;

    var shiftsByDate = {};
    SHIFTS.forEach(function(s) {
        if (!shiftsByDate[s.date]) shiftsByDate[s.date] = [];
        shiftsByDate[s.date].push(s);
    });

    var firstDay  = new Date(YEAR, MONTH - 1, 1);
    var daysInMonth = new Date(YEAR, MONTH, 0).getDate();
    var startOffset = (firstDay.getDay() + 6) % 7; // Mon=0 … Sun=6
    var totalCells  = Math.ceil((startOffset + daysInMonth) / 7) * 7;

    var html = '';
    var dayNum = 1;

    for (var i = 0; i < totalCells; i++) {
        if (i < startOffset || dayNum > daysInMonth) {
            html += '<div class="cal-day empty"></div>';
        } else {
            var dd = String(dayNum).padStart(2, '0');
            var mm = String(MONTH).padStart(2, '0');
            var dateStr = YEAR + '-' + mm + '-' + dd;
            var isToday = (dateStr === TODAY);
            var dayShifts = shiftsByDate[dateStr] || [];

            html += '<div class="cal-day' + (isToday ? ' today' : '') + '"'
                  + ' onclick="addShift(\'' + dateStr + '\')">';
            html += '<div class="cal-day-num">' + dayNum + '</div>';

            dayShifts.forEach(function(s) {
                var color = getBadgeColor(s);
                var timeLabel = s.start.slice(0,5) + '-' + s.end.slice(0,5);
                var editUrl = CTX + '/manager/shifts?action=edit&id=' + s.id
                            + '&empId=' + EMP_ID + '&year=' + YEAR + '&month=' + MONTH;

                html += '<div class="shift-badge" style="background:' + color + '"'
                      + ' onclick="event.stopPropagation();location.href=\'' + editUrl + '\'">';
                html += '<span class="sb-time">' + timeLabel + '</span>';
                html += '<button type="button" class="sb-del"'
                      + ' onclick="event.stopPropagation();deleteShift(' + s.id + ',\'' + dateStr + '\')"'
                      + ' title="Xóa ca">&#x2715;</button>';
                html += '</div>';
            });

            html += '</div>';
            dayNum++;
        }
    }

    grid.innerHTML = html;
}

/* ── Add shift (click on a day) ─────────────────────────────────────── */
function addShift(dateStr) {
    if (EMP_ID <= 0) return;
    document.getElementById('af-date').value = dateStr;
    document.getElementById('af-type').value = selectedType;
    document.getElementById('add-form').submit();
}

/* ── Delete shift ───────────────────────────────────────────────────── */
function deleteShift(shiftId, dateStr) {
    if (!confirm('Xóa ca làm việc ngày ' + dateStr + '?')) return;
    var f = document.createElement('form');
    f.method = 'post';
    f.action = CTX + '/manager/shifts';
    var fields = {action:'delete', shiftId:shiftId, empId:EMP_ID, year:YEAR, month:MONTH};
    Object.keys(fields).forEach(function(k) {
        var inp = document.createElement('input');
        inp.type = 'hidden'; inp.name = k; inp.value = fields[k];
        f.appendChild(inp);
    });
    document.body.appendChild(f);
    f.submit();
}

/* ── Aside stats ────────────────────────────────────────────────────── */
function updateStats() {
    var total = SHIFTS.length, done = 0, absent = 0;
    SHIFTS.forEach(function(s) {
        var ds = getDisplayStatus(s);
        if (ds === 'completed') done++;
        else if (ds === 'absent') absent++;
    });
    var el = function(id) { return document.getElementById(id); };
    if (el('stat-total'))  el('stat-total').textContent  = total;
    if (el('stat-done'))   el('stat-done').textContent   = done;
    if (el('stat-absent')) el('stat-absent').textContent = absent;
}
</script>
</body>
</html>
