package com.charble.backend.Models;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "global_baselines")
public class GlobalBaseline
{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "baseline_id")
    private UUID m_baselineId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @Column(name = "category_id")
    private Category m_categoryId;

    @Column(name = "mean")
    private Float m_mean;

    @Column(name = "median")
    private Float m_median;

    @Column(name = "standard_deviation")
    private Float m_standardDeviation;

    @Column(name = "sample_size")
    private Integer m_sampleSize;

    @Column(name = "source_size")
    private String m_sourceSize;

    //Getters
    public UUID baselineId() { return m_baselineId; }
    public Category categoryId() { return m_categoryId; }
    public Float mean() { return m_mean; }
    public Float median() { return m_median; }
    public Float standardDeviation() { return m_standardDeviation; }
    public Integer sampleSize() { return m_sampleSize; }
    public String sourceSize() { return m_sourceSize; }
}
