<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% request.setAttribute("activeNav", "shifts"); %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Chỉnh Sửa Ca — CGV Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
</head>
<body class="cgv-body">

<%@ include file="../_sidebar.jsp" %>

<div class="cgv-main">

    <header class="cgv-header">
        <h1 class="cgv-header-title">Chỉnh Sửa Ca Làm Việc</h1>
        <div class="cgv-header-right">
            <div class="cgv-header-actions">
                <a href="${pageContext.request.contextPath}/manager/shifts?empId=${shift.employeeId}&year=${param.year}&month=${param.month}"
                   class="btn--cgv-outline" style="margin-right:8px;">
                    ← Quay lại lịch
                </a>
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
        <div class="cgv-list-wrap" style="max-width:520px;">

            <c:if test="${empty shift}">
                <div class="cgv-alert cgv-alert-danger">Ca làm việc không tồn tại.</div>
            </c:if>

            <c:if test="${not empty shift}">
                <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:32px;">
                    <div style="font-family:var(--font-cgv-ui);font-size:10px;font-weight:700;letter-spacing:2px;text-transform:uppercase;color:rgba(94,63,58,0.5);margin-bottom:24px;">
                        THÔNG TIN CA LÀM VIỆC
                    </div>

                    <%-- Read-only shift info --%>
                    <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;margin-bottom:24px;padding:16px;background:#fafafa;border:1px solid var(--cgv-border);border-radius:8px;">
                        <div>
                            <div style="font-family:var(--font-cgv-ui);font-size:10px;font-weight:700;letter-spacing:.05em;color:rgba(94,63,58,.45);margin-bottom:4px;">NHÂN VIÊN</div>
                            <div style="font-size:14px;font-weight:600;">
                                ${not empty shift.employeeName ? shift.employeeName : shift.employeeEmail}
                            </div>
                        </div>
                        <div>
                            <div style="font-family:var(--font-cgv-ui);font-size:10px;font-weight:700;letter-spacing:.05em;color:rgba(94,63,58,.45);margin-bottom:4px;">NGÀY</div>
                            <div style="font-size:14px;font-weight:600;">${shift.shiftDate}</div>
                        </div>
                        <div>
                            <div style="font-family:var(--font-cgv-ui);font-size:10px;font-weight:700;letter-spacing:.05em;color:rgba(94,63,58,.45);margin-bottom:4px;">GIỜ VÀO</div>
                            <div style="font-size:14px;font-weight:600;">${shift.startTime}</div>
                        </div>
                        <div>
                            <div style="font-family:var(--font-cgv-ui);font-size:10px;font-weight:700;letter-spacing:.05em;color:rgba(94,63,58,.45);margin-bottom:4px;">GIỜ RA</div>
                            <div style="font-size:14px;font-weight:600;">${shift.endTime}</div>
                        </div>
                    </div>

                    <%-- Editable fields --%>
                    <form method="post" action="${pageContext.request.contextPath}/manager/shifts">
                        <input type="hidden" name="action"  value="update">
                        <input type="hidden" name="shiftId" value="${shift.shiftId}">
                        <input type="hidden" name="year"    value="${param.year}">
                        <input type="hidden" name="month"   value="${param.month}">

                        <div class="cgv-field">
                            <label class="cgv-label">Trạng Thái</label>
                            <select class="cgv-select" name="status" style="width:100%;">
                                <option value="Scheduled" ${shift.status eq 'Scheduled' ? 'selected' : ''}>Scheduled — Đã lên lịch</option>
                                <option value="Completed" ${shift.status eq 'Completed' ? 'selected' : ''}>Completed — Đã làm việc</option>
                                <option value="Absent"    ${shift.status eq 'Absent'    ? 'selected' : ''}>Absent — Vắng</option>
                            </select>
                        </div>

                        <div class="cgv-field">
                            <label class="cgv-label">Ghi Chú</label>
                            <input class="cgv-input" type="text" name="notes"
                                   value="${shift.notes}" placeholder="Ghi chú tùy chọn...">
                        </div>

                        <div style="display:flex;gap:12px;margin-top:24px;padding-top:20px;border-top:1px solid var(--cgv-border);">
                            <button type="submit" class="btn--cgv">Lưu thay đổi</button>
                            <a href="${pageContext.request.contextPath}/manager/shifts?empId=${shift.employeeId}&year=${param.year}&month=${param.month}"
                               class="btn--cgv-outline">Hủy</a>
                        </div>
                    </form>
                </div>
            </c:if>

        </div>
    </div>
</div>
</body>
</html>
