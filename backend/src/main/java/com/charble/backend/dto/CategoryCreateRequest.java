/**
 * Filename: CategoryCreateRequest.java
 * Author: Wilson Jimenez
 * Description: DTO for category creation payloads.
 */

//----------------------------------------------------------------------------------------------------
// Package
//----------------------------------------------------------------------------------------------------
package com.charble.backend.dto;

//----------------------------------------------------------------------------------------------------
// Imports
//----------------------------------------------------------------------------------------------------
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

//----------------------------------------------------------------------------------------------------
// Record Definition
//----------------------------------------------------------------------------------------------------
public record CategoryCreateRequest(String name,
                                    String description,
                                    String units,
                                    List<String> tags,
                                    @JsonProperty("sort_order") Boolean sortOrder,
                                    @JsonProperty("founding_username") String foundingUsername)
{
}
