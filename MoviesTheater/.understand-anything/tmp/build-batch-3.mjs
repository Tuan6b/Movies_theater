import fs from 'fs';

const OUT_PATH = 'E:/ASUS/Documents/Movies_theater/MoviesTheater/.understand-anything/intermediate/batch-3.json';

function complexityFromLines(lines) {
  if (lines < 20) return 'simple';
  if (lines <= 60) return 'moderate';
  return 'complex';
}

const nodes = [];
const edges = [];
const seenNodeIds = new Set();

function addNode(node) {
  if (seenNodeIds.has(node.id)) {
    throw new Error('Duplicate node id: ' + node.id);
  }
  seenNodeIds.add(node.id);
  nodes.push(node);
}

function addEdge(source, target, type, weight) {
  if (source === target) throw new Error('Self edge: ' + source);
  edges.push({ source, target, type, direction: 'forward', weight });
}

// ---------------------------------------------------------------------
// batchImportData (verbatim from dispatch prompt)
// ---------------------------------------------------------------------
const batchImportData = {
  "src/java/com/cinema/controller/EmployeeDashboardServlet.java": ["src/java/com/cinema/dao/AccountDAO.java","src/java/com/cinema/dao/EmployeeDAO.java","src/java/com/cinema/dao/PromotionDAO.java","src/java/com/cinema/dao/RoomDAO.java","src/java/com/cinema/dao/SeatDAO.java","src/java/com/cinema/dao/ShiftExchangeDAO.java","src/java/com/cinema/dao/TicketDAO.java","src/java/com/cinema/dao/WorkShiftDAO.java","src/java/com/cinema/dao/tbMovie.java","src/java/com/cinema/dao/tbSchedule.java","src/java/com/cinema/model/Account.java","src/java/com/cinema/model/Promotion.java","src/java/com/cinema/model/Room.java","src/java/com/cinema/model/Seat.java","src/java/com/cinema/model/ShiftExchangeRequest.java","src/java/com/cinema/model/Ticket.java","src/java/com/cinema/model/WorkShift.java","src/java/com/cinema/model/clsMovie.java","src/java/com/cinema/model/clsSchedule.java","src/java/com/cinema/util/DBUtils.java"],
  "src/java/com/cinema/controller/EmployeeServlet.java": ["src/java/com/cinema/dao/EmployeeDAO.java","src/java/com/cinema/model/Account.java"],
  "src/java/com/cinema/controller/ForgotPasswordController.java": ["src/java/com/cinema/dao/AccountDAO.java"],
  "src/java/com/cinema/controller/GenreController.java": ["src/java/com/cinema/dao/GenreDAO.java","src/java/com/cinema/model/Genre.java"],
  "src/java/com/cinema/controller/HomeController.java": ["src/java/com/cinema/dao/GenreDAO.java","src/java/com/cinema/dao/tbMovie.java","src/java/com/cinema/model/Genre.java","src/java/com/cinema/model/clsMovie.java"],
  "src/java/com/cinema/controller/LoginController.java": ["src/java/com/cinema/dao/AccountDAO.java","src/java/com/cinema/dao/WorkShiftDAO.java","src/java/com/cinema/model/Account.java","src/java/com/cinema/util/SystemLogService.java"],
  "src/java/com/cinema/controller/LoginGoogleController.java": ["src/java/com/cinema/dao/AccountDAO.java","src/java/com/cinema/model/Account.java","src/java/com/cinema/util/GoogleOAuthConfig.java"],
  "src/java/com/cinema/controller/LogoutController.java": [],
  "src/java/com/cinema/controller/ManagerServlet.java": ["src/java/com/cinema/util/DBUtils.java"],
  "src/java/com/cinema/controller/MovieController.java": ["src/java/com/cinema/dao/tbMovie.java","src/java/com/cinema/model/clsMovie.java"],
  "src/java/com/cinema/controller/MovieDetailController.java": ["src/java/com/cinema/dao/tbMovie.java","src/java/com/cinema/dao/tbSchedule.java","src/java/com/cinema/model/clsMovie.java","src/java/com/cinema/model/clsSchedule.java"],
  "src/java/com/cinema/controller/PromotionServlet.java": ["src/java/com/cinema/dao/PromotionDAO.java","src/java/com/cinema/exception/ConflictException.java","src/java/com/cinema/exception/NotFoundException.java","src/java/com/cinema/exception/ValidationException.java","src/java/com/cinema/model/Promotion.java","src/java/com/cinema/util/DBUtils.java"],
  "src/java/com/cinema/controller/RegisterController.java": ["src/java/com/cinema/dao/AccountDAO.java","src/java/com/cinema/model/Account.java"],
  "src/java/com/cinema/controller/ResetPasswordController.java": ["src/java/com/cinema/dao/AccountDAO.java","src/java/com/cinema/model/Account.java"],
  "src/java/com/cinema/controller/ReviewController.java": ["src/java/com/cinema/dao/MovieReviewDAO.java","src/java/com/cinema/model/Account.java","src/java/com/cinema/model/clsMovieReview.java"],
  "src/java/com/cinema/controller/RoomServlet.java": ["src/java/com/cinema/dao/RoomDAO.java","src/java/com/cinema/dao/ScheduleDAO.java","src/java/com/cinema/dao/SeatDAO.java","src/java/com/cinema/model/Room.java"],
  "src/java/com/cinema/controller/ScheduleController.java": ["src/java/com/cinema/dao/RoomDAO.java","src/java/com/cinema/dao/ScheduleDAO.java","src/java/com/cinema/dao/tbMovie.java","src/java/com/cinema/model/Room.java","src/java/com/cinema/model/Schedule.java","src/java/com/cinema/model/clsMovie.java"],
  "src/java/com/cinema/controller/SeatController.java": ["src/java/com/cinema/dao/RoomDAO.java","src/java/com/cinema/dao/ScheduleDAO.java","src/java/com/cinema/dao/SeatDAO.java","src/java/com/cinema/model/Room.java","src/java/com/cinema/model/Seat.java"],
  "src/java/com/cinema/controller/ShowtimeServlet.java": ["src/java/com/cinema/dao/tbMovie.java","src/java/com/cinema/dao/tbSchedule.java","src/java/com/cinema/model/clsMovie.java","src/java/com/cinema/model/clsSchedule.java"],
  "src/java/com/cinema/controller/TMDBController.java": ["src/java/com/cinema/util/TMDBService.java"],
  "src/java/com/cinema/controller/WorkShiftServlet.java": ["src/java/com/cinema/dao/EmployeeDAO.java","src/java/com/cinema/dao/WorkShiftDAO.java","src/java/com/cinema/model/Account.java","src/java/com/cinema/model/WorkShift.java"],
  "web/Error.jsp": [],
  "web/WEB-INF/admin/_sidebar.jsp": [],
  "web/WEB-INF/admin/config/index.jsp": [],
  "web/WEB-INF/admin/dashboard.jsp": [],
  "web/WEB-INF/admin/logs/index.jsp": [],
  "web/WEB-INF/employee/_sidebar.jsp": []
};

// ---------------------------------------------------------------------
// FILE definitions
// ---------------------------------------------------------------------
const files = [
  {
    path: "src/java/com/cinema/controller/EmployeeDashboardServlet.java",
    className: "EmployeeDashboardServlet", classRange: [40, 876],
    summary: "Servlet điều phối khu vực làm việc của nhân viên trực ca (nhân viên nghiệp vụ, roleId=3): dựa vào pathInfo (`/`, `/dashboard`, `/schedules`, `/tickets`, `/book`, `/checkin`, `/profile`, `/setup`, `/my-shifts`) để hiển thị dashboard, bán vé walk-in tại quầy, check-in khách, xem/đổi ca làm việc. Có guard bắt buộc hoàn tất thiết lập hồ sơ lần đầu và guard chặn thao tác nghiệp vụ khi không có ca làm việc đang hoạt động.",
    tags: ["nhan-vien-nghiep-vu", "dispatcher", "ban-ve-walk-in", "check-in", "quan-ly-ca-lam"],
    languageNotes: "Đây là dashboard NGHIỆP VỤ của nhân viên trực ca (bán vé, check-in) — khác hoàn toàn với EmployeeServlet.java, vốn là màn hình CRUD của MANAGER để quản trị tài khoản nhân viên.",
    functions: [
      ["processRequest", 56, 138, "Bộ định tuyến chính theo pathInfo và HTTP method: áp guard bắt buộc thiết lập hồ sơ (needsSetup) và guard chặn thao tác book/checkin khi nhân viên không có ca làm việc, sau đó dispatch tới các handler dashboard/schedules/tickets/book/checkin/profile/setup/my-shifts.", ["dispatcher","routing"]],
      ["showDashboard", 140, 189, "Tổng hợp dữ liệu tổng quan cho trang dashboard của nhân viên (tình trạng ca làm, cờ noShift...) rồi forward sang dashboard.jsp.", ["dashboard"]],
      ["showSchedules", 191, 227, "Lấy danh sách suất chiếu khả dụng để nhân viên chọn khi bán vé, forward sang schedules.jsp.", ["schedule"]],
      ["showTickets", 229, 263, "Hiển thị danh sách vé đã bán theo suất chiếu được chọn, forward sang tickets.jsp.", ["ticket"]],
      ["showBookForm", 265, 296, "Hiển thị form bán vé walk-in cho một suất chiếu cụ thể kèm sơ đồ ghế, forward sang book.jsp.", ["booking"]],
      ["handleBook", 298, 411, "Xử lý bán vé walk-in tại quầy: xác định/khởi tạo khách hàng, khoá ghế đã chọn, tính tiền, tạo vé và hoá đơn cho giao dịch bán trực tiếp.", ["booking","transaction"]],
      ["resolveCustomerId", 413, 449, "Tìm hoặc tạo mới bản ghi khách hàng dựa trên email/tên/số điện thoại nhập tại quầy để gắn vào vé bán trực tiếp.", ["customer"]],
      ["computeSubtotal", 451, 463, "Tính tổng tiền vé dựa trên danh sách ghế đã chọn và đơn giá cơ sở.", ["utility","pricing"]],
      ["showCheckin", 465, 610, "Tra cứu và hiển thị danh sách vé để nhân viên thực hiện check-in khách tại rạp, forward sang checkin.jsp.", ["check-in"]],
      ["handleCheckin", 612, 638, "Xử lý xác nhận check-in một vé cụ thể do nhân viên chọn.", ["check-in"]],
      ["handleSetup", 645, 691, "Xử lý submit form thiết lập hồ sơ lần đầu bắt buộc đối với nhân viên mới (needsSetup); hoàn tất sẽ tắt cờ needsSetup.", ["onboarding"]],
      ["showMyShifts", 693, 732, "Hiển thị ca làm việc của chính nhân viên đang đăng nhập cùng các yêu cầu đổi ca liên quan, forward sang my-shifts.jsp.", ["quan-ly-ca-lam"]],
      ["handleMyShiftsPost", 734, 799, "Xử lý các thao tác trên trang ca làm của tôi: gửi yêu cầu đổi ca, chấp nhận/từ chối yêu cầu đổi ca từ đồng nghiệp.", ["quan-ly-ca-lam","workflow"]],
      ["showProfile", 801, 810, "Hiển thị thông tin hồ sơ cá nhân của nhân viên đang đăng nhập, forward sang profile.jsp.", ["profile"]],
    ],
    routesTo: [
      "web/WEB-INF/employee/dashboard.jsp",
      "web/WEB-INF/employee/schedules.jsp",
      "web/WEB-INF/employee/tickets.jsp",
      "web/WEB-INF/employee/book.jsp",
      "web/WEB-INF/employee/checkin.jsp",
      "web/WEB-INF/employee/profile.jsp",
      "web/WEB-INF/employee/setup.jsp",
      "web/WEB-INF/employee/my-shifts.jsp",
    ],
    dependsOn: ["src/java/com/cinema/controller/LoginController.java"],
  },
  {
    path: "src/java/com/cinema/controller/EmployeeServlet.java",
    className: "EmployeeServlet", classRange: [23, 308],
    summary: "Servlet CRUD của MANAGER dùng để quản trị tài khoản nhân viên tại `/manager/employees` (UC44/UC45): liệt kê có phân trang, thêm/sửa tài khoản kèm validate email và họ tên, và bật/tắt trạng thái hoạt động.",
    tags: ["quan-ly-nhan-vien", "crud", "manager", "validation", "api-handler"],
    languageNotes: "Đừng nhầm với EmployeeDashboardServlet.java — file này là màn hình quản trị (CRUD) tài khoản nhân viên dành cho MANAGER, không phải dashboard nghiệp vụ của nhân viên trực ca.",
    functions: [
      ["processRequest", 41, 78, "Định tuyến CRUD tài khoản nhân viên theo action (list/add/edit/create/update/toggle) tại `/manager/employees`.", ["dispatcher"]],
      ["showList", 80, 106, "Hiển thị danh sách tài khoản nhân viên có phân trang (UC44).", ["listing"]],
      ["showEditForm", 115, 131, "Tải thông tin một tài khoản nhân viên để hiển thị form chỉnh sửa.", ["form"]],
      ["handleCreate", 133, 157, "Xử lý tạo mới tài khoản nhân viên sau khi validate; forward lại form nếu có lỗi (UC44).", ["create"]],
      ["handleUpdate", 159, 187, "Xử lý cập nhật thông tin tài khoản nhân viên đã tồn tại sau khi validate (UC45).", ["update"]],
      ["handleToggle", 189, 204, "Bật/tắt trạng thái hoạt động (khoá/mở khoá) của một tài khoản nhân viên.", ["toggle"]],
      ["validateEmail", 220, 232, "Kiểm tra định dạng email và trùng lặp với tài khoản khác (ngoại trừ chính tài khoản đang sửa).", ["validation"]],
      ["buildAccountFromRequest", 240, 250, "Dựng đối tượng Account từ các tham số request khi submit form thêm/sửa nhân viên.", ["builder"]],
    ],
    routesTo: [
      "web/WEB-INF/manager/employees/list.jsp",
      "web/WEB-INF/manager/employees/form.jsp",
    ],
  },
  {
    path: "src/java/com/cinema/controller/ForgotPasswordController.java",
    className: "ForgotPasswordController", classRange: [21, 124],
    summary: "Servlet xử lý luồng quên mật khẩu: xác thực email tồn tại, sinh và gửi liên kết/khôi phục, hiển thị thông báo lỗi hoặc thành công trên `/forgot-password.jsp`.",
    tags: ["quen-mat-khau", "xac-thuc", "api-handler", "email"],
    functions: [
      ["processRequest", 34, 83, "Xử lý luồng quên mật khẩu: nhận email, kiểm tra tài khoản tồn tại, sinh và gửi liên kết khôi phục, hiển thị thông báo tương ứng.", ["password-reset"]],
    ],
    routesTo: ["web/forgot-password.jsp"],
  },
  {
    path: "src/java/com/cinema/controller/GenreController.java",
    className: "GenreController", classRange: [19, 114],
    summary: "Servlet CRUD thể loại phim (danh mục Genre): hiển thị danh sách và xử lý thêm/sửa/xoá thể loại trên `/genre.jsp`.",
    tags: ["crud", "danh-muc", "api-handler", "movie"],
    functions: [
      ["doGet", 30, 45, "Hiển thị danh sách thể loại phim hiện có, forward sang genre.jsp.", ["listing"]],
      ["doPost", 56, 113, "Xử lý thêm/sửa/xoá một thể loại phim theo tham số action.", ["crud"]],
    ],
    routesTo: ["web/genre.jsp"],
  },
  {
    path: "src/java/com/cinema/controller/HomeController.java",
    className: "HomeController", classRange: [14, 68],
    summary: "Servlet trang chủ: tải danh sách phim đang chiếu/sắp chiếu cùng danh mục thể loại rồi forward sang `/home.jsp`.",
    tags: ["entry-point", "trang-chu", "api-handler", "movie"],
    functions: [
      ["doGet", 16, 67, "Tải danh sách phim đang chiếu/sắp chiếu và danh mục thể loại cho trang chủ, forward sang home.jsp.", ["homepage"]],
    ],
    routesTo: ["web/home.jsp"],
  },
  {
    path: "src/java/com/cinema/controller/LoginController.java",
    className: "LoginController", classRange: [23, 183],
    summary: "Servlet xác thực đăng nhập: kiểm tra tài khoản/mật khẩu, tự động check-in ca làm cho nhân viên (roleId=3) nếu đang trong ca, ghi log hệ thống, thiết lập thời hạn phiên theo tuỳ chọn 'remember me', rồi điều hướng theo vai trò (Admin/Manager → `/manager`, Employee → `/employee`, Customer → `/`) hoặc theo URL đã lưu trước đó (redirectAfterLogin).",
    tags: ["xac-thuc", "dang-nhap", "phan-quyen", "session", "api-handler"],
    languageNotes: "Vai trò được phân theo roleId: 5=Admin, 4=Manager, 3=Employee, còn lại là Customer. Có ghi chú trong code về việc cố tình giới hạn session còn 1 ngày (thay vì 7 ngày) cho 'remember me' để tránh OutOfMemoryError khi tải cao.",
    functions: [
      ["processRequest", 36, 142, "Xác thực đăng nhập: kiểm tra tài khoản/mật khẩu, tự động check-in ca làm cho nhân viên (roleId=3) nếu đang trong ca, ghi log đăng nhập, thiết lập thời hạn phiên theo 'remember me', và điều hướng theo vai trò hoặc URL đã lưu (redirectAfterLogin).", ["authentication","authorization"]],
    ],
    routesTo: ["web/login.jsp"],
    dependsOn: [
      "src/java/com/cinema/controller/ManagerServlet.java",
      "src/java/com/cinema/controller/EmployeeDashboardServlet.java",
    ],
  },
  {
    path: "src/java/com/cinema/controller/LoginGoogleController.java",
    className: "LoginGoogleController", classRange: [22, 131],
    summary: "Servlet đăng nhập bằng Google OAuth2: redirect người dùng sang Google, xử lý callback đổi authorization code lấy access token, lấy thông tin hồ sơ Google rồi tìm/tạo tài khoản và thiết lập phiên đăng nhập.",
    tags: ["oauth2", "google", "xac-thuc", "api-handler", "tich-hop-ben-thu-ba"],
    functions: [
      ["doGet", 26, 95, "Xử lý callback OAuth2 từ Google: đổi authorization code lấy access token, lấy thông tin người dùng Google, tìm/tạo tài khoản tương ứng, thiết lập phiên đăng nhập.", ["oauth-callback"]],
      ["exchangeCodeForToken", 97, 116, "Gọi Google OAuth token endpoint để đổi authorization code lấy access token.", ["oauth2"]],
      ["getUserInfo", 118, 130, "Gọi Google UserInfo endpoint để lấy hồ sơ người dùng (email, tên) bằng access token.", ["oauth2"]],
    ],
    dependsOn: [
      "src/java/com/cinema/controller/LoginController.java",
      "src/java/com/cinema/controller/RegisterController.java",
    ],
  },
  {
    path: "src/java/com/cinema/controller/LogoutController.java",
    className: "LogoutController", classRange: [10, 21],
    summary: "Servlet đăng xuất đơn giản: huỷ phiên làm việc hiện tại và điều hướng người dùng về trang đăng nhập.",
    tags: ["dang-xuat", "session", "api-handler"],
    functions: [],
    dependsOn: ["src/java/com/cinema/controller/LoginController.java"],
  },
  {
    path: "src/java/com/cinema/controller/ManagerServlet.java",
    className: "ManagerServlet", classRange: [18, 310],
    summary: "Servlet dashboard tổng quan dành cho MANAGER: tổng hợp doanh thu/vé/suất chiếu hôm nay bằng truy vấn SQL trực tiếp qua DBUtils, số khuyến mãi đang active, biểu đồ doanh thu 7 ngày gần nhất (zero-filled), và bảng doanh thu/vé theo tháng có thể sắp xếp — phần phân tích doanh thu này được gộp từ trang `/manager/analytics` cũ vào thẳng dashboard.",
    tags: ["manager", "dashboard", "thong-ke", "bao-cao", "sql-truc-tiep"],
    languageNotes: "Servlet này truy vấn SQL thô trực tiếp qua DBUtils thay vì đi qua DAO (ngoại trừ một lời gọi PromotionDAO bằng tên đầy đủ, không có import); cách làm này tiện cho báo cáo tổng hợp phức tạp nhưng khó tái sử dụng/kiểm thử so với việc tách sang lớp service/DAO riêng.",
    functions: [
      ["showDashboard", 28, 252, "Tổng hợp toàn bộ số liệu cho dashboard quản lý: doanh thu/vé/suất chiếu hôm nay (SQL trực tiếp), số khuyến mãi đang active (qua PromotionDAO), biểu đồ doanh thu 7 ngày gần nhất, và bảng doanh thu/vé theo tháng đã gộp từ trang analytics cũ, có thể sắp xếp theo tháng hoặc doanh thu.", ["dashboard","reporting","analytics"]],
    ],
    routesTo: ["web/WEB-INF/manager/dashboard.jsp"],
    dependsOn: ["src/java/com/cinema/dao/PromotionDAO.java"],
  },
  {
    path: "src/java/com/cinema/controller/MovieController.java",
    className: "MovieController", classRange: [15, 169],
    summary: "Servlet CRUD phim dành cho manager: thêm, sửa, bật/tắt trạng thái hiển thị (UC-16) và cập nhật danh sách thể loại liên kết, forward tới các trang `add_movie.jsp`/`edit_movie.jsp`/`manage_movie.jsp`.",
    tags: ["crud", "movie", "manager", "api-handler"],
    functions: [
      ["doGet", 19, 58, "Định tuyến hiển thị: form thêm phim, form sửa phim, hoặc danh sách quản lý phim theo tham số action.", ["dispatcher"]],
      ["doPost", 60, 168, "Xử lý CRUD phim: bật/tắt trạng thái hiển thị (UC-16), thêm phim mới hoặc cập nhật phim đã có kèm cập nhật danh sách thể loại liên kết.", ["crud","uc-16"]],
    ],
    routesTo: ["web/add_movie.jsp", "web/edit_movie.jsp", "web/manage_movie.jsp"],
  },
  {
    path: "src/java/com/cinema/controller/MovieDetailController.java",
    className: "MovieDetailController", classRange: [18, 97],
    summary: "Servlet trang chi tiết phim: tải thông tin phim và các suất chiếu liên quan rồi forward sang `/movie-detail.jsp`; redirect về trang chủ nếu phim không tồn tại.",
    tags: ["movie-detail", "api-handler", "schedule"],
    functions: [
      ["doGet", 24, 96, "Tải thông tin chi tiết một phim cùng các suất chiếu liên quan rồi forward sang trang chi tiết phim; redirect về trang chủ nếu phim không tồn tại.", ["detail-page"]],
    ],
    routesTo: ["web/movie-detail.jsp"],
    dependsOn: ["src/java/com/cinema/controller/HomeController.java"],
  },
  {
    path: "src/java/com/cinema/controller/PromotionServlet.java",
    className: "PromotionServlet", classRange: [29, 989],
    summary: "Servlet quản lý vòng đời khuyến mãi (989 dòng) với máy trạng thái draft/upcoming/active/expired/inactive: tạo/sửa/xoá mềm/xoá cứng, kích hoạt sớm (activateEarly) khuyến mãi upcoming, gia hạn (extend) khuyến mãi đã expired về active, kích hoạt lại (reactivate) khuyến mãi inactive nhưng CẤM với khuyến mãi expired (bắt buộc dùng extend), cùng validate mã/loại/giá trị giảm giá, ngày hiệu lực, giới hạn sử dụng và ràng buộc khi mã đã được khách hàng sử dụng. Dùng các exception nghiệp vụ riêng (ConflictException, NotFoundException, ValidationException) để kiểm soát luồng lỗi thay vì mã lỗi thô.",
    tags: ["khuyen-mai", "state-machine", "manager", "validation", "exception-handling"],
    languageNotes: "Máy trạng thái khuyến mãi có 5 trạng thái với các transition đặc thù (vd. reactivate chặn expired — chỉ extend mới đưa expired quay lại active). Toàn bộ logic nghiệp vụ (validate, transition, dựng entity) tập trung trong 1 servlet 989 dòng — là ứng viên tốt để tách ra service/domain layer riêng.",
    functions: [
      ["processRequest", 157, 233, "Định tuyến GET/POST theo tham số action và view: các view danh sách theo trạng thái (upcoming/active/expired/inactive) và các action tạo/sửa/xoá/kích hoạt/gia hạn khuyến mãi.", ["dispatcher","state-machine"]],
      ["generateNextCode", 235, 248, "Sinh mã khuyến mãi tiếp theo tự động theo pattern quy định (CODE_PATTERN).", ["code-generation"]],
      ["handleGenerateCode", 252, 262, "Trả về mã khuyến mãi mới được sinh cho form thêm khuyến mãi (nút 'Generate Code').", ["ajax"]],
      ["showList", 264, 302, "Hiển thị danh sách khuyến mãi có phân trang, tìm kiếm và lọc theo loại/trạng thái.", ["listing"]],
      ["showEditForm", 311, 338, "Tải một khuyến mãi theo id để hiển thị form chỉnh sửa.", ["form"]],
      ["handleCreate", 340, 365, "Xử lý tạo khuyến mãi mới: validate dữ liệu rồi gọi create(dto), forward lại form nếu lỗi.", ["create"]],
      ["handleUpdate", 367, 403, "Xử lý cập nhật khuyến mãi: validate, kiểm tra ràng buộc nếu mã đã được sử dụng, rồi áp dụng thay đổi.", ["update"]],
      ["handleDelete", 405, 423, "Xoá mềm khuyến mãi (chuyển inactive); bỏ qua nếu đã không còn tồn tại, báo lỗi nếu đang bị ràng buộc (ConflictException).", ["delete","state-machine"]],
      ["handleDeactivate", 425, 438, "Chuyển một khuyến mãi đang active sang trạng thái inactive.", ["state-machine"]],
      ["handleReactivate", 440, 459, "Kích hoạt lại khuyến mãi inactive về active; từ chối nếu khuyến mãi đã ở trạng thái expired (bắt buộc dùng Extend thay thế).", ["state-machine"]],
      ["handleActivateEarly", 461, 475, "Kích hoạt sớm một khuyến mãi đang ở trạng thái upcoming sang active trước ngày bắt đầu dự kiến.", ["state-machine"]],
      ["handleExtend", 477, 500, "Gia hạn ngày kết thúc của khuyến mãi đã expired, tự động đưa về trạng thái active nếu ngày mới hợp lệ (phải ở tương lai).", ["state-machine"]],
      ["handleHardDelete", 502, 532, "Xoá vĩnh viễn một khuyến mãi khỏi hệ thống, khác với xoá mềm/deactivate.", ["hard-delete"]],
      ["showStatusList", 534, 571, "Hàm dùng chung để hiển thị danh sách khuyến mãi lọc theo một trạng thái cụ thể (upcoming/active/expired/inactive) và forward sang JSP tương ứng.", ["listing","shared-helper"]],
      ["create", 592, 601, "Gọi PromotionDAO tạo bản ghi khuyến mãi mới từ DTO đã build.", ["dao-call"]],
      ["update", 603, 619, "Gọi PromotionDAO cập nhật bản ghi khuyến mãi theo id từ DTO.", ["dao-call"]],
      ["delete", 621, 630, "Gọi PromotionDAO xoá khuyến mãi theo id (xoá mềm).", ["dao-call"]],
      ["validateForCreate", 634, 649, "Chạy toàn bộ quy tắc validate khi tạo mới khuyến mãi (mã, loại, giá trị, ngày, giới hạn sử dụng).", ["validation"]],
      ["validateForUpdate", 651, 679, "Chạy quy tắc validate khi cập nhật, bao gồm kiểm tra ràng buộc bổ sung nếu khuyến mãi đã được sử dụng.", ["validation"]],
      ["validatePromotionCode", 681, 700, "Kiểm tra định dạng mã khuyến mãi theo CODE_PATTERN và trùng lặp với các mã khác.", ["validation"]],
      ["validateDiscountValue", 712, 725, "Kiểm tra giá trị giảm giá hợp lệ theo loại giảm giá (phần trăm hay số tiền cố định).", ["validation"]],
      ["validateDates", 733, 760, "Kiểm tra ngày bắt đầu/kết thúc khuyến mãi hợp lệ và logic (kết thúc phải sau bắt đầu).", ["validation"]],
      ["checkUsedRestrictions", 768, 784, "Kiểm tra các trường không được phép thay đổi nếu khuyến mãi đã được khách hàng sử dụng.", ["validation","business-rule"]],
      ["buildEntity", 786, 814, "Dựng đối tượng Promotion mới từ DTO khi tạo khuyến mãi.", ["builder"]],
      ["applyUpdates", 816, 870, "Áp các thay đổi từ DTO lên bản ghi Promotion đã tồn tại khi cập nhật.", ["builder"]],
      ["parseDateTime", 882, 896, "Parse chuỗi ngày giờ từ form theo định dạng FORM_FORMATTER, trả về null nếu không hợp lệ.", ["utility"]],
      ["buildDtoFromRequest", 898, 935, "Đọc các tham số request và dựng DTO khuyến mãi dùng chung cho create/update.", ["builder"]],
      ["parseIntParam", 949, 958, "Parse tham số dạng số nguyên từ request, trả về giá trị mặc định nếu không hợp lệ.", ["utility"]],
      ["getRoleId", 960, 971, "Lấy roleId của tài khoản đang đăng nhập trong session để kiểm tra quyền manager.", ["authorization"]],
    ],
    routesTo: [
      "web/WEB-INF/manager/promotions/list.jsp",
      "web/WEB-INF/manager/promotions/form.jsp",
      "web/WEB-INF/manager/promotions/upcoming.jsp",
      "web/WEB-INF/manager/promotions/active.jsp",
      "web/WEB-INF/manager/promotions/expired.jsp",
      "web/WEB-INF/manager/promotions/inactive.jsp",
    ],
  },
  {
    path: "src/java/com/cinema/controller/RegisterController.java",
    className: "RegisterController", classRange: [24, 181],
    summary: "Servlet đăng ký tài khoản khách hàng mới: validate họ tên/email/mật khẩu/số điện thoại bằng regex, tạo tài khoản mới rồi forward lỗi hoặc redirect thành công trên `/register.jsp`.",
    tags: ["dang-ky", "xac-thuc", "validation", "api-handler"],
    functions: [
      ["processRequest", 41, 105, "Xử lý đăng ký tài khoản khách hàng mới: validate dữ liệu nhập, tạo tài khoản, forward lỗi hoặc redirect thành công.", ["registration"]],
      ["validateInput", 107, 131, "Validate họ tên, email, mật khẩu/xác nhận mật khẩu và số điện thoại theo regex khi đăng ký.", ["validation"]],
    ],
    routesTo: ["web/register.jsp"],
  },
  {
    path: "src/java/com/cinema/controller/ResetPasswordController.java",
    className: "ResetPasswordController", classRange: [20, 137],
    summary: "Servlet đặt lại mật khẩu bằng token khôi phục: kiểm tra token hợp lệ/hết hạn, cập nhật mật khẩu mới, forward sang `/new-password.jsp`.",
    tags: ["quen-mat-khau", "xac-thuc", "token", "api-handler"],
    functions: [
      ["processRequest", 33, 96, "Xử lý đặt lại mật khẩu bằng token khôi phục: kiểm tra token hợp lệ/hết hạn rồi cập nhật mật khẩu mới.", ["password-reset"]],
    ],
    routesTo: ["web/new-password.jsp"],
    dependsOn: ["src/java/com/cinema/controller/LoginController.java"],
  },
  {
    path: "src/java/com/cinema/controller/ReviewController.java",
    className: "ReviewController", classRange: [14, 66],
    summary: "Servlet nhận đánh giá phim (rating + bình luận) từ khách hàng đã đăng nhập, lưu vào hệ thống rồi redirect về trang chi tiết phim; yêu cầu đăng nhập trước khi đánh giá.",
    tags: ["danh-gia-phim", "review", "api-handler", "customer"],
    functions: [
      ["doPost", 17, 65, "Nhận đánh giá phim (rating, bình luận) từ khách hàng đã đăng nhập, lưu vào hệ thống rồi redirect về trang chi tiết phim; yêu cầu đăng nhập trước khi đánh giá.", ["review-submission"]],
    ],
    routesTo: ["web/login.jsp"],
    dependsOn: ["src/java/com/cinema/controller/MovieDetailController.java"],
  },
  {
    path: "src/java/com/cinema/controller/RoomServlet.java",
    className: "RoomServlet", classRange: [20, 345],
    summary: "Servlet CRUD phòng chiếu dành cho manager: thêm/sửa/xoá phòng, kiểm tra trùng số phòng khi thêm mới, và chặn thay đổi cấu hình ghế khi phòng đã có lịch chiếu.",
    tags: ["crud", "phong-chieu", "manager", "validation", "api-handler"],
    functions: [
      ["processRequest", 38, 74, "Định tuyến CRUD phòng chiếu theo action (list/add/edit/update/delete).", ["dispatcher"]],
      ["listRooms", 80, 115, "Hiển thị danh sách phòng chiếu có phân trang.", ["listing"]],
      ["addRoom", 120, 159, "Thêm mới phòng chiếu, kiểm tra trùng số phòng trước khi lưu.", ["create","validation"]],
      ["updateRoom", 164, 240, "Cập nhật thông tin phòng chiếu; chặn thay đổi cấu hình ghế nếu phòng đã có lịch chiếu.", ["update","business-rule"]],
      ["deleteRoom", 245, 274, "Xoá một phòng chiếu khỏi hệ thống.", ["delete"]],
      ["showEditForm", 280, 304, "Tải thông tin phòng chiếu và kiểm tra đã có lịch chiếu hay chưa để hiển thị form sửa (giới hạn sửa layout ghế).", ["form"]],
    ],
    routesTo: ["web/room-list.jsp", "web/room-edit.jsp"],
  },
  {
    path: "src/java/com/cinema/controller/ScheduleController.java",
    className: "ScheduleController", classRange: [19, 445],
    summary: "Servlet CRUD lịch chiếu: liệt kê suất chiếu theo phim, thêm/sửa/xoá suất chiếu với kiểm tra điều kiện có thể sửa/xoá (isEditable/isDeletable) dựa trên trạng thái bán vé.",
    tags: ["crud", "lich-chieu", "manager", "validation", "api-handler"],
    functions: [
      ["processRequest", 25, 52, "Định tuyến CRUD lịch chiếu theo action.", ["dispatcher"]],
      ["listSchedules", 54, 133, "Liệt kê các suất chiếu của một phim, kèm thông tin phòng chiếu.", ["listing"]],
      ["showAddForm", 135, 154, "Hiển thị form thêm suất chiếu mới cho một phim.", ["form"]],
      ["addSchedule", 156, 253, "Thêm suất chiếu mới: kiểm tra trùng giờ/phòng và tính toán thời gian chiếu hợp lệ.", ["create","validation"]],
      ["isEditable", 256, 268, "Kiểm tra một suất chiếu có được phép sửa hay không (dựa trên trạng thái/thời điểm).", ["business-rule"]],
      ["isDeletable", 271, 282, "Kiểm tra một suất chiếu có được phép xoá hay không (ví dụ chưa bán vé).", ["business-rule"]],
      ["showEditForm", 284, 308, "Tải thông tin suất chiếu để hiển thị form sửa.", ["form"]],
      ["updateSchedule", 310, 370, "Cập nhật thông tin một suất chiếu đã tồn tại.", ["update"]],
      ["deleteSchedule", 372, 405, "Xoá một suất chiếu khỏi hệ thống nếu đủ điều kiện.", ["delete"]],
    ],
    routesTo: ["web/schedule-list.jsp", "web/schedule-add.jsp", "web/schedule-edit.jsp"],
  },
  {
    path: "src/java/com/cinema/controller/SeatController.java",
    className: "SeatController", classRange: [28, 153],
    summary: "Servlet quản lý sơ đồ ghế theo phòng chiếu: xem layout ghế và cập nhật loại ghế/trạng thái cho từng hàng ghế trong phòng.",
    tags: ["quan-ly-ghe", "phong-chieu", "manager", "api-handler"],
    functions: [
      ["processRequest", 44, 70, "Định tuyến xem/sửa sơ đồ ghế theo action.", ["dispatcher"]],
      ["viewSeatLayout", 75, 94, "Hiển thị sơ đồ ghế của một phòng chiếu.", ["listing"]],
      ["updateRow", 96, 112, "Cập nhật loại ghế/trạng thái cho một hàng ghế trong phòng.", ["update"]],
    ],
    routesTo: ["web/seat-layout.jsp"],
  },
  {
    path: "src/java/com/cinema/controller/ShowtimeServlet.java",
    className: "ShowtimeServlet", classRange: [25, 144],
    summary: "Servlet hiển thị danh sách suất chiếu công khai cho khách hàng tại `/showtimes`, forward sang `Error.jsp` khi dữ liệu phim/lịch chiếu không hợp lệ.",
    tags: ["lich-chieu", "customer-facing", "api-handler", "error-handling"],
    functions: [
      ["processRequest", 39, 103, "Hiển thị danh sách suất chiếu công khai cho khách hàng; forward sang trang lỗi nếu dữ liệu phim/suất chiếu không hợp lệ.", ["listing","error-handling"]],
    ],
    routesTo: ["web/showtimes.jsp", "web/Error.jsp"],
  },
  {
    path: "src/java/com/cinema/controller/TMDBController.java",
    className: "TMDBController", classRange: [14, 42],
    summary: "Servlet API nội bộ trả JSON: nhận tên phim từ giao diện, gọi TMDBService để lấy dữ liệu từ TMDB (The Movie Database) và trả kết quả JSON cho client, dùng khi thêm phim mới để tự động điền thông tin.",
    tags: ["api-json", "tich-hop-ben-thu-ba", "tmdb", "ajax"],
    functions: [
      ["doGet", 16, 41, "API nội bộ trả JSON: nhận tên phim qua tham số `query`, gọi TMDBService lấy dữ liệu từ TMDB rồi trả JSON cho client.", ["json-api"]],
    ],
  },
  {
    path: "src/java/com/cinema/controller/WorkShiftServlet.java",
    className: "WorkShiftServlet", classRange: [27, 357],
    summary: "Servlet quản lý ca làm việc của nhân viên dành cho manager tại `/manager/shifts`: xem lịch ca theo tháng/loại ca, tạo/sửa/xoá ca đơn lẻ và tạo hàng loạt ca làm (bulk create).",
    tags: ["quan-ly-ca-lam", "manager", "crud", "lich-lam-viec", "api-handler"],
    functions: [
      ["processRequest", 60, 97, "Định tuyến CRUD ca làm việc theo action (calendar/edit/create/update/delete/bulk-create).", ["dispatcher"]],
      ["showCalendar", 100, 145, "Hiển thị lịch ca làm việc theo tháng và loại ca (shiftType).", ["calendar"]],
      ["handleCreate", 157, 200, "Tạo mới một ca làm việc cho nhân viên.", ["create"]],
      ["handleUpdate", 202, 220, "Cập nhật thông tin một ca làm việc đã tồn tại.", ["update"]],
      ["handleDelete", 222, 245, "Xoá một ca làm việc khỏi lịch.", ["delete"]],
      ["handleBulkCreate", 247, 282, "Tạo hàng loạt ca làm việc cùng lúc theo mẫu (bulk create).", ["bulk-create"]],
    ],
    routesTo: ["web/WEB-INF/manager/shifts/list.jsp", "web/WEB-INF/manager/shifts/form.jsp"],
  },
];

// ---------------------------------------------------------------------
// Build java file/class/function nodes + edges
// ---------------------------------------------------------------------
for (const f of files) {
  const fileId = "file:" + f.path;
  const nonEmptyLines = f.classRange[1] - f.classRange[0] + 1;
  addNode({
    id: fileId,
    type: "file",
    name: f.path.split("/").pop(),
    filePath: f.path,
    summary: f.summary,
    tags: f.tags,
    complexity: complexityFromLines(nonEmptyLines),
    ...(f.languageNotes ? { languageNotes: f.languageNotes } : {}),
  });

  const classId = "class:" + f.path + ":" + f.className;
  addNode({
    id: classId,
    type: "class",
    name: f.className,
    filePath: f.path,
    lineRange: f.classRange,
    summary: f.summary,
    tags: f.tags,
    complexity: complexityFromLines(f.classRange[1] - f.classRange[0] + 1),
  });
  addEdge(fileId, classId, "contains", 1.0);
  addEdge(fileId, classId, "exports", 0.8);

  for (const [name, start, end, summary, extraTags] of f.functions) {
    const fnId = "function:" + f.path + ":" + name;
    const lines = end - start + 1;
    addNode({
      id: fnId,
      type: "function",
      name,
      filePath: f.path,
      lineRange: [start, end],
      summary,
      tags: [...new Set([...extraTags, "servlet-method"])].slice(0, 5),
      complexity: complexityFromLines(lines),
    });
    addEdge(fileId, fnId, "contains", 1.0);
  }

  // imports edges (1:1 from batchImportData, no aggregation)
  const imports = batchImportData[f.path] || [];
  for (const target of imports) {
    addEdge(fileId, "file:" + target, "imports", 0.7);
  }

  // routes edges (controller -> JSP view targets)
  for (const target of (f.routesTo || [])) {
    addEdge(fileId, "file:" + target, "routes", 0.6);
  }

  // depends_on edges (controller -> controller / controller -> DAO via FQN)
  for (const target of (f.dependsOn || [])) {
    addEdge(fileId, "file:" + target, "depends_on", 0.6);
  }
}

// ---------------------------------------------------------------------
// JSP files (no tree-sitter structure; hand-authored summaries)
// ---------------------------------------------------------------------
const jspFiles = [
  {
    path: "web/Error.jsp",
    summary: "Trang lỗi chung của hệ thống: hiển thị thông báo lỗi được đặt vào request attribute `ERROR` (mặc định là thông báo lỗi hệ thống chung nếu không có) kèm liên kết quay về trang chủ.",
    tags: ["error-page", "jsp", "xu-ly-loi"],
    lines: 22,
  },
  {
    path: "web/WEB-INF/admin/_sidebar.jsp",
    summary: "Fragment thanh điều hướng bên (sidebar) cho khu vực System Admin: các liên kết Dashboard, User Accounts, Staff & Roles, System Logs, System Config và Logout; highlight mục đang active qua request attribute `activeNav`.",
    tags: ["sidebar", "admin", "jsp-include", "navigation"],
    lines: 85,
    dependsOn: ["src/java/com/cinema/controller/AdminServlet.java"],
  },
  {
    path: "web/WEB-INF/admin/config/index.jsp",
    summary: "Trang cấu hình hệ thống (UC50) dành cho System Admin: form chỉnh thông tin rạp (tên/địa chỉ/SĐT/email/banner trang chủ) và chính sách đặt vé (số ghế tối đa mỗi lần đặt, giờ tối thiểu để huỷ vé, giá vé cơ bản mặc định).",
    tags: ["cau-hinh-he-thong", "admin", "form", "uc50"],
    lines: 149,
    dependsOn: ["web/WEB-INF/admin/_sidebar.jsp"],
  },
  {
    path: "web/WEB-INF/admin/dashboard.jsp",
    summary: "Trang dashboard System Admin: thẻ thống kê tổng số người dùng/tổng số nhân viên và biểu đồ Chart.js hiển thị hoạt động hệ thống trong 7 ngày gần nhất.",
    tags: ["dashboard", "admin", "chartjs", "thong-ke"],
    lines: 79,
    dependsOn: ["web/WEB-INF/admin/_sidebar.jsp"],
  },
  {
    path: "web/WEB-INF/admin/logs/index.jsp",
    summary: "Trang xem nhật ký hệ thống (UC51) dành cho System Admin: bảng log có bộ lọc theo loại hành động/từ khoá tìm kiếm, phân trang, hiển thị các sự kiện như LOGIN_SUCCESS, LOGIN_FAILED, BOOK_TICKET, CHECKIN, CONFIG_UPDATE.",
    tags: ["system-logs", "admin", "uc51", "audit"],
    lines: 183,
    dependsOn: ["web/WEB-INF/admin/_sidebar.jsp"],
  },
  {
    path: "web/WEB-INF/employee/_sidebar.jsp",
    summary: "Fragment thanh điều hướng bên cho khu vực nhân viên nghiệp vụ: liên kết Dashboard, Schedules, Check-in, Ca Làm Việc (my-shifts), My Profile và Logout — tương ứng chính xác các route được xử lý bởi EmployeeDashboardServlet.",
    tags: ["sidebar", "nhan-vien-nghiep-vu", "jsp-include", "navigation"],
    lines: 75,
    dependsOn: ["src/java/com/cinema/controller/EmployeeDashboardServlet.java"],
  },
];

for (const j of jspFiles) {
  const fileId = "file:" + j.path;
  addNode({
    id: fileId,
    type: "file",
    name: j.path.split("/").pop(),
    filePath: j.path,
    summary: j.summary,
    tags: j.tags,
    complexity: complexityFromLines(j.lines),
  });
  for (const target of (j.dependsOn || [])) {
    addEdge(fileId, "file:" + target, "depends_on", 0.6);
  }
}

// ---------------------------------------------------------------------
// Self-check: imports edge count must equal sum of batchImportData lengths
// ---------------------------------------------------------------------
const expectedImportCount = Object.values(batchImportData).reduce((sum, arr) => sum + arr.length, 0);
const actualImportCount = edges.filter(e => e.type === "imports").length;
if (expectedImportCount !== actualImportCount) {
  throw new Error(`Import edge count mismatch: expected ${expectedImportCount}, got ${actualImportCount}`);
}

const output = { nodes, edges };
fs.writeFileSync(OUT_PATH, JSON.stringify(output, null, 2), "utf8");

console.log("Nodes:", nodes.length);
console.log("Edges:", edges.length);
console.log("Import edges:", actualImportCount, "(expected " + expectedImportCount + ")");
const typeCounts = {};
for (const n of nodes) typeCounts[n.type] = (typeCounts[n.type]||0)+1;
console.log("Node type counts:", JSON.stringify(typeCounts));
const edgeTypeCounts = {};
for (const e of edges) edgeTypeCounts[e.type] = (edgeTypeCounts[e.type]||0)+1;
console.log("Edge type counts:", JSON.stringify(edgeTypeCounts));
