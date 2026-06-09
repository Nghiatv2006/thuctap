# 📦 Inventory Hub — Hệ thống Quản lý Kho hàng Nhiều Chi nhánh

Ứng dụng desktop quản lý kho hàng cho doanh nghiệp có nhiều chi nhánh, xây dựng bằng **Java + Spring Boot + JavaFX** với cơ sở dữ liệu **PostgreSQL**.

---

## 🚀 Tính năng chính

- **Đăng nhập & Phân quyền** — Xác thực BCrypt với 3 vai trò: `ADMIN`, `MANAGER`, `STAFF`
- **Dashboard Tồn kho** — Xem tồn kho theo chi nhánh, tìm kiếm, cảnh báo hàng sắp hết (bôi đỏ)
- **Quản lý Sản phẩm** — CRUD sản phẩm theo danh mục, hỗ trợ hạn sử dụng (NSX/HSD)
- **Lập phiếu kho** — 5 loại giao dịch: Nhập (IMPORT), Xuất (EXPORT), Điều chuyển (TRANSFER), Cân bằng tăng/giảm (ADJUST_IN/OUT)
- **Lịch sử Giao dịch** — Lọc theo loại phiếu, chi nhánh, người lập, khoảng thời gian
- **Quản lý Người dùng** — Admin toàn quyền; Manager quản lý nhân viên (STAFF) trong chi nhánh của mình
- **Quản lý Chi nhánh** — Admin CRUD chi nhánh, cấu hình ngưỡng cảnh báo tồn kho riêng

---

## 🛠️ Công nghệ sử dụng

| Thành phần | Công nghệ |
|-----------|-----------|
| Ngôn ngữ | Java 17 |
| Backend | Spring Boot 3.5, Spring Data JPA |
| Giao diện | JavaFX 17 (FXML) |
| Cơ sở dữ liệu | PostgreSQL |
| Bảo mật | Spring Security Crypto (BCrypt) |
| Build tool | Gradle |

---

## ⚙️ Cài đặt & Chạy

### 1. Yêu cầu

- JDK 17
- PostgreSQL đã cài đặt và đang chạy

### 2. Khởi tạo cơ sở dữ liệu

Tạo database mới rồi chạy file `schema.sql` để tạo bảng và nạp dữ liệu mẫu:

```bash
psql -U postgres -d ten_database -f schema.sql
```

### 3. Cấu hình biến môi trường

Sao chép `.env.example` thành `.env` và điền thông tin kết nối:

```env
DB_URL=jdbc:postgresql://localhost:5432/ten_database
DB_USERNAME=postgres
DB_PASSWORD=mat_khau_cua_ban
```

### 4. Chạy ứng dụng

```bash
./gradlew bootRun
```

---

## 👤 Tài khoản mẫu

| Username | Mật khẩu | Vai trò |
|----------|----------|---------|
| `admin` | `admin123` | ADMIN |
| `manager_hn` | `manager123` | MANAGER (Hà Nội) |
| `staff_hn` | `staff123` | STAFF (Hà Nội) |

---

## 📂 Cấu trúc dự án

```
src/main/java/com/example/project/
├── controller/    # JavaFX controllers (Login, Main)
├── entity/        # JPA entities (Product, Branch, Inventory, Receipt...)
├── repository/    # Spring Data JPA repositories
├── service/       # Tầng nghiệp vụ
├── JavaFXApplication.java   # Khởi chạy JavaFX + Spring context
└── ProjectApplication.java  # Entry point

src/main/resources/
├── fxml/          # Giao diện FXML (login, main)
└── application.properties
```

---

## 🔐 Phân quyền

| Chức năng | ADMIN | MANAGER | STAFF |
|-----------|:-----:|:-------:|:-----:|
| Xem tồn kho | ✅ (tất cả CN) | ✅ (CN mình) | ✅ (CN mình) |
| CRUD sản phẩm | ✅ | ✅ | ❌ |
| Lập phiếu kho | ✅ | ✅ | ✅ |
| Quản lý người dùng | ✅ (tất cả) | ✅ (STAFF cùng CN) | ❌ |
| Quản lý chi nhánh | ✅ | ❌ | ❌ |
