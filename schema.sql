-- =============================================================================
-- HỆ THỐNG QUẢN LÝ KHO HÀNG NHIỀU CHI NHÁNH
-- PostgreSQL Schema — Phiên bản: 2.0
-- Ghi chú: File này dùng để khởi tạo DB từ đầu.
--          Với DB đang chạy, dùng ALTER TABLE nếu chỉ cần thêm cột.
-- =============================================================================


-- =============================================================================
-- BƯỚC 1: DỌN DẸP — XÓA BẢNG CŨ (theo thứ tự tránh lỗi khóa ngoại)
-- =============================================================================
DROP TABLE IF EXISTS receipt_details  CASCADE;
DROP TABLE IF EXISTS receipts          CASCADE;
DROP TABLE IF EXISTS inventories       CASCADE;
DROP TABLE IF EXISTS products          CASCADE;
DROP TABLE IF EXISTS categories        CASCADE;
DROP TABLE IF EXISTS users             CASCADE;
DROP TABLE IF EXISTS branches          CASCADE;


-- =============================================================================
-- BƯỚC 2: TẠO BẢNG
-- =============================================================================

-- ----------------------------------------------------------------------------
-- branches: Chi nhánh / Kho hàng
--   low_stock_threshold: Ngưỡng cảnh báo tồn kho tối thiểu (mặc định 5)
-- ----------------------------------------------------------------------------
CREATE TABLE branches (
    id                  SERIAL PRIMARY KEY,
    name                VARCHAR(100) NOT NULL UNIQUE,
    address             VARCHAR(255) NOT NULL,
    phone               VARCHAR(20),
    low_stock_threshold INT          NOT NULL DEFAULT 5
);

-- ----------------------------------------------------------------------------
-- users: Người dùng hệ thống
--   role   : ADMIN (toàn quyền) | MANAGER (quản lý chi nhánh) | STAFF (nhân viên)
--   status : ACTIVE | LOCKED
--   password: BCrypt hash, KHÔNG lưu plain text
-- ----------------------------------------------------------------------------
CREATE TABLE users (
    id          SERIAL PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    full_name   VARCHAR(100) NOT NULL,
    role        VARCHAR(20)  NOT NULL CHECK (role   IN ('ADMIN', 'MANAGER', 'STAFF')),
    branch_id   INT          REFERENCES branches(id) ON DELETE SET NULL,
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'LOCKED'))
);

-- ----------------------------------------------------------------------------
-- categories: Danh mục sản phẩm
-- ----------------------------------------------------------------------------
CREATE TABLE categories (
    id   SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- ----------------------------------------------------------------------------
-- products: Sản phẩm / Hàng hóa
--   code      : Mã SKU, duy nhất, KHÔNG được đổi sau khi có giao dịch
--   has_expiry: TRUE nếu hàng có hạn sử dụng (phân biệt lô theo NSX/HSD)
--   mfg_date  : Ngày sản xuất mặc định (template khi lập phiếu)
--   exp_date  : Hạn sử dụng mặc định (template khi lập phiếu)
--   Lưu ý: mfg_date/exp_date = '1970-01-01' nghĩa là không áp dụng (has_expiry = FALSE)
-- ----------------------------------------------------------------------------
CREATE TABLE products (
    id          SERIAL PRIMARY KEY,
    code        VARCHAR(50)     NOT NULL UNIQUE,
    name        VARCHAR(150)    NOT NULL,
    unit        VARCHAR(50)     NOT NULL,
    price       DECIMAL(12, 2)  NOT NULL DEFAULT 0.00,
    category_id INT             REFERENCES categories(id) ON DELETE SET NULL,
    has_expiry  BOOLEAN         NOT NULL DEFAULT FALSE,
    mfg_date    DATE            NOT NULL DEFAULT '1970-01-01',
    exp_date    DATE            NOT NULL DEFAULT '1970-01-01'
);

-- ----------------------------------------------------------------------------
-- inventories: Tồn kho theo từng chi nhánh
--   Khóa chính phức hợp: (branch_id, product_id, mfg_date, exp_date)
--   → Cho phép lưu nhiều lô hàng khác nhau của cùng 1 sản phẩm tại 1 kho
--   quantity   : Không được âm (CHECK >= 0)
--   last_updated: Tự động cập nhật mỗi khi có giao dịch
-- ----------------------------------------------------------------------------
CREATE TABLE inventories (
    branch_id    INT            NOT NULL REFERENCES branches(id)  ON DELETE CASCADE,
    product_id   INT            NOT NULL REFERENCES products(id)  ON DELETE CASCADE,
    mfg_date     DATE           NOT NULL DEFAULT '1970-01-01',
    exp_date     DATE           NOT NULL DEFAULT '1970-01-01',
    quantity     INT            NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    last_updated TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (branch_id, product_id, mfg_date, exp_date)
);

-- ----------------------------------------------------------------------------
-- receipts: Phiếu giao dịch kho (dùng chung cho mọi loại)
--   type:
--     IMPORT    : Nhập hàng từ nhà cung cấp  → dest_branch có, source_branch NULL
--     EXPORT    : Xuất bán hàng               → source_branch có, dest_branch NULL
--     TRANSFER  : Điều chuyển nội bộ          → cả 2 đều có, phải khác nhau
--     ADJUST_IN : Cân bằng kho tăng           → dest_branch có, source_branch NULL
--     ADJUST_OUT: Cân bằng kho giảm           → source_branch có, dest_branch NULL
--   description: Ghi chú / Lý do (tùy chọn, tối đa 500 ký tự)
-- ----------------------------------------------------------------------------
CREATE TABLE receipts (
    id               SERIAL PRIMARY KEY,
    code             VARCHAR(50)  NOT NULL UNIQUE,
    type             VARCHAR(20)  NOT NULL CHECK (type IN (
                         'IMPORT', 'EXPORT', 'TRANSFER', 'ADJUST_IN', 'ADJUST_OUT'
                     )),
    source_branch_id INT          REFERENCES branches(id) ON DELETE RESTRICT,
    dest_branch_id   INT          REFERENCES branches(id) ON DELETE RESTRICT,
    user_id          INT          REFERENCES users(id)    ON DELETE SET NULL,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status           VARCHAR(20)  NOT NULL DEFAULT 'COMPLETED'
                         CHECK (status IN ('DRAFT', 'COMPLETED', 'CANCELLED')),
    description      VARCHAR(500),

    -- Ràng buộc logic chi nhánh theo từng loại phiếu
    CONSTRAINT chk_receipt_branches CHECK (
        (type = 'IMPORT'     AND dest_branch_id   IS NOT NULL AND source_branch_id IS NULL) OR
        (type = 'EXPORT'     AND source_branch_id IS NOT NULL AND dest_branch_id   IS NULL) OR
        (type = 'TRANSFER'   AND source_branch_id IS NOT NULL AND dest_branch_id   IS NOT NULL
                             AND source_branch_id <> dest_branch_id) OR
        (type = 'ADJUST_IN'  AND dest_branch_id   IS NOT NULL AND source_branch_id IS NULL) OR
        (type = 'ADJUST_OUT' AND source_branch_id IS NOT NULL AND dest_branch_id   IS NULL)
    )
);

-- ----------------------------------------------------------------------------
-- receipt_details: Chi tiết từng dòng sản phẩm trong phiếu
--   quantity: Phải > 0
--   price   : Giá tại thời điểm giao dịch (lịch sử giá)
-- ----------------------------------------------------------------------------
CREATE TABLE receipt_details (
    id         SERIAL PRIMARY KEY,
    receipt_id INT            NOT NULL REFERENCES receipts(id)  ON DELETE CASCADE,
    product_id INT            NOT NULL REFERENCES products(id)  ON DELETE RESTRICT,
    quantity   INT            NOT NULL CHECK (quantity > 0),
    price      DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    mfg_date   DATE           NOT NULL DEFAULT '1970-01-01',
    exp_date   DATE           NOT NULL DEFAULT '1970-01-01'
);


-- =============================================================================
-- BƯỚC 3: TẠO INDEX TỐI ƯU TRUY VẤN
-- =============================================================================
CREATE INDEX idx_products_code       ON products(code);
CREATE INDEX idx_products_category   ON products(category_id);
CREATE INDEX idx_receipts_code       ON receipts(code);
CREATE INDEX idx_receipts_type       ON receipts(type);
CREATE INDEX idx_receipts_created    ON receipts(created_at DESC);
CREATE INDEX idx_inventories_branch  ON inventories(branch_id);
CREATE INDEX idx_inventories_product ON inventories(product_id);
CREATE INDEX idx_receipt_det_receipt ON receipt_details(receipt_id);
CREATE INDEX idx_users_branch        ON users(branch_id);


-- =============================================================================
-- BƯỚC 4: DỮ LIỆU MẪU (SEED DATA)
-- =============================================================================

-- ----------------------------------------------------------------------------
-- Chi nhánh (low_stock_threshold tùy chỉnh cho từng chi nhánh)
-- ----------------------------------------------------------------------------
INSERT INTO branches (name, address, phone, low_stock_threshold) VALUES
('Chi nhánh Hà Nội',  '123 Cầu Giấy, Hà Nội',              '024.333.4444', 10),
('Chi nhánh TP.HCM',  '456 Quận 1, TP. Hồ Chí Minh',       '028.555.6666', 10),
('Chi nhánh Đà Nẵng', '789 Hải Châu, Đà Nẵng',             '023.777.8888',  5);

-- ----------------------------------------------------------------------------
-- Danh mục sản phẩm
-- ----------------------------------------------------------------------------
INSERT INTO categories (name) VALUES
('Điện tử & Công nghệ'),
('Đồ gia dụng'),
('Thực phẩm & Đồ uống');

-- ----------------------------------------------------------------------------
-- Sản phẩm
-- ----------------------------------------------------------------------------
INSERT INTO products (code, name, unit, price, category_id, has_expiry, mfg_date, exp_date) VALUES
('IPHONE15',    'Điện thoại iPhone 15 Pro Max',    'Cái',   29990000.00, 1, FALSE, '1970-01-01', '1970-01-01'),
('MACBOOKM3',   'Laptop MacBook Air M3 2024',       'Cái',   32490000.00, 1, FALSE, '1970-01-01', '1970-01-01'),
('SAMSUNG65',   'TV Samsung QLED 65 inch 4K',       'Cái',   22900000.00, 1, FALSE, '1970-01-01', '1970-01-01'),
('TULANHSG',    'Tủ lạnh Samsung Inverter 380L',    'Cái',   11500000.00, 2, FALSE, '1970-01-01', '1970-01-01'),
('NOICOMDIEN',  'Nồi cơm điện Cuckoo 1.8L',         'Cái',    2100000.00, 2, FALSE, '1970-01-01', '1970-01-01'),
('MAYGIAT',     'Máy giặt LG Inverter 9kg',         'Cái',    8500000.00, 2, FALSE, '1970-01-01', '1970-01-01'),
('MIHAOHAO',    'Mì ăn liền Hảo Hảo chua cay',     'Thùng',   115000.00, 3, TRUE,  '2026-01-01', '2026-12-31'),
('BIA333',      'Bia 333 lon 330ml',                 'Thùng',   255000.00, 3, TRUE,  '2026-03-01', '2026-09-01'),
('NUOCSUOI',    'Nước suối Aquafina 500ml',          'Thùng',   135000.00, 3, TRUE,  '2026-02-01', '2027-02-01'),
('CAFETRUNG',   'Cà phê Trung Nguyên Legend 500g',  'Hộp',     285000.00, 3, TRUE,  '2026-01-15', '2027-01-15');

-- ----------------------------------------------------------------------------
-- Người dùng
--   admin123    → $2a$10$3xO/Wziejx3Vu.Nj8em7UOWaAEfgmh7XbtvNRWNenMkIoZWF/G3P2
--   manager123  → $2a$10$Ad.Iw8GD91ov8tx.LxiTBOCjqmrC0IHVyta9XukptvAowO2VLsOvC
--   staff123    → $2a$10$vgHJpqpP4nNjOPAY9Trld.kE3vklu7tWu8ObrYAelz0h/vSQ0zX.q
-- ----------------------------------------------------------------------------
INSERT INTO users (username, password, full_name, role, branch_id, status) VALUES
('admin',       '$2a$10$3xO/Wziejx3Vu.Nj8em7UOWaAEfgmh7XbtvNRWNenMkIoZWF/G3P2', 'Hệ Thống Admin',           'ADMIN',   NULL, 'ACTIVE'),
('manager_hn',  '$2a$10$Ad.Iw8GD91ov8tx.LxiTBOCjqmrC0IHVyta9XukptvAowO2VLsOvC', 'Nguyễn Văn Quản Lý HN',    'MANAGER', 1,    'ACTIVE'),
('manager_hcm', '$2a$10$Ad.Iw8GD91ov8tx.LxiTBOCjqmrC0IHVyta9XukptvAowO2VLsOvC', 'Trần Thị Quản Lý HCM',     'MANAGER', 2,    'ACTIVE'),
('manager_dn',  '$2a$10$Ad.Iw8GD91ov8tx.LxiTBOCjqmrC0IHVyta9XukptvAowO2VLsOvC', 'Lê Thị Quản Lý Đà Nẵng',   'MANAGER', 3,    'ACTIVE'),
('staff_hn',    '$2a$10$vgHJpqpP4nNjOPAY9Trld.kE3vklu7tWu8ObrYAelz0h/vSQ0zX.q', 'Lê Văn Nhân Viên HN',      'STAFF',   1,    'ACTIVE'),
('staff_hcm',   '$2a$10$vgHJpqpP4nNjOPAY9Trld.kE3vklu7tWu8ObrYAelz0h/vSQ0zX.q', 'Phạm Thị Nhân Viên HCM',   'STAFF',   2,    'ACTIVE'),
('staff_dn',    '$2a$10$vgHJpqpP4nNjOPAY9Trld.kE3vklu7tWu8ObrYAelz0h/vSQ0zX.q', 'Hoàng Văn Nhân Viên ĐN',   'STAFF',   3,    'ACTIVE');

-- ----------------------------------------------------------------------------
-- Tồn kho ban đầu
-- ----------------------------------------------------------------------------
INSERT INTO inventories (branch_id, product_id, mfg_date, exp_date, quantity) VALUES
-- Hà Nội
(1, 1,  '1970-01-01', '1970-01-01',  50),  -- iPhone 15
(1, 2,  '1970-01-01', '1970-01-01',  30),  -- MacBook M3
(1, 3,  '1970-01-01', '1970-01-01',  12),  -- TV Samsung
(1, 4,  '1970-01-01', '1970-01-01',   8),  -- Tủ lạnh
(1, 7,  '2026-01-01', '2026-12-31', 200),  -- Mì Hảo Hảo lô 1
(1, 9,  '2026-02-01', '2027-02-01', 150),  -- Nước suối
(1, 10, '2026-01-15', '2027-01-15',  60),  -- Cà phê Trung Nguyên

-- TP.HCM
(2, 1,  '1970-01-01', '1970-01-01',  40),  -- iPhone 15
(2, 3,  '1970-01-01', '1970-01-01',  15),  -- TV Samsung
(2, 4,  '1970-01-01', '1970-01-01',   6),  -- Tủ lạnh
(2, 5,  '1970-01-01', '1970-01-01',  20),  -- Nồi cơm
(2, 8,  '2026-03-01', '2026-09-01', 150),  -- Bia 333
(2, 9,  '2026-02-01', '2027-02-01', 100),  -- Nước suối
(2, 10, '2026-01-15', '2027-01-15',  45),  -- Cà phê

-- Đà Nẵng
(3, 2,  '1970-01-01', '1970-01-01',  20),  -- MacBook M3
(3, 5,  '1970-01-01', '1970-01-01',  25),  -- Nồi cơm
(3, 6,  '1970-01-01', '1970-01-01',   7),  -- Máy giặt
(3, 7,  '2026-02-01', '2027-02-01', 100),  -- Mì Hảo Hảo lô 2
(3, 8,  '2026-03-01', '2026-09-01',  80);  -- Bia 333

-- ----------------------------------------------------------------------------
-- Phiếu giao dịch mẫu
-- ----------------------------------------------------------------------------

-- Phiếu 1: Nhập hàng Hà Nội (staff_hn)
INSERT INTO receipts (code, type, source_branch_id, dest_branch_id, user_id, status, description, created_at)
VALUES ('IM-HN-0001', 'IMPORT', NULL, 1, 5, 'COMPLETED', 'Nhập hàng điện tử đầu quý 1/2026', '2026-01-05 08:00:00');
INSERT INTO receipt_details (receipt_id, product_id, quantity, price, mfg_date, exp_date) VALUES
(1, 1, 50, 28000000.00, '1970-01-01', '1970-01-01'),
(1, 2, 30, 30000000.00, '1970-01-01', '1970-01-01');

-- Phiếu 2: Nhập hàng TP.HCM (staff_hcm)
INSERT INTO receipts (code, type, source_branch_id, dest_branch_id, user_id, status, description, created_at)
VALUES ('IM-HCM-0001', 'IMPORT', NULL, 2, 6, 'COMPLETED', 'Nhập hàng thực phẩm tháng 3/2026', '2026-03-10 09:00:00');
INSERT INTO receipt_details (receipt_id, product_id, quantity, price, mfg_date, exp_date) VALUES
(2, 8,  150, 240000.00, '2026-03-01', '2026-09-01'),
(2, 9,  100, 125000.00, '2026-02-01', '2027-02-01');

-- Phiếu 3: Xuất bán tại TP.HCM (staff_hcm)
INSERT INTO receipts (code, type, source_branch_id, dest_branch_id, user_id, status, description, created_at)
VALUES ('EX-HCM-0001', 'EXPORT', 2, NULL, 6, 'COMPLETED', 'Xuất bán lẻ tháng 3/2026', '2026-03-15 14:30:00');
INSERT INTO receipt_details (receipt_id, product_id, quantity, price, mfg_date, exp_date) VALUES
(3, 1,  5, 29990000.00, '1970-01-01', '1970-01-01'),
(3, 8, 10,   255000.00, '2026-03-01', '2026-09-01');

-- Phiếu 4: Điều chuyển Hà Nội → Đà Nẵng (manager_hn)
INSERT INTO receipts (code, type, source_branch_id, dest_branch_id, user_id, status, description, created_at)
VALUES ('TR-HN-DN-001', 'TRANSFER', 1, 3, 2, 'COMPLETED', 'Điều chuyển MacBook bổ sung Đà Nẵng', '2026-04-01 10:00:00');
INSERT INTO receipt_details (receipt_id, product_id, quantity, price, mfg_date, exp_date) VALUES
(4, 2, 5, 32490000.00, '1970-01-01', '1970-01-01');

-- Phiếu 5: Xuất bán Hà Nội (staff_hn)
INSERT INTO receipts (code, type, source_branch_id, dest_branch_id, user_id, status, description, created_at)
VALUES ('EX-HN-0001', 'EXPORT', 1, NULL, 5, 'COMPLETED', 'Xuất bán dự án tháng 4', '2026-04-10 16:00:00');
INSERT INTO receipt_details (receipt_id, product_id, quantity, price, mfg_date, exp_date) VALUES
(5, 3, 3, 22900000.00, '1970-01-01', '1970-01-01'),
(5, 7, 20,   115000.00, '2026-01-01', '2026-12-31');

-- Phiếu 6: Cân bằng kho — kiểm kê phát hiện thiếu (admin)
INSERT INTO receipts (code, type, source_branch_id, dest_branch_id, user_id, status, description, created_at)
VALUES ('AO-HN-0001', 'ADJUST_OUT', 1, NULL, 1, 'COMPLETED', 'Kiểm kê phát hiện thiếu 2 tủ lạnh — hao hụt vận chuyển', '2026-05-01 08:30:00');
INSERT INTO receipt_details (receipt_id, product_id, quantity, price, mfg_date, exp_date) VALUES
(6, 4, 2, 11500000.00, '1970-01-01', '1970-01-01');

-- Phiếu 7: Cân bằng kho — bổ sung (admin)
INSERT INTO receipts (code, type, source_branch_id, dest_branch_id, user_id, status, description, created_at)
VALUES ('AI-HCM-0001', 'ADJUST_IN', NULL, 2, 1, 'COMPLETED', 'Nhập bổ sung nồi cơm sau kiểm kê', '2026-05-15 09:00:00');
INSERT INTO receipt_details (receipt_id, product_id, quantity, price, mfg_date, exp_date) VALUES
(7, 5, 5, 2100000.00, '1970-01-01', '1970-01-01');
