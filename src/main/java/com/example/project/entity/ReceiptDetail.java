package com.example.project.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "receipt_details")
public class ReceiptDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_id", nullable = false)
    private Receipt receipt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "mfg_date", nullable = false)
    private LocalDate mfgDate = LocalDate.of(1970, 1, 1);

    @Column(name = "exp_date", nullable = false)
    private LocalDate expDate = LocalDate.of(1970, 1, 1);

    public ReceiptDetail() {
    }

    public ReceiptDetail(Integer id, Receipt receipt, Product product, Integer quantity, BigDecimal price) {
        this.id = id;
        this.receipt = receipt;
        this.product = product;
        this.quantity = quantity;
        this.price = price;
        this.mfgDate = LocalDate.of(1970, 1, 1);
        this.expDate = LocalDate.of(1970, 1, 1);
    }

    public ReceiptDetail(Integer id, Receipt receipt, Product product, Integer quantity, BigDecimal price, LocalDate mfgDate, LocalDate expDate) {
        this.id = id;
        this.receipt = receipt;
        this.product = product;
        this.quantity = quantity;
        this.price = price;
        this.mfgDate = mfgDate;
        this.expDate = expDate;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Receipt getReceipt() {
        return receipt;
    }

    public void setReceipt(Receipt receipt) {
        this.receipt = receipt;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReceiptDetail that = (ReceiptDetail) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ReceiptDetail{" +
                "product=" + product.getName() +
                ", quantity=" + quantity +
                ", price=" + price +
                ", mfgDate=" + mfgDate +
                ", expDate=" + expDate +
                '}';
    }
}
