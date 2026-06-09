# Danh sách Công việc (Task List) - Giai đoạn Nâng cấp Hệ thống

- [x] **Task 1: Cập nhật Cơ sở dữ liệu (Database Schema)**
  - [x] Cập nhật [schema.sql](file:///d:/IT/project/schema.sql) (thêm `mfg_date`, `exp_date`, `last_updated` và khóa chính kép mới)
- [x] **Task 2: Cập nhật Entities (JPA Model)**
  - [x] Cập nhật [InventoryId.java](file:///d:/IT/project/src/main/java/com/example/project/entity/InventoryId.java)
  - [x] Cập nhật [Inventory.java](file:///d:/IT/project/src/main/java/com/example/project/entity/Inventory.java)
  - [x] Cập nhật [ReceiptDetail.java](file:///d:/IT/project/src/main/java/com/example/project/entity/ReceiptDetail.java)
- [x] **Task 3: Cập nhật Tầng Nghiệp Vụ (Service)**
  - [x] Cập nhật [ReceiptService.java](file:///d:/IT/project/src/main/java/com/example/project/service/ReceiptService.java) (cập nhật tồn kho theo lô hạn sử dụng và lưu mốc thời gian cập nhật)
- [/] **Task 4: Thiết kế Giao diện (JavaFX FXML)**
  - [/] Cập nhật [main.fxml](file:///d:/IT/project/src/main/resources/fxml/main.fxml) (thêm CheckBox, DatePickers và nút bánh răng thiết lập cảnh báo)
- [ ] **Task 5: Lập trình Giao diện & Định dạng (MainController)**
  - [ ] Cập nhật [MainController.java](file:///d:/IT/project/src/main/java/com/example/project/controller/MainController.java) (định dạng tiền tệ, bôi đỏ cảnh báo tồn kho tối thiểu, ẩn hiện DatePicker, và sắp xếp bảng)
- [ ] **Task 6: Kiểm thử tự động (Unit Test)**
  - [ ] Cập nhật [ReceiptServiceTest.java](file:///d:/IT/project/src/test/java/com/example/project/ReceiptServiceTest.java) và chạy kiểm thử tự động
