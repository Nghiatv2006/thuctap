package com.example.project.controller;

import com.example.project.UserSession;
import com.example.project.entity.*;
import com.example.project.service.*;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class MainController {

    // =========================================================================
    // FXML COMPONENTS - SIDEBAR & HEADER
    // =========================================================================
    @FXML private Label profileNameLabel;
    @FXML private Label profileRoleLabel;
    @FXML private Label profileBranchLabel;
    @FXML private Label headerTitleLabel;

    @FXML private Button navDashboardBtn;
    @FXML private Button navProductBtn;
    @FXML private Button navReceiptBtn;
    @FXML private Button navHistoryBtn;
    @FXML private Button navUserBtn;

    // Các Panels trong StackPane
    @FXML private VBox dashboardPanel;
    @FXML private VBox productPanel;
    @FXML private VBox receiptPanel;
    @FXML private VBox historyPanel;
    @FXML private VBox userPanel;

    // =========================================================================
    // FXML COMPONENTS - 1. DASHBOARD / STOCK PANEL
    // =========================================================================
    @FXML private ComboBox<Object> dashBranchFilter; // Có thể chứa String "Tất cả" hoặc Branch
    @FXML private TextField dashSearchField;
    @FXML private TableView<Inventory> inventoryTable;
    @FXML private TableColumn<Inventory, String> colInvBranch;
    @FXML private TableColumn<Inventory, String> colInvProductCode;
    @FXML private TableColumn<Inventory, String> colInvProductName;
    @FXML private TableColumn<Inventory, String> colInvCategory;
    @FXML private TableColumn<Inventory, Integer> colInvQuantity;
    @FXML private TableColumn<Inventory, String> colInvUnit;
    @FXML private TableColumn<Inventory, LocalDate> colInvMfgDate;
    @FXML private TableColumn<Inventory, LocalDate> colInvExpDate;

    // =========================================================================
    // FXML COMPONENTS - 2. PRODUCT CRUD PANEL
    // =========================================================================
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, String> colProdCode;
    @FXML private TableColumn<Product, String> colProdName;
    @FXML private TableColumn<Product, String> colProdCategory;
    @FXML private TableColumn<Product, String> colProdUnit;
    @FXML private TableColumn<Product, BigDecimal> colProdPrice;
    @FXML private TableColumn<Product, LocalDate> colProdMfgDate;
    @FXML private TableColumn<Product, LocalDate> colProdExpDate;

    @FXML private TextField prodSearchField;
    @FXML private VBox prodEditorForm;
    @FXML private TextField txtProdCode;
    @FXML private TextField txtProdName;
    @FXML private TextField txtProdUnit;
    @FXML private TextField txtProdPrice;
    @FXML private ComboBox<Category> cbProdCategory;
    @FXML private Label prodErrorLabel;
    @FXML private Button btnProdAdd;
    @FXML private Button btnProdUpdate;
    @FXML private Button btnProdDelete;

    // =========================================================================
    // FXML COMPONENTS - 3. CREATE RECEIPT PANEL
    // =========================================================================
    @FXML private ComboBox<String> cbReceiptType;
    @FXML private TextField txtReceiptCode;
    @FXML private TextField txtReceiptDesc;
    @FXML private ComboBox<Branch> cbReceiptSrcBranch;
    @FXML private ComboBox<Branch> cbReceiptDestBranch;

    @FXML private ComboBox<Product> cbReceiptProduct;
    @FXML private TextField txtReceiptUnit;
    @FXML private TextField txtReceiptQty;
    @FXML private TextField txtReceiptPrice;

    @FXML private TableView<ReceiptDetail> receiptDraftTable;
    @FXML private TableColumn<ReceiptDetail, String> colDraftCode;
    @FXML private TableColumn<ReceiptDetail, String> colDraftName;
    @FXML private TableColumn<ReceiptDetail, Integer> colDraftQty;
    @FXML private TableColumn<ReceiptDetail, BigDecimal> colDraftPrice;
    @FXML private TableColumn<ReceiptDetail, BigDecimal> colDraftTotal;
    @FXML private TableColumn<ReceiptDetail, LocalDate> colDraftMfgDate;
    @FXML private TableColumn<ReceiptDetail, LocalDate> colDraftExpDate;
    @FXML private Label lblReceiptTotalSum;

    @FXML private CheckBox chkHasExpiry;
    @FXML private VBox mfgDateBox;
    @FXML private VBox expDateBox;
    @FXML private DatePicker dpMfgDate;
    @FXML private DatePicker dpExpDate;

    // Receipt form – riêng cho NSX/HSD theo lô khi lập phiếu
    @FXML private CheckBox chkReceiptHasExpiry;
    @FXML private VBox receiptMfgDateBox;
    @FXML private VBox receiptExpDateBox;
    @FXML private DatePicker dpReceiptMfgDate;
    @FXML private DatePicker dpReceiptExpDate;
    @FXML private Label lblReceiptAvailStock;

    // =========================================================================
    // FXML COMPONENTS - 4. TRANSACTION HISTORY PANEL
    // =========================================================================
    @FXML private ComboBox<String> histTypeFilter;
    @FXML private ComboBox<Object> histSrcFilter;
    @FXML private ComboBox<Object> histDestFilter;
    @FXML private ComboBox<Object> histUserFilter;
    @FXML private DatePicker histStartDatePicker;
    @FXML private DatePicker histEndDatePicker;

    @FXML private TableView<Receipt> historyTable;
    @FXML private TableColumn<Receipt, String> colHistCode;
    @FXML private TableColumn<Receipt, String> colHistType;
    @FXML private TableColumn<Receipt, String> colHistSrc;
    @FXML private TableColumn<Receipt, String> colHistDest;
    @FXML private TableColumn<Receipt, String> colHistUser;
    @FXML private TableColumn<Receipt, String> colHistDate;
    @FXML private TableColumn<Receipt, String> colHistDesc;

    @FXML private TableView<ReceiptDetail> historyDetailTable;
    @FXML private TableColumn<ReceiptDetail, String> colHistDetCode;
    @FXML private TableColumn<ReceiptDetail, String> colHistDetName;
    @FXML private TableColumn<ReceiptDetail, Integer> colHistDetQty;
    @FXML private TableColumn<ReceiptDetail, BigDecimal> colHistDetPrice;
    @FXML private TableColumn<ReceiptDetail, LocalDate> colHistDetMfgDate;
    @FXML private TableColumn<ReceiptDetail, LocalDate> colHistDetExpDate;
    @FXML private javafx.scene.control.TextArea txtHistDesc;

    // =========================================================================
    // FXML COMPONENTS - 5. USER MANAGEMENT PANEL
    // =========================================================================
    @FXML private TextField userSearchField;
    @FXML private ComboBox<Object> userRoleFilter;
    @FXML private ComboBox<Object> userBranchFilter;
    @FXML private ComboBox<Object> userStatusFilter;
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> colUserUsername;
    @FXML private TableColumn<User, String> colUserFullName;
    @FXML private TableColumn<User, String> colUserRole;
    @FXML private TableColumn<User, String> colUserBranch;
    @FXML private TableColumn<User, String> colUserStatus;

    @FXML private VBox userEditorForm;
    @FXML private TextField txtUserUsername;
    @FXML private PasswordField txtUserPassword;
    @FXML private TextField txtUserFullName;
    @FXML private ComboBox<String> cbUserRole;
    @FXML private ComboBox<Branch> cbUserBranch;
    @FXML private ComboBox<String> cbUserStatus;
    @FXML private Label userErrorLabel;
    @FXML private Button btnUserAdd;
    @FXML private Button btnUserUpdate;
    @FXML private Button btnUserToggleLock;
    @FXML private Button btnUserDelete;

    private ObservableList<User> masterUserList = FXCollections.observableArrayList();

    // =========================================================================
    // SPRING SERVICES & DATA LISTS
    // =========================================================================
    private final BranchService branchService;
    private final ProductService productService;
    private final InventoryService inventoryService;
    private final ReceiptService receiptService;
    private final UserService userService;
    private final ApplicationContext applicationContext;

    private User currentUser;
    private int lowStockThreshold = 5;
    private ObservableList<Inventory> masterInventoryList = FXCollections.observableArrayList();
    private ObservableList<Product> masterProductList = FXCollections.observableArrayList();
    private ObservableList<ReceiptDetail> draftDetails = FXCollections.observableArrayList();
    private ObservableList<Receipt> masterHistoryList = FXCollections.observableArrayList();

    @Autowired
    public MainController(BranchService branchService, ProductService productService,
                          InventoryService inventoryService, ReceiptService receiptService,
                          UserService userService,
                          ApplicationContext applicationContext) {
        this.branchService = branchService;
        this.productService = productService;
        this.inventoryService = inventoryService;
        this.receiptService = receiptService;
        this.userService = userService;
        this.applicationContext = applicationContext;
    }

    // =========================================================================
    // INITIALIZATION (Chạy tự động khi load giao diện)
    // =========================================================================
    @FXML
    public void initialize() {
        currentUser = UserSession.getCurrentUser();
        if (currentUser == null) return;

        // 1. Hiển thị thông tin Profile
        profileNameLabel.setText(currentUser.getFullName());
        profileRoleLabel.setText("Vai trò: " + currentUser.getRole());
        if (currentUser.getBranch() != null) {
            profileBranchLabel.setText("Chi nhánh: " + currentUser.getBranch().getName());
            lowStockThreshold = currentUser.getBranch().getLowStockThreshold() != null ? currentUser.getBranch().getLowStockThreshold() : 5;
        } else {
            profileBranchLabel.setText("Chi nhánh: Tất cả");
            lowStockThreshold = 5;
        }

        // 2. Cấu hình các bảng hiển thị
        setupTableColumns();

        // 3. Tải dữ liệu ban đầu
        loadInitialData();

        // 4. Phân quyền người dùng (Role-Based Access Control)
        applyRoleAuthorization();

        // 5. Hiển thị panel mặc định (Dashboard)
        showDashboardPanel(null);

        // 6. Cấu hình mặc định lập phiếu
        txtReceiptPrice.setEditable(false);
        txtReceiptPrice.setStyle("-fx-background-color: #e2e8f0;");
        chkHasExpiry.setSelected(false);
        handleExpiryCheckChange(null);
        chkReceiptHasExpiry.setSelected(false);
        handleReceiptExpiryCheckChange(null);

        // Định dạng dd/MM/yyyy cho các DatePicker
        setupDatePickerFormat(dpMfgDate);
        setupDatePickerFormat(dpExpDate);
        setupDatePickerFormat(dpReceiptMfgDate);
        setupDatePickerFormat(dpReceiptExpDate);
        setupDatePickerFormat(histStartDatePicker);
        setupDatePickerFormat(histEndDatePicker);

        // Mặc định lọc theo ngày hôm nay
        histStartDatePicker.setValue(LocalDate.now());
        histEndDatePicker.setValue(LocalDate.now());
    }

    private void setupTableColumns() {
        // A. Bảng Tồn Kho
        colInvBranch.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBranch().getName()));
        colInvProductCode.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProduct().getCode()));
        colInvProductName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProduct().getName()));
        colInvCategory.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProduct().getCategory().getName()));
        colInvQuantity.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getQuantity()));
        colInvQuantity.setCellFactory(column -> new TableCell<Inventory, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.valueOf(item));
                    if (item <= lowStockThreshold) {
                        setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
        colInvUnit.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProduct().getUnit()));
        colInvMfgDate.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getMfgDate()));
        colInvMfgDate.setCellFactory(column -> new TableCell<Inventory, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatDate(item));
                }
            }
        });
        colInvExpDate.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getExpDate()));
        colInvExpDate.setCellFactory(column -> new TableCell<Inventory, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatDate(item));
                }
            }
        });

        // B. Bảng Sản Phẩm
        colProdCode.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCode()));
        colProdName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colProdCategory.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategory().getName()));
        colProdUnit.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUnit()));
        colProdPrice.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getPrice()));
        colProdPrice.setCellFactory(column -> new TableCell<Product, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatCurrency(item));
                }
            }
        });
        colProdMfgDate.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getMfgDate()));
        colProdMfgDate.setCellFactory(column -> new TableCell<Product, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatDate(item));
            }
        });
        colProdExpDate.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getExpDate()));
        colProdExpDate.setCellFactory(column -> new TableCell<Product, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatDate(item));
            }
        });

        // C. Bảng Chi tiết phiếu nháp (Dự thảo)
        colDraftCode.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProduct().getCode()));
        colDraftName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProduct().getName()));
        colDraftQty.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getQuantity()));
        colDraftPrice.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getPrice()));
        colDraftPrice.setCellFactory(column -> new TableCell<ReceiptDetail, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatCurrency(item));
                }
            }
        });
        colDraftTotal.setCellValueFactory(data -> {
            BigDecimal qty = BigDecimal.valueOf(data.getValue().getQuantity());
            BigDecimal price = data.getValue().getPrice();
            return new SimpleObjectProperty<>(qty.multiply(price));
        });
        colDraftTotal.setCellFactory(column -> new TableCell<ReceiptDetail, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatCurrency(item));
                }
            }
        });
        colDraftMfgDate.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getMfgDate()));
        colDraftMfgDate.setCellFactory(column -> new TableCell<ReceiptDetail, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatDate(item));
                }
            }
        });
        colDraftExpDate.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getExpDate()));
        colDraftExpDate.setCellFactory(column -> new TableCell<ReceiptDetail, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatDate(item));
                }
            }
        });
        receiptDraftTable.setItems(draftDetails);

        // D. Bảng Lịch sử phiếu
        colHistCode.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCode()));
        colHistType.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType()));
        colHistSrc.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getSourceBranch() != null ? data.getValue().getSourceBranch().getName() : "-"));
        colHistDest.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getDestBranch() != null ? data.getValue().getDestBranch().getName() : "-"));
        colHistUser.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getUser() != null ? data.getValue().getUser().getFullName() : "Không rõ"));
        colHistDate.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        colHistDesc.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getDescription() != null ? data.getValue().getDescription() : "-"));

        // E. Bảng Chi tiết lịch sử phiếu
        colHistDetCode.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProduct().getCode()));
        colHistDetName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProduct().getName()));
        colHistDetQty.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getQuantity()));
        colHistDetPrice.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getPrice()));
        colHistDetPrice.setCellFactory(column -> new TableCell<ReceiptDetail, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatCurrency(item));
                }
            }
        });
        colHistDetMfgDate.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getMfgDate()));
        colHistDetMfgDate.setCellFactory(column -> new TableCell<ReceiptDetail, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatDate(item));
                }
            }
        });
        colHistDetExpDate.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getExpDate()));
        colHistDetExpDate.setCellFactory(column -> new TableCell<ReceiptDetail, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatDate(item));
                }
            }
        });

        // F. Bảng Quản Lý Người Dùng
        colUserUsername.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUsername()));
        colUserFullName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFullName()));
        colUserRole.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole()));
        colUserBranch.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getBranch() != null ? data.getValue().getBranch().getName() : "-"));
        colUserStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        userTable.setItems(masterUserList);

        // G. Cột Ghi chú trong Lịch sử - bật wrap text để hiển thị đầy đủ nội dung
        colHistDesc.setCellFactory(column -> {
            TableCell<Receipt, String> cell = new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item);
                        setWrapText(true);
                        setPrefHeight(javafx.scene.control.Control.USE_COMPUTED_SIZE);
                    }
                }
            };
            return cell;
        });
    }

    private void loadInitialData() {
        // Tải danh sách chi nhánh và sản phẩm
        List<Branch> branches = branchService.getAllBranches();
        List<Product> products = productService.getAllProducts();
        List<Category> categories = productService.getAllCategories();

        // Nạp ComboBox Danh mục ở Form Sản Phẩm
        cbProdCategory.setItems(FXCollections.observableArrayList(categories));

        // Nạp ComboBox Sản phẩm ở Form Lập Phiếu
        cbReceiptProduct.setItems(FXCollections.observableArrayList(products));

        // Cấu hình lọc chi nhánh ở Dashboard
        ObservableList<Object> filterOptions = FXCollections.observableArrayList();
        if ("ADMIN".equals(currentUser.getRole())) {
            filterOptions.add("Tất cả chi nhánh");
            filterOptions.addAll(branches);
            dashBranchFilter.setItems(filterOptions);
            dashBranchFilter.getSelectionModel().selectFirst();
        } else {
            // Khóa bộ lọc nếu là Manager hoặc Staff
            filterOptions.add(currentUser.getBranch());
            dashBranchFilter.setItems(filterOptions);
            dashBranchFilter.getSelectionModel().selectFirst();
            dashBranchFilter.setDisable(true);
        }

        // Cấu hình ComboBox Loại phiếu
        cbReceiptType.setItems(FXCollections.observableArrayList("IMPORT", "EXPORT", "TRANSFER"));
        cbReceiptType.getSelectionModel().selectFirst();

        // Cấu hình ComboBox Chi nhánh ở Form Lập Phiếu
        cbReceiptSrcBranch.setItems(FXCollections.observableArrayList(branches));
        cbReceiptDestBranch.setItems(FXCollections.observableArrayList(branches));

        // Cấu hình Bộ lọc bên Lịch sử giao dịch
        ObservableList<Object> histSrcOptions = FXCollections.observableArrayList();
        histSrcOptions.add("Tất cả");
        histSrcOptions.add("Không có");
        histSrcOptions.addAll(branches);
        histSrcFilter.setItems(histSrcOptions);
        histSrcFilter.getSelectionModel().selectFirst();

        ObservableList<Object> histDestOptions = FXCollections.observableArrayList();
        histDestOptions.add("Tất cả");
        histDestOptions.add("Không có");
        histDestOptions.addAll(branches);
        histDestFilter.setItems(histDestOptions);
        histDestFilter.getSelectionModel().selectFirst();

        ObservableList<String> histTypeOptions = FXCollections.observableArrayList(
                "Tất cả", "IMPORT", "EXPORT", "TRANSFER", "ADJUST_IN", "ADJUST_OUT");
        histTypeFilter.setItems(histTypeOptions);
        histTypeFilter.getSelectionModel().selectFirst();

        // Cấu hình Bộ lọc Người lập bên Lịch sử giao dịch
        ObservableList<Object> histUserOptions = FXCollections.observableArrayList();
        histUserOptions.add("Tất cả");
        histUserOptions.addAll(userService.getAllUsers());
        histUserFilter.setItems(histUserOptions);
        histUserFilter.getSelectionModel().selectFirst();

        // Tải dữ liệu tồn kho lên Table
        refreshInventoryList();
        
        // Tải sản phẩm lên Table
        masterProductList.setAll(products);
        productTable.setItems(masterProductList);

        // Cấu hình Form Quản lý Người dùng
        cbUserRole.setItems(FXCollections.observableArrayList("ADMIN", "MANAGER", "STAFF"));
        cbUserBranch.setItems(FXCollections.observableArrayList(branches));
        cbUserStatus.setItems(FXCollections.observableArrayList("ACTIVE", "LOCKED"));

        // Khởi tạo bộ lọc User panel
        ObservableList<Object> roleFilterOptions = FXCollections.observableArrayList();
        roleFilterOptions.add("Tất cả");
        roleFilterOptions.addAll("ADMIN", "MANAGER", "STAFF");
        userRoleFilter.setItems(roleFilterOptions);
        userRoleFilter.getSelectionModel().selectFirst();

        ObservableList<Object> branchFilterOptions = FXCollections.observableArrayList();
        branchFilterOptions.add("Tất cả");
        branchFilterOptions.add("Không có (Admin)");
        branchFilterOptions.addAll(branches);
        userBranchFilter.setItems(branchFilterOptions);
        userBranchFilter.getSelectionModel().selectFirst();

        ObservableList<Object> statusFilterOptions = FXCollections.observableArrayList();
        statusFilterOptions.add("Tất cả");
        statusFilterOptions.addAll("ACTIVE", "LOCKED");
        userStatusFilter.setItems(statusFilterOptions);
        userStatusFilter.getSelectionModel().selectFirst();

        // Nạp danh sách người dùng ban đầu
        masterUserList.setAll(userService.getAllUsers());
    }

    private void applyRoleAuthorization() {
        // Chỉ ADMIN mới được xem Quản lý Thành viên
        if (!"ADMIN".equals(currentUser.getRole())) {
            navUserBtn.setVisible(false);
            navUserBtn.setManaged(false);
        }

        // Nhân viên (STAFF) không được thực hiện CRUD Sản Phẩm
        if ("STAFF".equals(currentUser.getRole())) {
            btnProdAdd.setDisable(true);
            btnProdUpdate.setDisable(true);
            btnProdDelete.setDisable(true);
            prodEditorForm.setDisable(true); // Vô hiệu hóa form editor sản phẩm
        }
    }

    // =========================================================================
    // NAVIGATION PANEL SWITCHING
    // =========================================================================
    private void switchPanel(VBox activePanel, Button activeBtn, String title) {
        if (!checkCurrentUserActive()) return;
        dashboardPanel.setVisible(false);
        productPanel.setVisible(false);
        receiptPanel.setVisible(false);
        historyPanel.setVisible(false);
        userPanel.setVisible(false);

        activePanel.setVisible(true);
        headerTitleLabel.setText(title);

        // Reset style nút sidebar
        navDashboardBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #cbd5e1; -fx-padding: 12px 15px; -fx-background-radius: 5px; -fx-cursor: hand;");
        navProductBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #cbd5e1; -fx-padding: 12px 15px; -fx-background-radius: 5px; -fx-cursor: hand;");
        navReceiptBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #cbd5e1; -fx-padding: 12px 15px; -fx-background-radius: 5px; -fx-cursor: hand;");
        navHistoryBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #cbd5e1; -fx-padding: 12px 15px; -fx-background-radius: 5px; -fx-cursor: hand;");
        navUserBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #cbd5e1; -fx-padding: 12px 15px; -fx-background-radius: 5px; -fx-cursor: hand;");

        // Set active style
        activeBtn.setStyle("-fx-background-color: #1e293b; -fx-text-fill: white; -fx-padding: 12px 15px; -fx-background-radius: 5px; -fx-cursor: hand; -fx-font-weight: bold;");
    }

    @FXML void showDashboardPanel(ActionEvent event) {
        switchPanel(dashboardPanel, navDashboardBtn, "Tồn kho & Tổng quan hệ thống");
        refreshInventoryList();
    }

    @FXML void showProductPanel(ActionEvent event) {
        switchPanel(productPanel, navProductBtn, "Quản lý Danh mục Sản phẩm");
        masterProductList.setAll(productService.getAllProducts());
        cbProdCategory.setItems(FXCollections.observableArrayList(productService.getAllCategories()));
    }

    @FXML void showReceiptPanel(ActionEvent event) {
        switchPanel(receiptPanel, navReceiptBtn, "Lập phiếu Nhập / Xuất / Điều chuyển Kho");
        refreshProductCombobox();
        // Chỉ reset form khi chưa có sản phẩm nào được chọn - giữ nguyên state nếu đang nhập dở
        if (cbReceiptProduct.getValue() == null) {
            handleReceiptTypeChange(null);
        }
    }

    @FXML void showHistoryPanel(ActionEvent event) {
        switchPanel(historyPanel, navHistoryBtn, "Lịch sử Giao dịch Nhập - Xuất - Chuyển");
        refreshHistoryList();
    }

    @FXML void showUserPanel(ActionEvent event) {
        switchPanel(userPanel, navUserBtn, "Quản lý Thành viên");
        masterUserList.setAll(userService.getAllUsers());
    }

    // =========================================================================
    // 1. DASHBOARD PANEL LOGIC
    // =========================================================================
    private void refreshInventoryList() {
        List<Inventory> list;
        Object selection = dashBranchFilter.getSelectionModel().getSelectedItem();

        if (selection instanceof Branch) {
            Branch b = (Branch) selection;
            lowStockThreshold = b.getLowStockThreshold() != null ? b.getLowStockThreshold() : 5;
            list = inventoryService.getInventoriesByBranch(b);
        } else {
            if (currentUser != null && currentUser.getBranch() != null) {
                lowStockThreshold = currentUser.getBranch().getLowStockThreshold() != null ? currentUser.getBranch().getLowStockThreshold() : 5;
            } else {
                lowStockThreshold = 5;
            }
            list = inventoryService.getAllInventories(); // "Tất cả"
        }

        // Sắp xếp tồn kho theo thời gian cập nhật mới nhất (lastUpdated DESC)
        list.sort((a, b) -> {
            LocalDateTime timeA = a.getLastUpdated() != null ? a.getLastUpdated() : LocalDateTime.MIN;
            LocalDateTime timeB = b.getLastUpdated() != null ? b.getLastUpdated() : LocalDateTime.MIN;
            return timeB.compareTo(timeA);
        });

        masterInventoryList.setAll(list);
        applyInventoryFilter();
    }

    @FXML
    void handleDashBranchFilter(ActionEvent event) {
        refreshInventoryList();
    }

    @FXML
    void handleDashSearch() {
        applyInventoryFilter();
    }

    private void applyInventoryFilter() {
        String keyword = dashSearchField.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            inventoryTable.setItems(masterInventoryList);
        } else {
            ObservableList<Inventory> filtered = masterInventoryList.stream()
                    .filter(inv -> inv.getProduct().getName().toLowerCase().contains(keyword) 
                            || inv.getProduct().getCode().toLowerCase().contains(keyword))
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));
            inventoryTable.setItems(filtered);
        }
    }

    // =========================================================================
    // 2. PRODUCT PANEL LOGIC (CRUD)
    // =========================================================================
    @FXML
    void handleProdSearch() {
        String keyword = prodSearchField.getText().trim();
        masterProductList.setAll(productService.searchProducts(keyword));
    }

    @FXML
    void handleSelectProduct(MouseEvent event) {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            txtProdCode.setText(selected.getCode());
            txtProdCode.setEditable(false);
            txtProdCode.setStyle("-fx-background-color: #e2e8f0;");
            txtProdName.setText(selected.getName());
            txtProdUnit.setText(selected.getUnit());
            txtProdPrice.setText(formatCurrency(selected.getPrice()));
            cbProdCategory.getSelectionModel().select(selected.getCategory());
            
            chkHasExpiry.setSelected(selected.getHasExpiry() != null && selected.getHasExpiry());
            if (selected.getHasExpiry() != null && selected.getHasExpiry()) {
                dpMfgDate.setValue(selected.getMfgDate());
                dpExpDate.setValue(selected.getExpDate());
            } else {
                dpMfgDate.setValue(null);
                dpExpDate.setValue(null);
            }
            handleExpiryCheckChange(null);
        }
    }

    @FXML
    void handleProductClearForm(ActionEvent event) {
        txtProdCode.clear();
        txtProdCode.setEditable(true);
        txtProdCode.setStyle("");
        txtProdName.clear();
        txtProdUnit.clear();
        txtProdPrice.clear();
        cbProdCategory.getSelectionModel().clearSelection();
        prodErrorLabel.setVisible(false);
        prodErrorLabel.setManaged(false);
        
        chkHasExpiry.setSelected(false);
        dpMfgDate.setValue(null);
        dpExpDate.setValue(null);
        handleExpiryCheckChange(null);
    }

    @FXML
    void handleProductAdd(ActionEvent event) {
        if (!checkCurrentUserActive()) return;
        if (!validateProductForm()) return;
        
        if (productService.getProductByCode(txtProdCode.getText().trim()).isPresent()) {
            showProductError("Mã SKU này đã tồn tại trên hệ thống!");
            return;
        }

        Product p = new Product();
        p.setCode(txtProdCode.getText().trim().toUpperCase());
        p.setName(txtProdName.getText().trim());
        p.setUnit(txtProdUnit.getText().trim());
        
        String rawPrice = txtProdPrice.getText().trim().replaceAll("[.,\\s]", "");
        p.setPrice(new BigDecimal(rawPrice));
        p.setCategory(cbProdCategory.getValue());
        p.setHasExpiry(chkHasExpiry.isSelected());
        if (chkHasExpiry.isSelected()) {
            p.setMfgDate(getDatePickerValue(dpMfgDate));
            p.setExpDate(getDatePickerValue(dpExpDate));
        } else {
            p.setMfgDate(LocalDate.of(1970, 1, 1));
            p.setExpDate(LocalDate.of(1970, 1, 1));
        }

        productService.saveProduct(p);
        handleProductClearForm(null);
        handleProdSearch();
        refreshProductCombobox();
        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã thêm sản phẩm mới thành công.");
    }

    @FXML
    void handleProductUpdate(ActionEvent event) {
        if (!checkCurrentUserActive()) return;
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một sản phẩm từ danh sách để sửa.");
            return;
        }

        // Chỉ block khi đổi cờ has_expiry (có/không có HSD) của sản phẩm đã có giao dịch/tồn kho.
        // Lý do: đổi cờ này làm thay đổi cấu trúc key tồn kho, dữ liệu cũ sẽ không còn nhất quán.
        // Còn sửa ngày NSX/HSD mặc định thì cho phép vì chúng chỉ là giá trị template gợi ý khi lập phiếu.
        boolean oldHasExpiry = selected.getHasExpiry() != null && selected.getHasExpiry();
        boolean newHasExpiry = chkHasExpiry.isSelected();

        if (oldHasExpiry != newHasExpiry && isProductUsed(selected)) {
            showProductError("Không thể thay đổi cấu hình \"Có hạn sử dụng\" của sản phẩm đã phát sinh giao dịch hoặc tồn kho!");
            return;
        }

        if (!validateProductForm()) return;

        selected.setName(txtProdName.getText().trim());
        selected.setUnit(txtProdUnit.getText().trim());
        
        String rawPrice = txtProdPrice.getText().trim().replaceAll("[.,\\s]", "");
        selected.setPrice(new BigDecimal(rawPrice));
        selected.setCategory(cbProdCategory.getValue());
        selected.setHasExpiry(chkHasExpiry.isSelected());
        if (chkHasExpiry.isSelected()) {
            selected.setMfgDate(getDatePickerValue(dpMfgDate));
            selected.setExpDate(getDatePickerValue(dpExpDate));
        } else {
            selected.setMfgDate(LocalDate.of(1970, 1, 1));
            selected.setExpDate(LocalDate.of(1970, 1, 1));
        }

        productService.saveProduct(selected);
        handleProductClearForm(null);
        handleProdSearch();
        refreshProductCombobox();
        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Cập nhật thông tin sản phẩm thành công.");
    }

    @FXML
    void handleProductDelete(ActionEvent event) {
        if (!checkCurrentUserActive()) return;
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một sản phẩm từ bảng để xóa.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Bạn có chắc chắn muốn xóa sản phẩm " + selected.getName() + " không?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    productService.deleteProduct(selected.getId());
                    handleProductClearForm(null);
                    handleProdSearch();
                    refreshProductCombobox();
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa sản phẩm thành công.");
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa sản phẩm do sản phẩm này đã được lưu trong lịch sử các phiếu giao dịch.");
                }
            }
        });
    }

    private boolean validateProductForm() {
        if (txtProdCode.getText().trim().isEmpty() || txtProdName.getText().trim().isEmpty() 
            || txtProdUnit.getText().trim().isEmpty() || txtProdPrice.getText().trim().isEmpty() 
            || cbProdCategory.getValue() == null) {
            showProductError("Vui lòng điền đầy đủ tất cả các trường thông tin!");
            return false;
        }
        try {
            String rawPrice = txtProdPrice.getText().trim().replaceAll("[.,\\s]", "");
            double price = Double.parseDouble(rawPrice);
            if (price < 0) {
                showProductError("Giá bán sản phẩm không được là số âm!");
                return false;
            }
        } catch (NumberFormatException e) {
            showProductError("Giá sản phẩm phải là một số hợp lệ!");
            return false;
        }
        if (chkHasExpiry.isSelected()) {
            LocalDate mfg = getDatePickerValue(dpMfgDate);
            LocalDate exp = getDatePickerValue(dpExpDate);
            if (mfg == null || exp == null) {
                showProductError("Vui lòng nhập đầy đủ Ngày sản xuất và Hạn sử dụng!");
                return false;
            }
            if (mfg.isAfter(LocalDate.now())) {
                showProductError("Ngày sản xuất không thể lớn hơn ngày hiện tại!");
                return false;
            }
            if (exp.isBefore(mfg)) {
                showProductError("Hạn sử dụng phải sau hoặc bằng Ngày sản xuất!");
                return false;
            }
        }
        prodErrorLabel.setVisible(false);
        prodErrorLabel.setManaged(false);
        return true;
    }

    private boolean isProductUsed(Product product) {
        if (product == null || product.getId() == null) return false;
        // Dùng DB query thay vì load toàn bộ danh sách vào RAM
        return inventoryService.existsByProductId(product.getId())
                || receiptService.existsByProductId(product.getId());
    }

    /**
     * Sinh mã phiếu duy nhất: prefix + UUID ngắn (8 ký tự).
     * Nếu trùng trong DB, thử lại tối đa 5 lần.
     */
    private String generateUniqueReceiptCode(String type) {
        String prefix = type.substring(0, 2);
        for (int i = 0; i < 5; i++) {
            String code = prefix + "-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            if (!receiptService.existsByCode(code)) {
                return code;
            }
        }
        // Fallback: timestamp đầy đủ (sẽ không trùng trong thực tế)
        return prefix + "-" + System.currentTimeMillis();
    }

    private void showProductError(String msg) {
        prodErrorLabel.setText(msg);
        prodErrorLabel.setVisible(true);
        prodErrorLabel.setManaged(true);
    }

    // =========================================================================
    // 3. CREATE TRANSACTION / RECEIPT PANEL LOGIC
    // =========================================================================
    @FXML
    void handleReceiptTypeChange(ActionEvent event) {
        String type = cbReceiptType.getValue();
        if (type == null) return;

        // Sinh mã phiếu unique, check DB tránh trùng
        txtReceiptCode.setText(generateUniqueReceiptCode(type));

        // Xử lý phân quyền chi nhánh tương ứng từng loại phiếu
        if ("IMPORT".equals(type)) {
            cbReceiptSrcBranch.setValue(null);
            cbReceiptSrcBranch.setDisable(true);
            
            cbReceiptDestBranch.setDisable(false);
            if (!"ADMIN".equals(currentUser.getRole())) {
                cbReceiptDestBranch.setValue(currentUser.getBranch());
                cbReceiptDestBranch.setDisable(true); // Khóa chi nhánh nhận của chính họ
            } else {
                cbReceiptDestBranch.getSelectionModel().selectFirst();
            }

        } else if ("EXPORT".equals(type)) {
            cbReceiptDestBranch.setValue(null);
            cbReceiptDestBranch.setDisable(true);
            
            cbReceiptSrcBranch.setDisable(false);
            if (!"ADMIN".equals(currentUser.getRole())) {
                cbReceiptSrcBranch.setValue(currentUser.getBranch());
                cbReceiptSrcBranch.setDisable(true); // Khóa chi nhánh xuất của chính họ
            } else {
                cbReceiptSrcBranch.getSelectionModel().selectFirst();
            }

        } else if ("TRANSFER".equals(type)) {
            cbReceiptSrcBranch.setDisable(false);
            cbReceiptDestBranch.setDisable(false);

            Branch src = null;
            if (!"ADMIN".equals(currentUser.getRole())) {
                src = currentUser.getBranch();
                cbReceiptSrcBranch.setValue(src);
                cbReceiptSrcBranch.setDisable(true); // Khóa chi nhánh xuất (phải đi từ chi nhánh của họ)
            } else {
                cbReceiptSrcBranch.getSelectionModel().selectFirst();
                src = cbReceiptSrcBranch.getValue();
            }

            // Chọn mặc định chi nhánh nhận khác chi nhánh xuất
            ObservableList<Branch> destOptions = cbReceiptDestBranch.getItems();
            if (destOptions != null && src != null) {
                for (Branch b : destOptions) {
                    if (!b.getId().equals(src.getId())) {
                        cbReceiptDestBranch.setValue(b);
                        break;
                    }
                }
            } else {
                cbReceiptDestBranch.getSelectionModel().select(1);
            }
        }
        
        // Reset chi tiết hóa đơn nháp
        draftDetails.clear();
        calculateReceiptTotal();

        // Reset NSX/HSD của lập phiếu
        chkReceiptHasExpiry.setSelected(false);
        dpReceiptMfgDate.setValue(null);
        dpReceiptExpDate.setValue(null);
        handleReceiptExpiryCheckChange(null);
        
        updateAvailableStockDisplay();
    }

    @FXML
    void handleReceiptSrcBranchChange(ActionEvent event) {
        updateAvailableStockDisplay();
    }

    private void updateAvailableStockDisplay() {
        String type = cbReceiptType.getValue();
        Product prod = cbReceiptProduct.getValue();
        Branch srcBranch = cbReceiptSrcBranch.getValue();

        if (prod == null || type == null) {
            lblReceiptAvailStock.setVisible(false);
            lblReceiptAvailStock.setManaged(false);
            return;
        }

        boolean showAvail = ("EXPORT".equals(type) || "TRANSFER".equals(type));
        boolean hasExpiry = prod.getHasExpiry() != null && prod.getHasExpiry();

        if (showAvail && srcBranch != null) {
            // Lấy danh sách tồn kho của sản phẩm này tại chi nhánh xuất còn hàng
            List<Inventory> list = inventoryService.getInventoriesByBranch(srcBranch).stream()
                    .filter(inv -> inv.getProduct().getId().equals(prod.getId()) && inv.getQuantity() > 0)
                    .collect(Collectors.toList());

            if (list.isEmpty()) {
                lblReceiptAvailStock.setText("⚠️ Hết hàng tại chi nhánh này!");
                lblReceiptAvailStock.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px; -fx-font-weight: bold;");
                lblReceiptAvailStock.setVisible(true);
                lblReceiptAvailStock.setManaged(true);
                
                dpReceiptMfgDate.setValue(null);
                dpReceiptExpDate.setValue(null);
            } else {
                StringBuilder sb = new StringBuilder("Lô có sẵn: ");
                for (int i = 0; i < list.size(); i++) {
                    Inventory inv = list.get(i);
                    if (i > 0) sb.append(" | ");
                    if (hasExpiry) {
                        sb.append(formatDate(inv.getMfgDate())).append(" - ").append(formatDate(inv.getExpDate()))
                          .append(" (").append(inv.getQuantity()).append(" ").append(prod.getUnit()).append(")");
                    } else {
                        sb.append("Không HSD (").append(inv.getQuantity()).append(" ").append(prod.getUnit()).append(")");
                    }
                }
                lblReceiptAvailStock.setText(sb.toString());
                lblReceiptAvailStock.setStyle("-fx-text-fill: #0284c7; -fx-font-size: 12px; -fx-font-weight: bold;");
                lblReceiptAvailStock.setVisible(true);
                lblReceiptAvailStock.setManaged(true);

                // Tự động chọn lô đầu tiên để điền ngày (FIFO)
                Inventory firstInv = list.get(0);
                if (hasExpiry) {
                    dpReceiptMfgDate.setValue(firstInv.getMfgDate());
                    dpReceiptExpDate.setValue(firstInv.getExpDate());
                } else {
                    dpReceiptMfgDate.setValue(null);
                    dpReceiptExpDate.setValue(null);
                }
            }
        } else {
            lblReceiptAvailStock.setVisible(false);
            lblReceiptAvailStock.setManaged(false);
            
            // Đối với IMPORT: tự điền NSX/HSD mặc định của sản phẩm
            if ("IMPORT".equals(type)) {
                if (hasExpiry) {
                    dpReceiptMfgDate.setValue(prod.getMfgDate());
                    dpReceiptExpDate.setValue(prod.getExpDate());
                } else {
                    dpReceiptMfgDate.setValue(null);
                    dpReceiptExpDate.setValue(null);
                }
            }
        }
    }

    @FXML
    void handleReceiptProductSelect(ActionEvent event) {
        Product selected = cbReceiptProduct.getValue();
        if (selected != null) {
            txtReceiptUnit.setText(selected.getUnit());
            txtReceiptPrice.setText(formatCurrency(selected.getPrice()));
            txtReceiptQty.setText("1");

            // Tự động kế thừa thông tin NSX & HSD từ chính sản phẩm
            boolean hasExpiry = selected.getHasExpiry() != null && selected.getHasExpiry();
            chkReceiptHasExpiry.setSelected(hasExpiry);
            
            receiptMfgDateBox.setVisible(hasExpiry);
            receiptMfgDateBox.setManaged(hasExpiry);
            receiptExpDateBox.setVisible(hasExpiry);
            receiptExpDateBox.setManaged(hasExpiry);
            
            if (hasExpiry) {
                dpReceiptMfgDate.setValue(selected.getMfgDate());
                dpReceiptExpDate.setValue(selected.getExpDate());
            } else {
                dpReceiptMfgDate.setValue(null);
                dpReceiptExpDate.setValue(null);
            }
            updateAvailableStockDisplay();
        }
    }

    @FXML
    void handleReceiptAddDetail(ActionEvent event) {
        Product prod = cbReceiptProduct.getValue();
        if (prod == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một sản phẩm.");
            return;
        }

        int qty;
        BigDecimal price;
        try {
            qty = Integer.parseInt(txtReceiptQty.getText().trim());
            String rawPrice = txtReceiptPrice.getText().trim().replaceAll("[.,\\s]", "");
            price = new BigDecimal(rawPrice);
            if (qty <= 0 || price.compareTo(BigDecimal.ZERO) < 0) {
                throw new Exception();
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Số lượng phải là số nguyên > 0, và đơn giá phải là số >= 0.");
            return;
        }

        // Kiểm tra chặn nếu sản phẩm bắt buộc có hạn sử dụng nhưng lại bỏ chọn checkbox
        if (prod.getHasExpiry() != null && prod.getHasExpiry() && !chkReceiptHasExpiry.isSelected()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Sản phẩm này là hàng có hạn sử dụng, bắt buộc phải nhập NSX và HSD!");
            return;
        }

        // Lấy NSX/HSD từ ô nhập riêng hoặc mặc định 1970
        LocalDate mfgDate;
        LocalDate expDate;
        if (chkReceiptHasExpiry.isSelected()) {
            LocalDate mfgVal = getDatePickerValue(dpReceiptMfgDate);
            LocalDate expVal = getDatePickerValue(dpReceiptExpDate);
            if (mfgVal == null || expVal == null) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn đầy đủ Ngày sản xuất và Hạn sử dụng!");
                return;
            }
            if (mfgVal.isAfter(LocalDate.now())) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Ngày sản xuất không thể lớn hơn ngày hiện tại!");
                return;
            }
            if (expVal.isBefore(mfgVal)) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Hạn sử dụng phải sau hoặc bằng Ngày sản xuất!");
                return;
            }
            mfgDate = mfgVal;
            expDate = expVal;
        } else {
            mfgDate = LocalDate.of(1970, 1, 1);
            expDate = LocalDate.of(1970, 1, 1);
        }

        // Kiểm tra xem sản phẩm cùng lô hạn sử dụng đã có trong phiếu nháp chưa
        final LocalDate finalMfg = mfgDate;
        final LocalDate finalExp = expDate;

        String receiptType = cbReceiptType.getValue();
        if ("EXPORT".equals(receiptType) || "TRANSFER".equals(receiptType)) {
            Branch srcBranch = cbReceiptSrcBranch.getValue();
            if (srcBranch == null) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn chi nhánh xuất hàng trước!");
                return;
            }

            int currentStock = 0;
            Optional<Inventory> invOpt = inventoryService.getInventoriesByBranch(srcBranch)
                .stream()
                .filter(inv -> inv.getProduct().getId().equals(prod.getId()) &&
                               inv.getMfgDate().equals(finalMfg) &&
                               inv.getExpDate().equals(finalExp))
                .findFirst();
            if (invOpt.isPresent()) {
                currentStock = invOpt.get().getQuantity();
            }

            int draftQty = draftDetails.stream()
                .filter(d -> d.getProduct().equals(prod) &&
                             d.getMfgDate().equals(finalMfg) &&
                             d.getExpDate().equals(finalExp))
                .mapToInt(ReceiptDetail::getQuantity)
                .sum();

            if (draftQty + qty > currentStock) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Không đủ tồn kho khả dụng! " +
                        srcBranch.getName() + " chỉ còn " + currentStock + " sản phẩm " + prod.getName() +
                        " cho lô HSD: " + formatDate(finalExp) + " (Đã thêm trong danh sách nháp: " + draftQty + ").");
                return;
            }
        }

        Optional<ReceiptDetail> existing = draftDetails.stream()
                .filter(d -> d.getProduct().equals(prod) &&
                             d.getMfgDate().equals(finalMfg) &&
                             d.getExpDate().equals(finalExp))
                .findFirst();

        if (existing.isPresent()) {
            existing.get().setQuantity(existing.get().getQuantity() + qty);
            receiptDraftTable.refresh();
        } else {
            ReceiptDetail detail = new ReceiptDetail();
            detail.setProduct(prod);
            detail.setQuantity(qty);
            detail.setPrice(price);
            detail.setMfgDate(mfgDate);
            detail.setExpDate(expDate);
            draftDetails.add(detail);
        }

        calculateReceiptTotal();
    }

    @FXML
    void handleReceiptRemoveDetail(ActionEvent event) {
        ReceiptDetail selected = receiptDraftTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            draftDetails.remove(selected);
            calculateReceiptTotal();
        }
    }

    private void calculateReceiptTotal() {
        BigDecimal sum = BigDecimal.ZERO;
        for (ReceiptDetail d : draftDetails) {
            BigDecimal qty = BigDecimal.valueOf(d.getQuantity());
            sum = sum.add(qty.multiply(d.getPrice()));
        }
        lblReceiptTotalSum.setText("Tổng tiền: " + formatCurrency(sum) + " VND");
    }

    @FXML
    void handleReceiptConfirm(ActionEvent event) {
        if (!checkCurrentUserActive()) return;
        String type = cbReceiptType.getValue();
        String code = txtReceiptCode.getText().trim();
        Branch src = cbReceiptSrcBranch.getValue();
        Branch dest = cbReceiptDestBranch.getValue();

        if (code.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng điền mã số phiếu giao dịch!");
            return;
        }

        // Ràng buộc chi nhánh theo từng loại giao dịch
        if ("IMPORT".equals(type) && dest == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn chi nhánh nhận hàng!");
            return;
        }
        if ("EXPORT".equals(type) && src == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn chi nhánh xuất hàng!");
            return;
        }
        if ("TRANSFER".equals(type)) {
            if (src == null || dest == null) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn đầy đủ chi nhánh xuất và chi nhánh nhận!");
                return;
            }
            if (src.getId().equals(dest.getId())) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chi nhánh xuất và chi nhánh nhận không thể trùng nhau trong giao dịch điều chuyển!");
                return;
            }
        }

        if (draftDetails.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Phiếu của bạn đang trống! Hãy thêm ít nhất một sản phẩm.");
            return;
        }

        Receipt r = new Receipt();
        r.setCode(code);
        r.setType(type);
        r.setSourceBranch(src);
        r.setDestBranch(dest);
        r.setUser(currentUser);
        r.setStatus("COMPLETED");
        // Gắn ghi chú từ ô nhập
        String desc = txtReceiptDesc.getText().trim();
        r.setDescription(desc.isEmpty() ? null : desc);

        for (ReceiptDetail d : draftDetails) {
            r.addDetail(d);
        }

        try {
            // Lưu phiếu xuống DB và cập nhật tồn kho (Sử dụng service có @Transactional)
            receiptService.createReceipt(r);
            
            // Xóa sạch form và báo thành công
            draftDetails.clear();
            calculateReceiptTotal();
            txtReceiptCode.setText(generateUniqueReceiptCode(type));
            
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã lưu phiếu giao dịch và cập nhật tồn kho thành công!");
            
            // Cập nhật lại ComboBox Sản phẩm (và xóa form nhập)
            cbReceiptProduct.getSelectionModel().clearSelection();
            txtReceiptQty.clear();
            txtReceiptPrice.clear();
            txtReceiptUnit.clear();
            txtReceiptDesc.clear();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi Giao Dịch", e.getMessage());
        }
    }

    // =========================================================================
    // 4. TRANSACTION HISTORY PANEL LOGIC
    // =========================================================================
    private void refreshHistoryList() {
        List<Receipt> list;
        if ("ADMIN".equals(currentUser.getRole())) {
            list = receiptService.getAllReceipts();
        } else {
            // Chỉ hiển thị lịch sử liên quan đến chi nhánh của họ
            list = receiptService.getReceiptsByBranch(currentUser.getBranch().getId());
        }
        masterHistoryList.setAll(list);
        applyHistoryFilter();
        historyDetailTable.getItems().clear();
        txtHistDesc.clear();
        txtHistDesc.setPromptText("(Chọn một phiếu ở trên để xem ghi chú)");
    }

    @FXML
    void handleSelectHistoryReceipt(MouseEvent event) {
        Receipt selected = historyTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            historyDetailTable.setItems(FXCollections.observableArrayList(selected.getDetails()));
            // Hiện ghi chú của phiếu vào ô TextArea bên dưới
            String desc = selected.getDescription();
            txtHistDesc.setText(desc != null && !desc.isBlank() ? desc : "");
            txtHistDesc.setPromptText(desc != null && !desc.isBlank() ? "" : "(Phiếu này không có ghi chú)");
        }
    }

    private void applyHistoryFilter() {
        String typeFilter = histTypeFilter.getValue();
        Object srcFilter = histSrcFilter.getValue();
        Object destFilter = histDestFilter.getValue();
        Object userFilterVal = histUserFilter.getValue();
        LocalDate startDate = getDatePickerValue(histStartDatePicker);
        LocalDate endDate = getDatePickerValue(histEndDatePicker);

        List<Receipt> filtered = masterHistoryList.stream().filter(r -> {
            // 1. Loại giao dịch
            if (typeFilter != null && !"Tất cả".equals(typeFilter) && !r.getType().equals(typeFilter)) {
                return false;
            }
            // 2. Kho xuất
            if (srcFilter instanceof Branch) {
                if (r.getSourceBranch() == null || !r.getSourceBranch().getId().equals(((Branch) srcFilter).getId())) {
                    return false;
                }
            } else if ("Không có".equals(srcFilter)) {
                if (r.getSourceBranch() != null) return false;
            }
            // 3. Kho nhận
            if (destFilter instanceof Branch) {
                if (r.getDestBranch() == null || !r.getDestBranch().getId().equals(((Branch) destFilter).getId())) {
                    return false;
                }
            } else if ("Không có".equals(destFilter)) {
                if (r.getDestBranch() != null) return false;
            }
            // 4. Người lập
            if (userFilterVal instanceof User) {
                if (r.getUser() == null || !r.getUser().getId().equals(((User) userFilterVal).getId())) {
                    return false;
                }
            }
            // 5. Ngày tạo
            if (r.getCreatedAt() == null) {
                return false;
            }
            LocalDate createdAtDate = r.getCreatedAt().toLocalDate();
            if (startDate != null && createdAtDate.isBefore(startDate)) {
                return false;
            }
            if (endDate != null && createdAtDate.isAfter(endDate)) {
                return false;
            }
            return true;
        }).collect(Collectors.toList());

        historyTable.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    void handleHistFilter() {
        applyHistoryFilter();
    }

    @FXML
    void handleClearHistFilter(ActionEvent event) {
        histTypeFilter.getSelectionModel().selectFirst();
        histSrcFilter.getSelectionModel().selectFirst();
        histDestFilter.getSelectionModel().selectFirst();
        histUserFilter.getSelectionModel().selectFirst();
        histStartDatePicker.setValue(LocalDate.now());
        histEndDatePicker.setValue(LocalDate.now());
        applyHistoryFilter();
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0";
        java.text.DecimalFormat formatter = (java.text.DecimalFormat) java.text.NumberFormat.getInstance(java.util.Locale.GERMANY);
        formatter.applyPattern("#,##0");
        return formatter.format(amount);
    }

    private String formatDate(LocalDate date) {
        if (date == null || date.equals(LocalDate.of(1970, 1, 1))) {
            return "-";
        }
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    @FXML
    void handleExpiryCheckChange(ActionEvent event) {
        boolean selected = chkHasExpiry.isSelected();
        mfgDateBox.setVisible(selected);
        mfgDateBox.setManaged(selected);
        expDateBox.setVisible(selected);
        expDateBox.setManaged(selected);
    }

    @FXML
    void handleReceiptExpiryCheckChange(ActionEvent event) {
        boolean selected = chkReceiptHasExpiry.isSelected();
        receiptMfgDateBox.setVisible(selected);
        receiptMfgDateBox.setManaged(selected);
        receiptExpDateBox.setVisible(selected);
        receiptExpDateBox.setManaged(selected);
    }

    @FXML
    void showSettingsPopup(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(lowStockThreshold));
        dialog.setTitle("Thiết lập cảnh báo");
        dialog.setHeaderText("Cấu hình ngưỡng cảnh báo tồn kho tối thiểu");
        dialog.setContentText("Nhập mức tồn kho tối thiểu:");
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                int val = Integer.parseInt(result.get().trim());
                if (val < 0) {
                    showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Ngưỡng cảnh báo phải >= 0.");
                    return;
                }
                
                Object selection = dashBranchFilter.getSelectionModel().getSelectedItem();
                Branch targetBranch = null;
                if (selection instanceof Branch) {
                    targetBranch = (Branch) selection;
                } else if (currentUser != null && currentUser.getBranch() != null) {
                    targetBranch = currentUser.getBranch();
                }

                if (targetBranch != null) {
                    targetBranch.setLowStockThreshold(val);
                    branchService.saveBranch(targetBranch); // Save to DB
                    lowStockThreshold = val;
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã cập nhật ngưỡng tồn kho tối thiểu của chi nhánh " + targetBranch.getName() + " thành " + val + ".");
                } else {
                    lowStockThreshold = val;
                    showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Đang xem tất cả chi nhánh. Ngưỡng cảnh báo tạm thời được đặt là " + val + ".");
                }
                inventoryTable.refresh();
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập một số nguyên hợp lệ.");
            }
        }
    }

    // =========================================================================
    // UTILS
    // =========================================================================
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    void handleLogout(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Bạn có chắc chắn muốn đăng xuất không?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Xác nhận đăng xuất");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                UserSession.cleanUserSession();
                try {
                    // Switch back to Login scene
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
                    loader.setControllerFactory(applicationContext::getBean);
                    Parent root = loader.load();
                    
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.setScene(new Scene(root, 480, 620));
                    stage.setTitle("Đăng nhập - Hệ thống Quản lý Kho");
                    stage.setResizable(false);
                    stage.centerOnScreen();
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setupDatePickerFormat(DatePicker datePicker) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        datePicker.setPromptText("dd/MM/yyyy");
        datePicker.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return formatter.format(date);
                }
                return "";
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.trim().isEmpty()) {
                    try {
                        return LocalDate.parse(string, formatter);
                    } catch (java.time.format.DateTimeParseException e) {
                        return null;
                    }
                }
                return null;
            }
        });
    }

    private LocalDate getDatePickerValue(DatePicker dp) {
        if (dp == null) return null;
        try {
            String text = dp.getEditor().getText();
            if (text == null || text.trim().isEmpty()) {
                dp.setValue(null);
            } else {
                LocalDate parsed = dp.getConverter().fromString(text);
                dp.setValue(parsed);
            }
        } catch (Exception e) {
            // Bỏ qua nếu parse lỗi
        }
        return dp.getValue();
    }

    private boolean checkCurrentUserActive() {
        if (currentUser == null) return false;
        
        Optional<User> latestUserOpt = userService.getUserById(currentUser.getId());
        if (!latestUserOpt.isPresent() || "LOCKED".equals(latestUserOpt.get().getStatus())) {
            showAlert(Alert.AlertType.ERROR, "Lỗi bảo mật", "Tài khoản của bạn đã bị khóa hoặc không tồn tại!\nHệ thống sẽ tự động đăng xuất.");
            
            // Ép buộc đăng xuất
            UserSession.cleanUserSession();
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
                loader.setControllerFactory(applicationContext::getBean);
                Parent root = loader.load();
                
                Stage stage = (Stage) headerTitleLabel.getScene().getWindow();
                stage.setScene(new Scene(root, 480, 620));
                stage.setTitle("Đăng nhập - Hệ thống Quản lý Kho");
                stage.setResizable(false);
                stage.centerOnScreen();
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }
        return true;
    }

    @FXML
    void showInventoryAuditDialog(ActionEvent event) {
        if (!checkCurrentUserActive()) return;
        Inventory selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một dòng tồn kho cần cân bằng!");
            return;
        }

        Dialog<javafx.util.Pair<Integer, String>> dialog = new Dialog<>();
        dialog.setTitle("Cân bằng tồn kho");
        dialog.setHeaderText("Điều chỉnh tồn kho sản phẩm: " + selected.getProduct().getName() + "\n" +
                            "Chi nhánh: " + selected.getBranch().getName() + "\n" +
                            "Lô HSD: " + formatDate(selected.getMfgDate()) + " - " + formatDate(selected.getExpDate()));

        ButtonType confirmButtonType = new ButtonType("Xác nhận", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField txtActual = new TextField();
        txtActual.setPromptText("Nhập số lượng đếm thực tế...");
        txtActual.setText(String.valueOf(selected.getQuantity()));

        TextField txtReason = new TextField();
        txtReason.setPromptText("Nhập lý do điều chỉnh...");

        grid.add(new Label("Tồn kho hiện tại trên phần mềm:"), 0, 0);
        grid.add(new Label(String.valueOf(selected.getQuantity())), 1, 0);
        grid.add(new Label("Số lượng thực tế đếm được:"), 0, 1);
        grid.add(txtActual, 1, 1);
        grid.add(new Label("Lý do điều chỉnh / Ghi chú:"), 0, 2);
        grid.add(txtReason, 1, 2);

        dialog.getDialogPane().setContent(grid);

        javafx.application.Platform.runLater(() -> txtActual.requestFocus());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                try {
                    int actual = Integer.parseInt(txtActual.getText().trim());
                    if (actual < 0) return null;
                    return new javafx.util.Pair<>(actual, txtReason.getText().trim());
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        Optional<javafx.util.Pair<Integer, String>> result = dialog.showAndWait();

        if (result.isPresent()) {
            int actual = result.get().getKey();
            String reason = result.get().getValue();

            int system = selected.getQuantity();
            if (actual == system) {
                showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Số lượng đếm thực tế trùng khớp với hệ thống. Không cần cân bằng.");
                return;
            }

            if (reason.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Bạn bắt buộc phải nhập lý do điều chỉnh khi có sự chênh lệch tồn kho!");
                return;
            }

            try {
                Receipt receipt = new Receipt();
                receipt.setUser(currentUser);
                receipt.setStatus("COMPLETED");
                receipt.setDescription("Cân bằng kiểm kho: " + reason);

                ReceiptDetail detail = new ReceiptDetail();
                detail.setProduct(selected.getProduct());
                detail.setMfgDate(selected.getMfgDate());
                detail.setExpDate(selected.getExpDate());

                if (actual < system) {
                    receipt.setType("ADJUST_OUT");
                    receipt.setCode("CB-EX-" + (System.currentTimeMillis() % 1000000));
                    receipt.setSourceBranch(selected.getBranch());
                    receipt.setDestBranch(null);

                    detail.setQuantity(system - actual);
                    detail.setPrice(selected.getProduct().getPrice());
                } else {
                    receipt.setType("ADJUST_IN");
                    receipt.setCode("CB-IM-" + (System.currentTimeMillis() % 1000000));
                    receipt.setSourceBranch(null);
                    receipt.setDestBranch(selected.getBranch());

                    detail.setQuantity(actual - system);
                    detail.setPrice(selected.getProduct().getPrice());
                }

                receipt.addDetail(detail);

                receiptService.createReceipt(receipt);

                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã thực hiện cân bằng tồn kho thành công!\nSố lượng mới: " + actual);
                
                refreshInventoryList();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể thực hiện cân bằng: " + e.getMessage());
            }
        }
    }

    // =========================================================================
    // 5. USER MANAGEMENT PANEL LOGIC (CRUD & LOCK)
    // =========================================================================
    @FXML
    void handleUserSearch() {
        applyUserFilter();
    }

    private void applyUserFilter() {
        String keyword = userSearchField.getText().trim().toLowerCase();
        Object roleVal = userRoleFilter.getSelectionModel().getSelectedItem();
        Object branchVal = userBranchFilter.getSelectionModel().getSelectedItem();
        Object statusVal = userStatusFilter.getSelectionModel().getSelectedItem();

        ObservableList<User> filtered = masterUserList.stream().filter(u -> {
            // 1. Tìm kiếm theo từ khóa
            if (!keyword.isEmpty() &&
                !u.getUsername().toLowerCase().contains(keyword) &&
                !u.getFullName().toLowerCase().contains(keyword)) {
                return false;
            }
            // 2. Lọc theo vai trò
            if (roleVal != null && !"Tất cả".equals(roleVal) &&
                !roleVal.toString().equals(u.getRole())) {
                return false;
            }
            // 3. Lọc theo chi nhánh
            if (branchVal instanceof Branch) {
                if (u.getBranch() == null || !u.getBranch().getId().equals(((Branch) branchVal).getId())) {
                    return false;
                }
            } else if ("Không có (Admin)".equals(branchVal)) {
                if (u.getBranch() != null) return false;
            }
            // 4. Lọc theo trạng thái
            if (statusVal != null && !"Tất cả".equals(statusVal) &&
                !statusVal.toString().equals(u.getStatus())) {
                return false;
            }
            return true;
        }).collect(Collectors.toCollection(FXCollections::observableArrayList));

        userTable.setItems(filtered);
    }

    @FXML
    void handleUserFilter(ActionEvent event) {
        applyUserFilter();
    }

    @FXML
    void handleUserClearFilter(ActionEvent event) {
        userSearchField.clear();
        userRoleFilter.getSelectionModel().selectFirst();
        userBranchFilter.getSelectionModel().selectFirst();
        userStatusFilter.getSelectionModel().selectFirst();
        applyUserFilter();
    }

    @FXML
    void handleSelectUser(MouseEvent event) {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            txtUserUsername.setText(selected.getUsername());
            txtUserPassword.clear(); // Để trống mật khẩu cũ vì lý do bảo mật
            txtUserFullName.setText(selected.getFullName());
            cbUserRole.setValue(selected.getRole());
            cbUserBranch.setValue(selected.getBranch());
            cbUserStatus.setValue(selected.getStatus());
        }
    }

    @FXML
    void handleUserClearForm(ActionEvent event) {
        txtUserUsername.clear();
        txtUserPassword.clear();
        txtUserFullName.clear();
        cbUserRole.getSelectionModel().clearSelection();
        cbUserBranch.getSelectionModel().clearSelection();
        cbUserStatus.getSelectionModel().clearSelection();
        userErrorLabel.setVisible(false);
        userErrorLabel.setManaged(false);
    }

    @FXML
    void handleUserAdd(ActionEvent event) {
        if (!checkCurrentUserActive()) return;
        if (!validateUserForm(true)) return;
        
        String username = txtUserUsername.getText().trim();
        if (userService.getUserByUsername(username).isPresent()) {
            showUserError("Tên đăng nhập này đã tồn tại trên hệ thống!");
            return;
        }

        User u = new User();
        u.setUsername(username);
        u.setPassword(userService.encodePassword(txtUserPassword.getText()));
        u.setFullName(txtUserFullName.getText().trim());
        u.setRole(cbUserRole.getValue());
        u.setBranch("ADMIN".equals(cbUserRole.getValue()) ? null : cbUserBranch.getValue());
        u.setStatus(cbUserStatus.getValue() != null ? cbUserStatus.getValue() : "ACTIVE");

        userService.saveUser(u);
        handleUserClearForm(null);
        
        refreshUserListAndFilters();
        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã thêm thành viên mới thành công.");
    }

    @FXML
    void handleUserUpdate(ActionEvent event) {
        if (!checkCurrentUserActive()) return;
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một thành viên từ bảng để sửa.");
            return;
        }
        if (!validateUserForm(false)) return;

        if (selected.getId().equals(currentUser.getId())) {
            if (!"ADMIN".equals(cbUserRole.getValue())) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Bạn không thể tự hạ quyền ADMIN của chính mình!");
                return;
            }
            if ("LOCKED".equals(cbUserStatus.getValue())) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Bạn không thể tự khóa tài khoản của chính mình!");
                return;
            }
        }

        String newUsername = txtUserUsername.getText().trim();
        if (!selected.getUsername().equals(newUsername)) {
            if (userService.getUserByUsername(newUsername).isPresent()) {
                showUserError("Tên đăng nhập này đã tồn tại trên hệ thống!");
                return;
            }
            selected.setUsername(newUsername);
        }

        selected.setFullName(txtUserFullName.getText().trim());
        selected.setRole(cbUserRole.getValue());
        selected.setBranch("ADMIN".equals(cbUserRole.getValue()) ? null : cbUserBranch.getValue());
        selected.setStatus(cbUserStatus.getValue());

        String newPassword = txtUserPassword.getText();
        if (newPassword != null && !newPassword.isEmpty()) {
            selected.setPassword(userService.encodePassword(newPassword));
        }

        userService.saveUser(selected);

        if (selected.getId().equals(currentUser.getId())) {
            currentUser = selected;
            UserSession.setCurrentUser(selected);
            profileNameLabel.setText(selected.getFullName());
            profileRoleLabel.setText("Vai trò: " + selected.getRole());
            if (selected.getBranch() != null) {
                profileBranchLabel.setText("Chi nhánh: " + selected.getBranch().getName());
            } else {
                profileBranchLabel.setText("Chi nhánh: Tất cả");
            }
        }

        handleUserClearForm(null);
        
        refreshUserListAndFilters();
        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Cập nhật thông tin thành viên thành công.");
    }

    @FXML
    void handleUserToggleLock(ActionEvent event) {
        if (!checkCurrentUserActive()) return;
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một thành viên từ bảng để Khóa/Mở khóa.");
            return;
        }
        
        if (selected.getId().equals(currentUser.getId())) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Bạn không thể tự khóa tài khoản của chính mình!");
            return;
        }

        String newStatus = "ACTIVE".equals(selected.getStatus()) ? "LOCKED" : "ACTIVE";
        selected.setStatus(newStatus);
        userService.saveUser(selected);
        
        refreshUserListAndFilters();
        showAlert(Alert.AlertType.INFORMATION, "Thành công", 
                "Đã " + ("LOCKED".equals(newStatus) ? "khóa" : "mở khóa") + " tài khoản " + selected.getUsername() + " thành công.");
    }

    @FXML
    void handleUserDelete(ActionEvent event) {
        if (!checkCurrentUserActive()) return;
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một thành viên từ bảng để xóa.");
            return;
        }

        if (selected.getId().equals(currentUser.getId())) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Bạn không thể tự xóa tài khoản của chính mình!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, 
                "Bạn có chắc chắn muốn xóa thành viên " + selected.getFullName() + " không?", 
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    userService.deleteUser(selected.getId());
                    handleUserClearForm(null);
                    refreshUserListAndFilters();
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa thành viên thành công.");
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", 
                            "Không thể xóa thành viên này do đã có giao dịch kho liên quan đến tài khoản này.");
                }
            }
        });
    }

    private boolean validateUserForm(boolean isNewUser) {
        if (txtUserUsername.getText().trim().isEmpty() || txtUserFullName.getText().trim().isEmpty() 
            || cbUserRole.getValue() == null || cbUserStatus.getValue() == null) {
            showUserError("Vui lòng điền đầy đủ các thông tin bắt buộc!");
            return false;
        }
        
        if (isNewUser && txtUserPassword.getText().isEmpty()) {
            showUserError("Vui lòng nhập mật khẩu cho tài khoản mới!");
            return false;
        }

        if (!"ADMIN".equals(cbUserRole.getValue()) && cbUserBranch.getValue() == null) {
            showUserError("Nhân viên/Quản lý bắt buộc phải chọn chi nhánh làm việc!");
            return false;
        }

        userErrorLabel.setVisible(false);
        userErrorLabel.setManaged(false);
        return true;
    }

    private void showUserError(String msg) {
        userErrorLabel.setText(msg);
        userErrorLabel.setVisible(true);
        userErrorLabel.setManaged(true);
    }

    private void refreshUserListAndFilters() {
        masterUserList.setAll(userService.getAllUsers());
        applyUserFilter();
        
        ObservableList<Object> histUserOptions = FXCollections.observableArrayList();
        histUserOptions.add("Tất cả");
        histUserOptions.addAll(userService.getAllUsers());
        histUserFilter.setItems(histUserOptions);
        histUserFilter.getSelectionModel().selectFirst();
    }

    private void refreshProductCombobox() {
        List<Product> products = productService.getAllProducts();
        cbReceiptProduct.setItems(FXCollections.observableArrayList(products));
    }
}
