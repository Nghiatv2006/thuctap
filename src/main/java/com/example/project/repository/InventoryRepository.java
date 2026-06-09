package com.example.project.repository;

import com.example.project.entity.Inventory;
import com.example.project.entity.InventoryId;
import com.example.project.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, InventoryId> {
    
    List<Inventory> findByBranch(Branch branch);
    
    List<Inventory> findByBranchId(Integer branchId);

    boolean existsByProductId(Integer productId);
}
