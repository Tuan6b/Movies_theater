<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% request.setAttribute("activeNav", "employees"); %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${pageTitle} — CGV Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
</head>
<body class="cgv-body">

<%@ include file="../_sidebar.jsp" %>

<div class="cgv-main">

    <header class="cgv-header">
        <h1 class="cgv-header-title">${pageTitle}</h1>
        <div class="cgv-header-right">
            <div class="cgv-header-actions">
                <a href="${pageContext.request.contextPath}/manager/employees"
                   class="btn--cgv-outline" style="margin-right:8px;">
                    ← Back to Employees
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
        <div class="cgv-list-wrap" style="max-width:680px;">

            <c:if test="${not empty flashSuccess}">
                <div class="cgv-alert cgv-alert-success">${flashSuccess}</div>
            </c:if>
            <c:if test="${not empty flashError}">
                <div class="cgv-alert cgv-alert-danger">${flashError}</div>
            </c:if>
            <c:if test="${not empty errorMsg}">
                <div class="cgv-alert cgv-alert-danger">${errorMsg}</div>
            </c:if>

            <%-- Employee Info Form --%>
            <div style="background:#fff;border:1px solid var(--cgv-border);border-radius:12px;padding:32px;margin-bottom:24px;">
                <div style="font-family:var(--font-cgv-ui);font-size:10px;font-weight:700;letter-spacing:2px;text-transform:uppercase;color:rgba(94,63,58,0.5);margin-bottom:24px;">
                    EMPLOYEE DETAILS
                </div>

                <form method="post" action="${pageContext.request.contextPath}/manager/employees">
                    <input type="hidden" name="action" value="${formAction}">
                    <c:if test="${formAction eq 'update'}">
                        <input type="hidden" name="accountId" value="${employee.accountId}">
                    </c:if>

                    <div style="display:grid;grid-template-columns:1fr 1fr;gap:16px;">

                        <div class="cgv-field">
                            <label class="cgv-label">Full Name <span style="color:var(--cgv-red)">*</span></label>
                            <input class="cgv-input" type="text" name="fullName"
                                   value="${employee.fullName}" placeholder="Enter full name">
                            <c:if test="${not empty errors['fullName']}">
                                <div style="font-size:12px;color:var(--cgv-red);margin-top:4px;">${errors['fullName']}</div>
                            </c:if>
                        </div>

                        <div class="cgv-field">
                            <label class="cgv-label">Email <span style="color:var(--cgv-red)">*</span></label>
                            <input class="cgv-input" type="email" name="email"
                                   value="${employee.email}" placeholder="Enter email address">
                            <c:if test="${not empty errors['email']}">
                                <div style="font-size:12px;color:var(--cgv-red);margin-top:4px;">${errors['email']}</div>
                            </c:if>
                        </div>

                        <div class="cgv-field">
                            <label class="cgv-label">Phone Number <span style="color:var(--cgv-red)">*</span></label>
                            <input class="cgv-input" type="text" name="phoneNumber"
                                   value="${employee.phoneNumber}" placeholder="VD: 0901234567"
                                   maxlength="20" inputmode="tel">
                            <c:choose>
                                <c:when test="${not empty errors['phoneNumber']}">
                                    <div style="font-size:12px;color:var(--cgv-red);margin-top:4px;">${errors['phoneNumber']}</div>
                                </c:when>
                                <c:otherwise>
                                    <div style="font-size:12px;color:rgba(94,63,58,0.45);margin-top:4px;">10 chữ số, bắt đầu bằng 0, không trùng nhân viên khác</div>
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <div class="cgv-field">
                            <label class="cgv-label">Date of Birth <span style="color:var(--cgv-red)">*</span></label>
                            <input class="cgv-input" type="date" name="dateOfBirth"
                                   value="${employee.dateOfBirth}">
                            <c:choose>
                                <c:when test="${not empty errors['dateOfBirth']}">
                                    <div style="font-size:12px;color:var(--cgv-red);margin-top:4px;">${errors['dateOfBirth']}</div>
                                </c:when>
                                <c:otherwise>
                                    <div style="font-size:12px;color:rgba(94,63,58,0.45);margin-top:4px;">Phải đủ 18 tuổi theo Bộ luật Lao động</div>
                                </c:otherwise>
                            </c:choose>
                        </div>

                    </div>

                    <div class="cgv-field">
                        <label class="cgv-label">Address</label>
                        <input class="cgv-input" type="text" name="address"
                               value="${employee.address}" placeholder="Enter home address" maxlength="255">
                        <c:if test="${not empty errors['address']}">
                            <div style="font-size:12px;color:var(--cgv-red);margin-top:4px;">${errors['address']}</div>
                        </c:if>
                    </div>

                    <c:if test="${formAction eq 'create'}">
                        <div style="background:#fef9c3;border:1px solid #fde047;border-radius:8px;padding:10px 14px;font-size:12px;color:#713f12;">
                            Mật khẩu tạm thời sẽ được tạo tự động và hiển thị sau khi lưu. Nhân viên chỉ cần đổi mật khẩu khi đăng nhập lần đầu, nên hãy nhập đầy đủ thông tin cá nhân ở đây.
                        </div>
                    </c:if>

                    <div style="display:flex;gap:12px;margin-top:24px;padding-top:20px;border-top:1px solid var(--cgv-border);">
                        <button type="submit" class="btn--cgv">Save Employee</button>
                        <a href="${pageContext.request.contextPath}/manager/employees"
                           class="btn--cgv-outline">Cancel</a>
                    </div>

                </form>
            </div>

        </div>

        <%-- Aside: shift info for existing employee --%>
        <c:if test="${formAction eq 'update'}">
            <aside class="cgv-aside">
                <div class="cgv-stats-section">
                    <a href="${pageContext.request.contextPath}/manager/shifts?empId=${employee.accountId}"
                       class="btn--cgv" style="width:100%;text-align:center;display:block;">
                        View Shifts
                    </a>
                    <div style="margin-top:8px;">
                        <a href="${pageContext.request.contextPath}/manager/shifts?empId=${employee.accountId}"
                           class="btn--cgv-outline" style="width:100%;text-align:center;display:block;">
                            + Schedule Shift
                        </a>
                    </div>
                </div>
            </aside>
        </c:if>

    </div>
</div>
</body>
</html>
