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

        .shift-row {
            background: #fff;
            border: 1px solid var(--cgv-border, #e2d5d0);
            border-radius: 10px;
            padding: 14px 18px;
            margin-bottom: 8px;
            display: flex;
            align-items: center;
            gap: 16px;
        }
        .shift-row-date {
            font-family: var(--font-cgv-ui, sans-serif);
            font-size: 13px;
            font-weight: 700;
            color: var(--cgv-dark, #3d2424);
            min-width: 90px;
        }
        .shift-row-time {
            font-size: 13px;
            color: var(--cgv-primary, #c8253a);
            font-weight: 600;
            min-width: 130px;
        }
        .shift-row-status { flex: 1; }
        .shift-row-actions { flex-shrink: 0; }

        /* Handoff request panel (hidden by default) */
        .handoff-panel {
            display: none;
            background: #fef3f2;
            border: 1px solid #fca5a5;
            border-radius: 8px;
            padding: 14px 16px;
            margin-top: 8px;
        }
        .handoff-panel.open { display: block; }
        .handoff-panel-title {
            font-family: var(--font-cgv-ui, sans-serif);
            font-size: 12px;
            font-weight: 700;
            color: #991b1b;
            margin-bottom: 10px;
        }
        .handoff-panel-form {
            display: flex;
            gap: 8px;
            flex-wrap: wrap;
            align-items: center;
        }

        /* Incoming / outgoing request cards */
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
        .req-card-actions {
            display: flex;
            gap: 8px;
            margin-top: 10px;
        }

        .status-pill {
            display: inline-block;
            padding: 2px 10px;
            border-radius: 20px;
            font-size: 11px;
            font-weight: 700;
        }
        .status-pending  { background: #fef3c7; color: #92400e; }
        .status-accepted { background: #d1fae5; color: #065f46; }
        .status-rejected { background: #fee2e2; color: #991b1b; }
        .status-cancelled{ background: #f3f4f6; color: #6b7280; }

        .month-nav {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-bottom: 16px;
        }
        .month-nav a {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            width: 30px; height: 30px;
            border-radius: 6px;
            border: 1px solid var(--cgv-border, #e2d5d0);
            color: inherit;
            text-decoration: none;
            font-size: 16px;
            font-weight: 700;
        }
        .month-nav a:hover { background: #f5eeeb; }
        .month-nav span {
            font-family: var(--font-cgv-ui, sans-serif);
            font-size: 14px;
            font-weight: 700;
            color: #3d2424;
        }
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

            <%-- Incoming handoff requests ─────────────────────────────── --%>
            <c:if test="${not empty incoming}">
                <div class="ms-section-title">YÊU CẦU NHẬN CA (${fn:length(incoming)} đang chờ)</div>
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
                        </div>
                        <c:if test="${not empty req.message}">
                            <div class="req-card-msg">"${req.message}"</div>
                        </c:if>
                        <div class="req-card-actions">
                            <form method="post" action="${pageContext.request.contextPath}/employee/my-shifts" style="display:inline;">
                                <input type="hidden" name="action"    value="accept_exchange">
                                <input type="hidden" name="requestId" value="${req.requestId}">
                                <button type="submit" class="btn--cgv" style="font-size:12px;padding:6px 14px;"
                                        onclick="return confirm('Nhận ca ngày ${req.shiftDate}? Ca sẽ được chuyển sang cho bạn ngay.')">
                                    Nhận ca
                                </button>
                            </form>
                            <form method="post" action="${pageContext.request.contextPath}/employee/my-shifts" style="display:inline;">
                                <input type="hidden" name="action"    value="reject_exchange">
                                <input type="hidden" name="requestId" value="${req.requestId}">
                                <button type="submit" class="btn--cgv-outline" style="font-size:12px;padding:6px 14px;">
                                    Từ chối
                                </button>
                            </form>
                        </div>
                    </div>
                </c:forEach>
            </c:if>

            <%-- My shifts this month ──────────────────────────────────── --%>
            <div class="ms-section-title">CA CỦA TÔI</div>
            <div class="month-nav">
                <a href="${pageContext.request.contextPath}/employee/my-shifts?year=${prevYear}&month=${prevMonth}">&#8249;</a>
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
                <a href="${pageContext.request.contextPath}/employee/my-shifts?year=${nextYear}&month=${nextMonth}">&#8250;</a>
            </div>

            <c:choose>
                <c:when test="${not empty myShifts}">
                    <c:forEach var="shift" items="${myShifts}" varStatus="st">
                        <div id="shift-wrap-${shift.shiftId}">
                            <div class="shift-row">
                                <div class="shift-row-date">${shift.shiftDate}</div>
                                <div class="shift-row-time">${shift.startTime} – ${shift.endTime}</div>
                                <div class="shift-row-status">
                                    <c:choose>
                                        <c:when test="${shift.status eq 'Completed'}">
                                            <span class="cgv-badge active">Đã làm</span>
                                        </c:when>
                                        <c:when test="${shift.status eq 'Absent'}">
                                            <span class="cgv-badge inactive">Vắng</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="cgv-badge" style="background:#e0f2fe;color:#0369a1;">Đã lên lịch</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <div class="shift-row-actions">
                                    <%-- Only future scheduled shifts can be handed off --%>
                                    <c:if test="${shift.status eq 'Scheduled' and shift.shiftDate ge serverToday}">
                                        <button type="button" class="btn--cgv-outline" style="font-size:12px;padding:6px 12px;"
                                                onclick="toggleHandoff('${shift.shiftId}')">
                                            Chuyển ca
                                        </button>
                                    </c:if>
                                </div>
                            </div>

                            <%-- Handoff request panel for this shift --%>
                            <c:if test="${shift.status eq 'Scheduled' and shift.shiftDate ge serverToday}">
                                <div id="handoff-${shift.shiftId}" class="handoff-panel">
                                    <div class="handoff-panel-title">
                                        Chuyển ca ngày ${shift.shiftDate} (${shift.startTime}–${shift.endTime}) cho người khác
                                    </div>
                                    <form method="post" action="${pageContext.request.contextPath}/employee/my-shifts">
                                        <input type="hidden" name="action"  value="request_exchange">
                                        <input type="hidden" name="shiftId" value="${shift.shiftId}">
                                        <div class="handoff-panel-form">
                                            <select class="cgv-select" name="targetEmpId" style="height:36px;min-width:200px;" required>
                                                <option value="">— Chuyển cho —</option>
                                                <c:forEach var="col" items="${colleagues}">
                                                    <c:if test="${col.accountId ne sessionScope.account.accountId}">
                                                        <option value="${col.accountId}">${col.fullName}</option>
                                                    </c:if>
                                                </c:forEach>
                                            </select>
                                            <input class="cgv-input" type="text" name="message"
                                                   placeholder="Lý do (tuỳ chọn)" style="height:36px;min-width:200px;">
                                            <button type="submit" class="btn--cgv" style="font-size:12px;padding:6px 14px;">
                                                Gửi yêu cầu
                                            </button>
                                            <button type="button" class="btn--cgv-outline" style="font-size:12px;padding:6px 14px;"
                                                    onclick="toggleHandoff('${shift.shiftId}')">
                                                Hủy
                                            </button>
                                        </div>
                                    </form>
                                </div>
                            </c:if>
                        </div>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <div style="text-align:center;padding:48px 24px;color:rgba(94,63,58,.4);font-family:var(--font-cgv-ui);">
                        Không có ca làm việc nào trong tháng này.
                    </div>
                </c:otherwise>
            </c:choose>

            <%-- Outgoing requests I have sent ────────────────────────── --%>
            <c:if test="${not empty outgoing}">
                <div class="ms-section-title">YÊU CẦU ĐÃ GỬI</div>
                <c:forEach var="req" items="${outgoing}">
                    <div class="req-card">
                        <div class="req-card-header">
                            <div class="req-card-info">
                                <div class="req-card-who">
                                    Gửi cho ${req.targetName}
                                </div>
                                <div class="req-card-shift">
                                    Ca ngày ${req.shiftDate} &nbsp;|&nbsp; ${req.shiftStart} – ${req.shiftEnd}
                                </div>
                            </div>
                            <c:choose>
                                <c:when test="${req.status eq 'Pending'}">
                                    <span class="status-pill status-pending">Đang chờ</span>
                                </c:when>
                                <c:when test="${req.status eq 'Accepted'}">
                                    <span class="status-pill status-accepted">Đã chấp nhận</span>
                                </c:when>
                                <c:when test="${req.status eq 'Rejected'}">
                                    <span class="status-pill status-rejected">Đã từ chối</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="status-pill status-cancelled">Đã hủy</span>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <c:if test="${not empty req.message}">
                            <div class="req-card-msg">"${req.message}"</div>
                        </c:if>
                        <c:if test="${req.status eq 'Pending'}">
                            <div class="req-card-actions">
                                <form method="post" action="${pageContext.request.contextPath}/employee/my-shifts" style="display:inline;">
                                    <input type="hidden" name="action"    value="cancel_exchange">
                                    <input type="hidden" name="requestId" value="${req.requestId}">
                                    <button type="submit" class="btn--cgv-outline" style="font-size:12px;padding:6px 14px;">
                                        Hủy yêu cầu
                                    </button>
                                </form>
                            </div>
                        </c:if>
                    </div>
                </c:forEach>
            </c:if>

        </div>
    </div>
</div>

<script>
function toggleHandoff(shiftId) {
    var panel = document.getElementById('handoff-' + shiftId);
    if (!panel) return;
    panel.classList.toggle('open');
}
</script>
</body>
</html>
