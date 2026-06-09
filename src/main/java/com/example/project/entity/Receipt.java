package com.example.project.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "receipts")
public class Receipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 20)
    private String type; // IMPORT, EXPORT, TRANSFER

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "source_branch_id")
    private Branch sourceBranch;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "dest_branch_id")
    private Branch destBranch;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false, length = 20)
    private String status = "COMPLETED"; // DRAFT, COMPLETED, CANCELLED

    @Column(name = "description", length = 255)
    private String description;

    @OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ReceiptDetail> details = new ArrayList<>();

    public Receipt() {
    }

    public Receipt(Integer id, String code, String type, Branch sourceBranch, Branch destBranch, User user, String status) {
        this.id = id;
        this.code = code;
        this.type = type;
        this.sourceBranch = sourceBranch;
        this.destBranch = destBranch;
        this.user = user;
        this.status = status;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Branch getSourceBranch() {
        return sourceBranch;
    }

    public void setSourceBranch(Branch sourceBranch) {
        this.sourceBranch = sourceBranch;
    }

    public Branch getDestBranch() {
        return destBranch;
    }

    public void setDestBranch(Branch destBranch) {
        this.destBranch = destBranch;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ReceiptDetail> getDetails() {
        return details;
    }

    public void setDetails(List<ReceiptDetail> details) {
        this.details = details;
    }

    public void addDetail(ReceiptDetail detail) {
        details.add(detail);
        detail.setReceipt(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Receipt receipt = (Receipt) o;
        return Objects.equals(id, receipt.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Receipt{" +
                "code='" + code + '\'' +
                ", type='" + type + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
