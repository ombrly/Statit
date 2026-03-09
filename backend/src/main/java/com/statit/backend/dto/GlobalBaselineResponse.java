/**
 * Filename: BaselineStatsResponse.java
 * Author: Wilson Jimenez
 * Description: DTO for baseline statistics responses.
 */

//----------------------------------------------------------------------------------------------------
// Package
//----------------------------------------------------------------------------------------------------
package com.statit.backend.dto;

//----------------------------------------------------------------------------------------------------
// Imports
//----------------------------------------------------------------------------------------------------
import com.statit.backend.model.GlobalBaseline;

import java.util.Map;
import java.util.UUID;

//----------------------------------------------------------------------------------------------------
// Record Definition
//----------------------------------------------------------------------------------------------------
public record GlobalBaselineResponse(UUID baselineId,
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
    public static GlobalBaselineResponse fromGlobalBaseline(GlobalBaseline baseline)
    {
        return new GlobalBaselineResponse(
                baseline.getBaselineId(),
                baseline.getFilters(),
                baseline.getMean(),
                baseline.getMedian(),
                baseline.getStandardDeviation(),
                baseline.getLambda(),
                baseline.getMu(),
                baseline.getSigma(),
                baseline.getSampleSize(),
                baseline.getSourceName()
        );
    }
}
