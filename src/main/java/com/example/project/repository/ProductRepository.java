package com.example.project.repository;

import com.example.project.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    
    Optional<Product> findByCode(String code);
    
    List<Product> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(String name, String code);
}
