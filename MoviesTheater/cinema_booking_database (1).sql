USE master;
GO

IF EXISTS (SELECT name FROM sys.databases WHERE name = N'CinemaBookingDB')
    DROP DATABASE CinemaBookingDB;
GO

CREATE DATABASE CinemaBookingDB;
GO

USE CinemaBookingDB;
GO


CREATE TABLE Role (
    RoleID INT IDENTITY(1,1) PRIMARY KEY,
    RoleName NVARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE Account (
    AccountID        INT IDENTITY(1,1) PRIMARY KEY,
    Email            VARCHAR(255)  NOT NULL UNIQUE,
    Password         VARCHAR(255)  NOT NULL,
    RoleID           INT           NOT NULL,
    IsBlocked        BIT           NOT NULL DEFAULT 0,
    AccountStatus    VARCHAR(20)   NOT NULL DEFAULT 'noneed',
    CreatedAt        DATETIME      NOT NULL DEFAULT GETDATE(),
    ResetToken       VARCHAR(255)  NULL,
    ResetTokenExpiry DATETIME      NULL,
    CONSTRAINT FK_Account_Role FOREIGN KEY (RoleID) REFERENCES Role(RoleID)
);

CREATE TABLE UserProfile (
    AccountID INT PRIMARY KEY,
    FullName NVARCHAR(100) NULL,
    PhoneNumber VARCHAR(20) NULL,
    DoB DATE NULL,
    Address NVARCHAR(255) NULL,
    AvatarURL VARCHAR(500) NULL,
    CONSTRAINT FK_UserProfile_Account FOREIGN KEY (AccountID) REFERENCES Account(AccountID)
);

CREATE TABLE Movie (
    MovieID INT IDENTITY(1,1) PRIMARY KEY,
    MovieName NVARCHAR(255) NOT NULL,
    Description NVARCHAR(MAX) NULL,
    Duration INT NOT NULL, -- minutes
    ReleaseDate DATE NULL,
    Poster VARCHAR(500) NULL,
    Trailer VARCHAR(500) NULL,
    Language NVARCHAR(50) NULL,
    Subtitle NVARCHAR(50) NULL,
    Director NVARCHAR(100) NULL,
    Cast NVARCHAR(500) NULL,
    Country NVARCHAR(100) NULL,
    AgeRestriction INT NOT NULL DEFAULT 0,
    IsActive BIT NOT NULL DEFAULT 1
);

CREATE TABLE Genre (
    GenreID INT IDENTITY(1,1) PRIMARY KEY,
    GenreName NVARCHAR(100) NOT NULL UNIQUE
);

-- Junction table: Movie <-> Genre (M:N)
CREATE TABLE MovieGenre (
    MovieID INT NOT NULL,
    GenreID INT NOT NULL,
    CONSTRAINT PK_MovieGenre PRIMARY KEY (MovieID, GenreID),
    CONSTRAINT FK_MG_Movie FOREIGN KEY (MovieID) REFERENCES Movie(MovieID),
    CONSTRAINT FK_MG_Genre FOREIGN KEY (GenreID) REFERENCES Genre(GenreID)
);

CREATE TABLE Room (
    RoomID INT IDENTITY(1,1) PRIMARY KEY,
    RoomNumber NVARCHAR(50) NOT NULL UNIQUE,
    RoomType VARCHAR(20) NOT NULL DEFAULT '2D',  -- '2D','3D','IMAX','4DX'
    Capacity INT NOT NULL,
    NumberOfRows INT NOT NULL DEFAULT 5,
    SeatsPerRow INT NOT NULL DEFAULT 10,
    IsActive BIT NOT NULL DEFAULT 1,
    CONSTRAINT CHK_Room_Type CHECK (RoomType IN ('2D', '3D', 'IMAX', '4DX'))
);

CREATE TABLE Seat (
    SeatID INT IDENTITY(1,1) PRIMARY KEY,
    RoomID INT NOT NULL,
    RowChar VARCHAR(5) NOT NULL,   -- e.g. 'A', 'B', 'C'
    ColNumber INT NOT NULL,   -- e.g. 1, 2, 3
    SeatType VARCHAR(20) NOT NULL DEFAULT 'Normal',  -- 'Normal', 'VIP'
    IsActive BIT NOT NULL DEFAULT 1,
    CONSTRAINT FK_Seat_Room FOREIGN KEY (RoomID) REFERENCES Room(RoomID),
    CONSTRAINT UQ_Seat_Position UNIQUE (RoomID, RowChar, ColNumber),
    CONSTRAINT CHK_Seat_Type CHECK (SeatType IN ('Normal', 'VIP', 'Couple'))
);

CREATE TABLE Schedule (
    ScheduleID INT IDENTITY(1,1) PRIMARY KEY,
    RoomID INT NOT NULL,
    MovieID INT NOT NULL,
    StartTime DATETIME NOT NULL,
    EndTime DATETIME NOT NULL,  -- stored for query optimization (= StartTime + Duration)
    BaseTicketPrice DECIMAL(10,2) NOT NULL,
    Status VARCHAR(20) NOT NULL DEFAULT 'Scheduled', -- 'Scheduled','Ongoing','Finished','Cancelled'
    CONSTRAINT FK_Schedule_Room FOREIGN KEY (RoomID)  REFERENCES Room(RoomID),
    CONSTRAINT FK_Schedule_Movie FOREIGN KEY (MovieID) REFERENCES Movie(MovieID),
    CONSTRAINT CHK_Schedule_Time CHECK (EndTime > StartTime),
    CONSTRAINT CHK_Schedule_Status CHECK (Status IN ('Scheduled','Ongoing','Finished','Cancelled'))
);

CREATE TABLE Food (
    FoodID INT IDENTITY(1,1) PRIMARY KEY,
    FoodName NVARCHAR(100) NOT NULL,
    Price DECIMAL(10,2) NOT NULL,
    Image VARCHAR(500) NULL,
    IsActive BIT NOT NULL DEFAULT 1
);

CREATE TABLE Promotion (
    PromotionID INT            IDENTITY(1,1) PRIMARY KEY,
    PromotionCode VARCHAR(50)    NOT NULL UNIQUE,
    Description NVARCHAR(255)  NULL,
    DiscountType VARCHAR(20)    NOT NULL,  -- 'Percentage', 'FlatAmount'
    DiscountValue DECIMAL(10,2)  NOT NULL,
    MinOrderAmount DECIMAL(10,2)  NOT NULL DEFAULT 0,
    MaxDiscountAmount DECIMAL(10,2) NULL,
    StartDate DATETIME NOT NULL,
    EndDate DATETIME NOT NULL,
    UsageLimit INT NULL,
    UsedCount INT NOT NULL DEFAULT 0,
    IsActive BIT NOT NULL DEFAULT 1,
    Status NVARCHAR(20) NOT NULL DEFAULT 'active',
    CONSTRAINT CHK_Promotion_Type CHECK (DiscountType IN ('Percentage', 'FlatAmount')),
    CONSTRAINT CHK_Promotion_Value CHECK (DiscountValue > 0),
    CONSTRAINT CHK_Promotion_Date CHECK (EndDate > StartDate),
    CONSTRAINT CHK_Promotion_Status CHECK (Status IN ('upcoming', 'active', 'expired', 'inactive'))
);

CREATE TABLE Invoice (
    InvoiceID INT IDENTITY(1,1) PRIMARY KEY,
    AccountID INT NOT NULL,
    PromotionID INT NULL,
    SubTotal DECIMAL(10,2) NOT NULL,
    DiscountAmount DECIMAL(10,2) NOT NULL DEFAULT 0,
    TotalAmount DECIMAL(10,2) NOT NULL,
    PaymentMethod VARCHAR(50) NOT NULL,  -- 'Cash','VNPay','MoMo','ZaloPay'
    PaymentStatus VARCHAR(20) NOT NULL DEFAULT 'Pending',
    CreatedAt DATETIME NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_Invoice_Account FOREIGN KEY (AccountID) REFERENCES Account(AccountID),
    CONSTRAINT FK_Invoice_Promotion FOREIGN KEY (PromotionID) REFERENCES Promotion(PromotionID),
    CONSTRAINT CHK_Invoice_Status CHECK (PaymentStatus IN ('Pending','Paid','Failed','Refunded'))
);

CREATE TABLE Ticket (
    TicketID INT IDENTITY(1,1) PRIMARY KEY,
    ScheduleID INT NOT NULL,
    SeatID INT NOT NULL,
    InvoiceID INT NOT NULL,
    PriceAtBooking DECIMAL(10,2) NOT NULL,
    Code VARCHAR(100) NOT NULL UNIQUE,  -- QR code value
    IsCheckedIn BIT NOT NULL DEFAULT 0,
    CheckedInAt DATETIME NULL,
    CONSTRAINT FK_Ticket_Schedule FOREIGN KEY (ScheduleID) REFERENCES Schedule(ScheduleID),
    CONSTRAINT FK_Ticket_Seat FOREIGN KEY (SeatID) REFERENCES Seat(SeatID),
    CONSTRAINT FK_Ticket_Invoice FOREIGN KEY (InvoiceID) REFERENCES Invoice(InvoiceID),
    CONSTRAINT UQ_Ticket_Seat_Schedule UNIQUE (ScheduleID, SeatID)
);

-- Junction table: Invoice <-> Food (M:N)
CREATE TABLE InvoiceFood (
    InvoiceID INT NOT NULL,
    FoodID INT NOT NULL,
    Quantity INT NOT NULL DEFAULT 1,
    PriceAtBooking DECIMAL(10,2) NOT NULL,
    CONSTRAINT PK_InvoiceFood PRIMARY KEY (InvoiceID, FoodID),
    CONSTRAINT FK_IF_Invoice FOREIGN KEY (InvoiceID) REFERENCES Invoice(InvoiceID),
    CONSTRAINT FK_IF_Food FOREIGN KEY (FoodID) REFERENCES Food(FoodID),
    CONSTRAINT CHK_IF_Quantity CHECK (Quantity > 0)
);

CREATE TABLE MovieReview (
    ReviewID INT IDENTITY(1,1) PRIMARY KEY,
    MovieID INT NOT NULL,
    AccountID INT NOT NULL,
    TicketID INT NOT NULL UNIQUE,  -- 1 ticket = 1 review only
    RatingValue INT NOT NULL,
    Comment NVARCHAR(MAX) NULL,
    CreatedAt DATETIME NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_Review_Movie FOREIGN KEY (MovieID) REFERENCES Movie(MovieID),
    CONSTRAINT FK_Review_Account FOREIGN KEY (AccountID) REFERENCES Account(AccountID),
    CONSTRAINT FK_Review_Ticket FOREIGN KEY (TicketID) REFERENCES Ticket(TicketID),
    CONSTRAINT CHK_Review_Rating CHECK (RatingValue BETWEEN 1 AND 5)
);

CREATE TABLE WorkShift (
    ShiftID   INT IDENTITY(1,1) PRIMARY KEY,
    EmployeeID INT NOT NULL,
    ShiftDate  DATE NOT NULL,
    StartTime  TIME NOT NULL,
    EndTime    TIME NOT NULL,
    Status     VARCHAR(20) NOT NULL DEFAULT 'Scheduled',
    Notes      NVARCHAR(500) NULL,
    CreatedAt  DATETIME NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_WorkShift_Employee FOREIGN KEY (EmployeeID) REFERENCES Account(AccountID),
    CONSTRAINT CHK_WorkShift_Status CHECK (Status IN ('Scheduled', 'Completed', 'Absent'))
);

CREATE INDEX IDX_WorkShift_EmployeeID ON WorkShift(EmployeeID);
CREATE INDEX IDX_WorkShift_ShiftDate  ON WorkShift(ShiftDate);

CREATE TABLE ShiftExchangeRequest (
    RequestID   INT IDENTITY(1,1) PRIMARY KEY,
    ShiftID     INT NOT NULL,
    RequesterID INT NOT NULL,
    TargetEmpID INT NOT NULL,
    Message     NVARCHAR(500) NULL,
    Status      VARCHAR(20)   NOT NULL DEFAULT 'Pending',
    CreatedAt   DATETIME      NOT NULL DEFAULT GETDATE(),
    RespondedAt DATETIME      NULL,
    CONSTRAINT FK_SER_Shift      FOREIGN KEY (ShiftID)     REFERENCES WorkShift(ShiftID) ON DELETE CASCADE,
    CONSTRAINT FK_SER_Requester  FOREIGN KEY (RequesterID) REFERENCES Account(AccountID),
    CONSTRAINT FK_SER_Target     FOREIGN KEY (TargetEmpID) REFERENCES Account(AccountID),
    CONSTRAINT CHK_SER_Status    CHECK (Status IN ('Pending', 'Accepted', 'Rejected', 'Cancelled'))
);

CREATE INDEX IDX_SER_ShiftID     ON ShiftExchangeRequest(ShiftID);
CREATE INDEX IDX_SER_RequesterID ON ShiftExchangeRequest(RequesterID);
CREATE INDEX IDX_SER_TargetEmpID ON ShiftExchangeRequest(TargetEmpID);

CREATE TABLE SystemLog (
    LogID INT IDENTITY(1,1) PRIMARY KEY,
    AccountID INT NULL,
    ActionType VARCHAR(100) NOT NULL,
    Description NVARCHAR(MAX) NULL,
    IPAddress VARCHAR(45) NULL,
    CreatedAt DATETIME NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_Log_Account FOREIGN KEY (AccountID) REFERENCES Account(AccountID)
);

CREATE TABLE SystemConfig (
    ConfigKey   VARCHAR(100)   NOT NULL PRIMARY KEY,
    ConfigValue NVARCHAR(MAX)  NULL,
    Description NVARCHAR(255)  NULL,
    UpdatedAt   DATETIME       NOT NULL DEFAULT GETDATE(),
    UpdatedBy   INT            NULL,
    CONSTRAINT FK_SystemConfig_Account FOREIGN KEY (UpdatedBy) REFERENCES Account(AccountID)
);

CREATE INDEX IDX_Account_Email ON Account(Email);
CREATE INDEX IDX_Account_RoleID ON Account(RoleID);
CREATE INDEX IDX_Schedule_StartTime ON Schedule(StartTime);
CREATE INDEX IDX_Schedule_MovieID ON Schedule(MovieID);
CREATE INDEX IDX_Schedule_RoomID ON Schedule(RoomID);
CREATE INDEX IDX_Ticket_ScheduleID ON Ticket(ScheduleID);
CREATE INDEX IDX_Ticket_InvoiceID ON Ticket(InvoiceID);
CREATE INDEX IDX_Invoice_AccountID ON Invoice(AccountID);
CREATE INDEX IDX_MovieReview_MovieID ON MovieReview(MovieID);
CREATE INDEX IDX_Promotion_Code ON Promotion(PromotionCode);

-- Roles
INSERT INTO Role (RoleName) VALUES
    ('Guest'),
    ('Customer'),
    ('Employee'),
    ('Manager'),
    ('SystemAdmin');

-- Genres
INSERT INTO Genre (GenreName) VALUES
    (N'Hành động'),
    (N'Hài hước'),
    (N'Kinh dị'),
    (N'Tình cảm'),
    (N'Hoạt hình'),
    (N'Khoa học viễn tưởng'),
    (N'Tâm lý'),
    (N'Phiêu lưu');

-- Sample Accounts (password = 'Hashed_in_production')
INSERT INTO Account (Email, Password, RoleID) VALUES
    ('admin@cinema.vn',    'hashed_pw_admin',    5),
    ('manager@cinema.vn',  'hashed_pw_manager',  4),
    ('employee@cinema.vn', 'hashed_pw_employee', 3),
    ('customer1@gmail.com','hashed_pw_cust1',    2),
    ('customer2@gmail.com','hashed_pw_cust2',    2);

INSERT INTO UserProfile (AccountID, FullName, PhoneNumber) VALUES
    (1, N'Nguyễn Hệ Thống',  '0900000001'),
    (2, N'Trần Quản Lý',     '0900000002'),
    (3, N'Lê Nhân Viên',     '0900000003'),
    (4, N'Phạm Văn Khách',   '0912345678'),
    (5, N'Nguyễn Thị Lan',   '0987654321');

-- Rooms
INSERT INTO Room (RoomNumber, RoomType, Capacity, NumberOfRows, SeatsPerRow) VALUES
    (N'P01', '2D',   50, 5,  10),
    (N'P02', '3D',   40, 5,  8),
    (N'P03', 'IMAX', 120, 10, 12),
    (N'P04', '4DX',  30, 3,  10);

-- Seats for Room 1 (rows A-E, cols 1-10 = 50 Normal + 10 VIP row E)
DECLARE @row VARCHAR(1), @col INT;
DECLARE @rows TABLE (r VARCHAR(1));
INSERT INTO @rows VALUES ('A'),('B'),('C'),('D');
DECLARE row_cursor CURSOR FOR SELECT r FROM @rows;
OPEN row_cursor;
FETCH NEXT FROM row_cursor INTO @row;
WHILE @@FETCH_STATUS = 0
BEGIN
    SET @col = 1;
    WHILE @col <= 10
    BEGIN
        INSERT INTO Seat (RoomID, RowChar, ColNumber, SeatType) VALUES (1, @row, @col, 'Normal');
        SET @col = @col + 1;
    END
    FETCH NEXT FROM row_cursor INTO @row;
END
CLOSE row_cursor;
DEALLOCATE row_cursor;

-- VIP row E
SET @col = 1;
WHILE @col <= 10
BEGIN
    INSERT INTO Seat (RoomID, RowChar, ColNumber, SeatType) VALUES (1, 'E', @col, 'VIP');
    SET @col = @col + 1;
END

-- Seats for Room 2 (rows A-E, cols 1-8)
SET @col = 1;
DELETE FROM @rows;
INSERT INTO @rows VALUES ('A'),('B'),('C'),('D');
DECLARE row_cursor2 CURSOR FOR SELECT r FROM @rows;
OPEN row_cursor2;
FETCH NEXT FROM row_cursor2 INTO @row;
WHILE @@FETCH_STATUS = 0
BEGIN
    SET @col = 1;
    WHILE @col <= 8
    BEGIN
        INSERT INTO Seat (RoomID, RowChar, ColNumber, SeatType) VALUES (2, @row, @col, 'Normal');
        SET @col = @col + 1;
    END
    FETCH NEXT FROM row_cursor2 INTO @row;
END
CLOSE row_cursor2;
DEALLOCATE row_cursor2;
-- VIP row E for Room 2
SET @col = 1;
WHILE @col <= 8
BEGIN
    INSERT INTO Seat (RoomID, RowChar, ColNumber, SeatType) VALUES (2, 'E', @col, 'VIP');
    SET @col = @col + 1;
END

-- Seats for Room 3 (rows A-J, cols 1-12)
SET @col = 1;
DELETE FROM @rows;
INSERT INTO @rows VALUES ('A'),('B'),('C'),('D'),('E'),('F'),('G'),('H'),('I');
DECLARE row_cursor3 CURSOR FOR SELECT r FROM @rows;
OPEN row_cursor3;
FETCH NEXT FROM row_cursor3 INTO @row;
WHILE @@FETCH_STATUS = 0
BEGIN
    SET @col = 1;
    WHILE @col <= 12
    BEGIN
        INSERT INTO Seat (RoomID, RowChar, ColNumber, SeatType) VALUES (3, @row, @col, 'Normal');
        SET @col = @col + 1;
    END
    FETCH NEXT FROM row_cursor3 INTO @row;
END
CLOSE row_cursor3;
DEALLOCATE row_cursor3;
-- VIP row J for Room 3
SET @col = 1;
WHILE @col <= 12
BEGIN
    INSERT INTO Seat (RoomID, RowChar, ColNumber, SeatType) VALUES (3, 'J', @col, 'VIP');
    SET @col = @col + 1;
END

-- Seats for Room 4 (rows A-C, cols 1-10)
SET @col = 1;
DELETE FROM @rows;
INSERT INTO @rows VALUES ('A'),('B');
DECLARE row_cursor4 CURSOR FOR SELECT r FROM @rows;
OPEN row_cursor4;
FETCH NEXT FROM row_cursor4 INTO @row;
WHILE @@FETCH_STATUS = 0
BEGIN
    SET @col = 1;
    WHILE @col <= 10
    BEGIN
        INSERT INTO Seat (RoomID, RowChar, ColNumber, SeatType) VALUES (4, @row, @col, 'Normal');
        SET @col = @col + 1;
    END
    FETCH NEXT FROM row_cursor4 INTO @row;
END
CLOSE row_cursor4;
DEALLOCATE row_cursor4;
-- VIP row C for Room 4
SET @col = 1;
WHILE @col <= 10
BEGIN
    INSERT INTO Seat (RoomID, RowChar, ColNumber, SeatType) VALUES (4, 'C', @col, 'VIP');
    SET @col = @col + 1;
END

-- Movies
INSERT INTO Movie (MovieName, Description, Duration, ReleaseDate, Language, Subtitle, Director, Cast, Country, AgeRestriction, IsActive) VALUES
    (N'Avengers: Secret Wars',  N'Cuộc chiến bí mật của các siêu anh hùng Marvel.',    150, '2025-05-01', N'Anh', N'Việt',    'Joe Russo',    N'Robert Downey Jr., Chris Evans',  N'Mỹ',    13, 1),
    (N'Lật Mặt 8',              N'Phần tiếp theo của series phim Việt đình đám.',        120, '2025-04-18', N'Việt', NULL,       N'Lý Hải',      N'Lý Hải, Minh Hà',                 N'Việt Nam', 0, 1),
    (N'Venom: The Last Dance',  N'Hành trình cuối cùng của Venom.',                      112, '2025-06-15', N'Anh', N'Việt',    'Kelly Marcel', N'Tom Hardy',                       N'Mỹ',    13, 1);

-- MovieGenre
INSERT INTO MovieGenre VALUES (1,1),(1,6),(1,8),(2,2),(2,4),(3,1),(3,6);

-- Food
INSERT INTO Food (FoodName, Price, IsActive) VALUES
    (N'Bắp rang bơ lớn',    45000, 1),
    (N'Bắp rang bơ vừa',    35000, 1),
    (N'Pepsi lon',           25000, 1),
    (N'Nước suối',           15000, 1),
    (N'Combo 1 (Bắp L + Pepsi)', 65000, 1),
    (N'Hotdog',              40000, 1);

-- Promotions
INSERT INTO Promotion (PromotionCode, Description, DiscountType, DiscountValue, MinOrderAmount, MaxDiscountAmount, StartDate, EndDate, UsageLimit) VALUES
    ('SUMMER25',  N'Giảm 25% mùa hè',       'Percentage', 25, 100000, 50000, '2025-06-01', '2025-08-31', 500),
    ('FLAT50K',   N'Giảm thẳng 50.000đ',    'FlatAmount', 50000, 150000, NULL, '2025-07-01', '2025-07-31', 200),
    ('NEWUSER',   N'Ưu đãi khách hàng mới', 'Percentage', 15, 0, 30000, '2025-01-01', '2025-12-31', 1000);

-- Backfill Status and IsActive based on actual dates
UPDATE Promotion SET
    Status = CASE
        WHEN EndDate < GETDATE()                                               THEN 'expired'
        WHEN IsActive = 0 AND StartDate <= GETDATE() AND EndDate >= GETDATE() THEN 'inactive'
        WHEN StartDate > GETDATE()                                             THEN 'upcoming'
        ELSE 'active'
    END,
    IsActive = CASE
        WHEN StartDate <= GETDATE() AND EndDate >= GETDATE() THEN 1
        ELSE 0
    END;

-- System config defaults
INSERT INTO SystemConfig (ConfigKey, ConfigValue, Description) VALUES
    ('cinema_name',           N'CGV Cinema',        N'Tên rạp chiếu phim'),
    ('cinema_address',        N'Hà Nội, Việt Nam',  N'Địa chỉ rạp'),
    ('cinema_phone',          '1900 6017',           N'Số điện thoại liên hệ'),
    ('cinema_email',          'hotro@cgv.vn',        N'Email liên hệ'),
    ('banner_url',            '',                    N'URL ảnh banner trang chủ'),
    ('max_seats_per_booking', '8',                   N'Số ghế tối đa mỗi lần đặt'),
    ('cancel_hours_before',   '2',                   N'Số giờ tối thiểu trước suất chiếu để hủy'),
    ('base_ticket_price',     '90000',               N'Giá vé cơ bản mặc định (VND)');
GO -- THÊM LỆNH GO TẠI ĐÂY ĐỂ NGẮT LÔ THỰC THI

-- Available seats for a given schedule
CREATE VIEW vw_AvailableSeats AS
SELECT
    sc.ScheduleID,
    s.SeatID,
    s.RoomID,
    s.RowChar,
    s.ColNumber,
    s.SeatType,
    CASE WHEN s.SeatType = 'VIP'
         THEN sc.BaseTicketPrice * 1.5
         ELSE sc.BaseTicketPrice
    END AS FinalPrice
FROM Schedule sc
JOIN Seat s ON s.RoomID = sc.RoomID
WHERE s.SeatID NOT IN (
    SELECT t.SeatID FROM Ticket t WHERE t.ScheduleID = sc.ScheduleID
);
GO

-- Revenue statistics by movie
CREATE VIEW vw_MovieRevenue AS
SELECT
    m.MovieID,
    m.MovieName,
    COUNT(DISTINCT i.InvoiceID)      AS TotalInvoices,
    COUNT(t.TicketID)                AS TotalTicketsSold,
    SUM(t.PriceAtBooking)            AS TicketRevenue,
    ISNULL(SUM(inf.FoodRevenue), 0)  AS FoodRevenue,
    SUM(t.PriceAtBooking) + ISNULL(SUM(inf.FoodRevenue), 0) AS TotalRevenue
FROM Movie m
JOIN Schedule sc   ON sc.MovieID   = m.MovieID
JOIN Ticket t      ON t.ScheduleID = sc.ScheduleID
JOIN Invoice i     ON i.InvoiceID  = t.InvoiceID AND i.PaymentStatus = 'Paid'
LEFT JOIN (
    SELECT if2.InvoiceID, SUM(if2.Quantity * if2.PriceAtBooking) AS FoodRevenue
    FROM InvoiceFood if2
    GROUP BY if2.InvoiceID
) inf ON inf.InvoiceID = i.InvoiceID
GROUP BY m.MovieID, m.MovieName;
GO

-- Average rating per movie
CREATE VIEW vw_MovieRating AS
SELECT
    m.MovieID,
    m.MovieName,
    COUNT(r.ReviewID)         AS TotalReviews,
    AVG(CAST(r.RatingValue AS DECIMAL(3,2))) AS AvgRating
FROM Movie m
LEFT JOIN MovieReview r ON r.MovieID = m.MovieID
GROUP BY m.MovieID, m.MovieName;
GO

-- ─────────────────────────────────────────────────────────────────────────────
-- Seed account passwords (hashed)
-- ─────────────────────────────────────────────────────────────────────────────
-- All accounts use password = 123456
UPDATE Account SET Password = 'GxBf2JiV8tjQ8Va47w2dSN5/j3WSWL+1a3KSEDF3M16MFlGFj84AJfS2IW/J8XbL' WHERE Email = 'admin@cinema.vn';
UPDATE Account SET Password = 'GxBf2JiV8tjQ8Va47w2dSN5/j3WSWL+1a3KSEDF3M16MFlGFj84AJfS2IW/J8XbL' WHERE Email = 'manager@cinema.vn';
UPDATE Account SET Password = 'GxBf2JiV8tjQ8Va47w2dSN5/j3WSWL+1a3KSEDF3M16MFlGFj84AJfS2IW/J8XbL' WHERE Email = 'employee@cinema.vn';
UPDATE Account SET Password = 'GxBf2JiV8tjQ8Va47w2dSN5/j3WSWL+1a3KSEDF3M16MFlGFj84AJfS2IW/J8XbL' WHERE Email = 'customer1@gmail.com';
UPDATE Account SET Password = 'GxBf2JiV8tjQ8Va47w2dSN5/j3WSWL+1a3KSEDF3M16MFlGFj84AJfS2IW/J8XbL' WHERE Email = 'customer2@gmail.com';

-- ─────────────────────────────────────────────────────────────────────────────
-- Migration: chỉ chạy các câu này khi UPDATE DB cũ (không cần cho fresh install)
-- ─────────────────────────────────────────────────────────────────────────────
-- IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('Account') AND name = 'AccountStatus')
--     ALTER TABLE Account ADD AccountStatus VARCHAR(20) NOT NULL DEFAULT 'noneed';
-- IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('Account') AND name = 'ResetToken')
--     ALTER TABLE Account ADD ResetToken VARCHAR(255) NULL;
-- IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('Account') AND name = 'ResetTokenExpiry')
--     ALTER TABLE Account ADD ResetTokenExpiry DATETIME NULL;
-- ALTER TABLE UserProfile ALTER COLUMN FullName NVARCHAR(100) NULL;
-- ─────────────────────────────────────────────────────────────────────────────
