-- ============================================================================
-- SCRIPT BỔ SUNG DỮ LIỆU DEMO - PHIÊN BẢN SỬA LỖI
-- Dùng lookup theo tên, KHÔNG hardcode ID
-- Chạy SAU Database_Updated.sql
-- ============================================================================
USE CinemaBookingDB;
GO

-- ============================================================================
-- 1. THÊM 12 PHIM MỚI
-- ============================================================================
INSERT INTO Movie (MovieName, Description, Duration, DateAdded, Budget, GlobalBoxOffice, Language, Subtitle, Director, [Cast], Country, AgeRestriction, IsActive, Poster, Trailer) VALUES
(N'Deadpool & Wolverine', N'Deadpool và Wolverine hợp sức trong cuộc phiêu lưu đa vũ trụ điên rồ nhất lịch sử Marvel.', 128, '2025-06-01', N'200 Triệu USD', N'1.3 Tỷ USD', N'Tiếng Anh', N'Phụ đề Tiếng Việt', N'Shawn Levy', N'Ryan Reynolds, Hugh Jackman', N'Mỹ', 18, 1, 'https://image.tmdb.org/t/p/w500/8cdWjvZQUExUUTzyp4t6EDMubfO.jpg', 'https://www.youtube.com/embed/73_1biulkYk'),
(N'Inside Out 2', N'Riley bước vào tuổi dậy thì với những cảm xúc mới: Lo Âu, Ghen Tị, Chán Nản và Xấu Hổ.', 100, '2025-05-20', N'200 Triệu USD', N'1.7 Tỷ USD', N'Tiếng Anh', N'Lồng Tiếng Việt', N'Kelsey Mann', N'Amy Poehler, Maya Hawke', N'Mỹ', 0, 1, 'https://image.tmdb.org/t/p/w500/vpnVM9B6NMmQpWeZvzLvDESb2QY.jpg', 'https://www.youtube.com/embed/LEjhY15eCx0'),
(N'Dune: Part Two', N'Paul Atreides liên minh với người Fremen để trả thù những kẻ đã hủy diệt gia đình anh.', 166, '2025-05-10', N'190 Triệu USD', N'711 Triệu USD', N'Tiếng Anh', N'Phụ đề Tiếng Việt', N'Denis Villeneuve', N'Timothée Chalamet, Zendaya, Austin Butler', N'Mỹ', 13, 1, 'https://image.tmdb.org/t/p/w500/8b8R8l88Qje9dn9OE8PY05Nez7H.jpg', 'https://www.youtube.com/embed/Way9Dexny3w'),
(N'Godzilla x Kong: Đế Chế Mới', N'Godzilla và Kong đối đầu với mối đe dọa khổng lồ từ lòng đất.', 115, '2025-06-10', N'135 Triệu USD', N'570 Triệu USD', N'Tiếng Anh', N'Phụ đề Tiếng Việt', N'Adam Wingard', N'Rebecca Hall, Brian Tyree Henry', N'Mỹ', 13, 1, 'https://image.tmdb.org/t/p/w500/z1p34vh7dEOnLDmyCrlUVLuoDzd.jpg', 'https://www.youtube.com/embed/lV1OOlGwExM'),
(N'Kung Fu Panda 4', N'Po phải tìm và đào tạo Chiến binh Rồng mới trước khi trở thành Lãnh đạo Tâm linh.', 94, '2025-06-05', N'85 Triệu USD', N'545 Triệu USD', N'Tiếng Anh', N'Lồng Tiếng Việt', N'Mike Mitchell', N'Jack Black, Awkwafina, Viola Davis', N'Mỹ', 0, 1, 'https://image.tmdb.org/t/p/w500/kDp1vUBnMpe8ak4rjgl3cLELqjU.jpg', 'https://www.youtube.com/embed/_inKs4eeHiI'),
(N'Mai', N'Câu chuyện tình yêu và cuộc sống đầy xúc động của cô gái tên Mai.', 131, '2025-04-01', N'20 Tỷ VND', N'550 Tỷ VND', N'Tiếng Việt', NULL, N'Trấn Thành', N'Phương Anh Đào, Tuấn Trần, Trấn Thành', N'Việt Nam', 16, 1, 'https://image.tmdb.org/t/p/w500/aNl2wKGnJhRfKrUqJpzwMcEFxXQ.jpg', 'https://www.youtube.com/embed/PK5SdIEMBC0'),
(N'Thunderbolts*', N'Nhóm phản anh hùng Marvel được tập hợp cho nhiệm vụ bí mật.', 127, '2025-07-01', N'180 Triệu USD', NULL, N'Tiếng Anh', N'Phụ đề Tiếng Việt', N'Jake Schreier', N'Florence Pugh, Sebastian Stan, David Harbour', N'Mỹ', 13, 1, 'https://image.tmdb.org/t/p/w500/bFTfMFwxoEPRuCu5mAbDJCBxzO1.jpg', 'https://www.youtube.com/embed/Cqhj8aIZ5WI'),
(N'The Fantastic Four: First Steps', N'Bộ Tứ Siêu Đẳng lần đầu xuất hiện trong MCU.', 135, '2025-07-05', N'220 Triệu USD', NULL, N'Tiếng Anh', N'Phụ đề Tiếng Việt', N'Matt Shakman', N'Pedro Pascal, Vanessa Kirby, Joseph Quinn', N'Mỹ', 13, 1, 'https://image.tmdb.org/t/p/w500/x8Wv3fRakmD4TKgepWzXgC4PVWT.jpg', 'https://www.youtube.com/embed/bRH0z-KLBQM'),
(N'Mission: Impossible - The Final Reckoning', N'Ethan Hunt đối mặt với sứ mệnh nguy hiểm nhất.', 169, '2025-07-10', N'300 Triệu USD', NULL, N'Tiếng Anh', N'Phụ đề Tiếng Việt', N'Christopher McQuarrie', N'Tom Cruise, Hayley Atwell, Simon Pegg', N'Mỹ', 13, 1, 'https://image.tmdb.org/t/p/w500/z0FFER3fFN1LjYmKXkRYPjpD2KS.jpg', 'https://www.youtube.com/embed/NOhDyUmT9z0'),
(N'Elio', N'Cậu bé Elio bất ngờ trở thành đại sứ Trái Đất tại cộng đồng liên thiên hà.', 100, '2025-07-15', N'175 Triệu USD', NULL, N'Tiếng Anh', N'Lồng Tiếng Việt', N'Adrian Molina', N'Yonas Kibreab, America Ferrera', N'Mỹ', 0, 1, 'https://image.tmdb.org/t/p/w500/hVOxPaHfNBCmfDEwUmYCgBRaSoe.jpg', 'https://www.youtube.com/embed/B02Gxhz4OOk'),
(N'Jurassic World: Rebirth', N'Hành trình giải cứu khủng long trong thế giới hậu tận thế.', 140, '2025-07-20', N'250 Triệu USD', NULL, N'Tiếng Anh', N'Phụ đề Tiếng Việt', N'Gareth Edwards', N'Scarlett Johansson, Mahershala Ali', N'Mỹ', 13, 1, 'https://image.tmdb.org/t/p/w500/dGpBMFfJoWnqaa1eKOGWr6q63tr.jpg', 'https://www.youtube.com/embed/dTJqYFRHGKg'),
(N'Cô Dâu Hào Môn', N'Bộ phim hài lãng mạn Việt Nam về cuộc sống hôn nhân giàu sang.', 120, '2025-07-25', N'30 Tỷ VND', NULL, N'Tiếng Việt', NULL, N'Vũ Ngọc Đãng', N'Ninh Dương Lan Ngọc, Lê Xuân Tiền', N'Việt Nam', 13, 1, 'https://image.tmdb.org/t/p/w500/fJkNJkOYbVMluEqfhFdbKlDmzCZ.jpg', NULL);
GO

-- ============================================================================
-- 2. GẮN GENRE (lookup bằng tên)
-- ============================================================================
INSERT INTO MovieGenre (MovieID, GenreID)
SELECT m.MovieID, g.GenreID
FROM (VALUES
    (N'Deadpool & Wolverine',    N'Hành động'),
    (N'Deadpool & Wolverine',    N'Hài hước'),
    (N'Deadpool & Wolverine',    N'Khoa học viễn tưởng'),
    (N'Inside Out 2',            N'Hoạt hình'),
    (N'Inside Out 2',            N'Hài hước'),
    (N'Inside Out 2',            N'Tâm lý'),
    (N'Dune: Part Two',          N'Hành động'),
    (N'Dune: Part Two',          N'Khoa học viễn tưởng'),
    (N'Dune: Part Two',          N'Phiêu lưu'),
    (N'Godzilla x Kong: Đế Chế Mới', N'Hành động'),
    (N'Godzilla x Kong: Đế Chế Mới', N'Khoa học viễn tưởng'),
    (N'Kung Fu Panda 4',        N'Hoạt hình'),
    (N'Kung Fu Panda 4',        N'Hài hước'),
    (N'Kung Fu Panda 4',        N'Phiêu lưu'),
    (N'Mai',                     N'Tình cảm'),
    (N'Mai',                     N'Tâm lý'),
    (N'Thunderbolts*',           N'Hành động'),
    (N'Thunderbolts*',           N'Khoa học viễn tưởng'),
    (N'The Fantastic Four: First Steps', N'Hành động'),
    (N'The Fantastic Four: First Steps', N'Khoa học viễn tưởng'),
    (N'The Fantastic Four: First Steps', N'Phiêu lưu'),
    (N'Mission: Impossible - The Final Reckoning', N'Hành động'),
    (N'Mission: Impossible - The Final Reckoning', N'Phiêu lưu'),
    (N'Elio',                    N'Hoạt hình'),
    (N'Elio',                    N'Khoa học viễn tưởng'),
    (N'Elio',                    N'Phiêu lưu'),
    (N'Jurassic World: Rebirth', N'Hành động'),
    (N'Jurassic World: Rebirth', N'Khoa học viễn tưởng'),
    (N'Jurassic World: Rebirth', N'Phiêu lưu'),
    (N'Cô Dâu Hào Môn',         N'Hài hước'),
    (N'Cô Dâu Hào Môn',         N'Tình cảm')
) AS v(MName, GName)
JOIN Movie m ON m.MovieName = v.MName
JOIN Genre g ON g.GenreName = v.GName;
GO

-- ============================================================================
-- 3. TẠO SEATS CHO ROOM 2, 3, 4
-- ============================================================================
IF NOT EXISTS (SELECT 1 FROM Seat WHERE RoomID = 2)
BEGIN
    DECLARE @r2_row VARCHAR(1), @r2_col INT;
    DECLARE @r2_rows TABLE (r VARCHAR(1));
    INSERT INTO @r2_rows VALUES ('A'),('B'),('C'),('D'),('E'),('F'),('G'),('H');
    DECLARE r2_cursor CURSOR FOR SELECT r FROM @r2_rows;
    OPEN r2_cursor;
    FETCH NEXT FROM r2_cursor INTO @r2_row;
    WHILE @@FETCH_STATUS = 0
    BEGIN
        SET @r2_col = 1;
        WHILE @r2_col <= 10
        BEGIN
            INSERT INTO Seat (RoomID, RowChar, ColNumber, SeatType)
            VALUES (2, @r2_row, @r2_col, CASE WHEN @r2_row IN ('G','H') THEN 'VIP' ELSE 'Normal' END);
            SET @r2_col = @r2_col + 1;
        END
        FETCH NEXT FROM r2_cursor INTO @r2_row;
    END
    CLOSE r2_cursor; DEALLOCATE r2_cursor;
END
GO

IF NOT EXISTS (SELECT 1 FROM Seat WHERE RoomID = 3)
BEGIN
    DECLARE @r3_row VARCHAR(1), @r3_col INT;
    DECLARE @r3_rows TABLE (r VARCHAR(1));
    INSERT INTO @r3_rows VALUES ('A'),('B'),('C'),('D'),('E'),('F'),('G'),('H'),('I'),('J'),('K'),('L');
    DECLARE r3_cursor CURSOR FOR SELECT r FROM @r3_rows;
    OPEN r3_cursor;
    FETCH NEXT FROM r3_cursor INTO @r3_row;
    WHILE @@FETCH_STATUS = 0
    BEGIN
        SET @r3_col = 1;
        WHILE @r3_col <= 10
        BEGIN
            INSERT INTO Seat (RoomID, RowChar, ColNumber, SeatType)
            VALUES (3, @r3_row, @r3_col, CASE WHEN @r3_row IN ('K','L') THEN 'VIP' ELSE 'Normal' END);
            SET @r3_col = @r3_col + 1;
        END
        FETCH NEXT FROM r3_cursor INTO @r3_row;
    END
    CLOSE r3_cursor; DEALLOCATE r3_cursor;
END
GO

IF NOT EXISTS (SELECT 1 FROM Seat WHERE RoomID = 4)
BEGIN
    DECLARE @r4_row VARCHAR(1), @r4_col INT;
    DECLARE @r4_rows TABLE (r VARCHAR(1));
    INSERT INTO @r4_rows VALUES ('A'),('B'),('C'),('D'),('E'),('F');
    DECLARE r4_cursor CURSOR FOR SELECT r FROM @r4_rows;
    OPEN r4_cursor;
    FETCH NEXT FROM r4_cursor INTO @r4_row;
    WHILE @@FETCH_STATUS = 0
    BEGIN
        SET @r4_col = 1;
        WHILE @r4_col <= 10
        BEGIN
            INSERT INTO Seat (RoomID, RowChar, ColNumber, SeatType)
            VALUES (4, @r4_row, @r4_col, CASE WHEN @r4_row = 'F' THEN 'VIP' ELSE 'Normal' END);
            SET @r4_col = @r4_col + 1;
        END
        FETCH NEXT FROM r4_cursor INTO @r4_row;
    END
    CLOSE r4_cursor; DEALLOCATE r4_cursor;
END
GO

-- ============================================================================
-- 4. LỊCH CHIẾU "ĐANG CHIẾU"
-- ============================================================================
INSERT INTO Schedule (RoomID, MovieID, StartTime, EndTime, BaseTicketPrice, Status)
SELECT 1, m.MovieID, '2025-07-20 09:00', '2025-07-20 11:08', 95000, 'Finished' FROM Movie m WHERE m.MovieName = N'Deadpool & Wolverine'
UNION ALL SELECT 2, m.MovieID, '2025-07-21 14:00', '2025-07-21 16:08', 100000, 'Finished' FROM Movie m WHERE m.MovieName = N'Deadpool & Wolverine'
UNION ALL SELECT 1, m.MovieID, '2025-07-20 13:00', '2025-07-20 14:40', 85000, 'Finished' FROM Movie m WHERE m.MovieName = N'Inside Out 2'
UNION ALL SELECT 3, m.MovieID, '2025-07-22 10:00', '2025-07-22 11:40', 110000, 'Finished' FROM Movie m WHERE m.MovieName = N'Inside Out 2'
UNION ALL SELECT 3, m.MovieID, '2025-07-19 19:00', '2025-07-19 21:46', 120000, 'Finished' FROM Movie m WHERE m.MovieName = N'Dune: Part Two'
UNION ALL SELECT 3, m.MovieID, '2025-07-21 19:00', '2025-07-21 21:46', 120000, 'Finished' FROM Movie m WHERE m.MovieName = N'Dune: Part Two'
UNION ALL SELECT 2, m.MovieID, '2025-07-20 16:00', '2025-07-20 17:55', 95000, 'Finished' FROM Movie m WHERE m.MovieName = N'Godzilla x Kong: Đế Chế Mới'
UNION ALL SELECT 4, m.MovieID, '2025-07-22 14:00', '2025-07-22 15:55', 130000, 'Finished' FROM Movie m WHERE m.MovieName = N'Godzilla x Kong: Đế Chế Mới'
UNION ALL SELECT 1, m.MovieID, '2025-07-21 09:00', '2025-07-21 10:34', 80000, 'Finished' FROM Movie m WHERE m.MovieName = N'Kung Fu Panda 4'
UNION ALL SELECT 4, m.MovieID, '2025-07-22 09:00', '2025-07-22 10:34', 110000, 'Finished' FROM Movie m WHERE m.MovieName = N'Kung Fu Panda 4'
UNION ALL SELECT 2, m.MovieID, '2025-07-19 19:30', '2025-07-19 21:41', 90000, 'Finished' FROM Movie m WHERE m.MovieName = N'Mai'
UNION ALL SELECT 1, m.MovieID, '2025-07-22 19:30', '2025-07-22 21:41', 90000, 'Finished' FROM Movie m WHERE m.MovieName = N'Mai'
UNION ALL SELECT 3, m.MovieID, '2025-07-23 09:00', '2025-07-23 11:30', 120000, 'Scheduled' FROM Movie m WHERE m.MovieName = N'Avengers: Secret Wars'
UNION ALL SELECT 3, m.MovieID, '2025-07-23 14:00', '2025-07-23 16:30', 120000, 'Scheduled' FROM Movie m WHERE m.MovieName = N'Avengers: Secret Wars'
UNION ALL SELECT 1, m.MovieID, '2025-07-23 10:00', '2025-07-23 12:00', 85000, 'Scheduled' FROM Movie m WHERE m.MovieName = N'Lật Mặt 8'
UNION ALL SELECT 2, m.MovieID, '2025-07-23 19:00', '2025-07-23 20:52', 95000, 'Scheduled' FROM Movie m WHERE m.MovieName = N'Venom: The Last Dance';
GO

-- ============================================================================
-- 5. LỊCH CHIẾU "SẮP CHIẾU"
-- ============================================================================
INSERT INTO Schedule (RoomID, MovieID, StartTime, EndTime, BaseTicketPrice, Status)
SELECT 1, m.MovieID, '2025-07-28 09:00', '2025-07-28 11:07', 95000, 'Scheduled' FROM Movie m WHERE m.MovieName = N'Thunderbolts*'
UNION ALL SELECT 3, m.MovieID, '2025-07-28 14:00', '2025-07-28 16:07', 120000, 'Scheduled' FROM Movie m WHERE m.MovieName = N'Thunderbolts*'
UNION ALL SELECT 3, m.MovieID, '2025-07-30 19:00', '2025-07-30 21:15', 130000, 'Scheduled' FROM Movie m WHERE m.MovieName = N'The Fantastic Four: First Steps'
UNION ALL SELECT 3, m.MovieID, '2025-08-01 14:00', '2025-08-01 16:49', 130000, 'Scheduled' FROM Movie m WHERE m.MovieName = N'Mission: Impossible - The Final Reckoning'
UNION ALL SELECT 1, m.MovieID, '2025-08-01 19:00', '2025-08-01 21:49', 95000, 'Scheduled' FROM Movie m WHERE m.MovieName = N'Mission: Impossible - The Final Reckoning'
UNION ALL SELECT 1, m.MovieID, '2025-08-05 09:00', '2025-08-05 10:40', 85000, 'Scheduled' FROM Movie m WHERE m.MovieName = N'Elio'
UNION ALL SELECT 4, m.MovieID, '2025-08-05 14:00', '2025-08-05 15:40', 130000, 'Scheduled' FROM Movie m WHERE m.MovieName = N'Elio'
UNION ALL SELECT 3, m.MovieID, '2025-08-10 09:00', '2025-08-10 11:20', 130000, 'Scheduled' FROM Movie m WHERE m.MovieName = N'Jurassic World: Rebirth'
UNION ALL SELECT 3, m.MovieID, '2025-08-10 14:00', '2025-08-10 16:20', 130000, 'Scheduled' FROM Movie m WHERE m.MovieName = N'Jurassic World: Rebirth'
UNION ALL SELECT 2, m.MovieID, '2025-08-15 19:00', '2025-08-15 21:00', 90000, 'Scheduled' FROM Movie m WHERE m.MovieName = N'Cô Dâu Hào Môn';
GO

-- ============================================================================
-- 6. TẠO VÉ CHECK-IN CHO 2 CUSTOMER (lookup tất cả bằng subquery)
-- ============================================================================
DECLARE @cust1 INT = (SELECT AccountID FROM Account WHERE Email = 'customer1@gmail.com');
DECLARE @cust2 INT = (SELECT AccountID FROM Account WHERE Email = 'customer2@gmail.com');

-- ── CUSTOMER 1 ──────────────────────────────────────
-- C1: Deadpool & Wolverine (Room 1)
DECLARE @s1 INT = (SELECT TOP 1 ScheduleID FROM Schedule WHERE MovieID = (SELECT MovieID FROM Movie WHERE MovieName = N'Deadpool & Wolverine') AND RoomID = 1 ORDER BY StartTime);
DECLARE @seat1 INT = (SELECT SeatID FROM Seat WHERE RoomID = 1 AND RowChar = 'A' AND ColNumber = 1);
INSERT INTO Invoice (AccountID, SubTotal, DiscountAmount, TotalAmount, PaymentMethod, PaymentStatus) VALUES (@cust1, 95000, 0, 95000, 'VNPay', 'Paid');
DECLARE @i1 INT = SCOPE_IDENTITY();
INSERT INTO Ticket (ScheduleID, SeatID, InvoiceID, PriceAtBooking, Code, IsCheckedIn, CheckedInAt) VALUES (@s1, @seat1, @i1, 95000, 'TK-DEMO-C1-001', 1, '2025-07-20 08:50');

-- C1: Inside Out 2 (Room 1)
DECLARE @s2 INT = (SELECT TOP 1 ScheduleID FROM Schedule WHERE MovieID = (SELECT MovieID FROM Movie WHERE MovieName = N'Inside Out 2') AND RoomID = 1 ORDER BY StartTime);
DECLARE @seat2 INT = (SELECT SeatID FROM Seat WHERE RoomID = 1 AND RowChar = 'A' AND ColNumber = 3);
INSERT INTO Invoice (AccountID, SubTotal, DiscountAmount, TotalAmount, PaymentMethod, PaymentStatus) VALUES (@cust1, 85000, 0, 85000, 'MoMo', 'Paid');
DECLARE @i2 INT = SCOPE_IDENTITY();
INSERT INTO Ticket (ScheduleID, SeatID, InvoiceID, PriceAtBooking, Code, IsCheckedIn, CheckedInAt) VALUES (@s2, @seat2, @i2, 85000, 'TK-DEMO-C1-002', 1, '2025-07-20 12:50');

-- C1: Dune Part Two (Room 3)
DECLARE @s3 INT = (SELECT TOP 1 ScheduleID FROM Schedule WHERE MovieID = (SELECT MovieID FROM Movie WHERE MovieName = N'Dune: Part Two') AND RoomID = 3 ORDER BY StartTime);
DECLARE @seat3 INT = (SELECT SeatID FROM Seat WHERE RoomID = 3 AND RowChar = 'A' AND ColNumber = 1);
INSERT INTO Invoice (AccountID, SubTotal, DiscountAmount, TotalAmount, PaymentMethod, PaymentStatus) VALUES (@cust1, 120000, 0, 120000, 'ZaloPay', 'Paid');
DECLARE @i3 INT = SCOPE_IDENTITY();
INSERT INTO Ticket (ScheduleID, SeatID, InvoiceID, PriceAtBooking, Code, IsCheckedIn, CheckedInAt) VALUES (@s3, @seat3, @i3, 120000, 'TK-DEMO-C1-003', 1, '2025-07-19 18:50');

-- C1: Godzilla x Kong (Room 2)
DECLARE @s4 INT = (SELECT TOP 1 ScheduleID FROM Schedule WHERE MovieID = (SELECT MovieID FROM Movie WHERE MovieName = N'Godzilla x Kong: Đế Chế Mới') AND RoomID = 2 ORDER BY StartTime);
DECLARE @seat4 INT = (SELECT SeatID FROM Seat WHERE RoomID = 2 AND RowChar = 'A' AND ColNumber = 1);
INSERT INTO Invoice (AccountID, SubTotal, DiscountAmount, TotalAmount, PaymentMethod, PaymentStatus) VALUES (@cust1, 95000, 0, 95000, 'VNPay', 'Paid');
DECLARE @i4 INT = SCOPE_IDENTITY();
INSERT INTO Ticket (ScheduleID, SeatID, InvoiceID, PriceAtBooking, Code, IsCheckedIn, CheckedInAt) VALUES (@s4, @seat4, @i4, 95000, 'TK-DEMO-C1-004', 1, '2025-07-20 15:50');

-- C1: Mai (Room 2)
DECLARE @s5 INT = (SELECT TOP 1 ScheduleID FROM Schedule WHERE MovieID = (SELECT MovieID FROM Movie WHERE MovieName = N'Mai') AND RoomID = 2 ORDER BY StartTime);
DECLARE @seat5 INT = (SELECT SeatID FROM Seat WHERE RoomID = 2 AND RowChar = 'A' AND ColNumber = 2);
INSERT INTO Invoice (AccountID, SubTotal, DiscountAmount, TotalAmount, PaymentMethod, PaymentStatus) VALUES (@cust1, 90000, 0, 90000, 'Cash', 'Paid');
DECLARE @i5 INT = SCOPE_IDENTITY();
INSERT INTO Ticket (ScheduleID, SeatID, InvoiceID, PriceAtBooking, Code, IsCheckedIn, CheckedInAt) VALUES (@s5, @seat5, @i5, 90000, 'TK-DEMO-C1-005', 1, '2025-07-19 19:20');

-- C1: Kung Fu Panda 4 (Room 1)
DECLARE @s6 INT = (SELECT TOP 1 ScheduleID FROM Schedule WHERE MovieID = (SELECT MovieID FROM Movie WHERE MovieName = N'Kung Fu Panda 4') AND RoomID = 1 ORDER BY StartTime);
DECLARE @seat6 INT = (SELECT SeatID FROM Seat WHERE RoomID = 1 AND RowChar = 'A' AND ColNumber = 4);
INSERT INTO Invoice (AccountID, SubTotal, DiscountAmount, TotalAmount, PaymentMethod, PaymentStatus) VALUES (@cust1, 80000, 0, 80000, 'VNPay', 'Paid');
DECLARE @i6 INT = SCOPE_IDENTITY();
INSERT INTO Ticket (ScheduleID, SeatID, InvoiceID, PriceAtBooking, Code, IsCheckedIn, CheckedInAt) VALUES (@s6, @seat6, @i6, 80000, 'TK-DEMO-C1-006', 1, '2025-07-21 08:50');

-- ── CUSTOMER 2 ──────────────────────────────────────
-- C2: Deadpool & Wolverine (Room 2)
DECLARE @s7 INT = (SELECT TOP 1 ScheduleID FROM Schedule WHERE MovieID = (SELECT MovieID FROM Movie WHERE MovieName = N'Deadpool & Wolverine') AND RoomID = 2 ORDER BY StartTime);
DECLARE @seat7 INT = (SELECT SeatID FROM Seat WHERE RoomID = 2 AND RowChar = 'A' AND ColNumber = 3);
INSERT INTO Invoice (AccountID, SubTotal, DiscountAmount, TotalAmount, PaymentMethod, PaymentStatus) VALUES (@cust2, 100000, 0, 100000, 'VNPay', 'Paid');
DECLARE @i7 INT = SCOPE_IDENTITY();
INSERT INTO Ticket (ScheduleID, SeatID, InvoiceID, PriceAtBooking, Code, IsCheckedIn, CheckedInAt) VALUES (@s7, @seat7, @i7, 100000, 'TK-DEMO-C2-001', 1, '2025-07-21 13:50');

-- C2: Inside Out 2 (Room 3)
DECLARE @s8 INT = (SELECT TOP 1 ScheduleID FROM Schedule WHERE MovieID = (SELECT MovieID FROM Movie WHERE MovieName = N'Inside Out 2') AND RoomID = 3 ORDER BY StartTime);
DECLARE @seat8 INT = (SELECT SeatID FROM Seat WHERE RoomID = 3 AND RowChar = 'A' AND ColNumber = 2);
INSERT INTO Invoice (AccountID, SubTotal, DiscountAmount, TotalAmount, PaymentMethod, PaymentStatus) VALUES (@cust2, 110000, 0, 110000, 'MoMo', 'Paid');
DECLARE @i8 INT = SCOPE_IDENTITY();
INSERT INTO Ticket (ScheduleID, SeatID, InvoiceID, PriceAtBooking, Code, IsCheckedIn, CheckedInAt) VALUES (@s8, @seat8, @i8, 110000, 'TK-DEMO-C2-002', 1, '2025-07-22 09:50');

-- C2: Dune Part Two (Room 3)
DECLARE @s9 INT = (SELECT TOP 1 ScheduleID FROM Schedule WHERE MovieID = (SELECT MovieID FROM Movie WHERE MovieName = N'Dune: Part Two') AND RoomID = 3 ORDER BY StartTime DESC);
DECLARE @seat9 INT = (SELECT SeatID FROM Seat WHERE RoomID = 3 AND RowChar = 'A' AND ColNumber = 3);
INSERT INTO Invoice (AccountID, SubTotal, DiscountAmount, TotalAmount, PaymentMethod, PaymentStatus) VALUES (@cust2, 120000, 0, 120000, 'ZaloPay', 'Paid');
DECLARE @i9 INT = SCOPE_IDENTITY();
INSERT INTO Ticket (ScheduleID, SeatID, InvoiceID, PriceAtBooking, Code, IsCheckedIn, CheckedInAt) VALUES (@s9, @seat9, @i9, 120000, 'TK-DEMO-C2-003', 1, '2025-07-21 18:50');

-- C2: Kung Fu Panda 4 (Room 1)
DECLARE @s10 INT = (SELECT TOP 1 ScheduleID FROM Schedule WHERE MovieID = (SELECT MovieID FROM Movie WHERE MovieName = N'Kung Fu Panda 4') AND RoomID = 1 ORDER BY StartTime);
DECLARE @seat10 INT = (SELECT SeatID FROM Seat WHERE RoomID = 1 AND RowChar = 'A' AND ColNumber = 5);
INSERT INTO Invoice (AccountID, SubTotal, DiscountAmount, TotalAmount, PaymentMethod, PaymentStatus) VALUES (@cust2, 80000, 0, 80000, 'VNPay', 'Paid');
DECLARE @i10 INT = SCOPE_IDENTITY();
INSERT INTO Ticket (ScheduleID, SeatID, InvoiceID, PriceAtBooking, Code, IsCheckedIn, CheckedInAt) VALUES (@s10, @seat10, @i10, 80000, 'TK-DEMO-C2-004', 1, '2025-07-21 08:55');

-- C2: Godzilla x Kong (Room 4)
DECLARE @s11 INT = (SELECT TOP 1 ScheduleID FROM Schedule WHERE MovieID = (SELECT MovieID FROM Movie WHERE MovieName = N'Godzilla x Kong: Đế Chế Mới') AND RoomID = 4 ORDER BY StartTime);
DECLARE @seat11 INT = (SELECT SeatID FROM Seat WHERE RoomID = 4 AND RowChar = 'A' AND ColNumber = 1);
INSERT INTO Invoice (AccountID, SubTotal, DiscountAmount, TotalAmount, PaymentMethod, PaymentStatus) VALUES (@cust2, 130000, 0, 130000, 'MoMo', 'Paid');
DECLARE @i11 INT = SCOPE_IDENTITY();
INSERT INTO Ticket (ScheduleID, SeatID, InvoiceID, PriceAtBooking, Code, IsCheckedIn, CheckedInAt) VALUES (@s11, @seat11, @i11, 130000, 'TK-DEMO-C2-005', 1, '2025-07-22 13:50');

-- C2: Mai (Room 1)
DECLARE @s12 INT = (SELECT TOP 1 ScheduleID FROM Schedule WHERE MovieID = (SELECT MovieID FROM Movie WHERE MovieName = N'Mai') AND RoomID = 1 ORDER BY StartTime);
DECLARE @seat12 INT = (SELECT SeatID FROM Seat WHERE RoomID = 1 AND RowChar = 'A' AND ColNumber = 6);
INSERT INTO Invoice (AccountID, SubTotal, DiscountAmount, TotalAmount, PaymentMethod, PaymentStatus) VALUES (@cust2, 90000, 0, 90000, 'Cash', 'Paid');
DECLARE @i12 INT = SCOPE_IDENTITY();
INSERT INTO Ticket (ScheduleID, SeatID, InvoiceID, PriceAtBooking, Code, IsCheckedIn, CheckedInAt) VALUES (@s12, @seat12, @i12, 90000, 'TK-DEMO-C2-006', 1, '2025-07-22 19:20');
GO

-- ============================================================================
-- 7. KIỂM TRA
-- ============================================================================
PRINT '=== TỔNG PHIM ===';
SELECT COUNT(*) AS TotalMovies FROM Movie WHERE IsActive = 1;

PRINT '=== VÉ CHECK-IN CUSTOMER 1 ===';
SELECT t.TicketID, m.MovieName, t.Code, t.CheckedInAt
FROM Ticket t JOIN Schedule s ON t.ScheduleID = s.ScheduleID JOIN Movie m ON s.MovieID = m.MovieID JOIN Invoice i ON t.InvoiceID = i.InvoiceID
WHERE i.AccountID = (SELECT AccountID FROM Account WHERE Email = 'customer1@gmail.com') AND t.IsCheckedIn = 1;

PRINT '=== VÉ CHECK-IN CUSTOMER 2 ===';
SELECT t.TicketID, m.MovieName, t.Code, t.CheckedInAt
FROM Ticket t JOIN Schedule s ON t.ScheduleID = s.ScheduleID JOIN Movie m ON s.MovieID = m.MovieID JOIN Invoice i ON t.InvoiceID = i.InvoiceID
WHERE i.AccountID = (SELECT AccountID FROM Account WHERE Email = 'customer2@gmail.com') AND t.IsCheckedIn = 1;

PRINT '=== DONE! ===';
GO
