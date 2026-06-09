package com.example.project.repository;

import com.example.project.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Integer> {
    
    List<Receipt> findByType(String type);
    
    List<Receipt> findBySourceBranchIdOrDestBranchId(Integer sourceBranchId, Integer destBranchId);
    
    List<Receipt> findAllByOrderByCreatedAtDesc();

    boolean existsByCode(String code);

    boolean existsByDetailsProductId(Integer productId);
}
