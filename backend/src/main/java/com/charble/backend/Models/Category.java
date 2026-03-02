package com.charble.backend.Models;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "categories")
public class Category
{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "category_id")
    private UUID m_categoryId;

    @Column(name = "name", unique = true, nullable = false, length = 255)
    private String m_categoryName;

    @Column(name = "units_of_measurement", unique = true, nullable = false, length = 20)
    private String m_units;

    @Column(name = "sort_order", nullable = false)
    private Boolean m_sortOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Column(name = "founding_user")
    private User m_foundingUser;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime m_createdAt;

    //Getters
    public UUID categoryId() { return m_categoryId; }
    public String units() { return m_units; }
    public Boolean sortOrder() { return m_sortOrder; }
    public User foundingUser() { return m_foundingUser; }
    public LocalDateTime createdAt() { return m_createdAt; }
}
