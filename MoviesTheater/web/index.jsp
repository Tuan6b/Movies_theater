<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    // Tự động chuyển hướng ngay lập tức sang HomeController để đồng nhất Trang chủ
    response.sendRedirect(request.getContextPath() + "/HomeController");
%>
