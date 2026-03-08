/**
 * Filename: Category.java
 * Author: Charles Bassani
 * Description: Category DTO and model
 */

//----------------------------------------------------------------------------------------------------
// Package
//----------------------------------------------------------------------------------------------------
package com.charble.backend.model;

//----------------------------------------------------------------------------------------------------
// Imports
//----------------------------------------------------------------------------------------------------
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

//----------------------------------------------------------------------------------------------------
// Class Definition
//----------------------------------------------------------------------------------------------------
@Entity
@Table(name = "categories")
public class Category
{
    //------------------------------------------------------------------------------------------------
    // Constructors
    //------------------------------------------------------------------------------------------------
    public Category() {}

    public Category(String name,
                    String units,
                    Boolean separateByRegion,
                    Boolean separateBySex,
                    Boolean separateByAgeRange,
                    Boolean sortOrder,
                    User foundingUser)
    {
        this.categoryName = name;
        this.units = units;
        this.separateByRegion = separateByRegion;
        this.separateBySex = separateBySex;
        this.separateByAgeRange = separateByAgeRange;
        this.sortOrder = sortOrder;
        this.foundingUser = foundingUser;
    }

    //------------------------------------------------------------------------------------------------
    // Public Methods
    //------------------------------------------------------------------------------------------------
    public void update(String units,
                               Boolean separateByRegion,
                               Boolean separateBySex,
                               Boolean separateByAgeRange,
                               Boolean sortOrder)
    {
        this.units = units;
        this.separateByRegion = separateByRegion;
        this.separateBySex = separateBySex;
        this.separateByAgeRange = separateByAgeRange;
        this.sortOrder = sortOrder;
    }

    //Getters
    public UUID getCategoryId()                    { return categoryId; }
    public String getName()                        { return categoryName; }
    public Boolean isSeparatedByRegion()           { return separateByRegion; }
    public Boolean isSeparatedBySex()              { return separateBySex; }
    public Boolean isSeparatedByAgeRange()         { return separateByAgeRange; }
    public String getUnits()                       { return units; }
    public Boolean getSortOrder()                  { return sortOrder; }
    public User getFoundingUser()                  { return foundingUser; }
    public LocalDateTime getCreatedAt()            { return createdAt; }

    //Setters
    public void setName(String name) { this.categoryName = name; }

    //------------------------------------------------------------------------------------------------
    // Private Variables
    //------------------------------------------------------------------------------------------------
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "category_id")
    private UUID categoryId;

    @Column(name = "name", unique = true, nullable = false)
    private String categoryName;

    @Column(name = "units_of_measurement", nullable = false, length = 20)
    private String units;

    @Column(name = "separate_by_region", nullable = false)
    private Boolean separateByRegion;

    @Column(name = "separate_by_sex", nullable = false)
    private Boolean separateBySex;

    @Column(name = "separate_by_age_range", nullable = false)
    private Boolean separateByAgeRange;

    @Column(name = "sort_order", nullable = false)
    private Boolean sortOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "founding_user_id", nullable = false)
    private User foundingUser;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
};