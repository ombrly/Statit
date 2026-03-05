package com.charble.backend.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "categories")
public class Category
{
    public Category(String name, String units, Boolean sortOrder, User foundingUser)
    {
        this.categoryName = name;
        this.units = units;
        this.sortOrder = sortOrder;
        this.foundingUser = foundingUser;
    }

    public Category() {}

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "category_id")
    private UUID categoryId;

    @Column(name = "name", unique = true, nullable = false, length = 255)
    private String categoryName;

    @Column(name = "units_of_measurement", nullable = false, length = 20)
    private String units;

    @Column(name = "sort_order", nullable = false)
    private Boolean sortOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "founding_user_id", nullable = false)
    private User foundingUser;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    //Getters
    public UUID categoryId() { return categoryId; }
    public String name() { return categoryName; }
    public String units() { return units; }
    public Boolean sortOrder() { return sortOrder; }
    public User foundingUser() { return foundingUser; }
    public LocalDateTime createdAt() { return createdAt; }
}
