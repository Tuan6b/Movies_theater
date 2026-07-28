/* ============================================================================
   seed_test_data.sql — sinh dữ liệu thử cho CinemaBookingDB

   Chạy trên database CinemaBookingDB (script không có USE/GO nên chạy được cả
   trong SSMS lẫn qua JDBC như một lệnh duy nhất).

   CHẠY LẠI ĐƯỢC: phần 1 xoá sạch dữ liệu của lần seed trước rồi mới sinh lại,
   nên chạy bao nhiêu lần cũng không nhân đôi. Dữ liệu thật (tài khoản, suất
   chiếu, hoá đơn có sẵn) không bị đụng tới.

   CÁCH NHẬN BIẾT DÒNG DO SEED SINH RA
     Account.Email        bắt đầu bằng 'seed.'
     WorkShift.Notes      = 'SEED'
     ShiftExchangeRequest.Message bắt đầu bằng 'SEED:'
     Schedule             BaseTicketPrice có phần lẻ .01  (giá thật luôn tròn)
     Invoice / Ticket / Notification / SystemLog / MovieReview / InvoiceFood
                          gắn với tài khoản hoặc suất chiếu do seed tạo

   MẬT KHẨU: mọi tài khoản seed đăng nhập bằng  123456

   LƯU Ý: script tôn trọng các ràng buộc đang có, gồm cả
     - UQ_WorkShift_Emp_Date_Start (1 nhân viên không có 2 ca cùng giờ/ngày)
     - UQ_Ticket_Seat_Schedule     (1 ghế chỉ bán 1 lần cho 1 suất)
     - Account.Email UNIQUE, Ticket.Code UNIQUE
   và các quy tắc kiểm tra của màn hình quản lý nhân viên: số điện thoại 10 số
   bắt đầu bằng 0 và không trùng nhau, ngày sinh luôn từ 18 tuổi trở lên.
   ============================================================================ */

SET NOCOUNT ON;

/* ---------------------------------------------------------------------------
   THAM SỐ — chỉnh ở đây nếu muốn nhiều/ít dữ liệu hơn
   --------------------------------------------------------------------------- */
DECLARE @Employees          INT = 20;   -- nhân viên (role 3)
DECLARE @Customers          INT = 60;   -- khách hàng (role 2)
DECLARE @DaysBack           INT = 120;  -- số ngày lịch chiếu trong quá khứ
DECLARE @DaysForward        INT = 21;   -- số ngày lịch chiếu sắp tới
DECLARE @ShowsPerRoomPerDay INT = 3;    -- suất/phòng/ngày (10h, 14h, 18h)
DECLARE @ShiftDaysBack      INT = 45;   -- số ngày phân ca trong quá khứ
DECLARE @ShiftDaysForward   INT = 30;   -- số ngày phân ca sắp tới

/* Hash của mật khẩu "123456" — cùng chuỗi mà file seed gốc dùng cho mọi tài khoản */
DECLARE @Pwd VARCHAR(255) = 'GxBf2JiV8tjQ8Va47w2dSN5/j3WSWL+1a3KSEDF3M16MFlGFj84AJfS2IW/J8XbL';
DECLARE @Today DATE = CAST(GETDATE() AS DATE);

/* ===========================================================================
   1. XOÁ DỮ LIỆU SEED CỦA LẦN TRƯỚC  (theo đúng thứ tự khoá ngoại)
   =========================================================================== */
DECLARE @SeedAcc TABLE (AccountID INT PRIMARY KEY);
INSERT INTO @SeedAcc SELECT AccountID FROM Account WHERE Email LIKE 'seed.%';

DECLARE @SeedSch TABLE (ScheduleID INT PRIMARY KEY);
INSERT INTO @SeedSch
SELECT ScheduleID FROM Schedule WHERE ABS(BaseTicketPrice - FLOOR(BaseTicketPrice) - 0.01) < 0.001;

DELETE r FROM MovieReview r
  JOIN Ticket t ON t.TicketID = r.TicketID
  JOIN Invoice i ON i.InvoiceID = t.InvoiceID
 WHERE i.AccountID IN (SELECT AccountID FROM @SeedAcc);

DELETE f FROM InvoiceFood f
  JOIN Invoice i ON i.InvoiceID = f.InvoiceID
 WHERE i.AccountID IN (SELECT AccountID FROM @SeedAcc);

DELETE t FROM Ticket t
  JOIN Invoice i ON i.InvoiceID = t.InvoiceID
 WHERE i.AccountID IN (SELECT AccountID FROM @SeedAcc);

DELETE t FROM Ticket t WHERE t.ScheduleID IN (SELECT ScheduleID FROM @SeedSch);

DELETE FROM Invoice WHERE AccountID IN (SELECT AccountID FROM @SeedAcc);

DELETE FROM ShiftExchangeRequest WHERE Message LIKE 'SEED:%';
DELETE FROM WorkShift            WHERE Notes = 'SEED';   -- cascade nốt request còn sót
DELETE FROM Notification         WHERE AccountID IN (SELECT AccountID FROM @SeedAcc);
DELETE FROM SystemLog            WHERE AccountID IN (SELECT AccountID FROM @SeedAcc);
DELETE FROM Schedule             WHERE ScheduleID IN (SELECT ScheduleID FROM @SeedSch);
DELETE FROM UserProfile          WHERE AccountID IN (SELECT AccountID FROM @SeedAcc);
DELETE FROM Account              WHERE AccountID IN (SELECT AccountID FROM @SeedAcc);

/* ===========================================================================
   2. BẢNG SỐ — dùng để sinh hàng loạt thay vì viết tay từng dòng INSERT
   =========================================================================== */
IF OBJECT_ID('tempdb..#N') IS NOT NULL DROP TABLE #N;
SELECT TOP (20000) ROW_NUMBER() OVER (ORDER BY (SELECT NULL)) - 1 AS n
INTO #N
FROM sys.all_objects a CROSS JOIN sys.all_objects b;
CREATE UNIQUE CLUSTERED INDEX IX_N ON #N(n);

/* Họ tên ghép từ 3 mảnh cho ra tên đọc được, không trùng lặp nhàm chán */
DECLARE @Ho TABLE (i INT, v NVARCHAR(20));
INSERT INTO @Ho VALUES (0,N'Nguyễn'),(1,N'Trần'),(2,N'Lê'),(3,N'Phạm'),(4,N'Hoàng'),
                       (5,N'Vũ'),(6,N'Đặng'),(7,N'Bùi'),(8,N'Đỗ'),(9,N'Ngô');
DECLARE @Dem TABLE (i INT, v NVARCHAR(20));
INSERT INTO @Dem VALUES (0,N'Văn'),(1,N'Thị'),(2,N'Minh'),(3,N'Quang'),(4,N'Thu'),
                        (5,N'Hải'),(6,N'Ngọc'),(7,N'Thanh');
DECLARE @Ten TABLE (i INT, v NVARCHAR(20));
INSERT INTO @Ten VALUES (0,N'An'),(1,N'Bình'),(2,N'Cường'),(3,N'Dũng'),(4,N'Hà'),(5,N'Hùng'),
                        (6,N'Khanh'),(7,N'Lan'),(8,N'Mai'),(9,N'Nam'),(10,N'Oanh'),(11,N'Phúc'),
                        (12,N'Quân'),(13,N'Sơn'),(14,N'Trang'),(15,N'Yến');

/* ===========================================================================
   3. NHÂN VIÊN (role 3)
      - SĐT: 093xxxxxxx, sinh theo chỉ số nên chắc chắn không trùng nhau
      - Ngày sinh: luôn từ 18 tuổi trở lên, đúng quy tắc của màn hình tạo NV
      - Cứ 7 người có 1 người 'pending' để thử luồng kích hoạt lần đầu
      - Cứ 10 người có 1 người bị khoá để thử lọc trạng thái
   =========================================================================== */
INSERT INTO Account (Email, Password, RoleID, IsBlocked, AccountStatus, CreatedAt)
SELECT 'seed.nv' + RIGHT('00' + CAST(n AS VARCHAR(3)), 2) + '@cinema.vn',
       @Pwd, 3,
       CASE WHEN n % 10 = 0 THEN 1 ELSE 0 END,
       CASE WHEN n % 7  = 0 THEN 'pending' ELSE 'active' END,
       DATEADD(DAY, -(200 + n * 3), GETDATE())
FROM #N WHERE n BETWEEN 1 AND @Employees;

INSERT INTO UserProfile (AccountID, FullName, PhoneNumber, DoB, Address)
SELECT a.AccountID,
       h.v + N' ' + d.v + N' ' + t.v,
       '093' + RIGHT('0000000' + CAST(1000000 + x.n AS VARCHAR(10)), 7),
       DATEADD(DAY, -(x.n * 11), DATEADD(YEAR, -(19 + x.n % 22), @Today)),
       N'Số ' + CAST(10 + x.n AS NVARCHAR(4)) + N', Quận ' + CAST(1 + x.n % 12 AS NVARCHAR(2)) + N', TP.HCM'
FROM Account a
JOIN #N x ON a.Email = 'seed.nv' + RIGHT('00' + CAST(x.n AS VARCHAR(3)), 2) + '@cinema.vn'
JOIN @Ho  h ON h.i = x.n % 10
JOIN @Dem d ON d.i = x.n % 8
JOIN @Ten t ON t.i = x.n % 16
WHERE x.n BETWEEN 1 AND @Employees;

/* ===========================================================================
   4. KHÁCH HÀNG (role 2) — SĐT 094xxxxxxx
   =========================================================================== */
INSERT INTO Account (Email, Password, RoleID, IsBlocked, AccountStatus, CreatedAt)
SELECT 'seed.kh' + RIGHT('000' + CAST(n AS VARCHAR(4)), 3) + '@gmail.com',
       @Pwd, 2, 0, 'noneed',
       DATEADD(DAY, -(n * 2), GETDATE())
FROM #N WHERE n BETWEEN 1 AND @Customers;

INSERT INTO UserProfile (AccountID, FullName, PhoneNumber, DoB, Address)
SELECT a.AccountID,
       h.v + N' ' + d.v + N' ' + t.v,
       '094' + RIGHT('0000000' + CAST(2000000 + x.n AS VARCHAR(10)), 7),
       DATEADD(DAY, -(x.n * 5), DATEADD(YEAR, -(16 + x.n % 40), @Today)),
       N'Số ' + CAST(1 + x.n AS NVARCHAR(4)) + N', Quận ' + CAST(1 + x.n % 12 AS NVARCHAR(2)) + N', TP.HCM'
FROM Account a
JOIN #N x ON a.Email = 'seed.kh' + RIGHT('000' + CAST(x.n AS VARCHAR(4)), 3) + '@gmail.com'
JOIN @Ho  h ON h.i = (x.n + 3) % 10
JOIN @Dem d ON d.i = (x.n + 5) % 8
JOIN @Ten t ON t.i = (x.n + 7) % 16
WHERE x.n BETWEEN 1 AND @Customers;

/* ===========================================================================
   5. LỊCH CHIẾU — mỗi phòng @ShowsPerRoomPerDay suất/ngày, khung 10h/14h/18h
      Giá để phần lẻ .01 làm dấu nhận biết dòng seed.
   =========================================================================== */
DECLARE @Rooms TABLE (rn INT, RoomID INT, Price DECIMAL(10,2));
INSERT INTO @Rooms
SELECT ROW_NUMBER() OVER (ORDER BY RoomID) - 1, RoomID,
       CASE RoomType WHEN 'IMAX' THEN 150000.01 WHEN '4DX' THEN 180000.01
                     WHEN '3D' THEN 110000.01 ELSE 80000.01 END
FROM Room WHERE IsActive = 1;

DECLARE @Movies TABLE (rn INT, MovieID INT, Duration INT);
INSERT INTO @Movies
SELECT ROW_NUMBER() OVER (ORDER BY MovieID) - 1, MovieID, Duration
FROM Movie WHERE IsActive = 1;

DECLARE @MovieCount INT = (SELECT COUNT(*) FROM @Movies);

INSERT INTO Schedule (RoomID, MovieID, StartTime, EndTime, BaseTicketPrice, Status)
SELECT r.RoomID, m.MovieID,
       DATEADD(HOUR, 10 + s.n * 4, CAST(DATEADD(DAY, d.n - @DaysBack, @Today) AS DATETIME)),
       DATEADD(MINUTE, m.Duration,
           DATEADD(HOUR, 10 + s.n * 4, CAST(DATEADD(DAY, d.n - @DaysBack, @Today) AS DATETIME))),
       r.Price,
       CASE WHEN DATEADD(MINUTE, m.Duration,
                    DATEADD(HOUR, 10 + s.n * 4,
                        CAST(DATEADD(DAY, d.n - @DaysBack, @Today) AS DATETIME))) < GETDATE()
            THEN 'Finished' ELSE 'Scheduled' END
FROM #N d
CROSS JOIN @Rooms r
JOIN #N s ON s.n < @ShowsPerRoomPerDay
JOIN @Movies m ON m.rn = (d.n + r.rn + s.n) % @MovieCount
WHERE d.n <= @DaysBack + @DaysForward;

/* ===========================================================================
   6. HOÁ ĐƠN + VÉ cho các suất đã chiếu
      Mỗi suất đã chiếu -> 1 hoá đơn của 1 khách, kèm 4–12 vé (ghế liền nhau).
      MERGE ... OUTPUT là cách duy nhất lấy được InvoiceID vừa sinh kèm theo
      ScheduleID nguồn, để bước sau gắn vé vào đúng hoá đơn.
   =========================================================================== */
IF OBJECT_ID('tempdb..#Inv') IS NOT NULL DROP TABLE #Inv;
CREATE TABLE #Inv (ScheduleID INT PRIMARY KEY, InvoiceID INT, Seats INT, Price DECIMAL(10,2));

DECLARE @CustFrom INT = (SELECT MIN(AccountID) FROM Account WHERE Email LIKE 'seed.kh%');

MERGE INTO Invoice AS tgt
USING (
    SELECT sc.ScheduleID,
           @CustFrom + (sc.ScheduleID % @Customers) AS AccountID,
           4 + (sc.ScheduleID % 9)                  AS Seats,
           sc.BaseTicketPrice                       AS Price,
           DATEADD(MINUTE, -(30 + sc.ScheduleID % 300), sc.StartTime) AS CreatedAt,
           CASE sc.ScheduleID % 4 WHEN 0 THEN 'Cash' WHEN 1 THEN 'Card'
                                  WHEN 2 THEN 'VNPay' ELSE 'MoMo' END AS PaymentMethod
    FROM Schedule sc
    WHERE ABS(sc.BaseTicketPrice - FLOOR(sc.BaseTicketPrice) - 0.01) < 0.001
      AND sc.EndTime < GETDATE()
) AS src
ON 1 = 0
WHEN NOT MATCHED THEN
    INSERT (AccountID, PromotionID, SubTotal, DiscountAmount, TotalAmount,
            PaymentMethod, PaymentStatus, CreatedAt)
    VALUES (src.AccountID, NULL, src.Seats * src.Price, 0, src.Seats * src.Price,
            src.PaymentMethod,
            /* ~1/25 hoá đơn chưa thanh toán, để báo cáo doanh thu có cả dữ liệu bị loại */
            CASE WHEN src.ScheduleID % 25 = 0 THEN 'Pending' ELSE 'Paid' END,
            src.CreatedAt)
OUTPUT inserted.InvoiceID, src.ScheduleID, src.Seats, src.Price
  INTO #Inv (InvoiceID, ScheduleID, Seats, Price);

WITH SeatRank AS (
    SELECT SeatID, RoomID, ROW_NUMBER() OVER (PARTITION BY RoomID ORDER BY SeatID) AS rn
    FROM Seat WHERE IsActive = 1
)
INSERT INTO Ticket (ScheduleID, SeatID, InvoiceID, PriceAtBooking, Code, IsCheckedIn, CheckedInAt)
SELECT i.ScheduleID, s.SeatID, i.InvoiceID, i.Price,
       'SEED-' + RIGHT('00000000' + CAST(ROW_NUMBER() OVER (ORDER BY i.InvoiceID, s.rn) AS VARCHAR(10)), 8),
       CASE WHEN (i.ScheduleID + s.rn) % 10 < 7 THEN 1 ELSE 0 END,
       CASE WHEN (i.ScheduleID + s.rn) % 10 < 7
            THEN DATEADD(MINUTE, -20, sc.StartTime) ELSE NULL END
FROM #Inv i
JOIN Schedule sc ON sc.ScheduleID = i.ScheduleID
JOIN SeatRank s  ON s.RoomID = sc.RoomID AND s.rn <= i.Seats;

/* Bắp nước kèm theo, cứ 4 hoá đơn thì 1 hoá đơn có mua */
INSERT INTO InvoiceFood (InvoiceID, FoodID, Quantity, PriceAtBooking)
SELECT i.InvoiceID, f.FoodID, 1 + (i.InvoiceID % 3), f.Price
FROM #Inv i
JOIN Food f ON f.FoodID = (SELECT MIN(FoodID) FROM Food WHERE IsActive = 1)
             + (i.InvoiceID % NULLIF((SELECT COUNT(*) FROM Food WHERE IsActive = 1), 0))
WHERE i.InvoiceID % 4 = 0
  AND EXISTS (SELECT 1 FROM Food WHERE FoodID = f.FoodID);

/* Đánh giá phim cho một phần vé đã check-in (1 vé chỉ được 1 đánh giá) */
INSERT INTO MovieReview (MovieID, AccountID, TicketID, RatingValue, Comment, CreatedAt)
SELECT sc.MovieID, inv.AccountID, t.TicketID,
       3 + (t.TicketID % 3),
       N'Đánh giá thử nghiệm số ' + CAST(t.TicketID AS NVARCHAR(10)),
       DATEADD(HOUR, 3, sc.EndTime)
FROM Ticket t
JOIN Invoice inv ON inv.InvoiceID = t.InvoiceID
JOIN Schedule sc ON sc.ScheduleID = t.ScheduleID
WHERE t.Code LIKE 'SEED-%' AND t.IsCheckedIn = 1 AND t.TicketID % 17 = 0
  AND NOT EXISTS (SELECT 1 FROM MovieReview r WHERE r.TicketID = t.TicketID);

/* ===========================================================================
   7. CA LÀM VIỆC — 1 ca/ngày/nhân viên, nghỉ 2 ngày mỗi tuần.
      Giờ ca lấy đúng 5 loại ca cố định trong WorkShiftServlet.SHIFT_TIMES.
      Mỗi nhân viên chỉ có 1 ca mỗi ngày nên không đụng UQ_WorkShift_Emp_Date_Start.
   =========================================================================== */
DECLARE @Emp TABLE (rn INT, AccountID INT);
INSERT INTO @Emp
SELECT ROW_NUMBER() OVER (ORDER BY AccountID) - 1, AccountID
FROM Account WHERE Email LIKE 'seed.nv%';

DECLARE @Shift TABLE (i INT, S TIME, E TIME);
INSERT INTO @Shift VALUES (0,'08:00','14:00'),(1,'14:00','20:00'),(2,'20:00','23:59'),
                          (3,'08:00','17:30'),(4,'13:00','22:30');

INSERT INTO WorkShift (EmployeeID, ShiftDate, StartTime, EndTime, Status, Notes)
SELECT e.AccountID,
       DATEADD(DAY, d.n - @ShiftDaysBack, @Today),
       sh.S, sh.E,
       CASE WHEN DATEADD(DAY, d.n - @ShiftDaysBack, @Today) >= @Today THEN 'Scheduled'
            WHEN (e.rn + d.n) % 13 = 0                                THEN 'Absent'
            ELSE 'Completed' END,
       'SEED'
FROM @Emp e
JOIN #N d ON d.n <= @ShiftDaysBack + @ShiftDaysForward
JOIN @Shift sh ON sh.i = (e.rn + d.n / 7) % 5
WHERE (e.rn + d.n) % 7 < 5;   -- làm 5 ngày, nghỉ 2

/* ===========================================================================
   8. YÊU CẦU ĐỔI CA — dồn vào hàng chờ của quản lý
      Chỉ sinh Pending / Rejected / Cancelled: cả ba đều để ca nguyên chỗ cũ.
      Trạng thái 'Accepted' cố ý không sinh, vì duyệt còn phải chuyển chủ ca —
      hãy bấm Duyệt trên /manager/shift-exchanges để có dữ liệu đó cho đúng.
   =========================================================================== */
INSERT INTO ShiftExchangeRequest (ShiftID, RequesterID, TargetEmpID, Message, Status, CreatedAt, RespondedAt)
SELECT w.ShiftID, w.EmployeeID, tgt.AccountID,
       'SEED: ' + CASE w.ShiftID % 4
            WHEN 0 THEN N'Bận việc gia đình, nhờ bạn nhận giúp ca này'
            WHEN 1 THEN N'Trùng lịch học, xin đổi ca'
            WHEN 2 THEN N'Có hẹn khám sức khoẻ'
            ELSE        N'Xin nhường ca' END,
       CASE w.ShiftID % 5 WHEN 3 THEN 'Rejected' WHEN 4 THEN 'Cancelled' ELSE 'Pending' END,
       DATEADD(DAY, -2, GETDATE()),
       CASE WHEN w.ShiftID % 5 IN (3,4) THEN DATEADD(DAY, -1, GETDATE()) ELSE NULL END
FROM (
    SELECT w.ShiftID, w.EmployeeID,
           ROW_NUMBER() OVER (ORDER BY w.ShiftID) AS rn
    FROM WorkShift w
    WHERE w.Notes = 'SEED' AND w.Status = 'Scheduled' AND w.ShiftDate > @Today
      AND w.ShiftID % 23 = 0
) w
JOIN @Emp tgt ON tgt.rn = (w.rn * 3) % (SELECT COUNT(*) FROM @Emp)
WHERE tgt.AccountID <> w.EmployeeID;

/* ===========================================================================
   9. THÔNG BÁO cho nhân viên seed (chuông trên khu nhân viên)
   =========================================================================== */
INSERT INTO Notification (AccountID, Type, Title, Message, ReferenceID, IsRead, CreatedAt)
SELECT e.AccountID,
       CASE d.n % 3 WHEN 0 THEN 'SHIFT_EXCHANGE_REQUESTED'
                    WHEN 1 THEN 'SHIFT_EXCHANGE_ACCEPTED'
                    ELSE 'SHIFT_EXCHANGE_REJECTED' END,
       CASE d.n % 3 WHEN 0 THEN N'Shift Exchange Proposed'
                    WHEN 1 THEN N'Shift Exchange Approved'
                    ELSE N'Shift Exchange Declined' END,
       N'Thông báo thử nghiệm số ' + CAST(d.n AS NVARCHAR(4)) + N' cho tài khoản seed.',
       NULL,
       CASE WHEN d.n % 3 = 0 THEN 0 ELSE 1 END,
       DATEADD(HOUR, -(d.n * 5), GETDATE())
FROM @Emp e
JOIN #N d ON d.n BETWEEN 1 AND 4;

/* ===========================================================================
   10. NHẬT KÝ HỆ THỐNG (trang /admin/logs)
   =========================================================================== */
INSERT INTO SystemLog (AccountID, ActionType, Description, IPAddress, CreatedAt)
SELECT a.AccountID,
       CASE d.n % 6 WHEN 0 THEN 'LOGIN' WHEN 1 THEN 'LOGOUT' WHEN 2 THEN 'CREATE_TICKET'
                    WHEN 3 THEN 'CHECKIN_TICKET' WHEN 4 THEN 'UPDATE_PROFILE'
                    ELSE 'VIEW_REPORT' END,
       N'SEED: thao tác thử nghiệm số ' + CAST(d.n AS NVARCHAR(4)),
       '192.168.1.' + CAST(10 + (d.n % 200) AS VARCHAR(3)),
       DATEADD(HOUR, -(d.n * 7), GETDATE())
FROM (SELECT TOP 25 AccountID FROM Account WHERE Email LIKE 'seed.%' ORDER BY AccountID) a
JOIN #N d ON d.n BETWEEN 1 AND 12;

/* ===========================================================================
   11. TỔNG KẾT
   =========================================================================== */
DROP TABLE #N;
DROP TABLE #Inv;

SELECT 'Nhân viên seed'        AS Bang, COUNT(*) AS SoDong FROM Account WHERE Email LIKE 'seed.nv%'
UNION ALL SELECT 'Khách hàng seed',     COUNT(*) FROM Account WHERE Email LIKE 'seed.kh%'
UNION ALL SELECT 'Lịch chiếu seed',     COUNT(*) FROM Schedule
          WHERE ABS(BaseTicketPrice - FLOOR(BaseTicketPrice) - 0.01) < 0.001
UNION ALL SELECT 'Hoá đơn seed',        COUNT(*) FROM Invoice i
          WHERE i.AccountID IN (SELECT AccountID FROM Account WHERE Email LIKE 'seed.%')
UNION ALL SELECT 'Vé seed',             COUNT(*) FROM Ticket WHERE Code LIKE 'SEED-%'
UNION ALL SELECT 'Bắp nước theo hoá đơn', COUNT(*) FROM InvoiceFood f
          WHERE f.InvoiceID IN (SELECT InvoiceID FROM Invoice
                                WHERE AccountID IN (SELECT AccountID FROM Account WHERE Email LIKE 'seed.%'))
UNION ALL SELECT 'Đánh giá phim',       COUNT(*) FROM MovieReview r
          WHERE r.TicketID IN (SELECT TicketID FROM Ticket WHERE Code LIKE 'SEED-%')
UNION ALL SELECT 'Ca làm việc seed',    COUNT(*) FROM WorkShift WHERE Notes = 'SEED'
UNION ALL SELECT 'Yêu cầu đổi ca seed', COUNT(*) FROM ShiftExchangeRequest WHERE Message LIKE 'SEED:%'
UNION ALL SELECT '  → đang chờ duyệt',  COUNT(*) FROM ShiftExchangeRequest
          WHERE Message LIKE 'SEED:%' AND Status = 'Pending'
UNION ALL SELECT 'Thông báo seed',      COUNT(*) FROM Notification
          WHERE AccountID IN (SELECT AccountID FROM Account WHERE Email LIKE 'seed.%')
UNION ALL SELECT 'Nhật ký seed',        COUNT(*) FROM SystemLog
          WHERE AccountID IN (SELECT AccountID FROM Account WHERE Email LIKE 'seed.%');
