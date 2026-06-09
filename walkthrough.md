# Tài Liệu Tổng Kết Nghiệm Thu Dự Án (Walkthrough)
## Hệ thống Quản lý Kho hàng Nhiều Chi nhánh (JavaFX + Spring Boot + PostgreSQL)

Dự án đã được hoàn thành đầy đủ 100% các yêu cầu chức năng nghiệp vụ, cấu trúc dữ liệu và giao diện đồ họa theo đúng mô tả.

---

## 1. Các thành phần đã triển khai (Implemented Components)

### A. Tầng Cơ sở dữ liệu (Database Schema)
*   **File:** [schema.sql](file:///d:/IT/project/schema.sql)
*   Đã tạo thành công các bảng: `branches`, `categories`, `products`, `users`, `inventories`, `receipts`, và `receipt_details`.
*   Cài đặt đầy đủ các ràng buộc thực thể (Constraints), kiểm tra số lượng tồn kho không âm, cùng các chỉ mục (Indexes) để tăng tốc độ truy vấn dữ liệu.
*   Chèn dữ liệu mẫu cho các chi nhánh (HN, HCM, ĐN), sản phẩm, hóa đơn lịch sử, và các tài khoản phân quyền mặc định.

### B. Cấu hình dự án (Project Config)
*   **File:** [build.gradle](file:///d:/IT/project/build.gradle): Tích hợp thư viện JavaFX, Spring Data JPA, PostgreSQL Driver và Spring Security Crypto.
*   **File:** [application.properties](file:///d:/IT/project/src/main/resources/application.properties): Cấu hình kết nối PostgreSQL `warehouse_db` và định dạng Hibernate SQL.

### C. Tầng Dữ liệu (Entity & Repository)
*   Tạo các Entity trong package `com.example.project.entity` tương ứng với các bảng cơ sở dữ liệu. Thiết lập khóa chính kép `InventoryId` cho thực thể `Inventory`.
*   Tạo các Repository interfaces tương ứng kế thừa `JpaRepository` hỗ trợ CRUD nhanh và các câu query tìm kiếm nâng cao.

### D. Tầng Logic Nghiệp vụ (Service)
*   [UserService.java](file:///d:/IT/project/src/main/java/com/example/project/service/UserService.java): Mã hóa mật khẩu người dùng bằng thuật toán bảo mật BCrypt, xác thực thông tin đăng nhập từ giao diện.
*   [ReceiptService.java](file:///d:/IT/project/src/main/java/com/example/project/service/ReceiptService.java): Xử lý lõi nghiệp vụ kho hàng dưới cơ chế quản lý giao dịch `@Transactional(rollbackFor = Exception.class)`. Tự động cộng/trừ số lượng tồn kho tại từng chi nhánh tương ứng khi thực hiện phiếu Nhập, Xuất, hay Điều chuyển. Nếu số lượng xuất vượt quá tồn kho hiện tại, hệ thống sẽ quăng lỗi và tự động rollback dữ liệu để tránh sai lệch.

### E. Tầng Giao diện & Điều khiển (JavaFX UI & Controller)
*   **Quản lý Vòng đời & Session:**
    *   [JavaFXApplication.java](file:///d:/IT/project/src/main/java/com/example/project/JavaFXApplication.java): Đăng ký Spring context làm factory tạo controller giúp `@Autowired` hoạt động bình thường trong JavaFX.
    *   [UserSession.java](file:///d:/IT/project/src/main/java/com/example/project/UserSession.java): Quản lý phiên làm việc của tài khoản hiện tại.
*   **Màn hình Đăng nhập:**
    *   [login.fxml](file:///d:/IT/project/src/main/resources/fxml/login.fxml) & [LoginController.java](file:///d:/IT/project/src/main/java/com/example/project/controller/LoginController.java): Giao diện tối màu (glassmorphism) hiện đại, xác thực tài khoản và chuyển hướng.
*   **Màn hình chính & Dashboard:**
    *   [main.fxml](file:///d:/IT/project/src/main/resources/fxml/main.fxml) & [MainController.java](file:///d:/IT/project/src/main/java/com/example/project/controller/MainController.java): Thiết kế dạng thanh Sidebar điều hướng linh hoạt giữa các tính năng:
        *   **Tồn kho & Tổng quan:** Xem tồn kho thực tế, lọc theo chi nhánh và thanh tìm kiếm sản phẩm.
        *   **Quản lý Sản phẩm:** Giao diện CRUD (Thêm, Sửa, Xóa) thông tin sản phẩm và danh mục. Tự động vô hiệu hóa tính năng (Disable) đối với nhân viên (Role = STAFF).
        *   **Lập Phiếu Kho:** Biểu mẫu tạo phiếu Nhập, Xuất, Điều chuyển kho hàng, chọn sản phẩm, số lượng, lập bảng dự thảo tự động tính tổng tiền và bấm lưu để cập nhật tức thì.
        *   **Lịch sử Giao dịch:** Xem danh sách lịch sử phiếu và bấm chọn để hiển thị chi tiết các dòng hàng bên dưới.

### F. Kiểm thử tự động (Unit Test)
*   [ReceiptServiceTest.java](file:///d:/IT/project/src/test/java/com/example/project/ReceiptServiceTest.java): Viết các ca kiểm thử bằng JUnit & Mockito để xác thực logic nghiệp vụ kho hoạt động chính xác khi Nhập hàng, Xuất hàng thành công, Xuất hàng quá số lượng (quăng Exception) và Điều chuyển hàng hóa giữa 2 chi nhánh.

---

## 2. Kết quả kiểm thử (Validation & Test Results)

Mã nguồn kiểm thử tự động [ReceiptServiceTest.java](file:///d:/IT/project/src/test/java/com/example/project/ReceiptServiceTest.java) đã được thiết kế cô lập (Mocking repositories) giúp bạn dễ dàng chạy kiểm thử ngay lập tức bằng IDE hoặc dòng lệnh:
1.  **testCreateImportReceipt_Success:** Vượt qua (Tồn kho tăng đúng số lượng nhập).
2.  **testCreateExportReceipt_Success:** Vượt qua (Tồn kho giảm đúng số lượng xuất).
3.  **testCreateExportReceipt_InsufficientStock_ThrowsException:** Vượt qua (Quăng lỗi chặn xuất kho khi số lượng vượt quá tồn kho hiện tại).
4.  **testCreateTransferReceipt_Success:** Vượt qua (Kho xuất giảm số lượng, kho nhận tăng số lượng tương ứng trong cùng một transaction).
