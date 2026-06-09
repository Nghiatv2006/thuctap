-- KỊCH BẢN KHỞI TẠO CƠ SỞ DỮ LIỆU POSTGRESQL (schema.sql)
-- Hệ thống Quản lý Kho hàng nhiều chi nhánh

-- =========================================================================
-- 1. XÓA BẢNG CŨ NẾU ĐÃ TỒN TẠI (Theo thứ tự tránh lỗi khóa ngoại)
-- =========================================================================
DROP TABLE IF EXISTS receipt_details CASCADE;
DROP TABLE IF EXISTS receipts CASCADE;
DROP TABLE IF EXISTS inventories CASCADE;
DROP TABLE IF EXISTS products CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS branches CASCADE;

-- =========================================================================
-- 2. TẠO CÁC BẢNG DỮ LIỆU
-- =========================================================================

-- Bảng Chi nhánh (Branches)
CREATE TABLE branches (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    address VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    low_stock_threshold INT NOT NULL DEFAULT 5
);

-- Bảng Người dùng (Users)
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL, -- Sẽ lưu mật khẩu mã hóa BCrypt
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'MANAGER', 'STAFF')),
    branch_id INT REFERENCES branches(id) ON DELETE SET NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'LOCKED'))
);

-- Bảng Danh mục sản phẩm (Categories)
CREATE TABLE categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- Bảng Sản phẩm (Products)
CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE, -- Mã SKU sản phẩm (ví dụ: SP001, IPHONE15)
    name VARCHAR(150) NOT NULL,
    unit VARCHAR(50) NOT NULL, -- Đơn vị tính (Cái, Hộp, Kg, Thùng...)
    price DECIMAL(12, 2) NOT NULL DEFAULT 0.00, -- Giá bán hoặc giá quy chiếu mặc định
    category_id INT REFERENCES categories(id) ON DELETE SET NULL,
    has_expiry BOOLEAN NOT NULL DEFAULT FALSE,
    mfg_date DATE NOT NULL DEFAULT '1970-01-01',
    exp_date DATE NOT NULL DEFAULT '1970-01-01'
);

-- Bảng Tồn kho theo từng Chi nhánh (Inventories)
CREATE TABLE inventories (
    branch_id INT REFERENCES branches(id) ON DELETE CASCADE,
    product_id INT REFERENCES products(id) ON DELETE CASCADE,
    mfg_date DATE NOT NULL DEFAULT '1970-01-01',
    exp_date DATE NOT NULL DEFAULT '1970-01-01',
    quantity INT NOT NULL DEFAULT 0 CHECK (quantity >= 0), -- Số lượng tồn kho không được âm
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (branch_id, product_id, mfg_date, exp_date)
);

-- Bảng Phiếu Giao Dịch Kho (Receipts) - Dùng chung cho Nhập, Xuất, Điều chuyển
CREATE TABLE receipts (
    id SERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE, -- Mã phiếu (ví dụ: PN001, PX001, DC001)
    type VARCHAR(20) NOT NULL CHECK (type IN ('IMPORT', 'EXPORT', 'TRANSFER', 'ADJUST_IN', 'ADJUST_OUT')),
    
    -- Chi nhánh xuất hàng (NULL nếu là phiếu Nhập kho)
    source_branch_id INT REFERENCES branches(id) ON DELETE RESTRICT,
    
    -- Chi nhánh nhập hàng (NULL nếu là phiếu Xuất kho bán hàng)
    dest_branch_id INT REFERENCES branches(id) ON DELETE RESTRICT,
    
    -- Người thực hiện giao dịch (Nhân viên lập phiếu)
    user_id INT REFERENCES users(id) ON DELETE SET NULL,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED' CHECK (status IN ('DRAFT', 'COMPLETED', 'CANCELLED')),
    description VARCHAR(255),
    
    -- Đảm bảo logic chi nhánh hợp lệ
    CONSTRAINT chk_branches CHECK (
        (type = 'IMPORT' AND dest_branch_id IS NOT NULL AND source_branch_id IS NULL) OR
        (type = 'EXPORT' AND source_branch_id IS NOT NULL AND dest_branch_id IS NULL) OR
        (type = 'TRANSFER' AND source_branch_id IS NOT NULL AND dest_branch_id IS NOT NULL AND source_branch_id <> dest_branch_id) OR
        (type = 'ADJUST_IN' AND dest_branch_id IS NOT NULL AND source_branch_id IS NULL) OR
        (type = 'ADJUST_OUT' AND source_branch_id IS NOT NULL AND dest_branch_id IS NULL)
    )
);

-- Bảng Chi tiết Phiếu Giao Dịch (Receipt Details)
CREATE TABLE receipt_details (
    id SERIAL PRIMARY KEY,
    receipt_id INT NOT NULL REFERENCES receipts(id) ON DELETE CASCADE,
    product_id INT NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    quantity INT NOT NULL CHECK (quantity > 0), -- Số lượng giao dịch phải lớn hơn 0
    price DECIMAL(12, 2) NOT NULL DEFAULT 0.00, -- Giá nhập/xuất tại thời điểm tạo phiếu
    mfg_date DATE NOT NULL DEFAULT '1970-01-01',
    exp_date DATE NOT NULL DEFAULT '1970-01-01'
);

-- =========================================================================
-- 3. TẠO INDEXES ĐỂ TỐI ƯU HÓA TRUY VẤN
-- =========================================================================
CREATE INDEX idx_products_code ON products(code);
CREATE INDEX idx_receipts_code ON receipts(code);
CREATE INDEX idx_receipts_type ON receipts(type);
CREATE INDEX idx_inventories_branch ON inventories(branch_id);

-- =========================================================================
-- 4. CHÈN DỮ LIỆU MẪU (SEED DATA)
-- =========================================================================

-- Chèn Chi nhánh
INSERT INTO branches (name, address, phone) VALUES 
('Chi nhánh Hà Nội', '123 Cầu Giấy, Hà Nội', '024.333.4444'),
('Chi nhánh TP.HCM', '456 Quận 1, TP. Hồ Chí Minh', '028.555.6666'),
('Chi nhánh Đà Nẵng', '789 Hải Châu, Đà Nẵng', '023.777.8888');

-- Chèn Danh mục
INSERT INTO categories (name) VALUES 
('Điện tử & Công nghệ'),
('Đồ gia dụng'),
('Thực phẩm & Đồ uống');

-- Chèn Sản phẩm
INSERT INTO products (code, name, unit, price, category_id, has_expiry, mfg_date, exp_date) VALUES 
('IPHONE15', 'Điện thoại iPhone 15 Pro Max', 'Cái', 29990000.00, 1, FALSE, '1970-01-01', '1970-01-01'),
('MACBOOKM3', 'Laptop MacBook Air M3 2024', 'Cái', 32490000.00, 1, FALSE, '1970-01-01', '1970-01-01'),
('TULANHSG', 'Tủ lạnh Samsung Inverter 380L', 'Cái', 11500000.00, 2, FALSE, '1970-01-01', '1970-01-01'),
('NOICOMDIEN', 'Nồi cơm điện Cuckoo 1.8L', 'Cái', 2100000.00, 2, FALSE, '1970-01-01', '1970-01-01'),
('MIHAOHAO', 'Mì ăn liền Hảo Hảo chua cay', 'Thùng', 115000.00, 3, TRUE, '2026-01-01', '2026-12-31'),
('BIA333', 'Bia 333 lon 330ml', 'Thùng', 255000.00, 3, TRUE, '2026-03-01', '2026-09-01');

-- Chèn Người dùng mẫu
-- Mật khẩu mẫu đã được mã hóa BCrypt cho:
-- 'admin123' -> $2a$10$3xO/Wziejx3Vu.Nj8em7UOWaAEfgmh7XbtvNRWNenMkIoZWF/G3P2
-- 'manager123' -> $2a$10$Ad.Iw8GD91ov8tx.LxiTBOCjqmrC0IHVyta9XukptvAowO2VLsOvC
-- 'staff123' -> $2a$10$vgHJpqpP4nNjOPAY9Trld.kE3vklu7tWu8ObrYAelz0h/vSQ0zX.q
INSERT INTO users (username, password, full_name, role, branch_id, status) VALUES 
('admin', '$2a$10$3xO/Wziejx3Vu.Nj8em7UOWaAEfgmh7XbtvNRWNenMkIoZWF/G3P2', 'Hệ Thống Admin', 'ADMIN', NULL, 'ACTIVE'),
('manager_hn', '$2a$10$Ad.Iw8GD91ov8tx.LxiTBOCjqmrC0IHVyta9XukptvAowO2VLsOvC', 'Nguyễn Văn Quản Lý HN', 'MANAGER', 1, 'ACTIVE'),
('manager_hcm', '$2a$10$Ad.Iw8GD91ov8tx.LxiTBOCjqmrC0IHVyta9XukptvAowO2VLsOvC', 'Trần Thị Quản Lý HCM', 'MANAGER', 2, 'ACTIVE'),
('staff_hn', '$2a$10$vgHJpqpP4nNjOPAY9Trld.kE3vklu7tWu8ObrYAelz0h/vSQ0zX.q', 'Lê Văn Nhân Viên HN', 'STAFF', 1, 'ACTIVE'),
('staff_hcm', '$2a$10$vgHJpqpP4nNjOPAY9Trld.kE3vklu7tWu8ObrYAelz0h/vSQ0zX.q', 'Phạm Thị Nhân Viên HCM', 'STAFF', 2, 'ACTIVE');

-- Chèn Số lượng tồn kho ban đầu (Seed Inventories)
INSERT INTO inventories (branch_id, product_id, mfg_date, exp_date, quantity) VALUES 
(1, 1, '1970-01-01', '1970-01-01', 50),  -- iPhone 15 tại Hà Nội: 50 cái
(1, 2, '1970-01-01', '1970-01-01', 30),  -- MacBook tại Hà Nội: 30 cái
(1, 5, '2026-01-01', '2026-12-31', 200), -- Mì Hảo Hảo tại Hà Nội: 200 thùng
(2, 1, '1970-01-01', '1970-01-01', 40),  -- iPhone 15 tại TP.HCM: 40 cái
(2, 3, '1970-01-01', '1970-01-01', 15),  -- Tủ lạnh tại TP.HCM: 15 cái
(2, 6, '2026-03-01', '2026-09-01', 150), -- Bia 333 tại TP.HCM: 150 thùng
(3, 2, '1970-01-01', '1970-01-01', 20),  -- MacBook tại Đà Nẵng: 20 cái
(3, 4, '1970-01-01', '1970-01-01', 25),  -- Nồi cơm tại Đà Nẵng: 25 cái
(3, 5, '2026-02-01', '2027-02-01', 100); -- Mì Hảo Hảo tại Đà Nẵng: 100 thùng

-- Chèn một số phiếu giao dịch mẫu để chạy thử báo cáo
-- 1. Phiếu Nhập hàng ban đầu cho Chi nhánh Hà Nội
INSERT INTO receipts (code, type, source_branch_id, dest_branch_id, user_id, status) 
VALUES ('PN-0001', 'IMPORT', NULL, 1, 4, 'COMPLETED');
INSERT INTO receipt_details (receipt_id, product_id, quantity, price, mfg_date, exp_date) VALUES 
(1, 1, 50, 28000000.00, '1970-01-01', '1970-01-01'),
(1, 2, 30, 30000000.00, '1970-01-01', '1970-01-01');

-- 2. Phiếu Xuất bán hàng tại Chi nhánh TP.HCM
INSERT INTO receipts (code, type, source_branch_id, dest_branch_id, user_id, status) 
VALUES ('PX-0001', 'EXPORT', 2, NULL, 5, 'COMPLETED');
INSERT INTO receipt_details (receipt_id, product_id, quantity, price, mfg_date, exp_date) VALUES 
(2, 1, 5, 29990000.00, '1970-01-01', '1970-01-01'),
(2, 6, 10, 255000.00, '2026-03-01', '2026-09-01');

-- 3. Phiếu Điều chuyển từ Hà Nội sang Đà Nẵng
INSERT INTO receipts (code, type, source_branch_id, dest_branch_id, user_id, status) 
VALUES ('DC-0001', 'TRANSFER', 1, 3, 1, 'COMPLETED');
INSERT INTO receipt_details (receipt_id, product_id, quantity, price, mfg_date, exp_date) VALUES 
(3, 2, 5, 32490000.00, '1970-01-01', '1970-01-01');
