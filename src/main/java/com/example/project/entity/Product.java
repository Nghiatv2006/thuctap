package com.example.project.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 50)
    private String unit;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "has_expiry", nullable = false)
    private Boolean hasExpiry = false;

    @Column(name = "mfg_date", nullable = false)
    private LocalDate mfgDate = LocalDate.of(1970, 1, 1);

    @Column(name = "exp_date", nullable = false)
    private LocalDate expDate = LocalDate.of(1970, 1, 1);

    public Product() {
    }

    public Product(Integer id, String code, String name, String unit, BigDecimal price, Category category) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.unit = unit;
        this.price = price;
        this.category = category;
        this.hasExpiry = false;
        this.mfgDate = LocalDate.of(1970, 1, 1);
        this.expDate = LocalDate.of(1970, 1, 1);
    }

    public Product(Integer id, String code, String name, String unit, BigDecimal price, Category category, Boolean hasExpiry, LocalDate mfgDate, LocalDate expDate) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.unit = unit;
        this.price = price;
        this.category = category;
        this.hasExpiry = hasExpiry;
        this.mfgDate = mfgDate;
        this.expDate = expDate;
    }

    public Boolean getHasExpiry() {
        return hasExpiry;
    }

    public void setHasExpiry(Boolean hasExpiry) {
        this.hasExpiry = hasExpiry;
    }

    public LocalDate getMfgDate() {
        return mfgDate;
    }

    public void setMfgDate(LocalDate mfgDate) {
        this.mfgDate = mfgDate;
    }

    public LocalDate getExpDate() {
        return expDate;
    }

    public void setExpDate(LocalDate expDate) {
        this.expDate = expDate;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name + " (" + code + ")";
    }
}
