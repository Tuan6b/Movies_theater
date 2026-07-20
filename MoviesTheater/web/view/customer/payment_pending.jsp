<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    Integer invoiceId = (Integer) request.getAttribute("invoiceId");
    if (invoiceId == null) {
        String idParam = request.getParameter("invoiceId");
        if (idParam != null) {
            try { invoiceId = Integer.parseInt(idParam); } catch (Exception e) {}
        }
    }
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>CGV CINEMA - Đang xử lý thanh toán</title>
    <link rel="stylesheet" href="<%= ctx %>/css/main.css">
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: Arial, sans-serif; background: #f5f5f5; display: flex; justify-content: center; align-items: center; min-height: 100vh; }
        .pending-box { background: #fff; border-radius: 12px; padding: 40px; text-align: center; max-width: 450px; box-shadow: 0 2px 20px rgba(0,0,0,0.1); }
        .spinner { width: 50px; height: 50px; border: 4px solid #f3f3f3; border-top: 4px solid #d71f2b; border-radius: 50%; animation: spin 1s linear infinite; margin: 0 auto 20px; }
        @keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
        .pending-box h2 { color: #333; margin-bottom: 10px; }
        .pending-box p { color: #888; font-size: 14px; margin-bottom: 5px; }
        .btn-retry { display: inline-block; margin-top: 20px; padding: 10px 30px; background: #d71f2b; color: #fff; text-decoration: none; border-radius: 6px; font-size: 14px; border: none; cursor: pointer; }
        .btn-retry:hover { background: #b81a24; }
        .btn-home { display:block; margin-top:10px; color:#999; font-size:12px; text-decoration:none; }
    </style>
</head>
<body>
    <div class="pending-box">
        <div class="spinner"></div>
        <h2>ĐANG XỬ LÝ THANH TOÁN</h2>
        <p>Giao dịch của bạn đang được xử lý.</p>
        <p>Vui lòng chờ trong giây lát...</p>
        <p style="font-size:12px;color:#aaa;margin-top:15px;" id="statusMsg">Đang kiểm tra trạng thái...</p>
        <button class="btn-retry" onclick="checkStatus()">Kiểm tra lại</button>
        <a href="<%= ctx %>/" class="btn-home">Về trang chủ</a>
    </div>

    <script>
        var invoiceId = <%= invoiceId != null ? invoiceId : "null" %>;
        var ctx = '<%= ctx %>';
        var pollCount = 0;

        function checkStatus() {
            if (!invoiceId) return;
            document.getElementById("statusMsg").textContent = "Đang kiểm tra...";
            fetch(ctx + '/vnpay?action=status&invoiceId=' + invoiceId)
                .then(function(r) { return r.json(); })
                .then(function(data) {
                    if (data.status === 'Paid') {
                        window.location.href = ctx + '/vnpay?action=return&vnp_TxnRef=CGV' + invoiceId + '&vnp_ResponseCode=00';
                    } else if (data.status === 'Failed' || data.status === 'not_found') {
                        window.location.href = ctx + '/vnpay?action=return&vnp_TxnRef=CGV' + invoiceId + '&vnp_ResponseCode=99';
                    } else {
                        document.getElementById("statusMsg").textContent = "Giao dịch đang chờ xử lý...";
                        if (pollCount < 20) {
                            pollCount++;
                            setTimeout(checkStatus, 3000);
                        } else {
                            document.getElementById("statusMsg").textContent = "Giao dịch mất quá nhiều thời gian. Vui lòng kiểm tra lại sau.";
                        }
                    }
                })
                .catch(function() {
                    document.getElementById("statusMsg").textContent = "Lỗi kết nối. Đang thử lại...";
                    if (pollCount < 10) {
                        pollCount++;
                        setTimeout(checkStatus, 5000);
                    }
                });
        }

        if (invoiceId) {
            setTimeout(checkStatus, 2000);
        }
    </script>
</body>
</html>
