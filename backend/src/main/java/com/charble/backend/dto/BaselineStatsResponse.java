/**
 * Filename: BaselineStatsResponse.java
 * Author: Wilson Jimenez
 * Description: DTO for baseline statistics responses.
 */

//----------------------------------------------------------------------------------------------------
// Package
//----------------------------------------------------------------------------------------------------
package com.charble.backend.dto;

//----------------------------------------------------------------------------------------------------
// Imports
//----------------------------------------------------------------------------------------------------
import com.charble.backend.model.GlobalBaseline;

import java.util.Map;
import java.util.UUID;

//----------------------------------------------------------------------------------------------------
// Record Definition
//----------------------------------------------------------------------------------------------------
public record BaselineStatsResponse(UUID baselineId,
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
    public static BaselineStatsResponse fromBaseline(GlobalBaseline baseline)
    {
        return new BaselineStatsResponse(
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
