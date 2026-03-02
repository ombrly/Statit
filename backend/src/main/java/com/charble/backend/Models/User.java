package com.charble.backend.Models;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User
{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    private UUID m_userId;

    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String m_username;

    @Column(name = "email", unique = true, nullable = false)
    private String m_email;

    @Column(name = "password_hash", unique = false, nullable = false, length = 100)
    private String m_passwordHash;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime m_createdAt;

    //Getters
    public UUID userId() { return m_userId; }
    public String username() { return m_username; }
    public String email() { return m_email; }
    public String passwordHash() { return m_passwordHash; }
    public LocalDateTime createdAt() { return m_createdAt; }
}



