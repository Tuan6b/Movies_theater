<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<% request.setAttribute("activeNav", "my-shifts"); %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Ca Làm Việc — CGV Employee</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
    <style>
        .ms-section-title {
            font-family: var(--font-cgv-ui, sans-serif);
            font-size: 10px;
            font-weight: 700;
            letter-spacing: 2px;
            text-transform: uppercase;
            color: rgba(94,63,58,.5);
            margin-bottom: 12px;
            margin-top: 28px;
        }
        .ms-section-title:first-child { margin-top: 0; }

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
            min-height: 88px;
            background: #fff;
            border: 1px solid var(--cgv-border, #e2d5d0);
            border-radius: 8px;
            padding: 6px;
            position: relative;
            overflow: hidden;
        }
        .cal-day.empty { background: #fafafa; border-color: #eee; }
        .cal-day.today { border-color: #c8253a; box-shadow: 0 0 0 1px #c8253a20; }
        .cal-day.past  { background: #faf7f6; }
        .cal-day-num {
            font-family: var(--font-cgv-ui, sans-serif);
            font-size: 12px;
            font-weight: 700;
            color: rgba(94,63,58,.55);
            margin-bottom: 4px;
        }
        .cal-day.today .cal-day-num { color: #c8253a; }

        /* ── One shift inside a day cell ───────────────────────────────── */
        .cal-shift {
            display: block;
            width: 100%;
            text-align: left;
            border: none;
            border-radius: 6px;
            padding: 5px 6px;
            margin-bottom: 4px;
            cursor: pointer;
            color: #fff;
            font-family: var(--font-cgv-ui, sans-serif);
            transition: opacity .1s;
        }
        .cal-shift:hover { opacity: .88; }
        .cal-shift-time {
            font-size: 11px;
            font-weight: 700;
            display: block;
            white-space: nowrap;
        }
        .cal-shift-label {
            font-size: 10px;
            opacity: .9;
            display: block;
            margin-top: 1px;
        }
        .cal-shift.has-request {
            box-shadow: 0 0 0 2px #f59e0b inset;
        }

        .cal-legend {
            display: flex;
            flex-wrap: wrap;
            gap: 14px;
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
            border-radius: 3px;
            flex-shrink: 0;
        }

        /* ── Shift detail modal ────────────────────────────────────────── */
        .sm-overlay {
            display: none;
            position: fixed;
            inset: 0;
            background: rgba(61,36,36,.45);
            z-index: 1000;
            align-items: center;
            justify-content: center;
            padding: 24px;
        }
        .sm-overlay.open { display: flex; }
        .sm-box {
            background: #fff;
            border-radius: 14px;
            padding: 28px;
            width: 100%;
            max-width: 460px;
            position: relative;
            max-height: 90vh;
            overflow-y: auto;
        }
        .sm-close {
            position: absolute;
            top: 14px; right: 14px;
            width: 28px; height: 28px;
            border: none;
            border-radius: 50%;
            background: #f3ece9;
            color: #5e3f3a;
            font-size: 16px;
            line-height: 1;
            cursor: pointer;
        }
        .sm-close:hover { background: #e7dcd8; }
        .sm-date {
            font-family: var(--font-cgv-ui, sans-serif);
            font-size: 18px;
            font-weight: 700;
            color: var(--cgv-dark, #3d2424);
        }
        .sm-time {
            font-size: 14px;
            font-weight: 600;
            color: var(--cgv-primary, #c8253a);
            margin: 4px 0 14px;
        }
        .sm-block {
            border-top: 1px solid var(--cgv-border, #e2d5d0);
            margin-top: 16px;
            padding-top: 16px;
        }
        .sm-hint {
            font-size: 12px;
            color: rgba(94,63,58,.55);
            margin-top: 10px;
        }
        .sm-warn {
            background: #fffbeb;
            border: 1px solid #fde68a;
            border-radius: 8px;
            padding: 12px 14px;
            font-size: 13px;
            color: #92400e;
        }

        /* ── Request cards (đề xuất đến / yêu cầu đã gửi) ──────────────── */
        .req-card {
            background: #fff;
            border: 1px solid var(--cgv-border, #e2d5d0);
            border-radius: 10px;
            padding: 16px 18px;
            margin-bottom: 8px;
        }
        .req-card-header {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-bottom: 8px;
        }
        .req-card-info { flex: 1; }
        .req-card-who {
            font-weight: 700;
            font-size: 13px;
            color: var(--cgv-dark, #3d2424);
        }
        .req-card-shift {
            font-size: 12px;
            color: rgba(94,63,58,.6);
            margin-top: 2px;
        }
        .req-card-msg {
            font-size: 12px;
            color: rgba(94,63,58,.7);
            font-style: italic;
            margin-top: 6px;
        }
        /* Lý do là chữ tự do, cắt bớt để không kéo giãn cả bảng — chữ đầy đủ nằm
           ở thuộc tính title khi rê chuột. Phải là div bên trong ô: max-width đặt
           thẳng lên <td> bị thuật toán dàn bảng bỏ qua. */
        .req-msg-cell {
            max-width: 220px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            font-style: italic;
            color: rgba(94,63,58,.7);
            font-size: 12px;
        }

        .status-pill {
            display: inline-block;
            padding: 2px 10px;
            border-radius: 20px;
            font-size: 11px;
            font-weight: 700;
            white-space: nowrap;
        }
        .status-pending  { background: #fef3c7; color: #92400e; }
        .status-accepted { background: #d1fae5; color: #065f46; }
        .status-rejected { background: #fee2e2; color: #991b1b; }
        .status-cancelled{ background: #f3f4f6; color: #6b7280; }
    </style>
</head>
<body class="cgv-body">

<%@ include file="_sidebar.jsp" %>

<div class="cgv-main">

    <header class="cgv-header">
        <h1 class="cgv-header-title">Ca Làm Việc Của Tôi</h1>
        <div class="cgv-header-right">
            <%@ include file="_notifications.jsp" %>
            <div class="cgv-user-wrap">
                <div class="cgv-avatar">EM</div>
                <span class="cgv-user-name">${sessionScope.account.fullName}</span>
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

            <%-- Lịch ca theo tháng ─────────────────────────────────────── --%>
            <div class="cal-month-nav">
                <a href="${pageContext.request.contextPath}/employee/my-shifts?year=${prevYear}&month=${prevMonth}"
                   title="Tháng trước">&#8249;</a>
                <span>
                    <c:choose>
                        <c:when test="${selMonth eq 1}">Tháng 1</c:when>
                        <c:when test="${selMonth eq 2}">Tháng 2</c:when>
                        <c:when test="${selMonth eq 3}">Tháng 3</c:when>
                        <c:when test="${selMonth eq 4}">Tháng 4</c:when>
                        <c:when test="${selMonth eq 5}">Tháng 5</c:when>
                        <c:when test="${selMonth eq 6}">Tháng 6</c:when>
                        <c:when test="${selMonth eq 7}">Tháng 7</c:when>
                        <c:when test="${selMonth eq 8}">Tháng 8</c:when>
                        <c:when test="${selMonth eq 9}">Tháng 9</c:when>
                        <c:when test="${selMonth eq 10}">Tháng 10</c:when>
                        <c:when test="${selMonth eq 11}">Tháng 11</c:when>
                        <c:otherwise>Tháng 12</c:otherwise>
                    </c:choose>
                    ${selYear}
                </span>
                <a href="${pageContext.request.contextPath}/employee/my-shifts?year=${nextYear}&month=${nextMonth}"
                   title="Tháng sau">&#8250;</a>
            </div>

            <div class="cal-weekday-row">
                <div class="cal-weekday">T2</div>
                <div class="cal-weekday">T3</div>
                <div class="cal-weekday">T4</div>
                <div class="cal-weekday">T5</div>
                <div class="cal-weekday">T6</div>
                <div class="cal-weekday">T7</div>
                <div class="cal-weekday">CN</div>
            </div>

            <%-- Ô lịch dựng bằng JS từ mảng SHIFTS ở cuối trang --%>
            <div id="cal-grid" class="cal-grid"></div>

            <div id="cal-empty" style="display:none;text-align:center;padding:32px 24px;color:rgba(94,63,58,.4);font-family:var(--font-cgv-ui);">
                Không có ca làm việc nào trong tháng này.
            </div>

            <div class="cal-legend">
                <div class="cal-legend-item">
                    <span class="cal-legend-dot" style="background:#3b82f6;"></span>Đã lên lịch
                </div>
                <div class="cal-legend-item">
                    <span class="cal-legend-dot" style="background:#10b981;"></span>Đã làm
                </div>
                <div class="cal-legend-item">
                    <span class="cal-legend-dot" style="background:#dc2626;"></span>Vắng
                </div>
                <div class="cal-legend-item">
                    <span class="cal-legend-dot" style="background:#9ca3af;"></span>Đã qua, chưa chấm công
                </div>
                <div class="cal-legend-item">
                    <span class="cal-legend-dot" style="background:#fff;box-shadow:0 0 0 2px #f59e0b inset;"></span>Đang chờ duyệt đổi ca
                </div>
            </div>

            <div style="font-size:12px;color:rgba(94,63,58,.5);margin-top:12px;">
                Bấm vào một ca trên lịch để xem chi tiết và gửi yêu cầu chuyển ca.
                Ca chỉ đổi người sau khi quản lý duyệt.
            </div>

            <%-- Ca người khác đề xuất chuyển cho mình. Chỉ để xem: quản lý mới là
                 người duyệt, nên ở đây không có nút nhận/từ chối. ─────────── --%>
            <c:if test="${not empty incoming}">
                <div class="ms-section-title">CA ĐƯỢC ĐỀ XUẤT CHUYỂN CHO BẠN (${fn:length(incoming)} chờ quản lý duyệt)</div>
                <c:forEach var="req" items="${incoming}">
                    <div class="req-card">
                        <div class="req-card-header">
                            <div class="req-card-info">
                                <div class="req-card-who">
                                    ${req.requesterName} muốn chuyển ca cho bạn
                                </div>
                                <div class="req-card-shift">
                                    ${req.shiftDate} &nbsp;|&nbsp;
                                    ${req.shiftStart} – ${req.shiftEnd}
                                </div>
                            </div>
                            <span class="status-pill status-pending">Chờ quản lý duyệt</span>
                        </div>
                        <c:if test="${not empty req.message}">
                            <div class="req-card-msg">"${req.message}"</div>
                        </c:if>
                        <div style="font-size:12px;color:rgba(94,63,58,.55);margin-top:10px;">
                            Ca chỉ chuyển sang cho bạn sau khi quản lý duyệt. Bạn sẽ nhận được thông báo khi có kết quả.
                        </div>
                    </div>
                </c:forEach>
            </c:if>

            <%-- Lịch sử yêu cầu mình đã gửi. Danh sách này chỉ dài thêm theo thời
                 gian nên để dạng bảng + phân trang thay vì đổ hết ra thẻ. ──── --%>
            <c:if test="${reqTotal gt 0}">
                <div class="ms-section-title" id="req-list">YÊU CẦU ĐÃ GỬI (${reqTotal})</div>
                <div class="cgv-data-wrap">
                    <table class="cgv-dt">
                        <thead>
                            <tr>
                                <th>Ca làm việc</th>
                                <th>Gửi cho</th>
                                <th>Lý do</th>
                                <th>Ngày gửi</th>
                                <th>Trạng thái</th>
                                <th></th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="req" items="${outgoing}">
                                <tr>
                                    <td style="font-weight:500;white-space:nowrap;">
                                        ${req.shiftDate}
                                        <div style="font-size:12px;color:var(--cgv-primary);font-weight:600;">
                                            ${req.shiftStart} – ${req.shiftEnd}
                                        </div>
                                    </td>
                                    <td>${req.targetName}</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty req.message}">
                                                <div class="req-msg-cell" title="${req.message}">${req.message}</div>
                                            </c:when>
                                            <c:otherwise>—</c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td style="font-size:13px;white-space:nowrap;">${req.createdAtDisplay}</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${req.status eq 'Pending'}">
                                                <span class="status-pill status-pending">Chờ quản lý duyệt</span>
                                            </c:when>
                                            <c:when test="${req.status eq 'Accepted'}">
                                                <span class="status-pill status-accepted">Đã duyệt</span>
                                            </c:when>
                                            <c:when test="${req.status eq 'Rejected'}">
                                                <span class="status-pill status-rejected">Đã từ chối</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="status-pill status-cancelled">Đã hủy</span>
                                            </c:otherwise>
                                        </c:choose>
                                        <c:if test="${not empty req.respondedAtDisplay}">
                                            <div style="font-size:11px;color:rgba(94,63,58,.45);margin-top:3px;white-space:nowrap;">
                                                ${req.respondedAtDisplay}
                                            </div>
                                        </c:if>
                                    </td>
                                    <td>
                                        <c:if test="${req.status eq 'Pending'}">
                                            <form method="post" action="${pageContext.request.contextPath}/employee/my-shifts" style="display:inline;">
                                                <input type="hidden" name="action"    value="cancel_exchange">
                                                <input type="hidden" name="requestId" value="${req.requestId}">
                                                <button type="submit" class="btn--cgv-outline" style="font-size:12px;padding:6px 12px;white-space:nowrap;">
                                                    Hủy
                                                </button>
                                            </form>
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>

                    <div class="cgv-pager">
                        <span>
                            <c:choose>
                                <c:when test="${not empty reqTotal and reqTotal gt 0}">
                                    Hiển thị ${(reqPage - 1) * 10 + 1} – ${reqPage * 10 gt reqTotal ? reqTotal : reqPage * 10} / ${reqTotal} yêu cầu
                                </c:when>
                                <c:otherwise>
                                    Hiển thị 0 yêu cầu
                                </c:otherwise>
                            </c:choose>
                        </span>
                        <c:if test="${reqTotalPages gt 1}">
                            <div class="cgv-pager-pages">
                                <button type="button" class="cgv-pager-btn" ${reqPage eq 1 ? 'disabled' : ''}
                                        onclick="location.href='${pageContext.request.contextPath}/employee/my-shifts?year=${selYear}&month=${selMonth}&reqPage=${reqPage - 1}#req-list'">&lsaquo;</button>
                                <c:forEach begin="1" end="${reqTotalPages}" var="pg">
                                    <button type="button" class="cgv-pager-btn ${pg eq reqPage ? 'active' : ''}"
                                            onclick="location.href='${pageContext.request.contextPath}/employee/my-shifts?year=${selYear}&month=${selMonth}&reqPage=${pg}#req-list'">${pg}</button>
                                </c:forEach>
                                <button type="button" class="cgv-pager-btn" ${reqPage eq reqTotalPages ? 'disabled' : ''}
                                        onclick="location.href='${pageContext.request.contextPath}/employee/my-shifts?year=${selYear}&month=${selMonth}&reqPage=${reqPage + 1}#req-list'">&rsaquo;</button>
                            </div>
                        </c:if>
                    </div>
                </div>
            </c:if>

        </div>

    </div>
</div>

<%-- Chi tiết một ca + form chuyển ca ─────────────────────────────────── --%>
<div id="sm-overlay" class="sm-overlay" onclick="closeShiftModal(event)">
    <div class="sm-box" onclick="event.stopPropagation();">
        <button type="button" class="sm-close" onclick="closeShiftModal()" title="Đóng">&#x2715;</button>

        <div class="sm-date" id="sm-date"></div>
        <div class="sm-time" id="sm-time"></div>
        <div id="sm-status"></div>

        <%-- Ca đang có yêu cầu chuyển chờ duyệt --%>
        <div class="sm-block" id="sm-pending" style="display:none;">
            <div class="sm-warn" id="sm-pending-text"></div>
            <form method="post" action="${pageContext.request.contextPath}/employee/my-shifts" style="margin-top:12px;">
                <input type="hidden" name="action"    value="cancel_exchange">
                <input type="hidden" name="requestId" id="sm-cancel-req" value="">
                <button type="submit" class="btn--cgv-outline" style="font-size:12px;padding:6px 14px;">
                    Hủy yêu cầu
                </button>
            </form>
        </div>

        <%-- Ca còn chuyển được --%>
        <div class="sm-block" id="sm-handoff" style="display:none;">
            <div class="ms-section-title" style="margin-top:0;">GỬI YÊU CẦU CHUYỂN CA</div>
            <form method="post" action="${pageContext.request.contextPath}/employee/my-shifts">
                <input type="hidden" name="action"  value="request_exchange">
                <input type="hidden" name="shiftId" id="sm-shift-id" value="">
                <div class="cgv-field">
                    <label class="cgv-label">Chuyển cho</label>
                    <select class="cgv-select" name="targetEmpId" style="height:38px;width:100%;" required>
                        <option value="">— Chọn đồng nghiệp —</option>
                        <c:forEach var="col" items="${colleagues}">
                            <c:if test="${col.accountId ne sessionScope.account.accountId}">
                                <option value="${col.accountId}">${col.fullName}</option>
                            </c:if>
                        </c:forEach>
                    </select>
                </div>
                <div class="cgv-field">
                    <label class="cgv-label">Lý do (tuỳ chọn)</label>
                    <input class="cgv-input" type="text" name="message" style="height:38px;"
                           placeholder="Ví dụ: bận việc gia đình">
                </div>
                <button type="submit" class="btn--cgv" style="width:100%;">Gửi yêu cầu</button>
                <div class="sm-hint">
                    Yêu cầu sẽ được gửi tới quản lý. Ca chỉ đổi người sau khi quản lý duyệt.
                </div>
            </form>
        </div>

        <%-- Ca đã qua hoặc đã chốt --%>
        <div class="sm-block" id="sm-note" style="display:none;">
            <div class="sm-hint" id="sm-note-text"></div>
        </div>
    </div>
</div>

<script>
/* ── Hằng số do server đưa xuống ─────────────────────────────────────── */
var YEAR  = ${selYear};
var MONTH = ${selMonth};
var TODAY = '${serverToday}';

/* ── Ca của tôi trong tháng đang xem ─────────────────────────────────── */
var SHIFTS = [
<c:forEach var="s" items="${myShifts}" varStatus="st"><c:if test="${!st.first}">,
</c:if>{id:${s.shiftId},date:'${s.shiftDate}',start:'${s.startTime}',end:'${s.endTime}',status:'${s.status}'}
</c:forEach>
];

/* Yêu cầu chuyển ca đang chờ duyệt, tra theo ShiftID. Lấy từ outgoingPending
   (không phân trang) chứ không phải danh sách bảng bên dưới, nếu không ca ở
   trang 2 trở đi sẽ mất dấu "đang chờ duyệt" trên lịch. */
var PENDING = {};
<c:forEach var="r" items="${outgoingPending}">
PENDING[${r.shiftId}] = {reqId:${r.requestId}, target:'${r.targetName}'};
</c:forEach>

var SHIFT_BY_ID = {};
SHIFTS.forEach(function(s) { SHIFT_BY_ID[s.id] = s; });

/* Cùng bảng màu với lịch phân ca của quản lý, nhưng ở đây phân theo trạng
   thái chứ không theo loại ca: nhân viên chỉ quan tâm ca của chính mình. */
function shiftColor(s) {
    if (s.status === 'Absent')    return '#dc2626';
    if (s.status === 'Completed') return '#10b981';
    if (s.date < TODAY)           return '#9ca3af';
    return '#3b82f6';
}

function statusLabel(s) {
    if (s.status === 'Absent')    return 'Vắng';
    if (s.status === 'Completed') return 'Đã làm';
    if (s.date < TODAY)           return 'Chưa chấm công';
    if (s.date === TODAY)         return 'Hôm nay';
    return 'Đã lên lịch';
}

function hhmm(t) {
    return t ? t.substring(0, 5) : '';
}

/* Ca chỉ chuyển được khi còn ở tương lai và chưa bị chốt trạng thái —
   cùng điều kiện mà EmployeeDashboardServlet.isShiftExchangeable áp dụng
   ở phía server, nên nút hiện ra ở đây thì server cũng sẽ chấp nhận. */
function canHandoff(s) {
    return s.status === 'Scheduled' && s.date >= TODAY;
}

/* ── Dựng lưới lịch ──────────────────────────────────────────────────── */
function buildCalendar() {
    var grid = document.getElementById('cal-grid');
    if (!grid) return;

    var byDate = {};
    SHIFTS.forEach(function(s) {
        if (!byDate[s.date]) byDate[s.date] = [];
        byDate[s.date].push(s);
    });

    var firstDay    = new Date(YEAR, MONTH - 1, 1);
    var daysInMonth = new Date(YEAR, MONTH, 0).getDate();
    var startOffset = (firstDay.getDay() + 6) % 7; // Thứ 2 = 0 … Chủ nhật = 6
    var totalCells  = Math.ceil((startOffset + daysInMonth) / 7) * 7;

    var html = '';
    var dayNum = 1;

    for (var i = 0; i < totalCells; i++) {
        if (i < startOffset || dayNum > daysInMonth) {
            html += '<div class="cal-day empty"></div>';
            continue;
        }

        var dateStr = YEAR + '-' + String(MONTH).padStart(2, '0')
                    + '-' + String(dayNum).padStart(2, '0');
        var cls = 'cal-day';
        if (dateStr === TODAY) cls += ' today';
        else if (dateStr < TODAY) cls += ' past';

        html += '<div class="' + cls + '">';
        html += '<div class="cal-day-num">' + dayNum + '</div>';

        (byDate[dateStr] || []).forEach(function(s) {
            var extra = PENDING[s.id] ? ' has-request' : '';
            html += '<button type="button" id="shift-' + s.id + '"'
                  + ' class="cal-shift' + extra + '"'
                  + ' style="background:' + shiftColor(s) + '"'
                  + ' onclick="openShiftModal(' + s.id + ')">'
                  + '<span class="cal-shift-time">' + hhmm(s.start) + '–' + hhmm(s.end) + '</span>'
                  + '<span class="cal-shift-label">' + statusLabel(s) + '</span>'
                  + '</button>';
        });

        html += '</div>';
        dayNum++;
    }

    grid.innerHTML = html;
    document.getElementById('cal-empty').style.display = SHIFTS.length ? 'none' : 'block';
}

/* ── Modal chi tiết ca ───────────────────────────────────────────────── */
function openShiftModal(shiftId) {
    var s = SHIFT_BY_ID[shiftId];
    if (!s) return;

    document.getElementById('sm-date').textContent = 'Ca ngày ' + s.date;
    document.getElementById('sm-time').textContent = hhmm(s.start) + ' – ' + hhmm(s.end);
    document.getElementById('sm-status').innerHTML =
        '<span class="status-pill" style="background:' + shiftColor(s) + ';color:#fff;">'
        + statusLabel(s) + '</span>';

    var pending  = document.getElementById('sm-pending');
    var handoff  = document.getElementById('sm-handoff');
    var note     = document.getElementById('sm-note');
    pending.style.display = 'none';
    handoff.style.display = 'none';
    note.style.display    = 'none';

    var req = PENDING[s.id];
    if (req) {
        document.getElementById('sm-pending-text').textContent =
            'Đang chờ quản lý duyệt chuyển ca này cho ' + req.target + '.';
        document.getElementById('sm-cancel-req').value = req.reqId;
        pending.style.display = 'block';
    } else if (canHandoff(s)) {
        document.getElementById('sm-shift-id').value = s.id;
        handoff.style.display = 'block';
    } else {
        document.getElementById('sm-note-text').textContent = s.date < TODAY
            ? 'Ca đã qua nên không thể chuyển cho người khác.'
            : 'Ca này đã được chốt trạng thái nên không thể chuyển.';
        note.style.display = 'block';
    }

    document.getElementById('sm-overlay').classList.add('open');
}

// Hộp thoại tự chặn sự kiện của chính nó (stopPropagation ở .sm-box), nên hàm
// này chỉ chạy khi bấm ra nền hoặc bấm nút đóng.
function closeShiftModal() {
    document.getElementById('sm-overlay').classList.remove('open');
}

document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        document.getElementById('sm-overlay').classList.remove('open');
    }
});

buildCalendar();
</script>
</body>
</html>
