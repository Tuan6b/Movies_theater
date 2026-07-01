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
        /* ── Shift type pill selector ──────────────────────────────────── */
        .cal-type-bar {
            display: flex;
            flex-wrap: wrap;
            gap: 8px;
            margin-bottom: 20px;
        }
        .cal-type-pill {
            display: flex;
            flex-direction: column;
            align-items: center;
            padding: 10px 18px;
            border-radius: 8px;
            border: 2px solid transparent;
            text-decoration: none;
            font-family: var(--font-cgv-ui, sans-serif);
            font-size: 12px;
            font-weight: 700;
            color: #444;
            background: #f5f5f5;
            transition: all .15s;
            user-select: none;
        }
        .cal-type-pill small {
            font-size: 10px;
            font-weight: 400;
            opacity: .75;
            margin-top: 2px;
        }
        .cal-type-pill.selected { color: #fff; }
        .cal-type-pill:hover { opacity: .85; }

        /* ── Month navigation ──────────────────────────────────────────── */
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

        /* ── Calendar grid ─────────────────────────────────────────────── */
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
        .cal-day.empty { background: #fafafa; border-color: #eee; cursor: default; }
        .cal-day.today { border-color: #c8253a; box-shadow: 0 0 0 1px #c8253a20; }
        .cal-day.past-disabled {
            background: #e9e0dd;
            border-color: #d8c2bf;
            cursor: not-allowed;
        }
        .cal-day.past-disabled:hover { background: #e9e0dd; }
        .cal-day.past-disabled .cal-day-num { color: rgba(94,63,58,.3); }
        .cal-day-num {
            font-family: var(--font-cgv-ui, sans-serif);
            font-size: 12px;
            font-weight: 700;
            color: rgba(94,63,58,.55);
            margin-bottom: 4px;
        }
        .cal-day.today .cal-day-num { color: #c8253a; }

        /* ── Shift badge inside a day cell ─────────────────────────────── */
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
            gap: 4px;
        }
        .shift-badge:hover { opacity: .88; }
        .sb-name {
            font-family: var(--font-cgv-ui, sans-serif);
            font-size: 10px;
            font-weight: 700;
            color: rgba(255,255,255,.95);
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            flex: 1;
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

        /* ── Employee selector bar — always visible at top ─────────────── */
        #emp-bar {
            background: #fff;
            border: 2px solid var(--cgv-border, #e2d5d0);
            border-radius: 12px;
            padding: 16px 20px;
            margin-bottom: 20px;
            display: flex;
            align-items: center;
            gap: 16px;
            flex-wrap: wrap;
        }
        #emp-bar.shake {
            border-color: #c8253a;
            animation: shake .35s ease;
        }
        @keyframes shake {
            0%,100% { transform: translateX(0); }
            20%      { transform: translateX(-6px); }
            40%      { transform: translateX(6px); }
            60%      { transform: translateX(-4px); }
            80%      { transform: translateX(4px); }
        }
        #emp-bar-label {
            font-family: var(--font-cgv-ui, sans-serif);
            font-size: 11px;
            font-weight: 700;
            letter-spacing: 1.5px;
            color: rgba(94,63,58,.5);
            white-space: nowrap;
        }
        #global-emp-select {
            flex: 1;
            min-width: 260px;
            max-width: 420px;
            height: 44px;
            font-size: 14px;
            font-weight: 600;
        }
        #emp-bar-hint {
            font-size: 12px;
            color: rgba(94,63,58,.45);
            font-family: var(--font-cgv-ui, sans-serif);
        }

        /* ── Confirmation banner on day click ───────────────────────────── */
        #add-panel {
            display: none;
            background: #fff7f7;
            border-bottom: 3px solid #c8253a;
            padding: 16px 32px;
            animation: slideDown .15s ease;
        }
        @keyframes slideDown {
            from { opacity: 0; transform: translateY(-6px); }
            to   { opacity: 1; transform: translateY(0); }
        }
        #add-panel-inner {
            display: flex;
            align-items: center;
            gap: 16px;
            flex-wrap: wrap;
        }
        #add-panel-title {
            font-family: var(--font-cgv-ui, sans-serif);
            font-size: 14px;
            font-weight: 700;
            color: #c8253a;
            flex: 1;
        }
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

    <%-- Confirmation banner — appears when manager clicks a calendar day ─ --%>
    <div id="add-panel">
        <div id="add-panel-inner">
            <div id="add-panel-title">
                Thêm <strong id="ap-emp-name"></strong> vào ca ngày
                <strong id="ap-date-label"></strong>?
            </div>
            <button type="button" class="btn--cgv"
                    style="height:38px;padding:0 22px;font-size:13px;"
                    onclick="submitAddShift()">Xác nhận</button>
            <button type="button" class="btn--cgv-outline"
                    style="height:38px;padding:0 16px;font-size:13px;"
                    onclick="closeAddPanel()">Hủy</button>
        </div>
    </div>

    <div class="cgv-page">
        <div class="cgv-list-wrap">

            <c:if test="${not empty flashSuccess}">
                <div class="cgv-alert cgv-alert-success">${flashSuccess}</div>
            </c:if>
            <c:if test="${not empty flashError}">
                <div class="cgv-alert cgv-alert-danger">${flashError}</div>
            </c:if>

            <%-- Step 1: Choose employee (always visible) ────────────────── --%>
            <div id="emp-bar">
                <div id="emp-bar-label">BƯỚC 1 — CHỌN NHÂN VIÊN</div>
                <select id="global-emp-select" class="cgv-select">
                    <option value="">— Chọn nhân viên để phân ca —</option>
                    <c:forEach var="emp" items="${employees}">
                        <option value="${emp.accountId}">${emp.fullName}</option>
                    </c:forEach>
                </select>
                <div id="emp-bar-hint">Sau đó chọn loại ca và click ngày trên lịch</div>
            </div>

            <%-- Step 2: Choose shift type (navigating pills) ────────────── --%>
            <div style="font-family:var(--font-cgv-ui);font-size:10px;font-weight:700;letter-spacing:2px;color:rgba(94,63,58,.5);margin-bottom:8px;">
                BƯỚC 2 — CHỌN LOẠI CA
            </div>
            <div class="cal-type-bar">
                <a href="${pageContext.request.contextPath}/manager/shifts?shiftType=6H_SANG&year=${selectedYear}&month=${selectedMonth}"
                   class="cal-type-pill ${selectedShiftType eq '6H_SANG' ? 'selected' : ''}"
                   style="${selectedShiftType eq '6H_SANG' ? 'background:#3b82f6;' : ''}">
                    Ca 6h Sáng<small>08:00–14:00</small>
                </a>
                <a href="${pageContext.request.contextPath}/manager/shifts?shiftType=6H_CHIEU&year=${selectedYear}&month=${selectedMonth}"
                   class="cal-type-pill ${selectedShiftType eq '6H_CHIEU' ? 'selected' : ''}"
                   style="${selectedShiftType eq '6H_CHIEU' ? 'background:#10b981;' : ''}">
                    Ca 6h Chiều<small>14:00–20:00</small>
                </a>
                <a href="${pageContext.request.contextPath}/manager/shifts?shiftType=6H_TOI&year=${selectedYear}&month=${selectedMonth}"
                   class="cal-type-pill ${selectedShiftType eq '6H_TOI' ? 'selected' : ''}"
                   style="${selectedShiftType eq '6H_TOI' ? 'background:#8b5cf6;' : ''}">
                    Ca 6h Tối<small>20:00–23:59</small>
                </a>
                <a href="${pageContext.request.contextPath}/manager/shifts?shiftType=8H_SANG&year=${selectedYear}&month=${selectedMonth}"
                   class="cal-type-pill ${selectedShiftType eq '8H_SANG' ? 'selected' : ''}"
                   style="${selectedShiftType eq '8H_SANG' ? 'background:#f59e0b;' : ''}">
                    Ca 8h Sáng<small>08:00–17:30</small>
                </a>
                <a href="${pageContext.request.contextPath}/manager/shifts?shiftType=8H_CHIEU&year=${selectedYear}&month=${selectedMonth}"
                   class="cal-type-pill ${selectedShiftType eq '8H_CHIEU' ? 'selected' : ''}"
                   style="${selectedShiftType eq '8H_CHIEU' ? 'background:#f97316;' : ''}">
                    Ca 8h Chiều<small>13:00–22:30</small>
                </a>
            </div>

            <%-- Step 3: Click a day on the calendar ──────────────────── --%>
            <div style="font-family:var(--font-cgv-ui);font-size:10px;font-weight:700;letter-spacing:2px;color:rgba(94,63,58,.5);margin-bottom:8px;">
                BƯỚC 3 — CLICK NGÀY TRÊN LỊCH ĐỂ PHÂN CA
            </div>

            <%-- Month navigation ─────────────────────────────────────── --%>
            <div class="cal-month-nav">
                <a href="${pageContext.request.contextPath}/manager/shifts?shiftType=${selectedShiftType}&year=${prevYear}&month=${prevMonth}">&#8249;</a>
                <span>${monthName}</span>
                <a href="${pageContext.request.contextPath}/manager/shifts?shiftType=${selectedShiftType}&year=${nextYear}&month=${nextMonth}">&#8250;</a>
            </div>

            <%-- Day of week headers ──────────────────────────────────── --%>
            <div class="cal-weekday-row">
                <div class="cal-weekday">T2</div>
                <div class="cal-weekday">T3</div>
                <div class="cal-weekday">T4</div>
                <div class="cal-weekday">T5</div>
                <div class="cal-weekday">T6</div>
                <div class="cal-weekday">T7</div>
                <div class="cal-weekday">CN</div>
            </div>

            <%-- Calendar grid (built by JS) ──────────────────────────── --%>
            <div id="cal-grid" class="cal-grid"></div>

            <%-- Legend ───────────────────────────────────────────────── --%>
            <div style="display:flex;flex-wrap:wrap;gap:10px;margin-top:16px;">
                <div style="display:flex;align-items:center;gap:6px;font-family:var(--font-cgv-ui);font-size:11px;color:rgba(94,63,58,.7);">
                    <div style="width:10px;height:10px;border-radius:50%;background:var(--shift-color);flex-shrink:0;"></div>Đã lên lịch
                </div>
                <div style="display:flex;align-items:center;gap:6px;font-family:var(--font-cgv-ui);font-size:11px;color:rgba(94,63,58,.7);">
                    <div style="width:10px;height:10px;border-radius:50%;background:var(--shift-dark);flex-shrink:0;"></div>Đã làm
                </div>
                <div style="display:flex;align-items:center;gap:6px;font-family:var(--font-cgv-ui);font-size:11px;color:rgba(94,63,58,.7);">
                    <div style="width:10px;height:10px;border-radius:50%;background:#dc2626;flex-shrink:0;"></div>Vắng
                </div>
            </div>

        </div>

        <%-- Aside ────────────────────────────────────────────────────── --%>
        <aside class="cgv-aside">
            <div class="cgv-stats-section">
                <div class="cgv-aside-heading">THÁNG NÀY</div>
                <div class="cgv-stats-group">
                    <div>
                        <div class="cgv-stat-num" id="stat-total">—</div>
                        <div class="cgv-stat-key">TỔNG CA ĐÃ PHÂN</div>
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
                <div class="cgv-aside-heading">PHÂN CA THEO THÁNG</div>
                <form method="post" action="${pageContext.request.contextPath}/manager/shifts"
                      style="display:flex;flex-direction:column;gap:8px;margin-top:8px;">
                    <input type="hidden" name="action"    value="bulk_create">
                    <input type="hidden" name="shiftType" value="${selectedShiftType}">
                    <input type="hidden" name="year"      value="${selectedYear}">
                    <input type="hidden" name="month"     value="${selectedMonth}">
                    <select class="cgv-select" name="employeeId" style="height:36px;">
                        <option value="">— Chọn nhân viên —</option>
                        <c:forEach var="emp" items="${employees}">
                            <option value="${emp.accountId}">${emp.fullName}</option>
                        </c:forEach>
                    </select>
                    <button type="submit" class="btn--cgv" style="width:100%;text-align:center;"
                            onclick="return confirm('Phân ca cho nhân viên đã chọn toàn bộ ${monthName}?\n(Bỏ qua ngày đã có ca và ngày đã qua)')">
                        Phân ca ${monthName}
                    </button>
                </form>
                <div style="font-size:11px;color:rgba(94,63,58,.4);margin-top:8px;">
                    Tạo ca cho tất cả ngày còn lại trong tháng, bỏ qua ngày đã có.
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

<%-- Hidden submit form for adding a shift ─────────────────────────────── --%>
<form id="add-form" method="post"
      action="${pageContext.request.contextPath}/manager/shifts" style="display:none;">
    <input type="hidden" name="action"     value="create">
    <input type="hidden" id="af-emp"       name="employeeId" value="">
    <input type="hidden" id="af-date"      name="shiftDate"  value="">
    <input type="hidden" name="shiftType"  value="${selectedShiftType}">
    <input type="hidden" name="year"       value="${selectedYear}">
    <input type="hidden" name="month"      value="${selectedMonth}">
</form>

<script>
/* ── Server-injected constants ──────────────────────────────────────── */
var CTX        = '<%=request.getContextPath()%>';
var YEAR       = ${selectedYear};
var MONTH      = ${selectedMonth};
var SHIFT_TYPE = '${selectedShiftType}';
var TODAY      = '${serverToday}';
var NOW        = '${serverTime}';

/* ── Shift data (all employees for the selected type this month) ─────── */
var SHIFTS = [
<c:forEach var="s" items="${shifts}" varStatus="st">
<c:if test="${!st.first}">,
</c:if>{id:${s.shiftId},date:'${s.shiftDate}',status:'${s.status}',empId:${s.employeeId}}
</c:forEach>
];

/* ── Employee name lookup built from hidden select options ───────────── */
var EMPLOYEES = {};
<c:forEach var="emp" items="${employees}">
EMPLOYEES[${emp.accountId}] = '${emp.fullName}';
</c:forEach>

/* ── Shift type color map ────────────────────────────────────────────── */
var TYPE_COLOR = {
    '6H_SANG':  {color:'#3b82f6', dark:'#1e40af'},
    '6H_CHIEU': {color:'#10b981', dark:'#065f46'},
    '6H_TOI':   {color:'#8b5cf6', dark:'#4c1d95'},
    '8H_SANG':  {color:'#f59e0b', dark:'#92400e'},
    '8H_CHIEU': {color:'#f97316', dark:'#7c2d12'}
};

var typeColors = TYPE_COLOR[SHIFT_TYPE] || {color:'#6b7280', dark:'#374151'};

/* ── Expose CSS vars for legend ─────────────────────────────────────── */
document.documentElement.style.setProperty('--shift-color', typeColors.color);
document.documentElement.style.setProperty('--shift-dark',  typeColors.dark);

function getBadgeColor(status, dateStr) {
    if (status === 'Absent') return '#dc2626';
    if (dateStr < TODAY) return typeColors.dark;
    if (dateStr === TODAY) return typeColors.color;
    if (status === 'Completed') return typeColors.dark;
    return typeColors.color;
}

function getDisplayName(empId) {
    var full = EMPLOYEES[empId] || ('NV#' + empId);
    // Show the last word (Vietnamese given name) truncated to 10 chars.
    var parts = full.trim().split(/\s+/);
    var name = parts[parts.length - 1];
    return name.length > 10 ? name.substring(0, 10) + '…' : name;
}

/* ── Build calendar grid ─────────────────────────────────────────────── */
function buildCalendar() {
    var grid = document.getElementById('cal-grid');
    if (!grid) return;

    var shiftsByDate = {};
    SHIFTS.forEach(function(s) {
        if (!shiftsByDate[s.date]) shiftsByDate[s.date] = [];
        shiftsByDate[s.date].push(s);
    });

    var firstDay     = new Date(YEAR, MONTH - 1, 1);
    var daysInMonth  = new Date(YEAR, MONTH, 0).getDate();
    var startOffset  = (firstDay.getDay() + 6) % 7; // Mon=0 … Sun=6
    var totalCells   = Math.ceil((startOffset + daysInMonth) / 7) * 7;

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
            var isPast  = (dateStr < TODAY);
            var dayShifts = shiftsByDate[dateStr] || [];

            var cls = 'cal-day';
            if (isToday) cls += ' today';
            if (isPast)  cls += ' past-disabled';

            var clickAttr = isPast ? '' : ' onclick="openAddPanel(\'' + dateStr + '\')"';
            html += '<div class="' + cls + '"' + clickAttr + '>';
            html += '<div class="cal-day-num">' + dayNum + '</div>';

            dayShifts.forEach(function(s) {
                var color = getBadgeColor(s.status, dateStr);
                var name  = getDisplayName(s.empId);
                var editUrl = CTX + '/manager/shifts?action=edit&id=' + s.id
                            + '&shiftType=' + SHIFT_TYPE + '&year=' + YEAR + '&month=' + MONTH;

                html += '<div class="shift-badge" style="background:' + color + '"'
                      + ' onclick="event.stopPropagation();location.href=\'' + editUrl + '\'">';
                html += '<span class="sb-name">' + name + '</span>';
                if (!isPast) {
                    html += '<button type="button" class="sb-del"'
                          + ' onclick="event.stopPropagation();deleteShift(' + s.id + ',\'' + dateStr + '\')"'
                          + ' title="Xóa ca">&#x2715;</button>';
                }
                html += '</div>';
            });

            html += '</div>';
            dayNum++;
        }
    }

    grid.innerHTML = html;
}

/* ── Add panel ───────────────────────────────────────────────────────── */
var pendingDate = null;

function openAddPanel(dateStr) {
    if (dateStr < TODAY) return;
    var sel   = document.getElementById('global-emp-select');
    var empId = sel ? sel.value : '';
    if (!empId) {
        // Shake the employee bar to direct attention to it
        var bar = document.getElementById('emp-bar');
        bar.classList.remove('shake');
        void bar.offsetWidth; // reflow to restart animation
        bar.classList.add('shake');
        bar.style.borderColor = '#c8253a';
        sel.focus();
        return;
    }
    pendingDate = dateStr;
    document.getElementById('ap-emp-name').textContent  = EMPLOYEES[empId] || empId;
    document.getElementById('ap-date-label').textContent = dateStr;
    document.getElementById('add-panel').style.display  = 'block';
}

function closeAddPanel() {
    document.getElementById('add-panel').style.display = 'none';
    pendingDate = null;
}

function submitAddShift() {
    var empId = document.getElementById('global-emp-select').value;
    if (!empId || !pendingDate) return;
    document.getElementById('af-emp').value  = empId;
    document.getElementById('af-date').value = pendingDate;
    document.getElementById('add-form').submit();
}

/* ── Delete shift ────────────────────────────────────────────────────── */
function deleteShift(shiftId, dateStr) {
    if (dateStr < TODAY) { alert('Không thể xóa ca làm việc trong quá khứ.'); return; }
    if (!confirm('Xóa ca làm việc ngày ' + dateStr + '?')) return;
    var f = document.createElement('form');
    f.method = 'post';
    f.action = CTX + '/manager/shifts';
    var fields = {action:'delete', shiftId:shiftId, shiftType:SHIFT_TYPE, year:YEAR, month:MONTH};
    Object.keys(fields).forEach(function(k) {
        var inp = document.createElement('input');
        inp.type = 'hidden'; inp.name = k; inp.value = fields[k];
        f.appendChild(inp);
    });
    document.body.appendChild(f);
    f.submit();
}

/* ── Aside stats ─────────────────────────────────────────────────────── */
function updateStats() {
    var total = SHIFTS.length, done = 0, absent = 0;
    SHIFTS.forEach(function(s) {
        if (s.status === 'Completed') done++;
        else if (s.status === 'Absent' || s.date < TODAY) absent++;
    });
    document.getElementById('stat-total').textContent  = total;
    document.getElementById('stat-done').textContent   = done;
    document.getElementById('stat-absent').textContent = absent;
}

document.addEventListener('DOMContentLoaded', function() {
    buildCalendar();
    updateStats();
    var sel = document.getElementById('global-emp-select');
    if (sel) {
        sel.addEventListener('change', function() {
            var bar = document.getElementById('emp-bar');
            bar.classList.remove('shake');
            bar.style.borderColor = this.value ? '#10b981' : '';
        });
    }
});
</script>
</body>
</html>
