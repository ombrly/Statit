package com.charble.backend.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "global_baselines")
public class GlobalBaseline
{
    public GlobalBaseline(Category category, Float mean, Float median, Float stdDev, Integer sampleSize, String sourceName)
    {
        this.category = category;
        this.mean = mean;
        this.median = median;
        this.standardDeviation = stdDev;
        this.sampleSize = sampleSize;
        this.sourceName = sourceName;
    }

    public GlobalBaseline() {}

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "baseline_id")
    private UUID baselineId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "mean")
    private Float mean;

    @Column(name = "median")
    private Float median;

    @Column(name = "standard_deviation")
    private Float standardDeviation;

    @Column(name = "sample_size")
    private Integer sampleSize;

    @Column(name = "source_name")
    private String sourceName;

    //Getters
    public UUID baselineId() { return baselineId; }
    public Category category() { return category; }
    public Float mean() { return mean; }
    public Float median() { return median; }
    public Float standardDeviation() { return standardDeviation; }
    public Integer sampleSize() { return sampleSize; }
    public String sourceName() { return sourceName; }

    //Setters
    public void updateStatistics(Float mean, Float median, Float stdDev, Integer sampleSize, String sourceName)
    {
        this.mean = mean;
        this.median = median;
        this.standardDeviation = stdDev;
        this.sampleSize = sampleSize;
        this.sourceName = sourceName;
    }
}
