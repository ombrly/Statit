/**
 * Filename: User.java
 * Author: Charles Bassani
 * Description: User DTO and model
 */

//----------------------------------------------------------------------------------------------------
// Package
//----------------------------------------------------------------------------------------------------
package com.charble.backend.model;

//----------------------------------------------------------------------------------------------------
// Imports
//----------------------------------------------------------------------------------------------------
import com.charble.backend.model.enums.*;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import java.time.temporal.ChronoUnit;
import org.hibernate.annotations.CreationTimestamp;

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
                Integer ageMonths,
                Region region,
                Sex sex)
    {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.birthday = birthday;
        this.ageMonths = ageMonths;
        this.region = region;
        this.sex = sex;
    }

    //------------------------------------------------------------------------------------------------
    // Public Functions
    //------------------------------------------------------------------------------------------------
    public void update(String username,
                       String email,
                       String passwordHash,
                       LocalDateTime birthday,
                       Integer ageMonths,
                       Region region,
                       Sex sex)
    {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.birthday = birthday;
        this.ageMonths = ageMonths;
        this.region = region;
        this.sex = sex;
    }

    //Getters
    public UUID getUserId()                { return userId; }
    public String getUsername()            { return username; }
    public String getEmail()               { return email; }
    public String getPasswordHash()        { return passwordHash; }
    public LocalDateTime getCreatedAt()    { return createdAt; }
    public LocalDateTime getBirthday()     { return birthday; }
    public Integer getAgeMonths()          { return (int)ChronoUnit.MONTHS.between(birthday, LocalDateTime.now()); }
    public Integer getAgeYears()           { return (int)ChronoUnit.YEARS.between(birthday, LocalDateTime.now()); }
    public Region getRegion()              { return region; }
    public Sex getSex()                    { return sex; }

    //Setters
    public void setUsername(String username)            { this.username = username; }
    public void setEmail(String email)                  { this.email = email; }
    public void setPasswordHash(String passwordHash)    { this.passwordHash = passwordHash; }
    public void setBirthday(LocalDateTime birthday)     { this.birthday = birthday; }
    public void setRegion(Region region)                { this.region = region; }
    public void setSex(Sex sex)                         { this.sex = sex; }

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

    @Column(name = "password_hash", unique = false, nullable = false, length = 100)
    private String passwordHash;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "birthday")
    private LocalDateTime birthday;

    @Column(name = "ageMonths")
    private Integer ageMonths;

    @Enumerated(EnumType.STRING)
    @Column(name = "region")
    private Region region;

    @Enumerated(EnumType.STRING)
    @Column(name = "sex")
    private Sex sex;
};