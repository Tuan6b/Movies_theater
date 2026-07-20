<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String error = (String) request.getAttribute("error");
    if (error == null) error = "Thanh toán không thành công. Vui lòng thử lại.";
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>CGV CINEMA - Thanh toán thất bại</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/main.css">
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: Arial, sans-serif; background: #f5f5f5; display: flex; justify-content: center; align-items: center; min-height: 100vh; }
        .failed-box { background: #fff; border-radius: 12px; padding: 40px; text-align: center; max-width: 450px; box-shadow: 0 2px 20px rgba(0,0,0,0.1); }
        .icon { font-size: 60px; color: #d71f2b; margin-bottom: 20px; }
        .failed-box h2 { color: #d71f2b; margin-bottom: 10px; }
        .failed-box p { color: #666; font-size: 14px; margin-bottom: 5px; line-height: 1.5; }
        .error-detail { background: #fff5f5; border: 1px solid #fcc; border-radius: 6px; padding: 12px; margin: 15px 0; color: #d71f2b; font-size: 13px; }
        .btn-group { margin-top: 20px; display: flex; gap: 10px; justify-content: center; flex-wrap: wrap; }
        .btn-retry { padding: 10px 30px; background: #d71f2b; color: #fff; text-decoration: none; border-radius: 6px; font-size: 14px; }
        .btn-retry:hover { background: #b81a24; }
        .btn-home { padding: 10px 30px; background: #fff; color: #333; text-decoration: none; border-radius: 6px; font-size: 14px; border: 1px solid #ddd; }
        .btn-home:hover { background: #f5f5f5; }
    </style>
</head>
<body>
    <div class="failed-box">
        <div class="icon">&#10007;</div>
        <h2>THANH TOÁN THẤT BẠI</h2>
        <div class="error-detail"><%= error %></div>
        <p>Nếu bạn đã bị trừ tiền, vui lòng liên hệ hotline CSKH để được hỗ trợ.</p>
        <div class="btn-group">
            <a href="<%= request.getContextPath() %>/booking?action=checkout" class="btn-retry">Thử lại</a>
            <a href="<%= request.getContextPath() %>/" class="btn-home">Về trang chủ</a>
        </div>
    </div>
</body>
</html>
