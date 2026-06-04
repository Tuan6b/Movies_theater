-- ============================================================
-- Cập nhật mật khẩu thật (SHA-256 + salt) cho tài khoản mẫu
-- ============================================================
-- admin@cinema.vn     → admin123
-- manager@cinema.vn   → manager123
-- employee@cinema.vn  → employee123
-- customer1@gmail.com → cust123456
-- customer2@gmail.com → cust123456
-- ============================================================

UPDATE Account SET Password = 'tW6xUVvLgFOJZgjs1KKHblyx/HBcZTvXUvZDk9q6xtMiDFCQLBceRQ2PJEGmdRgl' WHERE Email = 'admin@cinema.vn';
UPDATE Account SET Password = 'Fg+bgQhB8TtCeLC2SaS58K8dFhEjUYl1kZRk6HuA20Q5WIUTRskQXmWyJjenmmFo' WHERE Email = 'manager@cinema.vn';
UPDATE Account SET Password = 'szLS/yCJAJSVJF9/FBP77aBXWrzWuhJYkFSnxP9EJ/KiiLngCs9eBOzrW/zCU/Do' WHERE Email = 'employee@cinema.vn';
UPDATE Account SET Password = '2Cjq7k8qM9leGLZ8OjxZOQ9xzA0tx1TSG03AQahJ8LFJH+HiWcI3a77n/12mSGPx' WHERE Email = 'customer1@gmail.com';
UPDATE Account SET Password = '2Cjq7k8qM9leGLZ8OjxZOQ9xzA0tx1TSG03AQahJ8LFJH+HiWcI3a77n/12mSGPx' WHERE Email = 'customer2@gmail.com';
