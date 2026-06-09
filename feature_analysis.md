# Phân Tích Chức Năng & Phân Chia Mục Tiêu Dự Án
## Quản Lý Kho Hàng Nhiều Chi Nhánh (JavaFX + Spring Boot + PostgreSQL)

Tài liệu này phân tích chi tiết độ khó của các chức năng được yêu cầu trong [mota.txt](file:///d:/IT/project/mota.txt) và phân chia các giai đoạn (mục tiêu) thực hiện để đảm bảo dự án hoàn thành đúng hạn, khoa học và đạt kết quả tốt nhất.

---

## I. Phân Tích Độ Khó Các Chức Năng (Feature Difficulty Analysis)

Hệ thống được chia làm hai nhóm chức năng: **Dễ (Cơ bản/CRUD)** và **Khó (Logic nghiệp vụ/Tích hợp)**.

### 1. Nhóm chức năng DỄ (Cơ bản & CRUD)
Đây là các chức năng mang tính chất quản lý thông tin tĩnh, thao tác cơ sở dữ liệu đơn giản, giao diện chủ yếu là các bảng hiển thị dữ liệu và form nhập liệu cơ bản.

*   **Cấu hình dự án & Database ban đầu:**
    *   Tạo cơ sở dữ liệu PostgreSQL cục bộ.
    *   Cấu hình Gradle để tải các thư viện Spring Boot, JPA, và JavaFX.
*   **Giao diện Đăng nhập (Login Screen):**
    *   Thiết kế form đăng nhập (tài khoản, mật khẩu).
    *   Kiểm tra tài khoản trong database để cho phép vào hệ thống.
*   **Quản lý Danh mục & Sản phẩm (Product CRUD):**
    *   Thêm, sửa, xóa, tìm kiếm thông tin sản phẩm (mã, tên, giá, đơn vị tính, danh mục).
    *   Giao diện hiển thị danh sách dạng bảng (`TableView` trong JavaFX).
*   **Quản lý Chi nhánh (Branch CRUD):**
    *   Thêm, sửa, xóa thông tin chi nhánh (tên chi nhánh, địa chỉ, số điện thoại).

### 2. Nhóm chức năng KHÓ (Logic Nghiệp Vụ & Tích Hợp)
Đây là các chức năng đòi hỏi tư duy logic lập trình tốt, xử lý đồng bộ dữ liệu (Database Transactions), phân quyền người dùng phức tạp và tối ưu trải nghiệm người dùng trên Desktop.

*   **Tích hợp Spring Boot và JavaFX:**
    *   *Độ khó:* Trung bình - Khó. Do Spring Boot và JavaFX có vòng đời (Application Lifecycle) khác nhau, cần phải thiết lập một tầng khởi chạy tùy chỉnh (`Custom FXMLLoader`) để Spring Boot có thể tiêm dependency (Dependency Injection) trực tiếp vào các Controller của JavaFX.
*   **Logic Nhập/Xuất Kho (Inventory Transactions):**
    *   *Độ khó:* Khó. Khi tạo một phiếu nhập hoặc xuất:
        1. Phải ghi nhận thông tin phiếu (người lập, ngày lập, chi nhánh).
        2. Ghi nhận chi tiết phiếu (danh sách sản phẩm, số lượng, đơn giá).
        3. Cập nhật số lượng tồn kho tương ứng của các sản phẩm tại chi nhánh đó.
    *   *Yêu cầu kỹ thuật:* Sử dụng `@Transactional` của Spring để đảm bảo nếu một bước bị lỗi (ví dụ: mất kết nối DB khi đang cập nhật tồn kho) thì toàn bộ giao dịch sẽ được rollback, tránh mất mát hoặc sai lệch dữ liệu kho.
*   **Nghiệp vụ Điều chuyển kho giữa các chi nhánh (Inter-branch Transfer):**
    *   *Độ khó:* Khó. Chuyển sản phẩm A từ Chi nhánh 1 sang Chi nhánh 2:
        1. Tạo phiếu điều chuyển.
        2. Trừ tồn kho tại Chi nhánh 1.
        3. Cộng tồn kho tại Chi nhánh 2.
        4. Phải kiểm tra điều kiện (số lượng tồn kho ở Chi nhánh 1 có đủ để chuyển đi không).
*   **Phân quyền người dùng trên giao diện (Role-Based Access Control - RBAC):**
    *   *Độ khó:* Trung bình. Tùy thuộc vào tài khoản đăng nhập (Admin, Quản lý kho, Nhân viên) mà giao diện JavaFX sẽ ẩn/hiện hoặc vô hiệu hóa (disable) các chức năng/menu tương ứng.
*   **Thống kê & Báo cáo trực quan (Dashboard & Charts):**
    *   *Độ khó:* Trung bình - Khó. Viết các câu lệnh SQL gom nhóm (Aggregate Queries) để tính doanh số, số lượng nhập/xuất theo thời gian.
    *   Sử dụng các thành phần biểu đồ của JavaFX (`BarChart`, `PieChart`, `LineChart`) để vẽ dữ liệu trực quan lên màn hình chính.

---

## II. Phân Chia Mục Tiêu Thực Hiện (Milestones & Roadmap)

Để thực hiện dự án một cách hiệu quả, công việc được chia làm **4 Giai đoạn** rõ ràng:

### Giai đoạn 1: Thiết kế Hệ thống & Cấu hình Môi trường
*   **Mục tiêu:** Hoàn thành thiết kế lý thuyết và cấu hình nền tảng.
*   **Công việc chi tiết:**
    1.  Vẽ sơ đồ Use Case để làm rõ các chức năng của từng vai trò (Admin, Quản lý, Nhân viên).
    2.  Thiết kế Sơ đồ Cơ sở Dữ liệu (ERD) chuẩn hóa.
    3.  Cấu hình tích hợp thành công **Spring Boot + JavaFX + PostgreSQL** trong file `build.gradle` và chạy thử một cửa sổ JavaFX trống được Spring quản lý.

### Giai đoạn 2: Xây dựng Cơ sở dữ liệu & Chức năng CRUD cơ bản
*   **Mục tiêu:** Hoàn thành phần khung dữ liệu và các giao diện quản lý cơ bản.
*   **Công việc chi tiết:**
    1.  Tạo các bảng cơ sở dữ liệu trên PostgreSQL.
    2.  Xây dựng các lớp Entity, Repository (Spring Data JPA) cho `User`, `Product`, `Branch`.
    3.  Lập trình giao diện Đăng nhập và phân quyền đơn giản.
    4.  Lập trình giao diện CRUD Sản phẩm và Chi nhánh (hiển thị danh sách, thêm mới, chỉnh sửa dữ liệu trực tiếp trên bảng).

### Giai đoạn 3: Phát triển Logic Nhập/Xuất & Điều chuyển Kho
*   **Mục tiêu:** Giải quyết phần lõi nghiệp vụ khó nhất của hệ thống kho hàng.
*   **Công việc chi tiết:**
    1.  Xây dựng bảng Cơ sở dữ liệu cho Tồn kho (`Inventory`), Phiếu nhập/xuất/điều chuyển (`Receipt`, `Transfer`) và Chi tiết phiếu (`ReceiptDetail`, `TransferDetail`).
    2.  Viết các Service xử lý nghiệp vụ nhập/xuất kho, đảm bảo tính toàn vẹn dữ liệu bằng `@Transactional`.
    3.  Thiết kế giao diện lập phiếu Nhập/Xuất kho (giao diện cho phép chọn chi nhánh, chọn sản phẩm từ danh sách tìm kiếm, nhập số lượng và bấm xác nhận).
    4.  Thiết kế giao diện Điều chuyển hàng hóa giữa các chi nhánh và kiểm tra điều kiện tồn kho.

### Giai đoạn 4: Thống kê Báo cáo, Kiểm thử & Tối ưu Giao diện
*   **Mục tiêu:** Hoàn thiện trải nghiệm người dùng, vẽ biểu đồ và kiểm thử sửa lỗi.
*   **Công việc chi tiết:**
    1.  Viết Service thống kê số lượng tồn kho của tất cả chi nhánh và lịch sử xuất nhập.
    2.  Vẽ biểu đồ thống kê sản phẩm nhập/xuất nhiều nhất bằng `BarChart`/`PieChart` trên trang Dashboard chính.
    3.  Tối ưu hóa giao diện (sử dụng CSS của JavaFX để làm giao diện hiện đại, mượt mà hơn).
    4.  Viết Unit Test kiểm thử logic nhập xuất và sửa các lỗi phát sinh.

---

## III. Phân Vai Trò Thực Hiện (Dành cho làm việc nhóm - Nếu có)
Nếu bạn làm việc theo nhóm (3-5 thành viên) như mô tả trong syllabus, có thể phân chia vai trò như sau để tối ưu năng lực:

| Vai trò | Nhiệm vụ chính | Yêu cầu năng lực |
| :--- | :--- | :--- |
| **Developer 1 (Trưởng nhóm/Khá giỏi)** | Cấu hình tích hợp Spring-JavaFX, xử lý logic Nhập/Xuất kho, thiết kế Database, viết Service hỗ trợ Transactions. | Kiến thức Java vững, hiểu về Spring JPA và cơ sở dữ liệu PostgreSQL. |
| **Developer 2 (Trung bình)** | Thiết kế và lập trình các giao diện CRUD (Sản phẩm, Chi nhánh), giao diện Đăng nhập, phân quyền hiển thị trên UI. | Lập trình JavaFX tốt, sử dụng thành thạo Scene Builder. |
| **Developer 3 (Trung bình - Yếu)** | Làm báo cáo thống kê, vẽ biểu đồ JavaFX, viết tài liệu đặc tả Use Case, thiết kế Wireframe cơ bản. | Kỹ năng viết tài liệu, truy vấn SQL cơ bản, vẽ biểu đồ. |
| **Tester & Documenter (Yếu)** | Kiểm thử chức năng (test các case nhập xuất âm, nhập thiếu dữ liệu...), viết tài liệu hướng dẫn sử dụng phần mềm. | Cẩn thận, chi tiết, kỹ năng viết tài liệu tốt. |
