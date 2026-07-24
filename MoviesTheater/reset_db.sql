-- ============================================================================
-- CHẠY SCRIPT NÀY TRƯỚC ĐỂ FORCE RESET DATABASE
-- Sau đó chạy cinema_booking_database (1).sql
-- Sau đó chạy seed_demo_data.sql
-- ============================================================================
USE master;
GO

-- Đá hết các connection đang dùng DB
ALTER DATABASE CinemaBookingDB SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
GO

-- Drop DB
DROP DATABASE CinemaBookingDB;
GO

PRINT 'Database đã được xóa thành công. Bây giờ hãy chạy cinema_booking_database (1).sql';
GO
