-- VNPAY Integration: Add transaction reference columns to Invoice table
ALTER TABLE Invoice ADD TransactionRef VARCHAR(50) NULL;
ALTER TABLE Invoice ADD BankCode VARCHAR(20) NULL;
ALTER TABLE Invoice ADD PayDate VARCHAR(14) NULL;
