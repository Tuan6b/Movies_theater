DECLARE @MovieID INT = 10;
DECLARE @RoomID INT = 2;

-- Insert a new schedule that is currently happening (Started 2 hours ago, ends in 1 hour)
INSERT INTO Schedule (RoomID, MovieID, StartTime, EndTime, BaseTicketPrice, Status)
VALUES (@RoomID, @MovieID, DATEADD(hour, -2, GETDATE()), DATEADD(hour, 1, GETDATE()), 50000, 'Scheduled');

DECLARE @NewScheduleID INT = SCOPE_IDENTITY();

-- Update the ticket of customer1 (AccountID 4) to this new schedule so it makes sense that they checked in
DECLARE @AccountID INT = 4;
DECLARE @InvoiceID INT = (SELECT TOP 1 InvoiceID FROM Invoice WHERE AccountID = @AccountID);

UPDATE Ticket SET ScheduleID = @NewScheduleID WHERE InvoiceID = @InvoiceID;
PRINT 'Inserted new current schedule and updated ticket.';
