package com.example.project.service;

import com.example.project.entity.*;
import com.example.project.repository.InventoryRepository;
import com.example.project.repository.ReceiptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final InventoryRepository inventoryRepository;

    @Autowired
    public ReceiptService(ReceiptRepository receiptRepository, InventoryRepository inventoryRepository) {
        this.receiptRepository = receiptRepository;
        this.inventoryRepository = inventoryRepository;
    }

    public List<Receipt> getAllReceipts() {
        return receiptRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Receipt> getReceiptsByType(String type) {
        return receiptRepository.findByType(type);
    }

    public Optional<Receipt> getReceiptById(Integer id) {
        return receiptRepository.findById(id);
    }

    public List<Receipt> getReceiptsByBranch(Integer branchId) {
        return receiptRepository.findBySourceBranchIdOrDestBranchId(branchId, branchId);
    }

    /** Kiểm tra mã phiếu đã tồn tại trong DB chưa. */
    public boolean existsByCode(String code) {
        return receiptRepository.existsByCode(code);
    }

    /** Kiểm tra sản phẩm đã có trong lịch sử phiếu nào chưa (dùng DB query, không load toàn bộ). */
    public boolean existsByProductId(Integer productId) {
        return receiptRepository.existsByDetailsProductId(productId);
    }

    /**
     * Tạo một phiếu giao dịch mới và tự động cập nhật số lượng tồn kho.
     * Sử dụng @Transactional để đảm bảo nếu một chi tiết sản phẩm bị lỗi (ví dụ: thiếu hàng khi xuất)
     * thì toàn bộ quá trình sẽ được ROLLBACK.
     */
    @Transactional(rollbackFor = Exception.class)
    public Receipt createReceipt(Receipt receipt) {
        // 1. Kiểm tra tính hợp lệ cơ bản của phiếu
        if (receipt.getDetails() == null || receipt.getDetails().isEmpty()) {
            throw new RuntimeException("Phiếu kho không thể trống! Vui lòng chọn ít nhất một sản phẩm.");
        }

        // Thiết lập mối quan hệ hai chiều
        for (ReceiptDetail detail : receipt.getDetails()) {
            detail.setReceipt(receipt);
        }

        // Lưu phiếu và chi tiết phiếu trước
        Receipt savedReceipt = receiptRepository.save(receipt);

        // 2. Cập nhật tồn kho theo loại phiếu
        for (ReceiptDetail detail : savedReceipt.getDetails()) {
            Product product = detail.getProduct();
            int qty = detail.getQuantity();

            if ("IMPORT".equals(savedReceipt.getType()) || "ADJUST_IN".equals(savedReceipt.getType())) {
                // NHẬP KHO hoặc CÂN BẰNG TĂNG: Cộng thêm vào chi nhánh nhận (destBranch)
                Branch destBranch = savedReceipt.getDestBranch();
                if (destBranch == null) {
                    throw new RuntimeException("Giao dịch nhập kho hoặc cân bằng tăng bắt buộc phải có chi nhánh nhận!");
                }

                InventoryId invId = new InventoryId(destBranch.getId(), product.getId(), detail.getMfgDate(), detail.getExpDate());
                Inventory inventory = inventoryRepository.findById(invId)
                        .orElse(new Inventory(destBranch, product, detail.getMfgDate(), detail.getExpDate(), 0));

                inventory.setQuantity(inventory.getQuantity() + qty);
                inventory.setLastUpdated(java.time.LocalDateTime.now());
                inventoryRepository.save(inventory);

            } else if ("EXPORT".equals(savedReceipt.getType()) || "ADJUST_OUT".equals(savedReceipt.getType())) {
                // XUẤT KHO hoặc CÂN BẰNG GIẢM: Trừ bớt của chi nhánh xuất (sourceBranch)
                Branch sourceBranch = savedReceipt.getSourceBranch();
                if (sourceBranch == null) {
                    throw new RuntimeException("Giao dịch xuất kho hoặc cân bằng giảm bắt buộc phải có chi nhánh xuất!");
                }

                InventoryId invId = new InventoryId(sourceBranch.getId(), product.getId(), detail.getMfgDate(), detail.getExpDate());
                Inventory inventory = inventoryRepository.findById(invId)
                        .orElseThrow(() -> new RuntimeException("Sản phẩm " + product.getName() + 
                                " với hạn sử dụng " + (detail.getExpDate().equals(java.time.LocalDate.of(1970, 1, 1)) ? "mặc định" : detail.getExpDate().toString()) +
                                " hiện không có trong kho của chi nhánh " + sourceBranch.getName()));

                if (inventory.getQuantity() < qty) {
                    throw new RuntimeException("Không đủ tồn kho! Bạn muốn xuất " + qty + " " + product.getUnit() + 
                            " nhưng " + sourceBranch.getName() + " chỉ còn " + inventory.getQuantity() + " sản phẩm.");
                }

                inventory.setQuantity(inventory.getQuantity() - qty);
                inventory.setLastUpdated(java.time.LocalDateTime.now());
                inventoryRepository.save(inventory);

            } else if ("TRANSFER".equals(savedReceipt.getType())) {
                // ĐIỀU CHUYỂN KHO: Trừ kho xuất, cộng kho nhận
                Branch sourceBranch = savedReceipt.getSourceBranch();
                Branch destBranch = savedReceipt.getDestBranch();

                if (sourceBranch == null || destBranch == null) {
                    throw new RuntimeException("Giao dịch điều chuyển yêu cầu cả chi nhánh xuất và chi nhánh nhận!");
                }

                // A. Trừ kho xuất
                InventoryId srcInvId = new InventoryId(sourceBranch.getId(), product.getId(), detail.getMfgDate(), detail.getExpDate());
                Inventory srcInventory = inventoryRepository.findById(srcInvId)
                        .orElseThrow(() -> new RuntimeException("Sản phẩm " + product.getName() + 
                                " với hạn sử dụng " + (detail.getExpDate().equals(java.time.LocalDate.of(1970, 1, 1)) ? "mặc định" : detail.getExpDate().toString()) +
                                " hiện không có tại chi nhánh xuất " + sourceBranch.getName()));

                if (srcInventory.getQuantity() < qty) {
                    throw new RuntimeException("Không đủ tồn kho để điều chuyển! " + sourceBranch.getName() + 
                            " chỉ còn " + srcInventory.getQuantity() + " sản phẩm " + product.getName());
                }

                srcInventory.setQuantity(srcInventory.getQuantity() - qty);
                srcInventory.setLastUpdated(java.time.LocalDateTime.now());
                inventoryRepository.save(srcInventory);

                // B. Cộng kho nhận
                InventoryId destInvId = new InventoryId(destBranch.getId(), product.getId(), detail.getMfgDate(), detail.getExpDate());
                Inventory destInventory = inventoryRepository.findById(destInvId)
                        .orElse(new Inventory(destBranch, product, detail.getMfgDate(), detail.getExpDate(), 0));

                destInventory.setQuantity(destInventory.getQuantity() + qty);
                destInventory.setLastUpdated(java.time.LocalDateTime.now());
                inventoryRepository.save(destInventory);
            }
        }

        return savedReceipt;
    }
}
