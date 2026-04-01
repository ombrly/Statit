/**
 * Filename: GlobalBaseline.java
 * Author: Charles Bassani
 * Description: GlobalBaseline DTO and model, utilizing JSONB for dynamic filtering
 */

//----------------------------------------------------------------------------------------------------
// Package
//----------------------------------------------------------------------------------------------------
package com.charble.backend.model;

//----------------------------------------------------------------------------------------------------
// Imports
//----------------------------------------------------------------------------------------------------
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

//----------------------------------------------------------------------------------------------------
// Class Definition
//----------------------------------------------------------------------------------------------------
@Entity
@Table(name = "global_baselines")
public class GlobalBaseline
{
    //------------------------------------------------------------------------------------------------
    // Constructors
    //------------------------------------------------------------------------------------------------
    public GlobalBaseline() {}

    public GlobalBaseline(Category category,
                          Map<String, String> filters,
                          Float mean,
                          Float median,
                          Float standardDeviation,
                          Float lambda,
                          Float mu,
                          Float sigma,
                          Integer sampleSize,
                          String sourceName)
    {
        this.category = category;
        this.filters = filters != null ? filters : new HashMap<>();
        this.mean = mean;
        this.median = median;
        this.standardDeviation = standardDeviation;
        this.lambda = lambda;
        this.mu = mu;
        this.sigma = sigma;
        this.sampleSize = sampleSize;
        this.sourceName = sourceName;
    }

    public GlobalBaseline(Category category)
    {
        this.category = category;
        this.filters = new HashMap<>();
    }

    //------------------------------------------------------------------------------------------------
    // Public Functions
    //------------------------------------------------------------------------------------------------
    public void update(Map<String, String> filters,
                       Float mean,
                       Float median,
                       Float standardDeviation,
                       Float lambda,
                       Float mu,
                       Float sigma,
                       Integer sampleSize,
                       String sourceName)
    {
        this.filters = filters != null ? filters : new HashMap<>();
        this.mean = mean;
        this.median = median;
        this.standardDeviation = standardDeviation;
        this.lambda = lambda;
        this.mu = mu;
        this.sigma = sigma;
        this.sampleSize = sampleSize;
        this.sourceName = sourceName;
    }

    //Getters
    public UUID getBaselineId()                  { return baselineId; }
    public Category getCategory()                { return category; }
    public Map<String, String> getFilters()      { return filters; }
    public Float getMean()                       { return mean; }
    public Float getMedian()                     { return median; }
    public Float getStandardDeviation()          { return standardDeviation; }
    public Float getLambda()                     { return lambda; }
    public Float getMu()                         { return mu; }
    public Float getSigma()                      { return sigma; }
    public Integer getSampleSize()               { return sampleSize; }
    public String getSourceName()                { return sourceName; }

    //Setters
    public void setFilters(Map<String, String> filters)             { this.filters = filters; }
    public void setMean(Float mean)                                 { this.mean = mean; }
    public void setMedian(Float median)                             { this.median = median; }
    public void setStandardDeviation(Float standardDeviation)       { this.standardDeviation = standardDeviation; }
    public void setLambda(Float lambda)                             { this.lambda = lambda; }
    public void setMu(Float mu)                                     { this.mu = mu; }
    public void setSigma(Float sigma)                               { this.sigma = sigma; }
    public void setSampleSize(Integer sampleSize)                   { this.sampleSize = sampleSize; }
    public void setSourceName(String sourceName)                    { this.sourceName = sourceName; }

    //------------------------------------------------------------------------------------------------
    // Private Variables
    //------------------------------------------------------------------------------------------------
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "baseline_id")
    private UUID baselineId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "filters", columnDefinition = "jsonb")
    private Map<String, String> filters = new HashMap<>();

    @Column(name = "mean")
    private Float mean;

    @Column(name = "median")
    private Float median;

    @Column(name = "standard_deviation")
    private Float standardDeviation;

    @Column(name = "lambda")
    private Float lambda;

    @Column(name = "mu")
    private Float mu;

    @Column(name = "sigma")
    private Float sigma;

    @Column(name = "sample_size")
    private Integer sampleSize;

    @Column(name = "source_name")
    private String sourceName;
}