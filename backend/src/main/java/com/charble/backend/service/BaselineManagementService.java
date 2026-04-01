/**
 * Filename: BaselineManagementService.java
 * Author: Wilson Jimenez
 * Description: Handles CRUD operations for global baselines with uniqueness by category, region, sex, and age range
 */

//----------------------------------------------------------------------------------------------------
// Package
//----------------------------------------------------------------------------------------------------
package com.charble.backend.service;

//----------------------------------------------------------------------------------------------------
// Imports
//----------------------------------------------------------------------------------------------------
import com.charble.backend.model.Category;
import com.charble.backend.model.GlobalBaseline;
import com.charble.backend.model.enums.Region;
import com.charble.backend.model.enums.Sex;
import com.charble.backend.repository.CategoryRepository;
import com.charble.backend.repository.GlobalBaselineRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

//----------------------------------------------------------------------------------------------------
// Class Definition
//----------------------------------------------------------------------------------------------------
@Service
public class BaselineManagementService
{
    //------------------------------------------------------------------------------------------------
    // Constructors
    //------------------------------------------------------------------------------------------------
    public BaselineManagementService(GlobalBaselineRepository globalBaselineRepository,
                                     CategoryRepository categoryRepository)
    {
        this.globalBaselineRepository = globalBaselineRepository;
        this.categoryRepository = categoryRepository;
    }

    //------------------------------------------------------------------------------------------------
    // Public Methods
    //------------------------------------------------------------------------------------------------
    @Transactional
    public GlobalBaseline createBaseline(UUID categoryId,
                                         Region region,
                                         Sex sex,
                                         Integer ageMinYears,
                                         Integer ageMaxYears,
                                         Float mean,
                                         Float median,
                                         Float standardDeviation,
                                         Float lambda,
                                         Float mu,
                                         Float sigma,
                                         Integer sampleSize,
                                         String sourceName)
    {
        validateAgeRange(ageMinYears, ageMaxYears);
        validateStatistics(sampleSize, standardDeviation);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found."));

        Map<String, String> filters = buildDemographicFilters(region, sex, ageMinYears, ageMaxYears);

        if(findBaselineByUniqueKey(category, filters).isPresent())
        {
            throw new IllegalArgumentException("Baseline already exists for this category/region/sex/age range.");
        }

        GlobalBaseline baseline = new GlobalBaseline(
                category,
                filters,
                mean,
                median,
                standardDeviation,
                lambda,
                mu,
                sigma,
                sampleSize != null ? sampleSize : 0,
                sourceName
        );

        return globalBaselineRepository.save(baseline);
    }

    public GlobalBaseline getBaseline(UUID baselineId)
    {
        return globalBaselineRepository.findById(baselineId)
                .orElseThrow(() -> new IllegalArgumentException("Baseline not found."));
    }

    public List<GlobalBaseline> getAllBaselines()
    {
        return globalBaselineRepository.findAll();
    }

    public List<GlobalBaseline> getBaselinesByCategory(UUID categoryId)
    {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found."));

        return globalBaselineRepository.findAllByCategory(category);
    }

    public GlobalBaseline getBaselineByKey(UUID categoryId,
                                           Region region,
                                           Sex sex,
                                           Integer ageMinYears,
                                           Integer ageMaxYears)
    {
        validateAgeRange(ageMinYears, ageMaxYears);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found."));

        Map<String, String> filters = buildDemographicFilters(region, sex, ageMinYears, ageMaxYears);

        return findBaselineByUniqueKey(category, filters)
                .orElseThrow(() -> new IllegalArgumentException("Baseline not found for requested key."));
    }

    @Transactional
    public GlobalBaseline updateBaseline(UUID baselineId,
                                         Region region,
                                         Sex sex,
                                         Integer ageMinYears,
                                         Integer ageMaxYears,
                                         Float mean,
                                         Float median,
                                         Float standardDeviation,
                                         Float lambda,
                                         Float mu,
                                         Float sigma,
                                         Integer sampleSize,
                                         String sourceName)
    {
        validateAgeRange(ageMinYears, ageMaxYears);
        validateStatistics(sampleSize, standardDeviation);

        GlobalBaseline baseline = globalBaselineRepository.findById(baselineId)
                .orElseThrow(() -> new IllegalArgumentException("Baseline not found."));

        Map<String, String> filters = buildDemographicFilters(region, sex, ageMinYears, ageMaxYears);

        Optional<GlobalBaseline> collision = findBaselineByUniqueKey(baseline.getCategory(), filters);
        if(collision.isPresent() && !collision.get().getBaselineId().equals(baselineId))
        {
            throw new IllegalArgumentException("Baseline already exists for this category/region/sex/age range.");
        }

        baseline.setFilters(filters);
        baseline.setMean(mean);
        baseline.setMedian(median);
        baseline.setStandardDeviation(standardDeviation);
        baseline.setLambda(lambda);
        baseline.setMu(mu);
        baseline.setSigma(sigma);
        baseline.setSampleSize(sampleSize != null ? sampleSize : 0);
        baseline.setSourceName(sourceName);

        return globalBaselineRepository.save(baseline);
    }

    @Transactional
    public void deleteBaseline(UUID baselineId)
    {
        GlobalBaseline baseline = globalBaselineRepository.findById(baselineId)
                .orElseThrow(() -> new IllegalArgumentException("Baseline not found."));

        globalBaselineRepository.delete(baseline);
    }

    //------------------------------------------------------------------------------------------------
    // Private Methods
    //------------------------------------------------------------------------------------------------
    private Optional<GlobalBaseline> findBaselineByUniqueKey(Category category, Map<String, String> requestedFilters)
    {
        List<GlobalBaseline> categoryBaselines = globalBaselineRepository.findAllByCategory(category);

        for(GlobalBaseline baseline : categoryBaselines)
        {
            if(filtersMatch(baseline.getFilters(), requestedFilters))
            {
                return Optional.of(baseline);
            }
        }

        return Optional.empty();
    }

    private Map<String, String> buildDemographicFilters(Region region,
                                                        Sex sex,
                                                        Integer ageMinYears,
                                                        Integer ageMaxYears)
    {
        Map<String, String> filters = new HashMap<>();

        Region finalRegion = region != null ? region : Region.UNKNOWN;
        Sex finalSex = sex != null ? sex : Sex.UNKNOWN;

        filters.put(FILTER_REGION, finalRegion.name());
        filters.put(FILTER_SEX, finalSex.name());
        filters.put(FILTER_AGE_MIN_YEARS, ageMinYears != null ? String.valueOf(ageMinYears) : FILTER_AGE_ANY);
        filters.put(FILTER_AGE_MAX_YEARS, ageMaxYears != null ? String.valueOf(ageMaxYears) : FILTER_AGE_ANY);

        return filters;
    }

    private Boolean filtersMatch(Map<String, String> existingFilters, Map<String, String> requestedFilters)
    {
        String existingRegion = normalizeFilterValue(existingFilters, FILTER_REGION, Region.UNKNOWN.name());
        String requestedRegion = normalizeFilterValue(requestedFilters, FILTER_REGION, Region.UNKNOWN.name());

        String existingSex = normalizeFilterValue(existingFilters, FILTER_SEX, Sex.UNKNOWN.name());
        String requestedSex = normalizeFilterValue(requestedFilters, FILTER_SEX, Sex.UNKNOWN.name());

        String existingAgeMin = normalizeFilterValue(existingFilters, FILTER_AGE_MIN_YEARS, FILTER_AGE_ANY);
        String requestedAgeMin = normalizeFilterValue(requestedFilters, FILTER_AGE_MIN_YEARS, FILTER_AGE_ANY);

        String existingAgeMax = normalizeFilterValue(existingFilters, FILTER_AGE_MAX_YEARS, FILTER_AGE_ANY);
        String requestedAgeMax = normalizeFilterValue(requestedFilters, FILTER_AGE_MAX_YEARS, FILTER_AGE_ANY);

        return existingRegion.equalsIgnoreCase(requestedRegion)
                && existingSex.equalsIgnoreCase(requestedSex)
                && existingAgeMin.equalsIgnoreCase(requestedAgeMin)
                && existingAgeMax.equalsIgnoreCase(requestedAgeMax);
    }

    private String normalizeFilterValue(Map<String, String> filters, String key, String defaultValue)
    {
        if(filters == null) return defaultValue;

        String value = filters.get(key);
        if(value == null || value.isBlank()) return defaultValue;

        return value.trim();
    }

    private void validateAgeRange(Integer ageMinYears, Integer ageMaxYears)
    {
        if(ageMinYears != null && ageMinYears < 0)
        {
            throw new IllegalArgumentException("Age min must be >= 0.");
        }

        if(ageMaxYears != null && ageMaxYears < 0)
        {
            throw new IllegalArgumentException("Age max must be >= 0.");
        }

        if(ageMinYears != null && ageMaxYears != null && ageMinYears > ageMaxYears)
        {
            throw new IllegalArgumentException("Age min cannot be greater than age max.");
        }
    }

    private void validateStatistics(Integer sampleSize, Float standardDeviation)
    {
        if(sampleSize != null && sampleSize < 0)
        {
            throw new IllegalArgumentException("Sample size must be >= 0.");
        }

        if(standardDeviation != null && standardDeviation < 0.0f)
        {
            throw new IllegalArgumentException("Standard deviation must be >= 0.");
        }
    }

    //------------------------------------------------------------------------------------------------
    // Private Variables
    //------------------------------------------------------------------------------------------------
    private final GlobalBaselineRepository globalBaselineRepository;
    private final CategoryRepository categoryRepository;

    //------------------------------------------------------------------------------------------------
    // Private Constants
    //------------------------------------------------------------------------------------------------
    private static final String FILTER_REGION = "region";
    private static final String FILTER_SEX = "sex";
    private static final String FILTER_AGE_MIN_YEARS = "age_min_years";
    private static final String FILTER_AGE_MAX_YEARS = "age_max_years";
    private static final String FILTER_AGE_ANY = "ALL";
}