/**
 * Filename: ScoreResponse.java
 * Author: Wilson Jimenez
 * Description: DTO for score submission responses.
 */

//----------------------------------------------------------------------------------------------------
// Package
//----------------------------------------------------------------------------------------------------
package com.statit.backend.dto;

import com.statit.backend.model.Score;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

//----------------------------------------------------------------------------------------------------
// Record Definition
//----------------------------------------------------------------------------------------------------
public record ScoreResponse(UUID scoreId,
                            Float score,
                            Map<String, String> tags,
                            Boolean anonymous,
                            LocalDateTime submittedAt,
                            String message)
{
    public static ScoreResponse fromScore(Score score, String message)
    {
        return new ScoreResponse(
                score.getScoreId(),
                score.getScore(),
                score.getTags(),
                score.getAnonymous(),
                score.getSubmittedAt(),
                message
        );
    }
}
