package com.example.project.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "inventories")
@IdClass(InventoryId.class)
public class Inventory {

    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id")
    private Product product;

    @Id
    @Column(name = "mfg_date", nullable = false)
    private LocalDate mfgDate = LocalDate.of(1970, 1, 1);

    @Id
    @Column(name = "exp_date", nullable = false)
    private LocalDate expDate = LocalDate.of(1970, 1, 1);

    @Column(nullable = false)
    private Integer quantity = 0;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated = LocalDateTime.now();

    public Inventory() {
    }

    public Inventory(Branch branch, Product product, Integer quantity) {
        this.branch = branch;
        this.product = product;
        this.quantity = quantity;
        this.mfgDate = LocalDate.of(1970, 1, 1);
        this.expDate = LocalDate.of(1970, 1, 1);
        this.lastUpdated = LocalDateTime.now();
    }

    public Inventory(Branch branch, Product product, LocalDate mfgDate, LocalDate expDate, Integer quantity) {
        this.branch = branch;
        this.product = product;
        this.mfgDate = mfgDate;
        this.expDate = expDate;
        this.quantity = quantity;
        this.lastUpdated = LocalDateTime.now();
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Inventory inventory = (Inventory) o;
        return Objects.equals(branch, inventory.branch) &&
                Objects.equals(product, inventory.product) &&
                Objects.equals(mfgDate, inventory.mfgDate) &&
                Objects.equals(expDate, inventory.expDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(branch, product, mfgDate, expDate);
    }

    @Override
    public String toString() {
        return "Inventory{" +
                "branch=" + branch.getName() +
                ", product=" + product.getName() +
                ", quantity=" + quantity +
                ", mfgDate=" + mfgDate +
                ", expDate=" + expDate +
                '}';
    }
}
