package com.example.project.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class InventoryId implements Serializable {
    
    private Integer branch;
    private Integer product;
    private LocalDate mfgDate;
    private LocalDate expDate;

    public InventoryId() {
    }

    public InventoryId(Integer branch, Integer product) {
        this.branch = branch;
        this.product = product;
        this.mfgDate = LocalDate.of(1970, 1, 1);
        this.expDate = LocalDate.of(1970, 1, 1);
    }

    public InventoryId(Integer branch, Integer product, LocalDate mfgDate, LocalDate expDate) {
        this.branch = branch;
        this.product = product;
        this.mfgDate = mfgDate;
        this.expDate = expDate;
    }

    public Integer getBranch() {
        return branch;
    }

    public void setBranch(Integer branch) {
        this.branch = branch;
    }

    public Integer getProduct() {
        return product;
    }

    public void setProduct(Integer product) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InventoryId that = (InventoryId) o;
        return Objects.equals(branch, that.branch) &&
                Objects.equals(product, that.product) &&
                Objects.equals(mfgDate, that.mfgDate) &&
                Objects.equals(expDate, that.expDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(branch, product, mfgDate, expDate);
    }
}
