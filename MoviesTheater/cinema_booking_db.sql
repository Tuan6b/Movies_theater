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
    AccountID INT IDENTITY(1,1) PRIMARY KEY,
    Email VARCHAR(255) NOT NULL UNIQUE,
    Password    VARCHAR(255) NOT NULL,
    RoleID INT NOT NULL,
    IsBlocked BIT NOT NULL DEFAULT 0,
    CreatedAt DATETIME NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_Account_Role FOREIGN KEY (RoleID) REFERENCES Role(RoleID)
);

CREATE TABLE UserProfile (
    AccountID INT PRIMARY KEY,
    FullName NVARCHAR(100) NOT NULL,
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
    Duration INT NOT NULL,
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
    RoomType VARCHAR(20) NOT NULL DEFAULT '2D',
    Capacity INT NOT NULL,
    NumberOfRows INT NOT NULL DEFAULT 0,
    SeatsPerRow INT NOT NULL DEFAULT 0,
    IsActive BIT NOT NULL DEFAULT 1,
    CONSTRAINT CHK_Room_Type CHECK (RoomType IN ('2D', '3D', 'IMAX', '4DX'))
);

CREATE TABLE Seat (
    SeatID INT IDENTITY(1,1) PRIMARY KEY,
    RoomID INT NOT NULL,
    RowChar VARCHAR(5) NOT NULL,
    ColNumber INT NOT NULL,
    SeatType VARCHAR(20) NOT NULL DEFAULT 'Normal',
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
    EndTime DATETIME NOT NULL,
    BaseTicketPrice DECIMAL(10,2) NOT NULL,
    Status VARCHAR(20) NOT NULL DEFAULT 'Scheduled',
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
    DiscountType VARCHAR(20)    NOT NULL,
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
    PaymentMethod VARCHAR(50) NOT NULL,
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
    Code VARCHAR(100) NOT NULL UNIQUE,
    IsCheckedIn BIT NOT NULL DEFAULT 0,
    CheckedInAt DATETIME NULL,
    CONSTRAINT FK_Ticket_Schedule FOREIGN KEY (ScheduleID) REFERENCES Schedule(ScheduleID),
    CONSTRAINT FK_Ticket_Seat FOREIGN KEY (SeatID) REFERENCES Seat(SeatID),
    CONSTRAINT FK_Ticket_Invoice FOREIGN KEY (InvoiceID) REFERENCES Invoice(InvoiceID),
    CONSTRAINT UQ_Ticket_Seat_Schedule UNIQUE (ScheduleID, SeatID)
);

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
    TicketID INT NOT NULL UNIQUE,
    RatingValue INT NOT NULL,
    Comment NVARCHAR(MAX) NULL,
    CreatedAt DATETIME NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_Review_Movie FOREIGN KEY (MovieID) REFERENCES Movie(MovieID),
    CONSTRAINT FK_Review_Account FOREIGN KEY (AccountID) REFERENCES Account(AccountID),
    CONSTRAINT FK_Review_Ticket FOREIGN KEY (TicketID) REFERENCES Ticket(TicketID),
    CONSTRAINT CHK_Review_Rating CHECK (RatingValue BETWEEN 1 AND 5)
);

CREATE TABLE SystemLog (
    LogID INT IDENTITY(1,1) PRIMARY KEY,
    AccountID INT NULL,
    ActionType VARCHAR(100) NOT NULL,
    Description NVARCHAR(MAX) NULL,
    IPAddress VARCHAR(45) NULL,
    CreatedAt DATETIME NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_Log_Account FOREIGN KEY (AccountID) REFERENCES Account(AccountID)
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

-- ===================== SEED DATA =====================

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

-- Accounts
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

-- Rooms (with NumberOfRows & SeatsPerRow)
INSERT INTO Room (RoomNumber, RoomType, Capacity, NumberOfRows, SeatsPerRow) VALUES
    (N'P01', '2D',   100, 10, 10),
    (N'P02', '3D',   80,  8,  10),
    (N'P03', 'IMAX', 120, 12, 10),
    (N'P04', '4DX',  60,  6,  10);

-- Seats for all rooms (generated programmatically)
DECLARE @rid INT, @row INT, @col INT, @rowChar VARCHAR(1);
DECLARE room_cursor CURSOR FOR SELECT RoomID, NumberOfRows, SeatsPerRow FROM Room;
OPEN room_cursor;
FETCH NEXT FROM room_cursor INTO @rid, @row, @col;
WHILE @@FETCH_STATUS = 0
BEGIN
    DECLARE @r INT = 0;
    WHILE @r < @row
    BEGIN
        SET @rowChar = CHAR(ASCII('A') + @r);
        DECLARE @c INT = 1;
        WHILE @c <= @col
        BEGIN
            INSERT INTO Seat (RoomID, RowChar, ColNumber, SeatType, IsActive)
            VALUES (@rid, @rowChar, @c, 'Normal', 1);
            SET @c = @c + 1;
        END
        SET @r = @r + 1;
    END
    FETCH NEXT FROM room_cursor INTO @rid, @row, @col;
END
CLOSE room_cursor;
DEALLOCATE room_cursor;
GO

-- Movies
INSERT INTO Movie (MovieName, Description, Duration, ReleaseDate, Language, Subtitle, Director, Cast, Country, AgeRestriction, IsActive) VALUES
    (N'Avengers: Secret Wars',  N'Cuộc chiến bí mật của các siêu anh hùng Marvel.',    150, '2025-05-01', N'Anh', N'Việt',    'Joe Russo',    N'Robert Downey Jr., Chris Evans',  N'Mỹ',    13, 1),
    (N'Lật Mặt 8',              N'Phần tiếp theo của series phim Việt đình đám.',        120, '2025-04-18', N'Việt', NULL,       N'Lý Hải',      N'Lý Hải, Minh Hà',                 N'Việt Nam', 0, 1),
    (N'Venom: The Last Dance',  N'Hành trình cuối cùng của Venom.',                      112, '2025-06-15', N'Anh', N'Việt',    'Kelly Marcel', N'Tom Hardy',                       N'Mỹ',    13, 1);

-- MovieGenre
INSERT INTO MovieGenre VALUES (1,1),(1,6),(1,8),(2,2),(2,4),(3,1),(3,6);

-- Schedule
INSERT INTO Schedule (RoomID, MovieID, StartTime, EndTime, BaseTicketPrice, Status) VALUES
    (1, 1, '2025-07-01 09:00', '2025-07-01 11:30', 90000, 'Scheduled'),
    (1, 1, '2025-07-01 14:00', '2025-07-01 16:30', 90000, 'Scheduled'),
    (2, 2, '2025-07-01 10:00', '2025-07-01 12:00', 85000, 'Scheduled'),
    (3, 3, '2025-07-02 19:00', '2025-07-02 21:00', 120000,'Scheduled');

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

-- Sample Invoice + Ticket
INSERT INTO Invoice (AccountID, PromotionID, SubTotal, DiscountAmount, TotalAmount, PaymentMethod, PaymentStatus)
VALUES (4, 1, 180000, 45000, 135000, 'VNPay', 'Paid');

INSERT INTO Ticket (ScheduleID, SeatID, InvoiceID, PriceAtBooking, Code, IsCheckedIn, CheckedInAt)
VALUES (1, 5, 1, 90000, 'TK-20250701-0001', 1, '2025-07-01 08:55'),
       (1, 6, 1, 90000, 'TK-20250701-0002', 1, '2025-07-01 08:55');

INSERT INTO InvoiceFood (InvoiceID, FoodID, Quantity, PriceAtBooking)
VALUES (1, 5, 1, 65000);

INSERT INTO MovieReview (MovieID, AccountID, TicketID, RatingValue, Comment)
VALUES (1, 4, 1, 5, N'Phim hay cực kỳ, hiệu ứng đỉnh nóc!');

INSERT INTO SystemLog (AccountID, ActionType, Description, IPAddress)
VALUES (4, 'BOOK_TICKET', N'Customer đặt 2 vé suất 09:00 ngày 01/07/2025', '192.168.1.100');
GO

-- Views
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
