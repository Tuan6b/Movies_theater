<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% request.setAttribute("activeNav", "unlock"); %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Yêu cầu mở khóa — Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
</head>
<body class="cgv-body">
<%@ include file="_sidebar.jsp" %>
<div class="cgv-main">
    <header class="cgv-header">
        <h1 class="cgv-header-title">Yêu cầu mở khóa tài khoản</h1>
        <div class="cgv-header-right">
            <div class="cgv-user-wrap">
                <div class="cgv-avatar">SA</div>
                <span class="cgv-user-name">${sessionScope.account.fullName}</span>
            </div>
        </div>
    </header>
    <div class="cgv-page">
        <div class="cgv-list-wrap">

            <h3 style="margin-bottom:16px;">Đang chờ xử lý (${pendingList.size()})</h3>
            <table style="width:100%;border-collapse:collapse;font-size:14px;margin-bottom:32px;">
                <thead><tr style="background:#fafafa;border-bottom:2px solid var(--cgv-border);">
                    <th style="padding:10px;text-align:left;">ID</th>
                    <th style="padding:10px;text-align:left;">Account ID</th>
                    <th style="padding:10px;text-align:left;">Lý do</th>
                    <th style="padding:10px;text-align:left;">Ngày tạo</th>
                    <th style="padding:10px;text-align:left;">Thao tác</th>
                </tr></thead>
                <tbody>
                    <c:forEach var="r" items="${pendingList}">
                    <tr style="border-bottom:1px solid var(--cgv-border);">
                        <td style="padding:10px;">${r.requestId}</td>
                        <td style="padding:10px;">${r.accountId}</td>
                        <td style="padding:10px;max-width:300px;">${r.reason}</td>
                        <td style="padding:10px;">${r.createdAt}</td>
                        <td style="padding:10px;">
                            <form method="post" action="${pageContext.request.contextPath}/unlock-request" style="display:flex;gap:8px;">
                                <input type="hidden" name="action" value="review">
                                <input type="hidden" name="id" value="${r.requestId}">
                                <button type="submit" name="status" value="Approved" style="background:#16a34a;color:#fff;border:none;padding:6px 14px;border-radius:6px;cursor:pointer;">Duyệt</button>
                                <button type="submit" name="status" value="Rejected" style="background:#b91c1c;color:#fff;border:none;padding:6px 14px;border-radius:6px;cursor:pointer;">Từ chối</button>
                            </form>
                        </td>
                    </tr>
                    </c:forEach>
                    <c:if test="${empty pendingList}">
                    <tr><td colspan="5" style="padding:40px;text-align:center;color:var(--cgv-text-muted);">Không có yêu cầu nào đang chờ.</td></tr>
                    </c:if>
                </tbody>
            </table>

            <h3 style="margin-bottom:16px;">Tất cả yêu cầu</h3>
            <table style="width:100%;border-collapse:collapse;font-size:14px;">
                <thead><tr style="background:#fafafa;border-bottom:2px solid var(--cgv-border);">
                    <th style="padding:10px;text-align:left;">ID</th>
                    <th style="padding:10px;text-align:left;">Account ID</th>
                    <th style="padding:10px;text-align:left;">Lý do</th>
                    <th style="padding:10px;text-align:left;">Trạng thái</th>
                    <th style="padding:10px;text-align:left;">Ngày tạo</th>
                </tr></thead>
                <tbody>
                    <c:forEach var="r" items="${allList}">
                    <tr style="border-bottom:1px solid var(--cgv-border);">
                        <td style="padding:10px;">${r.requestId}</td>
                        <td style="padding:10px;">${r.accountId}</td>
                        <td style="padding:10px;max-width:300px;">${r.reason}</td>
                        <td style="padding:10px;">
                            <c:choose>
                                <c:when test="${r.status == 'Approved'}"><span style="color:#16a34a;font-weight:600;">Đã duyệt</span></c:when>
                                <c:when test="${r.status == 'Rejected'}"><span style="color:#b91c1c;font-weight:600;">Từ chối</span></c:when>
                                <c:otherwise><span style="color:#f59e0b;font-weight:600;">Chờ</span></c:otherwise>
                            </c:choose>
                        </td>
                        <td style="padding:10px;">${r.createdAt}</td>
                    </tr>
                    </c:forEach>
                </tbody>
            </table>

        </div>
    </div>
</div>
</body>
</html>
