<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% request.setAttribute("activeNav", "shift-exchanges"); %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Duyệt Đổi Ca — CGV Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
    <style>
        .sx-status {
            display: inline-block;
            padding: 2px 10px;
            border-radius: 20px;
            font-size: 11px;
            font-weight: 700;
            white-space: nowrap;
        }
        .sx-pending  { background: #fef3c7; color: #92400e; }
        .sx-accepted { background: #d1fae5; color: #065f46; }
        .sx-rejected { background: #fee2e2; color: #991b1b; }
        .sx-cancelled{ background: #f3f4f6; color: #6b7280; }

        .sx-move {
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 13px;
        }
        .sx-move-arrow { color: var(--cgv-red); font-weight: 700; }
        .sx-msg {
            font-size: 12px;
            color: rgba(94,63,58,.6);
            font-style: italic;
            max-width: 220px;
        }
        .sx-count-badge {
            display: inline-block;
            min-width: 20px;
            padding: 1px 7px;
            margin-left: 6px;
            border-radius: 999px;
            background: var(--cgv-red);
            color: #fff;
            font-size: 11px;
            font-weight: 700;
        }
        .sx-past {
            font-size: 11px;
            color: #b45309;
            font-weight: 600;
            display: block;
            margin-top: 2px;
        }
    </style>
</head>
<body class="cgv-body">

<%@ include file="../_sidebar.jsp" %>

<div class="cgv-main">

    <header class="cgv-header">
        <h1 class="cgv-header-title">Duyệt Đổi Ca</h1>
        <div class="cgv-header-right">
            <div class="cgv-header-actions">
                <a href="${pageContext.request.contextPath}/manager/shifts"
                   class="btn--cgv-outline" style="margin-right:8px;">Lịch phân ca</a>
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
        <div class="cgv-table-wrap">

            <c:if test="${not empty flashSuccess}">
                <div class="cgv-alert cgv-alert-success">${flashSuccess}</div>
            </c:if>
            <c:if test="${not empty flashError}">
                <div class="cgv-alert cgv-alert-danger">${flashError}</div>
            </c:if>

            <div class="cgv-toolbar">
                <div class="cgv-pills">
                    <a href="${pageContext.request.contextPath}/manager/shift-exchanges?status=Pending"
                       class="cgv-pill ${selectedStatus eq 'Pending' ? 'active' : ''}">
                        Chờ duyệt
                        <c:if test="${pendingCount gt 0}">
                            <span class="sx-count-badge"
                                  style="${selectedStatus eq 'Pending' ? 'background:#fff;color:var(--cgv-red);' : ''}">${pendingCount}</span>
                        </c:if>
                    </a>
                    <a href="${pageContext.request.contextPath}/manager/shift-exchanges?status=Accepted"
                       class="cgv-pill ${selectedStatus eq 'Accepted' ? 'active' : ''}">Đã duyệt</a>
                    <a href="${pageContext.request.contextPath}/manager/shift-exchanges?status=Rejected"
                       class="cgv-pill ${selectedStatus eq 'Rejected' ? 'active' : ''}">Đã từ chối</a>
                    <a href="${pageContext.request.contextPath}/manager/shift-exchanges?status=Cancelled"
                       class="cgv-pill ${selectedStatus eq 'Cancelled' ? 'active' : ''}">Đã hủy</a>
                    <a href="${pageContext.request.contextPath}/manager/shift-exchanges?status=all"
                       class="cgv-pill ${selectedStatus eq 'all' ? 'active' : ''}">Tất cả</a>
                </div>
            </div>

            <div class="cgv-data-wrap">
                <table class="cgv-dt">
                    <thead>
                        <tr>
                            <th>#</th>
                            <th>Ca làm việc</th>
                            <th>Chuyển ca</th>
                            <th>Lý do</th>
                            <th>Ngày gửi</th>
                            <th>Trạng thái</th>
                            <th>Xử lý</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty requests}">
                                <c:forEach var="req" items="${requests}" varStatus="st">
                                    <tr>
                                        <td style="color:rgba(94,63,58,0.5);font-size:12px;">${st.index + 1}</td>
                                        <td style="font-weight:500;">
                                            ${req.shiftDate}
                                            <div style="font-size:12px;color:var(--cgv-red);font-weight:600;">
                                                ${req.shiftStart} – ${req.shiftEnd}
                                            </div>
                                            <%-- A shift that has already started can no longer be swapped
                                                 usefully, so flag it instead of hiding the row. --%>
                                            <c:if test="${req.status eq 'Pending' and req.shiftDate lt serverToday}">
                                                <span class="sx-past">Ca đã qua</span>
                                            </c:if>
                                        </td>
                                        <td>
                                            <div class="sx-move">
                                                <span>${req.requesterName}</span>
                                                <span class="sx-move-arrow">→</span>
                                                <span style="font-weight:600;">${req.targetName}</span>
                                            </div>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty req.message}">
                                                    <div class="sx-msg">"${req.message}"</div>
                                                </c:when>
                                                <c:otherwise>—</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="font-size:13px;white-space:nowrap;">${req.createdAtDisplay}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${req.status eq 'Pending'}">
                                                    <span class="sx-status sx-pending">Chờ duyệt</span>
                                                </c:when>
                                                <c:when test="${req.status eq 'Accepted'}">
                                                    <span class="sx-status sx-accepted">Đã duyệt</span>
                                                </c:when>
                                                <c:when test="${req.status eq 'Rejected'}">
                                                    <span class="sx-status sx-rejected">Đã từ chối</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="sx-status sx-cancelled">Đã hủy</span>
                                                </c:otherwise>
                                            </c:choose>
                                            <c:if test="${not empty req.respondedAtDisplay}">
                                                <div style="font-size:11px;color:rgba(94,63,58,.45);margin-top:3px;white-space:nowrap;">
                                                    ${req.respondedAtDisplay}
                                                </div>
                                            </c:if>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${req.status eq 'Pending'}">
                                                    <div style="display:flex;gap:6px;flex-wrap:wrap;">
                                                        <form method="post"
                                                              action="${pageContext.request.contextPath}/manager/shift-exchanges"
                                                              style="display:inline;">
                                                            <input type="hidden" name="action"    value="approve">
                                                            <input type="hidden" name="requestId" value="${req.requestId}">
                                                            <input type="hidden" name="status"    value="${selectedStatus}">
                                                            <button type="submit" class="btn--cgv"
                                                                    onclick="return confirm('Duyệt chuyển ca ngày ${req.shiftDate} từ ${req.requesterName} sang ${req.targetName}?')">
                                                                Duyệt
                                                            </button>
                                                        </form>
                                                        <form method="post"
                                                              action="${pageContext.request.contextPath}/manager/shift-exchanges"
                                                              style="display:inline;">
                                                            <input type="hidden" name="action"    value="reject">
                                                            <input type="hidden" name="requestId" value="${req.requestId}">
                                                            <input type="hidden" name="status"    value="${selectedStatus}">
                                                            <button type="submit" class="btn--cgv-outline"
                                                                    style="color:var(--cgv-red);border-color:var(--cgv-red);"
                                                                    onclick="return confirm('Từ chối yêu cầu đổi ca này?')">
                                                                Từ chối
                                                            </button>
                                                        </form>
                                                    </div>
                                                </c:when>
                                                <c:otherwise>
                                                    <span style="color:rgba(94,63,58,0.4);font-size:12px;">Đã xử lý</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="7" style="text-align:center;padding:48px;color:rgba(94,63,58,0.4);">
                                        <c:choose>
                                            <c:when test="${selectedStatus eq 'Pending'}">
                                                Không có yêu cầu đổi ca nào đang chờ duyệt.
                                            </c:when>
                                            <c:otherwise>
                                                Không có yêu cầu đổi ca nào.
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>

        </div>

        <aside class="cgv-aside">
            <div class="cgv-stats-section">
                <div class="cgv-aside-heading">CÁCH HOẠT ĐỘNG</div>
                <div style="font-size:12px;color:rgba(94,63,58,.6);line-height:1.7;margin-top:8px;">
                    Nhân viên gửi yêu cầu chuyển ca và chọn người nhận. Ca chỉ đổi chủ
                    khi quản lý bấm <b>Duyệt</b> — người nhận không tự xác nhận được.
                </div>
            </div>

            <div class="cgv-aside-divider">
                <div class="cgv-aside-heading">ĐIỀU HƯỚNG</div>
                <div style="display:flex;flex-direction:column;gap:8px;margin-top:8px;">
                    <a href="${pageContext.request.contextPath}/manager/shifts"
                       class="btn--cgv-outline" style="text-align:center;">Lịch phân ca</a>
                    <a href="${pageContext.request.contextPath}/manager/employees"
                       class="btn--cgv-outline" style="text-align:center;">Nhân viên</a>
                </div>
            </div>
        </aside>
    </div>
</div>
</body>
</html>
