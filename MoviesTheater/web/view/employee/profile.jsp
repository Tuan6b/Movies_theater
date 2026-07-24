<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% request.setAttribute("activeNav", "profile"); %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Hồ sơ cá nhân — Nhân viên CGV</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
</head>
<body class="cgv-body">
<%@ include file="_sidebar.jsp" %>
<div class="cgv-main">
    <header class="cgv-header">
        <h1 class="cgv-header-title">Hồ sơ cá nhân</h1>
        <div class="cgv-header-right">
            <%@ include file="_notifications.jsp" %>
        </div>
    </header>
    <div class="cgv-page">
        <%@ include file="/view/common/profile-content.jsp" %>
    </div>
</div>
</body>
</html>
