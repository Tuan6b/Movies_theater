<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="com.cinema.model.clsMovie,com.cinema.model.clsSchedule,com.cinema.model.Room" %>
<%@ page import="java.util.*,java.sql.Date,java.text.SimpleDateFormat" %>

<%
    // Retrieve model attributes from servlet request context
    clsMovie movie = (clsMovie) request.getAttribute("movie");
    List<Date> availableDates = (List<Date>) request.getAttribute("availableDates");
    List<clsSchedule> schedules = (List<clsSchedule>) request.getAttribute("schedules");
    String selectedDate = (String) request.getAttribute("selectedDate");

    // Initialize date formatter objects for cinematic layout
    SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("E", new Locale("vi", "VN"));
    SimpleDateFormat dayNumFormat = new SimpleDateFormat("dd");
    SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    // Group showtime schedules by Room ID to avoid repeated rows
    Map<Integer, List<clsSchedule>> roomSchedules = new LinkedHashMap<>();
    Map<Integer, Room> rooms = new HashMap<>();
    if (schedules != null) {
        for (clsSchedule s : schedules) {
            Room r = s.getRoom();
            if (r != null) {
                int roomId = r.getRoomId();
                if (!roomSchedules.containsKey(roomId)) {
                    roomSchedules.put(roomId, new ArrayList<>());
                    rooms.put(roomId, r);
                }
                roomSchedules.get(roomId).add(s);
            }
        }
    }

    // Determine the first movie ID for navigation fallback
    int firstMovieId = movie != null ? movie.getMovieId() : 1;
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>
        Lịch chiếu - <%= movie != null ? movie.getMovieName() : "CGV Cinema" %>
    </title>
    <!-- Import global main style -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main.css">
    <!-- Import specialized showtimes style -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/showtimes.css">
</head>
<body>

<!-- Header -->
<header class="site-header">
    <div class="site-header-inner">
        <a href="${pageContext.request.contextPath}/" class="site-logo">
            <img src="${pageContext.request.contextPath}/Image/Icon/cgvlogo.png" alt="CGV">
            <span class="site-logo-text">CGV CINEMA</span>
        </a>

        <nav class="site-nav">
            <a href="${pageContext.request.contextPath}/">Trang chủ</a>
            <a href="#">Phim đang chiếu</a>
            <a href="#">Ưu đãi</a>
            <a href="#">Góc điện ảnh</a>
        </nav>

        <div class="site-header-actions">
            <c:choose>
                <c:when test="${not empty sessionScope.account}">
                    <a href="${pageContext.request.contextPath}/profile" style="font-size:13px;color:var(--cgv-text-muted);font-weight:500;text-decoration:none;">
                        Xin chào, <strong>${sessionScope.account.profile.fullName}</strong>
                    </a>
                    <a href="${pageContext.request.contextPath}/Logout" class="btn btn-ghost">Đăng xuất</a>
                </c:when>
                <c:otherwise>
                    <a href="${pageContext.request.contextPath}/Login" class="btn btn-ghost">Đăng nhập</a>
                    <a href="${pageContext.request.contextPath}/Register" class="btn btn-primary">Đăng ký</a>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</header>

<% if (movie == null) { %>

    <!-- Error container for missing movie metadata -->
    <section class="showtimes-main-section">
        <div class="showtimes-main-container">
            <div class="no-showtimes-placeholder">
                <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <polygon points="7.86 2 16.14 2 22 7.86 22 16.14 16.14 22 7.86 22 2 16.14 2 7.86 7.86 2"></polygon>
                    <line x1="12" y1="8" x2="12" y2="12"></line>
                    <line x1="12" y1="16" x2="12.01" y2="16"></line>
                </svg>
                <p>Không tìm thấy thông tin phim chi tiết.</p>
                <a href="${pageContext.request.contextPath}/" class="btn btn-primary" style="margin-top: 20px;">Quay lại trang chủ</a>
            </div>
        </div>
    </section>

<% } else { %>

    <!-- Movie Hero/Banner block with theatrical blur backdrop -->
    <section class="movie-hero-section">
        <div class="movie-hero-backdrop" style="background-image: url('<%= movie.getPoster() %>');"></div>
        <div class="movie-hero-container">
            <div class="movie-hero-content">
                
                <!-- Poster element -->
                <div class="movie-hero-poster">
                    <% if (movie.getPoster() != null && !movie.getPoster().isEmpty()) { %>
                        <img src="<%= movie.getPoster() %>" alt="<%= movie.getMovieName() %>" />
                    <% } else { %>
                        <div class="movie-hero-poster-placeholder">
                            <span>Không có ảnh</span>
                        </div>
                    <% } %>
                </div>

                <!-- Text metadata element -->
                <div class="movie-hero-info">
                    <h1 class="movie-hero-title"><%= movie.getMovieName() %></h1>
                    
                    <!-- Badge metadata row -->
                    <div class="movie-badge-strip">
                        <% 
                            int age = movie.getAgeRestriction();
                            String ageBadgeClass = "badge-age-p";
                            String ageBadgeText = "P - Mọi lứa tuổi";
                            if (age >= 18) {
                                ageBadgeClass = "badge-age-c18";
                                ageBadgeText = "C18 - Cấm dưới 18";
                            } else if (age >= 16) {
                                ageBadgeClass = "badge-age-c16";
                                ageBadgeText = "C16 - Cấm dưới 16";
                            } else if (age >= 13) {
                                ageBadgeClass = "badge-age-c13";
                                ageBadgeText = "C13 - Cấm dưới 13";
                            }
                        %>
                        <span class="movie-badge-item <%= ageBadgeClass %>"><%= ageBadgeText %></span>
                        <span class="movie-badge-item badge-format">2D</span>
                        <span class="movie-badge-item badge-format">Digital</span>
                    </div>

                    <!-- Movie synopsis description -->
                    <p class="movie-hero-synopsis">
                        <%= movie.getDescription() != null ? movie.getDescription() : "Tóm tắt phim đang được cập nhật." %>
                    </p>

                    <!-- Technical metadata grid -->
                    <div class="movie-hero-details-grid">
                        <div class="detail-item">
                            <span class="detail-label">Đạo diễn:</span>
                            <span class="detail-value"><%= movie.getDirector() != null ? movie.getDirector() : "Đang cập nhật" %></span>
                        </div>
                        <div class="detail-item">
                            <span class="detail-label">Diễn viên:</span>
                            <span class="detail-value"><%= movie.getCast() != null ? movie.getCast() : "Đang cập nhật" %></span>
                        </div>
                        <div class="detail-item">
                            <span class="detail-label">Thời lượng:</span>
                            <span class="detail-value"><%= movie.getDuration() %> phút</span>
                        </div>
                        <div class="detail-item">
                            <span class="detail-label">Ngôn ngữ:</span>
                            <span class="detail-value"><%= movie.getLanguage() %> (Phụ đề: <%= movie.getSubtitle() != null ? movie.getSubtitle() : "Không" %>)</span>
                        </div>
                        <div class="detail-item">
                            <span class="detail-label">Quốc gia:</span>
                            <span class="detail-value"><%= movie.getCountry() != null ? movie.getCountry() : "Đang cập nhật" %></span>
                        </div>
                        <div class="detail-item">
                            <span class="detail-label">Khởi chiếu:</span>
                            <span class="detail-value"><%= movie.getReleaseDate() != null ? movie.getReleaseDate() : "Đang cập nhật" %></span>
                        </div>
                    </div>

                </div>

            </div>
        </div>
    </section>

    <!-- Main Content Area: Date Picker & Showtimes -->
    <section class="showtimes-main-section">
        <div class="showtimes-main-container">

            <!-- Date selection component -->
            <div class="date-timeline-container">
                <h2 class="section-title-alt">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                        <line x1="16" y1="2" x2="16" y2="6"></line>
                        <line x1="8" y1="2" x2="8" y2="6"></line>
                        <line x1="3" y1="10" x2="21" y2="10"></line>
                    </svg>
                    Chọn ngày xem
                </h2>
                
                <div class="date-timeline">
                    <% 
                        if (availableDates != null && !availableDates.isEmpty()) {
                            for (Date d : availableDates) {
                                boolean active = d.toString().equals(selectedDate);
                                String dayName = dayOfWeekFormat.format(d);
                                String dayNum = dayNumFormat.format(d);
                                String monthNum = monthFormat.format(d);
                    %>
                                <a href="${pageContext.request.contextPath}/showtimes?movieId=<%= movie.getMovieId() %>&date=<%= d %>" 
                                   class="date-card <%= active ? "active" : "" %>">
                                    <span class="date-card-dayname"><%= dayName %></span>
                                    <span class="date-card-daynum"><%= dayNum %></span>
                                    <span class="date-card-month">Tháng <%= monthNum %></span>
                                </a>
                    <% 
                            }
                        } else { 
                    %>
                            <div class="no-showtimes-placeholder" style="width: 100%; padding: 30px;">
                                <p>Không tìm thấy lịch chiếu phim khả dụng.</p>
                            </div>
                    <% 
                        } 
                    %>
                </div>
            </div>

            <!-- Grouped showtimes slot lists -->
            <div class="showtimes-list-container">
                <h2 class="section-title-alt">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <circle cx="12" cy="12" r="10"></circle>
                        <polyline points="12 6 12 12 16 14"></polyline>
                    </svg>
                    Lịch chiếu suất
                </h2>

                <% if (roomSchedules != null && !roomSchedules.isEmpty()) { %>
                    <div class="showtimes-rooms-list">
                        <% 
                            for (Map.Entry<Integer, List<clsSchedule>> entry : roomSchedules.entrySet()) {
                                Room room = rooms.get(entry.getKey());
                                List<clsSchedule> roomSlots = entry.getValue();
                        %>
                                <div class="room-card animate-fade-in">
                                    <div class="room-card-header">
                                        
                                        <!-- Room info badges -->
                                        <div class="room-info-block">
                                            <span class="room-number-badge">Phòng <%= room.getRoomNumber() %></span>
                                            <span class="room-type-badge"><%= room.getRoomType() %></span>
                                        </div>

                                        <!-- Room seating capacity details -->
                                        <div class="room-capacity">
                                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                                                <circle cx="9" cy="7" r="4"></circle>
                                                <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
                                                <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                                            </svg>
                                            Sức chứa: <%= room.getCapacity() %> ghế ngồi
                                        </div>

                                    </div>

                                    <!-- Time slot button selections -->
                                    <div class="showtime-slots-grid">
                                        <% for (clsSchedule s : roomSlots) { %>
                                            <a href="#" class="showtime-slot-btn" onclick="handleBooking(event, <%= s.getScheduleId() %>, '<%= timeFormat.format(s.getStartTime()) %>')">
                                                <span class="showtime-slot-time"><%= timeFormat.format(s.getStartTime()) %></span> 
                                                <span class="showtime-slot-price"><%= String.format("%,.0f", s.getBaseTicketPrice()) %> đ</span>
                                            </a>
                                        <% } %>
                                    </div>
                                </div>
                        <% } %>
                    </div>
                <% } else { %>
                    <!-- Fallback placeholder for empty dates -->
                    <div class="no-showtimes-placeholder">
                        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <circle cx="12" cy="12" r="10"></circle>
                            <line x1="8" y1="12" x2="16" y2="12"></line>
                        </svg>
                        <p>Hôm nay không có suất chiếu nào khả dụng cho bộ phim này.</p>
                    </div>
                <% } %>
            </div>

        </div>
    </section>

<% } %>

<!-- Footer -->
<footer class="site-footer">
    <div class="footer-inner">
        <a href="${pageContext.request.contextPath}/" class="footer-brand">
            <img src="${pageContext.request.contextPath}/Image/Icon/cgvlogo.png" alt="CGV">
            <span class="footer-brand-text">CGV CINEMA</span>
        </a>
        <p class="footer-copy">&copy; 2026 CGV Cinema. Hệ thống quản lý rạp chiếu phim.</p>
        <div class="footer-links">
            <c:choose>
                <c:when test="${sessionScope.account.roleId eq 5}"><a href="${pageContext.request.contextPath}/admin">Quản lý</a></c:when>
                <c:when test="${sessionScope.account.roleId eq 4}"><a href="${pageContext.request.contextPath}/manager">Quản lý</a></c:when>
                <c:when test="${sessionScope.account.roleId eq 3}"><a href="${pageContext.request.contextPath}/employee">Quản lý</a></c:when>
                <c:otherwise><a href="${pageContext.request.contextPath}/manager">Quản lý</a></c:otherwise>
            </c:choose>
            <a href="#">Điều khoản</a>
            <a href="#">Hỗ trợ</a>
        </div>
    </div>
</footer>

<!-- Interactive Toast Banner Element -->
<div id="cinematicToast" class="cinematic-toast">
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <line x1="12" y1="2" x2="12" y2="6"></line>
        <line x1="12" y1="18" x2="12" y2="22"></line>
        <line x1="4.93" y1="4.93" x2="7.76" y2="7.76"></line>
        <line x1="16.24" y1="16.24" x2="19.07" y2="19.07"></line>
        <line x1="2" y1="12" x2="6" y2="12"></line>
        <line x1="18" y1="12" x2="22" y2="12"></line>
        <line x1="4.93" y1="19.07" x2="7.76" y2="16.24"></line>
        <line x1="16.24" y1="7.76" x2="19.07" y2="4.93"></line>
    </svg>
    <span id="cinematicToastText">Đang chuyển hướng tới trang chọn ghế...</span>
</div>

<script>
    /**
     * Display a customized cinematic toast message
     * @param {string} msg Text to display in toast
     */
    function showCinematicToast(msg) {
        var toast = document.getElementById("cinematicToast");
        var text = document.getElementById("cinematicToastText");
        text.innerText = msg;
        toast.classList.add("show");
        
        setTimeout(function() {
            toast.classList.remove("show");
        }, 3000);
    }

    /**
     * Handle slot selection click states and redirection
     * @param {Event} event Browser click event
     * @param {number} scheduleId Database schedule ID
     * @param {string} time Start time string
     */
    function handleBooking(event, scheduleId, time) {
        event.preventDefault();
        
        // Show cinematic feedback notification
        showCinematicToast("Đang chuẩn bị phòng chiếu lúc " + time + "...");
        
        // Redirect to booking seat mapping
        setTimeout(function() {
            window.location.href = "${pageContext.request.contextPath}/booking?scheduleId=" + scheduleId;
        }, 1000);
    }
</script>

</body>
</html>