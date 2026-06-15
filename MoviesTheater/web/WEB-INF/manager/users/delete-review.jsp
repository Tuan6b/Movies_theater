<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% request.setAttribute("activeNav", "users"); %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Review Deletion Request — CGV Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
    <style>
        .review-card { max-width:600px; margin:40px auto; background:#fff; border-radius:16px; padding:36px 32px; box-shadow:0 2px 12px rgba(0,0,0,0.08); }
        .review-info { margin-bottom:24px; }
        .review-info label { font-size:12px; font-weight:600; color:rgba(94,63,58,0.5); text-transform:uppercase; letter-spacing:1px; display:block; margin-bottom:2px; }
        .review-info .val { font-size:15px; color:var(--cgv-text); }
        .review-reason { background:#f9f9f9; border-radius:8px; padding:16px; margin-bottom:24px; font-size:14px; line-height:1.6; }
        .review-actions { display:flex; gap:12px; margin-top:24px; }
    </style>
</head>
<body class="cgv-body">

<%@ include file="../_sidebar.jsp" %>

<div class="cgv-main">
    <header class="cgv-header">
        <h1 class="cgv-header-title">Review Deletion Request</h1>
        <div class="cgv-header-right">
            <div class="cgv-user-wrap">
                <div class="cgv-avatar">MG</div>
                <span class="cgv-user-name">${sessionScope.account.profile.fullName}</span>
            </div>
        </div>
    </header>

    <div class="cgv-page">
        <div class="review-card">
            <h2 style="font-size:20px;margin-bottom:24px;">Deletion Request #${deletionRequest.requestId}</h2>

            <c:if test="${not empty flashError}">
                <div class="cgv-alert cgv-alert-danger">${flashError}</div>
                <% session.removeAttribute("flashError"); %>
            </c:if>

            <div class="review-info">
                <label>Account</label>
                <div class="val">${deletionRequest.fullName} (${deletionRequest.accountEmail})</div>
            </div>
            <div class="review-info">
                <label>Status</label>
                <div class="val"><span class="cgv-badge ${deletionRequest.status eq 'Pending' ? 'upcoming' : deletionRequest.status eq 'Approved' ? 'active' : 'inactive'}">${deletionRequest.status}</span></div>
            </div>
            <div class="review-info">
                <label>Submitted</label>
                <div class="val">${deletionRequest.createdAt}</div>
            </div>

            <div style="font-size:13px;font-weight:600;margin-bottom:8px;color:rgba(94,63,58,0.6);">Reason</div>
            <div class="review-reason">${deletionRequest.reason}</div>

            <c:if test="${deletionRequest.status eq 'Pending'}">
            <hr style="border:none;border-top:1px solid #eee;margin:24px 0;">
            <h3 style="font-size:16px;margin-bottom:16px;">Decision</h3>

            <form action="${pageContext.request.contextPath}/manager/deletion-requests" method="post" style="margin-bottom:16px;">
                <input type="hidden" name="id" value="${deletionRequest.requestId}">
                <div class="edit-field">
                    <label for="reviewNote" style="display:block;font-size:13px;font-weight:600;color:rgba(94,63,58,0.6);margin-bottom:4px;">Admin Note (optional)</label>
                    <textarea id="reviewNote" name="reviewNote" class="cgv-input" style="width:100%;min-height:80px;padding:10px;resize:vertical;"></textarea>
                </div>
                <div class="review-actions">
                    <button type="submit" name="action" value="approve" class="btn--cgv" style="background:#b91c1c;" onclick="return confirm('Approve deletion? This will permanently delete the account.')">Approve & Delete</button>
                    <button type="submit" name="action" value="reject" class="btn--cgv-outline">Reject</button>
                    <a href="${pageContext.request.contextPath}/manager/deletion-requests" class="btn--cgv-outline">Back</a>
                </div>
            </form>
            </c:if>

            <c:if test="${deletionRequest.status ne 'Pending'}">
            <div class="review-info" style="margin-top:16px;">
                <label>Reviewed By</label>
                <div class="val">Admin (ID: ${deletionRequest.reviewedBy})</div>
            </div>
            <c:if test="${not empty deletionRequest.reviewNote}">
            <div class="review-info">
                <label>Review Note</label>
                <div class="val">${deletionRequest.reviewNote}</div>
            </div>
            </c:if>
            <div class="review-info">
                <label>Reviewed At</label>
                <div class="val">${deletionRequest.reviewedAt}</div>
            </div>
            <a href="${pageContext.request.contextPath}/manager/deletion-requests" class="btn--cgv-outline" style="margin-top:8px;">&larr; Back</a>
            </c:if>
        </div>
    </div>
</div>
</body>
</html>
