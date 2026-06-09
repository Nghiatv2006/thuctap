package com.example.project.service;

import com.example.project.entity.Branch;
import com.example.project.entity.Inventory;
import com.example.project.entity.InventoryId;
import com.example.project.entity.Product;
import com.example.project.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Autowired
    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public List<Inventory> getAllInventories() {
        return inventoryRepository.findAll();
    }

    public List<Inventory> getInventoriesByBranch(Branch branch) {
        return inventoryRepository.findByBranch(branch);
    }

    public List<Inventory> getInventoriesByBranchId(Integer branchId) {
        return inventoryRepository.findByBranchId(branchId);
    }

    public Optional<Inventory> getInventory(Branch branch, Product product) {
        if (branch == null || product == null) {
            return Optional.empty();
        }
        return inventoryRepository.findById(new InventoryId(branch.getId(), product.getId()));
    }

    public Optional<Inventory> getInventory(Integer branchId, Integer productId) {
        if (branchId == null || productId == null) {
            return Optional.empty();
        }
        return inventoryRepository.findById(new InventoryId(branchId, productId));
    }

    public Inventory saveInventory(Inventory inventory) {
        return inventoryRepository.save(inventory);
    }

    /** Kiểm tra sản phẩm có đang có tồn kho không (dùng DB query, không load toàn bộ). */
    public boolean existsByProductId(Integer productId) {
        return inventoryRepository.existsByProductId(productId);
    }
}
