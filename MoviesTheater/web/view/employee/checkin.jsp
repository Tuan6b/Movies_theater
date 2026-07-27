<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<% request.setAttribute("activeNav", "checkin"); %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Check-in Vé — Nhân viên CGV</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/manager.css">
    <style>
        .checkin-lookup {
            background: #fff;
            border: 1px solid var(--cgv-border);
            border-radius: 12px;
            padding: 32px;
            margin-bottom: 32px;
        }
        .checkin-lookup-title {
            font-family: var(--font-cgv-ui);
            font-weight: 700;
            font-size: 10px;
            letter-spacing: 2px;
            text-transform: uppercase;
            color: rgba(94,63,58,0.5);
            margin-bottom: 20px;
        }
        .checkin-search-row {
            display: flex;
            gap: 12px;
            align-items: center;
            flex-wrap: wrap;
        }
        .checkin-result {
            margin-top: 24px;
            background: #faf6f5;
            border: 1px solid var(--cgv-border);
            border-radius: 10px;
            padding: 20px 24px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 16px;
            flex-wrap: wrap;
        }
        .checkin-result-info { display: flex; flex-direction: column; gap: 4px; }
        .checkin-result-name { font-family: var(--font-cgv-display); font-size: 20px; color: var(--cgv-dark); }
        .checkin-result-meta { font-size: 13px; color: rgba(94,63,58,0.6); }

        /* ---- QR scanner (web/js/checkin-scanner.js) ---- */
        .cgv-scan-hint {
            margin-top: 12px; font-size: 13px; line-height: 1.5;
            color: var(--cgv-amber); background: #fdf6ea;
            border: 1px solid #f0dcb8; border-radius: 8px; padding: 10px 14px;
        }
        .cgv-scan-panel {
            margin-top: 24px; display: grid; gap: 20px;
            grid-template-columns: minmax(0, 320px) minmax(0, 1fr);
            align-items: start;
        }
        /* display:grid above beats the browser's own [hidden]{display:none}, because
           author styles win over the UA stylesheet. Without this rule the panel is
           visible before anything is clicked — a black video box next to the words
           "Đang bật camera…", which looks exactly like a camera that failed. */
        .cgv-scan-panel[hidden] { display: none; }
        @media (max-width: 720px) { .cgv-scan-panel { grid-template-columns: 1fr; } }
        .cgv-scan-stage {
            position: relative; background: #1b1b1b; border-radius: 10px;
            overflow: hidden; aspect-ratio: 4 / 3;
        }
        .cgv-scan-stage video { width: 100%; height: 100%; object-fit: cover; display: block; }
        /* Framing guide: purely visual, the detector reads the whole frame. */
        .cgv-scan-reticle {
            position: absolute; inset: 18%; border: 2px solid rgba(255,255,255,0.85);
            border-radius: 8px; box-shadow: 0 0 0 9999px rgba(0,0,0,0.28); pointer-events: none;
        }
        .cgv-scan-side { display: flex; flex-direction: column; gap: 12px; min-width: 0; }
        .cgv-scan-status {
            font-size: 14px; font-weight: 600; color: var(--cgv-dark);
            background: #faf6f5; border: 1px solid var(--cgv-border);
            border-radius: 8px; padding: 10px 14px;
        }
        .cgv-scan-status.ok  { color: #1c6b34; background: #e8f5ec; border-color: #b6dcc2; }
        .cgv-scan-status.bad { color: var(--cgv-red); background: #fdeceb; border-color: #f2c3bf; }
        .cgv-scan-log { display: flex; flex-direction: column; gap: 8px; max-height: 260px; overflow-y: auto; }
        .cgv-scan-row {
            border: 1px solid var(--cgv-border); border-left-width: 3px;
            border-radius: 6px; padding: 8px 12px; background: #fff;
        }
        .cgv-scan-row.ok  { border-left-color: #28a745; }
        .cgv-scan-row.bad { border-left-color: var(--cgv-red); }
        .cgv-scan-row-head { font-size: 13.5px; font-weight: 600; color: var(--cgv-dark); }
        .cgv-scan-row-meta {
            font-family: monospace; font-size: 11.5px; letter-spacing: .3px;
            color: rgba(94,63,58,0.6); margin-top: 3px; word-break: break-all;
        }
    </style>
</head>
<body class="cgv-body">

<%@ include file="_sidebar.jsp" %>

<div class="cgv-main">

    <header class="cgv-header">
        <h1 class="cgv-header-title">Soát vé khách hàng (Check-in)</h1>
        <div class="cgv-header-right">
            <%@ include file="_notifications.jsp" %>
            <div class="cgv-user-wrap">
                <div class="cgv-avatar">EM</div>
                <span class="cgv-user-name">${sessionScope.account.fullName}</span>
            </div>
        </div>
    </header>

    <div class="cgv-page">
        <div class="cgv-table-wrap" style="flex: 1;">

            <c:if test="${not empty requestScope.flashSuccess}">
                <div class="cgv-alert cgv-alert-success">${requestScope.flashSuccess}</div>
            </c:if>
            <c:if test="${not empty requestScope.flashError}">
                <div class="cgv-alert cgv-alert-danger">${requestScope.flashError}</div>
            </c:if>

            <div class="checkin-lookup">
                <div class="checkin-lookup-title">TRA CỨU MÃ VÉ CHECK-IN</div>
                <form method="get" action="${pageContext.request.contextPath}/employee/checkin" class="checkin-search-row">
                    <input class="cgv-input" style="flex:1;max-width:320px;" type="text"
                           name="code" placeholder="Nhập mã vé hoặc mã QR..."
                           value="${param.code}" autofocus>
                    <button type="submit" class="btn--cgv">Xác thực vé</button>
                    <button type="button" id="cgvScanOpen" class="btn--cgv-outline">Quét mã QR</button>
                </form>

                <%-- Scanner: reads the QR straight into the same check-in rules the
                     button below uses. Hidden until the employee opens it so the page
                     never asks for the camera on its own. --%>
                <%-- Mọi câu chữ của scanner nằm ở đây, không nằm trong file .js:
                     JSP khai báo UTF-8 trong contentType nên tiếng Việt luôn đúng dấu,
                     còn file .js phụ thuộc charset của response và của cache — đã từng
                     hiển thị thành "Ä?Æ°a mĂ£ QR" vì lý do đó. --%>
                <div id="cgvScan" data-base="${pageContext.request.contextPath}"
                     data-msg-insecure="Camera chỉ hoạt động qua HTTPS hoặc http://localhost. Hãy mở trang bằng localhost thay vì địa chỉ IP, hoặc nhập mã vé bằng tay."
                     data-msg-nocamera="Trình duyệt này không cho truy cập camera."
                     data-msg-nodecoder="Không nạp được bộ giải mã QR (js/jsqr.min.js). Hãy tải lại trang."
                     data-msg-starting="Đang bật camera…"
                     data-msg-ready="Đưa mã QR trên vé vào khung hình."
                     data-msg-checking="Đang kiểm tra"
                     data-msg-session="Phiên đăng nhập đã hết. Hãy tải lại trang."
                     data-msg-netfail="Không gửi được yêu cầu."
                     data-msg-camfail="Không mở được camera."
                     data-msg-denied="Bạn đã từ chối quyền camera. Cấp lại quyền ở biểu tượng camera trên thanh địa chỉ rồi bấm Quét mã QR lại."
                     data-msg-notfound="Máy không có camera nào khả dụng."
                     data-msg-busy="Camera đang bị ứng dụng khác chiếm (Zoom, Teams, Camera…). Đóng ứng dụng đó rồi thử lại."
                     data-msg-seat="Ghế"
                     data-msg-counted="đã check-in">
                    <div id="cgvScanHint" class="cgv-scan-hint" hidden></div>

                    <div id="cgvScanPanel" class="cgv-scan-panel" hidden>
                        <div class="cgv-scan-stage">
                            <video id="cgvScanVideo" playsinline muted></video>
                            <div class="cgv-scan-reticle"></div>
                        </div>
                        <div class="cgv-scan-side">
                            <%-- Neutral until start() runs: a panel that has not been
                                 opened yet must never claim the camera is coming up. --%>
                            <div id="cgvScanStatus" class="cgv-scan-status">Chưa bật camera.</div>
                            <div id="cgvScanLog" class="cgv-scan-log"></div>
                            <button type="button" id="cgvScanClose" class="btn--cgv-outline">
                                Đóng camera
                            </button>
                        </div>
                    </div>
                </div>

                <c:if test="${not empty booking}">
                    <div class="checkin-result">
                        <div class="checkin-result-info">
                            <div class="checkin-result-name">${booking.movieTitle}</div>
                            <div class="checkin-result-meta">
                                Suất chiếu: ${booking.showDate} · ${booking.startTime}
                            </div>
                            <div class="checkin-result-meta" style="margin-top:4px;">
                                Khách hàng: <strong>${booking.customerName}</strong> ·
                                Ghế: <strong style="color:var(--cgv-primary);">${booking.seats}</strong>
                            </div>
                        </div>
                        <div style="display:flex;gap:12px;align-items:center;">
                            <span class="cgv-badge ${booking.checkedIn ? 'active' : 'inactive'}">
                                ${booking.checkedIn ? 'ĐÃ CHECK-IN' : 'CHƯA CHECK-IN'}
                            </span>
                            <c:if test="${not booking.checkedIn}">
                                <form method="post" action="${pageContext.request.contextPath}/employee/checkin">
                                    <input type="hidden" name="bookingId" value="${booking.bookingId}">
                                    <button type="submit" class="btn--cgv" style="background:#28a745;">Xác nhận Check-in</button>
                                </form>
                            </c:if>
                        </div>
                    </div>
                </c:if>
            </div>

            <div class="cgv-toolbar">
                <div class="cgv-pills">
                    <a href="?filter=today"   class="cgv-pill ${empty param.filter || param.filter eq 'today'   ? 'active' : ''}">Vé Hôm Nay</a>
                    <a href="?filter=pending" class="cgv-pill ${param.filter eq 'pending' ? 'active' : ''}">Chờ Check-in</a>
                    <a href="?filter=checked" class="cgv-pill ${param.filter eq 'checked' ? 'active' : ''}">Đã Check-in</a>
                </div>
            </div>

            <div class="cgv-data-wrap">
                <table class="cgv-dt">
                    <thead>
                        <tr>
                            <th>Mã Vé</th>
                            <th>Khách Hàng</th>
                            <th>Phim</th>
                            <th>Suất Chiếu</th>
                            <th>Ghế</th>
                            <th>Giá Vé</th>
                            <th>Trạng Thái</th>
                            <th>Hành Động</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty bookingList}">
                                <c:forEach var="b" items="${bookingList}">
                                    <tr>
                                        <td>
                                            <code style="font-family:monospace;background:#f3f3f3;padding:2px 8px;border-radius:4px;font-size:12px;letter-spacing:1px;font-weight:600;">
                                                ${b.code}
                                            </code>
                                        </td>
                                        <td style="font-weight:700; color:var(--cgv-dark);">${b.customerName}</td>
                                        <td>${b.movieTitle}</td>
                                        <td style="font-size:13px;white-space:nowrap;">${b.showDate} ${b.startTime}</td>
                                        <td><span class="cgv-pill">${b.seats}</span></td>
                                        <td style="font-weight:600; color:var(--cgv-dark);">${b.totalAmount} VND</td>
                                        <td>
                                            <span class="cgv-badge ${b.checkedIn ? 'active' : 'inactive'}">
                                                ${b.checkedIn ? 'Đã Check In' : 'Chờ Check In'}
                                            </span>
                                        </td>
                                        <td>
                                            <c:if test="${not b.checkedIn}">
                                                <form method="post" action="${pageContext.request.contextPath}/employee/checkin">
                                                    <input type="hidden" name="bookingId" value="${b.bookingId}">
                                                    <button type="submit" class="btn--cgv-outline" style="font-size:11px; padding:6px 12px; border-color:#28a745; color:#28a745;">Check In</button>
                                                </form>
                                            </c:if>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr><td colspan="8" style="text-align:center;padding:48px;color:rgba(94,63,58,0.4);">Không tìm thấy thông tin vé nào.</td></tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
<%-- jsQR decodes in plain JavaScript. It must load before the scanner: the browser's
     own BarcodeDetector does not exist on Windows (any Chromium), so jsQR is the
     path that actually runs here, not a fallback. --%>
<%-- ?v= is a cache buster. Tomcat serves these with a far-future-ish heuristic and a
     stale checkin-scanner.js is indistinguishable from a broken camera: the old copy
     disabled the scan button, so nothing happened and no permission prompt appeared.
     Bump the number whenever either file changes. --%>
<script src="${pageContext.request.contextPath}/js/jsqr.min.js?v=4"></script>
<script src="${pageContext.request.contextPath}/js/checkin-scanner.js?v=4"></script>
</body>
</html>
