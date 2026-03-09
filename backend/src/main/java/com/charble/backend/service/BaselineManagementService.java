/**
 * Filename: BaselineManagementService.java
 * Author: Wilson Jimenez
 * Description: Baseline management is intentionally disabled; baseline behavior is handled in ScoreService.
 */

//----------------------------------------------------------------------------------------------------
// Package
//----------------------------------------------------------------------------------------------------
package com.charble.backend.service;

//----------------------------------------------------------------------------------------------------
// Imports
//----------------------------------------------------------------------------------------------------
import com.charble.backend.model.GlobalBaseline;
import com.charble.backend.model.enums.Region;
import com.charble.backend.model.enums.Sex;
import com.charble.backend.repository.CategoryRepository;
import com.charble.backend.repository.GlobalBaselineRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
        throw new UnsupportedOperationException("BaselineManagementService is disabled.");
    }

    public GlobalBaseline getBaseline(UUID baselineId)
    {
        throw new UnsupportedOperationException("BaselineManagementService is disabled.");
    }

    public List<GlobalBaseline> getAllBaselines()
    {
        throw new UnsupportedOperationException("BaselineManagementService is disabled.");
    }

    public List<GlobalBaseline> getBaselinesByCategory(UUID categoryId)
    {
        throw new UnsupportedOperationException("BaselineManagementService is disabled.");
    }

    public GlobalBaseline getBaselineByKey(UUID categoryId,
                                           Region region,
                                           Sex sex,
                                           Integer ageMinYears,
                                           Integer ageMaxYears)
    {
        throw new UnsupportedOperationException("BaselineManagementService is disabled.");
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
        throw new UnsupportedOperationException("BaselineManagementService is disabled.");
    }

    @Transactional
    public void deleteBaseline(UUID baselineId)
    {
        throw new UnsupportedOperationException("BaselineManagementService is disabled.");
    }
}
