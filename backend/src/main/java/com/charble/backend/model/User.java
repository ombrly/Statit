/**
 * Filename: User.java
 * Author: Charles Bassani
 * Description: User DTO and model, utilizing JSONB for dynamic demographics.
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
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

//----------------------------------------------------------------------------------------------------
// Class Definition
//----------------------------------------------------------------------------------------------------
@Entity
@Table(name = "users")
public class User
{
    //------------------------------------------------------------------------------------------------
    // Constructors
    //------------------------------------------------------------------------------------------------
    public User() {}

    public User(String username,
                String email,
                String passwordHash,
                LocalDateTime birthday,
                Map<String, String> demographics)
    {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.birthday = birthday;
        this.demographics = demographics != null ? demographics : new HashMap<>();
    }

    //------------------------------------------------------------------------------------------------
    // Public Functions
    //------------------------------------------------------------------------------------------------
    public void update(String username,
                       String email,
                       String passwordHash,
                       LocalDateTime birthday,
                       Map<String, String> demographics)
    {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.birthday = birthday;
        this.demographics = demographics != null ? demographics : new HashMap<>();
    }

    //Age calculations
    public Integer getAgeMonths()
    {
        if (birthday == null) return 0;
        return (int)ChronoUnit.MONTHS.between(birthday, LocalDateTime.now());
    }
    public Integer getAgeYears()
    {
        if (birthday == null) return 0;
        return (int)ChronoUnit.YEARS.between(birthday, LocalDateTime.now());
    }

    //Getters
    public UUID getUserId()                      { return userId; }
    public String getUsername()                  { return username; }
    public String getEmail()                     { return email; }
    public String getPasswordHash()              { return passwordHash; }
    public LocalDateTime getCreatedAt()          { return createdAt; }
    public LocalDateTime getBirthday()           { return birthday; }
    public Map<String, String> getDemographics() { return demographics; }

    //Setters
    public void setUsername(String username)                        { this.username = username; }
    public void setEmail(String email)                              { this.email = email; }
    public void setPasswordHash(String passwordHash)                { this.passwordHash = passwordHash; }
    public void setBirthday(LocalDateTime birthday)                 { this.birthday = birthday; }
    public void setDemographics(Map<String, String> demographics)   { this.demographics = demographics; }

    //------------------------------------------------------------------------------------------------
    // Private Variables
    //------------------------------------------------------------------------------------------------
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "birthday")
    private LocalDateTime birthday;

    // This is the Postgres JSONB magic
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "demographics", columnDefinition = "jsonb")
    private Map<String, String> demographics = new HashMap<>();
};