package com.charble.backend.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "scores")
public class Score
{
    public Score(Category category, User user, Float score, Boolean anonymous)
    {
        this.category = category;
        this.user = user;
        this.score = score;
        this.anonymous = anonymous;
    }

    public Score() {}

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "score_id")
    private UUID scoreId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "score_value", nullable = false)
    private Float score;

    @Column(name = "is_anonymous", nullable = false)
    private Boolean anonymous;

    @CreationTimestamp
    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    //Getters
    public UUID scoreId() { return scoreId; }
    public Category category() { return category; }
    public User user() { return user; }
    public Float score() { return score; }
    public Boolean anonymous() { return anonymous; }
    public LocalDateTime submittedAt() { return submittedAt; }
}



