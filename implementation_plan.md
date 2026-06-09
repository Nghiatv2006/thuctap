# Kế Hoạch Triển Khai Nâng Cấp Hệ Thống Quản Lý Kho Hàng (Cập nhật)

Kế hoạch này thực hiện 5 nâng cấp lớn theo yêu cầu của người dùng để cải thiện trải nghiệm sử dụng (UX), tăng tính trực quan của báo động tồn kho và quản lý hạn sử dụng linh hoạt.

---

## 1. Các Nâng Cấp Đề Xuất (Proposed Upgrades)

### 📌 Nâng cấp 1: Định dạng hiển thị tiền tệ (VND Format)
*   **Vấn đề:** Các giá trị tiền tệ hiện tại đang hiển thị dạng số thô (ví dụ: `100000.00`) rất khó đọc.
*   **Giải pháp:** 
    *   Tạo hàm tiện ích định dạng tiền tệ sử dụng Locale Đức (`Locale.GERMANY`) để phân tách phần nghìn bằng dấu chấm `.` (ví dụ: `100.000` VND).
    *   Định cấu hình định dạng này trên tất cả các cột hiển thị đơn giá/thành tiền trong các bảng: Sản phẩm, Chi tiết phiếu nháp, Lịch sử phiếu và Nhãn tổng tiền.

### 📌 Nâng cấp 2: Tự động đưa sản phẩm vừa thay đổi tồn kho lên đầu bảng
*   **Vấn đề:** Khi nhập/xuất/điều chuyển hàng, dữ liệu tồn kho thay đổi nhưng người dùng khó nhận biết dòng nào vừa thay đổi nếu danh sách quá dài.
*   **Giải pháp:**
    *   Thêm cột `last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP` vào bảng `inventories` trong Database.
    *   Cập nhật Entity `Inventory` để lưu trữ trường `lastUpdated`.
    *   Trong `ReceiptService`, mỗi khi cộng/trừ số lượng tồn kho, cập nhật `lastUpdated = LocalDateTime.now()`.
    *   Trong `MainController`, cấu hình bảng Tồn kho mặc định sắp xếp theo `lastUpdated DESC` để các sản phẩm vừa thay đổi luôn nằm ở dòng đầu tiên.

### 📌 Nâng cấp 3: Khóa trường nhập đơn giá khi lập phiếu kho
*   **Vấn đề:** Hiện tại đơn giá trong form lập phiếu kho đang cho phép sửa, không an toàn cho nghiệp vụ kho.
*   **Giải pháp:**
    *   Cấu hình `txtReceiptPrice.setEditable(false)` để khóa trường nhập đơn giá. Đơn giá sẽ tự động điền theo giá gốc của sản phẩm được chọn từ cơ sở dữ liệu.

### 📌 Nâng cấp 4: Quản lý hạn sử dụng (NSX & HSD) bằng ô tích chọn (CheckBox)
*   **Vấn đề:** Hạn sử dụng cần được bật/tắt linh hoạt cho từng mặt hàng lúc lập phiếu thay vì tự động dựa trên danh mục cứng nhắc.
*   **Giải pháp:**
    *   **Giao diện lập phiếu:**
        *   Thêm một ô tích chọn `CheckBox` (`chkHasExpiry`) với nhãn: **"Hàng có hạn sử dụng (NSX/HSD)"**.
        *   Thêm 2 bộ chọn ngày `DatePicker` (`dpMfgDate` cho Ngày sản xuất và `dpExpDate` cho Hạn sử dụng).
        *   **Logic ẩn/hiện:** Nếu người dùng tích chọn ô này -> Hiển thị và bắt buộc nhập NSX, HSD. Nếu bỏ tích -> Ẩn 2 bộ chọn ngày đi và tự động gán ngày mặc định `1970-01-01` khi lưu xuống Database.
    *   **Database:**
        *   Cập nhật bảng `inventories` và `receipt_details` để thêm hai trường `mfg_date` (NSX) và `exp_date` (HSD).
        *   Các mặt hàng không tích chọn hạn sử dụng sẽ được lưu mặc định là `1970-01-01` và hiển thị dấu `-` trên giao diện.
        *   Khóa chính của tồn kho sẽ là `(branch_id, product_id, mfg_date, exp_date)`.

### 📌 Nâng cấp 5: Cấu hình báo động Tồn kho tối thiểu (Gear Button / Popup)
*   **Vấn đề:** Người dùng muốn cấu hình mức tồn kho tối thiểu (ví dụ: mặc định là 5). Khi số lượng tồn kho của một sản phẩm giảm xuống bằng hoặc dưới mức này, hệ thống sẽ tự động cảnh báo.
*   **Giải pháp:**
    *   **Giao diện Dashboard:**
        *   Thêm nút bánh răng `⚙ Cài đặt cảnh báo` bên cạnh bộ lọc chi nhánh ở tab Tồn kho.
        *   Khi click vào, mở một hộp thoại nhỏ `TextInputDialog` cho phép sửa ngưỡng cảnh báo tồn kho tối thiểu (lưu vào một biến tĩnh `lowStockThreshold` trong `MainController`).
    *   **Logic hiển thị bảng Tồn kho:**
        *   Cấu hình cột "Số lượng tồn" (`colInvQuantity`). Nếu số lượng tồn kho của dòng đó `<=` ngưỡng thiết lập thì **chuyển màu chữ thành màu ĐỎ và IN ĐẬM** để người dùng lập tức nhận diện được sản phẩm sắp hết hàng.
        *   Khi người dùng cập nhật ngưỡng mới trong popup, bảng tồn kho tự động được refresh để áp dụng màu sắc cảnh báo ngay lập tức.

---

## 2. Các File Thay Đổi Dự Kiến (Proposed Changes)

### Tầng Dữ liệu (Database Schema & Entities)

#### [MODIFY] [schema.sql](file:///d:/IT/project/schema.sql)
*   Thêm cột `mfg_date DATE NOT NULL DEFAULT '1970-01-01'` và `exp_date DATE NOT NULL DEFAULT '1970-01-01'` vào bảng `inventories` và `receipt_details`.
*   Cập nhật Khóa chính của `inventories` thành: `PRIMARY KEY (branch_id, product_id, mfg_date, exp_date)`.
*   Thêm cột `last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP` vào `inventories`.

#### [MODIFY] [InventoryId.java](file:///d:/IT/project/src/main/java/com/example/project/entity/InventoryId.java)
*   Thêm `mfgDate` và `expDate` vào lớp khóa ngoại kép.

#### [MODIFY] [Inventory.java](file:///d:/IT/project/src/main/java/com/example/project/entity/Inventory.java)
*   Thêm `@Id private LocalDate mfgDate;` và `@Id private LocalDate expDate;`.
*   Thêm trường `private LocalDateTime lastUpdated;`.

#### [MODIFY] [ReceiptDetail.java](file:///d:/IT/project/src/main/java/com/example/project/entity/ReceiptDetail.java)
*   Thêm trường `private LocalDate mfgDate;` và `private LocalDate expDate;`.

---

### Tầng Logic Nghiệp Vụ (Service)

#### [MODIFY] [ReceiptService.java](file:///d:/IT/project/src/main/java/com/example/project/service/ReceiptService.java)
*   Cập nhật logic tìm kiếm và cập nhật `Inventory` theo `(branch, product, mfgDate, expDate)`.
*   Cập nhật `lastUpdated` thành thời gian hiện tại mỗi khi lưu tồn kho.

---

### Tầng Giao Diện (FXML & Controller)

#### [MODIFY] [main.fxml](file:///d:/IT/project/src/main/resources/fxml/main.fxml)
*   Thêm nút thiết lập `⚙ Thiết lập cảnh báo` cạnh bộ lọc chi nhánh ở tab Tồn kho.
*   Thêm `CheckBox` (`chkHasExpiry`) và 2 bộ chọn ngày `DatePicker` (`dpMfgDate`, `dpExpDate`) vào Form lập phiếu.
*   Thêm các cột hiển thị NSX, HSD (`colInvMfgDate`, `colInvExpDate`, `colDraftMfgDate`, `colDraftExpDate`, `colHistDetMfgDate`, `colHistDetExpDate`) vào các bảng.

#### [MODIFY] [MainController.java](file:///d:/IT/project/src/main/java/com/example/project/controller/MainController.java)
*   Thêm hàm tiện ích `formatCurrency` để định dạng tiền tệ dấu chấm.
*   Thiết lập Cell Factories cho các cột giá để hiển thị định dạng mới.
*   Khóa chức năng sửa đơn giá (`txtReceiptPrice.setEditable(false)`).
*   Lắng nghe sự kiện CheckBox thay đổi để ẩn/hiện bộ chọn ngày NSX/HSD.
*   Mặc định sắp xếp danh sách tồn kho theo `lastUpdated DESC`.
*   Thêm biến tĩnh `lowStockThreshold` (mặc định = 5).
*   Lập trình hàm `showSettingsPopup` mở hộp thoại cấu hình ngưỡng cảnh báo tồn kho.
*   Cài đặt Cell Factory cho cột số lượng tồn kho (`colInvQuantity`) tự động chuyển màu đỏ + in đậm nếu nhỏ hơn hoặc bằng ngưỡng cảnh báo.

---

## 3. Kế Hoạch Kiểm Thử (Verification Plan)

### Kiểm thử Tự động
*   Cập nhật [ReceiptServiceTest.java](file:///d:/IT/project/src/test/java/com/example/project/ReceiptServiceTest.java) tương thích ngày sản xuất/hạn sử dụng.
*   Chạy `./gradlew test` để đảm bảo không lỗi logic.

### Kiểm thử Thủ công
1.  Chạy ứng dụng bằng `bootRun`.
2.  Mở màn hình **Tồn kho & Tổng quan**:
    *   Bấm vào nút bánh răng `⚙ Thiết lập cảnh báo` -> Hộp thoại popup hiện ra hiển thị giá trị mặc định là 5.
    *   Nhập thử giá trị mới là `40` và bấm OK -> Kiểm tra xem các dòng tồn kho có số lượng từ 40 trở xuống (ví dụ: MacBook đang có 30 cái) có tự động chuyển sang màu đỏ và in đậm ngay lập tức không.
3.  Mở màn hình **Lập Phiếu Kho**:
    *   Kiểm tra việc ẩn/hiện bộ chọn ngày NSX/HSD tương ứng khi tích chọn CheckBox.
