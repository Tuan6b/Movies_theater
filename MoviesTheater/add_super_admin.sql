USE CinemaBookingDB;
GO

-- Add SuperAdmin role (roleId = 6)
IF NOT EXISTS (SELECT 1 FROM Role WHERE RoleName = 'SuperAdmin')
BEGIN
    SET IDENTITY_INSERT Role ON;
    INSERT INTO Role (RoleID, RoleName) VALUES (6, 'SuperAdmin');
    SET IDENTITY_INSERT Role OFF;
END
GO

-- Add SuperAdmin account
-- Email: superadmin@cinema.vn
-- Password: Admin@123
-- Hash computed with SHA-256 using all-zero 16-byte salt:
--   Base64(salt[16] + SHA256(salt + "Admin@123"))
IF NOT EXISTS (SELECT 1 FROM Account WHERE Email = 'superadmin@cinema.vn')
BEGIN
    INSERT INTO Account (Email, Password, RoleID, IsBlocked)
    VALUES (
        'superadmin@cinema.vn',
        'AAAAAAAAAAAAAAAAAAAAAP7jdX/uiNYOHPw60mW80vw5TSqbsBjOetucU6OwN//u',
        6,
        0
    );
END
GO

-- Add UserProfile for SuperAdmin
IF NOT EXISTS (
    SELECT 1 FROM UserProfile u
    JOIN Account a ON u.AccountID = a.AccountID
    WHERE a.Email = 'superadmin@cinema.vn'
)
BEGIN
    INSERT INTO UserProfile (AccountID, FullName, PhoneNumber)
    SELECT AccountID, N'Super Administrator', '0900000000'
    FROM Account WHERE Email = 'superadmin@cinema.vn';
END
GO
