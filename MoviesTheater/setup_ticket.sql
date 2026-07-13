DECLARE @AccountID INT = 4; -- customer1@gmail.com
DECLARE @MovieID INT = 10;
DECLARE @ScheduleID INT = 12;
DECLARE @SeatID INT = (SELECT TOP 1 SeatID FROM Seat WHERE RoomID = 2);

-- Check if an invoice already exists for this user to avoid clutter
DECLARE @InvoiceID INT = (SELECT TOP 1 InvoiceID FROM Invoice WHERE AccountID = @AccountID);

IF @InvoiceID IS NULL
BEGIN
    INSERT INTO Invoice (AccountID, BookingDate, TotalAmount, Status, PaymentMethod)
    VALUES (@AccountID, GETDATE(), 50000, 'Paid', 'Cash');
    SET @InvoiceID = SCOPE_IDENTITY();
END

-- Check if ticket already exists
IF NOT EXISTS (SELECT 1 FROM Ticket WHERE InvoiceID = @InvoiceID AND ScheduleID = @ScheduleID)
BEGIN
    INSERT INTO Ticket (InvoiceID, ScheduleID, SeatID, Price, IsCheckedIn)
    VALUES (@InvoiceID, @ScheduleID, @SeatID, 50000, 1);
    PRINT 'Inserted new ticket and checked it in.';
END
ELSE
BEGIN
    UPDATE Ticket SET IsCheckedIn = 1 WHERE InvoiceID = @InvoiceID AND ScheduleID = @ScheduleID;
    PRINT 'Updated existing ticket to checked in.';
END
