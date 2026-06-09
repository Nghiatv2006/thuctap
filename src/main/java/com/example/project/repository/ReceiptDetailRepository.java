package com.example.project.repository;

import com.example.project.entity.ReceiptDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReceiptDetailRepository extends JpaRepository<ReceiptDetail, Integer> {
    
    List<ReceiptDetail> findByReceiptId(Integer receiptId);
}
