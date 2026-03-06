package com.charble.backend.model;

import com.charble.backend.model.enums.*;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User
{
    public User(String username,
                String email,
                String passwordHash,
                LocalDateTime birthday,
                Integer age,
                Region region,
                Sex sex)
    {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.birthday = birthday;
        this.age = age;
        this.region = region;
        this.sex = sex;
    }

    public User() {}

    public void UpdateUser(String username,
                String email,
                String passwordHash,
                LocalDateTime birthday,
                Integer age,
                Region region,
                Sex sex)
    {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.birthday = birthday;
        this.age = age;
        this.region = region;
        this.sex = sex;
    }

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

    @Column(name = "age")
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(name = "region")
    private Region region;

    @Enumerated(EnumType.STRING)
    @Column(name = "sex")
    private Sex sex;

    //Getters
    public UUID userId() { return userId; }
    public String username() { return username; }
    public String email() { return email; }
    public String passwordHash() { return passwordHash; }
    public LocalDateTime createdAt() { return createdAt; }
    public LocalDateTime birthday() { return birthday; }
    public Integer age() { return age; }
    public Region region() { return region; }
    public Sex sex() { return sex; }
}



