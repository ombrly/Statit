package com.charble.backend.Models;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "scores")
public class Score
{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "score_id")
    private UUID m_scoreId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category m_categoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User m_userId;

    @Column(name = "score_value", nullable = false)
    private Float m_score;

    @Column(name = "is_anonymous", nullable = false)
    private Boolean m_anonymous;

    @CreationTimestamp
    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime m_submittedAt;

    //Getters
    public UUID scoreId() { return m_scoreId; }
    public Category categoryId() { return m_categoryId; }
    public User userId() { return m_userId; }
    public Float score() { return m_score; }
    public Boolean anonymous() { return m_anonymous; }
    public LocalDateTime submittedAt() { return m_submittedAt; }
}



