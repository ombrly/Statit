/**
 * Filename: ScoreSubmitRequest.java
 * Author:
 * Description: DTO for score submission payloads.
 */

//----------------------------------------------------------------------------------------------------
// Package
//----------------------------------------------------------------------------------------------------
package com.charble.backend.dto;

//----------------------------------------------------------------------------------------------------
// Imports
//----------------------------------------------------------------------------------------------------
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

//----------------------------------------------------------------------------------------------------
// Record Definition
//----------------------------------------------------------------------------------------------------
public record ScoreSubmitRequest(String username,
                                 @JsonProperty("category_name") String categoryName,
                                 Float score,
                                 Map<String, String> tags,
                                 Boolean anonymous)
{
}
