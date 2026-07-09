DECLARE @AccountID INT = 4; -- customer1@gmail.com
DECLARE @MovieID INT = 10;
DECLARE @ScheduleID INT = 12;
DECLARE @SeatID INT = 1;

DECLARE @InvoiceID INT = (SELECT TOP 1 InvoiceID FROM Invoice WHERE AccountID = @AccountID);

IF @InvoiceID IS NULL
BEGIN
    INSERT INTO Invoice (AccountID, SubTotal, DiscountAmount, TotalAmount, PaymentMethod, PaymentStatus)
    VALUES (@AccountID, 50000, 0, 50000, 'Cash', 'Paid');
    SET @InvoiceID = SCOPE_IDENTITY();
END

IF NOT EXISTS (SELECT 1 FROM Ticket WHERE InvoiceID = @InvoiceID AND ScheduleID = @ScheduleID)
BEGIN
    INSERT INTO Ticket (InvoiceID, ScheduleID, SeatID, PriceAtBooking, Code, IsCheckedIn)
    VALUES (@InvoiceID, @ScheduleID, @SeatID, 50000, LEFT(CAST(NEWID() AS VARCHAR(36)), 20), 1);
    PRINT 'Inserted new ticket and checked it in.';
END
ELSE
BEGIN
    UPDATE Ticket SET IsCheckedIn = 1 WHERE InvoiceID = @InvoiceID AND ScheduleID = @ScheduleID;
    PRINT 'Updated existing ticket to checked in.';
END
