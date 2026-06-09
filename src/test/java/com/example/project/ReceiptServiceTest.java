package com.example.project;

import com.example.project.entity.*;
import com.example.project.repository.InventoryRepository;
import com.example.project.repository.ReceiptRepository;
import com.example.project.service.ReceiptService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReceiptServiceTest {

    @Mock
    private ReceiptRepository receiptRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private ReceiptService receiptService;

    private Branch hanoiBranch;
    private Branch hcmBranch;
    private Product iphone;
    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        hanoiBranch = new Branch(1, "Chi nhánh Hà Nội", "123 Cầu Giấy", "024.333.4444");
        hcmBranch = new Branch(2, "Chi nhánh TP.HCM", "456 Quận 1", "028.555.6666");

        Category category = new Category(1, "Điện thoại");
        iphone = new Product(1, "IPHONE15", "iPhone 15 Pro", "Cái", BigDecimal.valueOf(25000000.0), category);

        testUser = new User(1, "staff_hn", "pass", "Nhân viên HN", "STAFF", hanoiBranch, "ACTIVE");
    }

    @Test
    void testCreateImportReceipt_Success() {
        // Arrange: Tạo phiếu nhập kho nháp
        Receipt receipt = new Receipt(1, "PN-9999", "IMPORT", null, hanoiBranch, testUser, "COMPLETED");
        ReceiptDetail detail = new ReceiptDetail(1, receipt, iphone, 10, BigDecimal.valueOf(24000000.0));
        receipt.addDetail(detail);

        // Giả lập DB trả về tồn kho rỗng (chưa từng nhập sản phẩm này)
        InventoryId invId = new InventoryId(hanoiBranch.getId(), iphone.getId());
        when(inventoryRepository.findById(invId)).thenReturn(Optional.empty());
        when(receiptRepository.save(any(Receipt.class))).thenReturn(receipt);

        // Act
        Receipt result = receiptService.createReceipt(receipt);

        // Assert
        assertNotNull(result);
        assertEquals("PN-9999", result.getCode());
        
        // Kiểm tra xem hệ thống đã lưu tồn kho mới với số lượng là 10 chưa
        verify(inventoryRepository, times(1)).save(argThat(inv -> 
            inv.getBranch().equals(hanoiBranch) && 
            inv.getProduct().equals(iphone) && 
            inv.getQuantity() == 10
        ));
    }

    @Test
    void testCreateExportReceipt_Success() {
        // Arrange: Tạo phiếu xuất kho nháp (Xuất 5 cái)
        Receipt receipt = new Receipt(2, "PX-9999", "EXPORT", hanoiBranch, null, testUser, "COMPLETED");
        ReceiptDetail detail = new ReceiptDetail(2, receipt, iphone, 5, BigDecimal.valueOf(25000000.0));
        receipt.addDetail(detail);

        // Giả lập trong kho đang có 12 cái
        Inventory inventory = new Inventory(hanoiBranch, iphone, 12);
        InventoryId invId = new InventoryId(hanoiBranch.getId(), iphone.getId());
        when(inventoryRepository.findById(invId)).thenReturn(Optional.of(inventory));
        when(receiptRepository.save(any(Receipt.class))).thenReturn(receipt);

        // Act
        Receipt result = receiptService.createReceipt(receipt);

        // Assert
        assertNotNull(result);
        // Tồn kho sau xuất phải giảm đi 5 (còn 7 cái)
        verify(inventoryRepository, times(1)).save(argThat(inv -> inv.getQuantity() == 7));
    }

    @Test
    void testCreateExportReceipt_InsufficientStock_ThrowsException() {
        // Arrange: Xuất 20 cái
        Receipt receipt = new Receipt(3, "PX-9998", "EXPORT", hanoiBranch, null, testUser, "COMPLETED");
        ReceiptDetail detail = new ReceiptDetail(3, receipt, iphone, 20, BigDecimal.valueOf(25000000.0));
        receipt.addDetail(detail);

        // Giả lập trong kho chỉ còn 12 cái
        Inventory inventory = new Inventory(hanoiBranch, iphone, 12);
        InventoryId invId = new InventoryId(hanoiBranch.getId(), iphone.getId());
        when(inventoryRepository.findById(invId)).thenReturn(Optional.of(inventory));
        when(receiptRepository.save(any(Receipt.class))).thenReturn(receipt);

        // Act & Assert: Hệ thống phải quăng ngoại lệ do không đủ tồn kho
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            receiptService.createReceipt(receipt);
        });

        assertTrue(exception.getMessage().contains("Không đủ tồn kho"));
        // Đảm bảo không có lệnh lưu tồn kho nào được thực thi
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void testCreateTransferReceipt_Success() {
        // Arrange: Điều chuyển 8 cái từ Hà Nội -> TP.HCM
        Receipt receipt = new Receipt(4, "DC-9999", "TRANSFER", hanoiBranch, hcmBranch, testUser, "COMPLETED");
        ReceiptDetail detail = new ReceiptDetail(4, receipt, iphone, 8, BigDecimal.valueOf(25000000.0));
        receipt.addDetail(detail);

        // Giả lập kho xuất (Hà Nội) có 10 cái, kho nhận (TP.HCM) có 2 cái
        Inventory srcInv = new Inventory(hanoiBranch, iphone, 10);
        Inventory destInv = new Inventory(hcmBranch, iphone, 2);

        InventoryId srcId = new InventoryId(hanoiBranch.getId(), iphone.getId());
        InventoryId destId = new InventoryId(hcmBranch.getId(), iphone.getId());

        when(inventoryRepository.findById(srcId)).thenReturn(Optional.of(srcInv));
        when(inventoryRepository.findById(destId)).thenReturn(Optional.of(destInv));
        when(receiptRepository.save(any(Receipt.class))).thenReturn(receipt);

        // Act
        Receipt result = receiptService.createReceipt(receipt);

        // Assert
        assertNotNull(result);
        
        // Kiểm tra kho Hà Nội giảm còn 2 (10 - 8 = 2)
        verify(inventoryRepository, times(1)).save(argThat(inv -> 
            inv.getBranch().equals(hanoiBranch) && inv.getQuantity() == 2));
            
        // Kiểm tra kho TP.HCM tăng lên 10 (2 + 8 = 10)
        verify(inventoryRepository, times(1)).save(argThat(inv -> 
            inv.getBranch().equals(hcmBranch) && inv.getQuantity() == 10));
    }

    @Test
    void testCreateImportReceipt_WithExpiryDate_Success() {
        // Arrange: Tạo phiếu nhập kho nháp với hạn sử dụng cụ thể
        Receipt receipt = new Receipt(5, "PN-1111", "IMPORT", null, hanoiBranch, testUser, "COMPLETED");
        java.time.LocalDate mfg = java.time.LocalDate.of(2026, 6, 1);
        java.time.LocalDate exp = java.time.LocalDate.of(2027, 6, 1);
        ReceiptDetail detail = new ReceiptDetail(5, receipt, iphone, 15, BigDecimal.valueOf(24000000.0), mfg, exp);
        receipt.addDetail(detail);

        InventoryId invId = new InventoryId(hanoiBranch.getId(), iphone.getId(), mfg, exp);
        when(inventoryRepository.findById(invId)).thenReturn(Optional.empty());
        when(receiptRepository.save(any(Receipt.class))).thenReturn(receipt);

        // Act
        Receipt result = receiptService.createReceipt(receipt);

        // Assert
        assertNotNull(result);
        verify(inventoryRepository, times(1)).save(argThat(inv -> 
            inv.getBranch().equals(hanoiBranch) && 
            inv.getProduct().equals(iphone) && 
            inv.getMfgDate().equals(mfg) &&
            inv.getExpDate().equals(exp) &&
            inv.getQuantity() == 15
        ));
    }
}
