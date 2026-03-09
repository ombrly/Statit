/**
 * Filename: CategoryResponse.java
 * Author: Wilson Jimenez
 * Description: DTO for category responses.
 */

//----------------------------------------------------------------------------------------------------
// Package
//----------------------------------------------------------------------------------------------------
package com.charble.backend.dto;

//----------------------------------------------------------------------------------------------------
// Imports
//----------------------------------------------------------------------------------------------------
import com.charble.backend.model.Category;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

//----------------------------------------------------------------------------------------------------
// Record Definition
//----------------------------------------------------------------------------------------------------
public record CategoryResponse(UUID categoryId,
                               String name,
                               String description,
                               String units,
                               List<String> tags,
                               Boolean sortOrder,
                               LocalDateTime createdAt,
                               String message)
{
    public static CategoryResponse fromCategory(Category category, String message)
    {
        return new CategoryResponse(
                category.getCategoryId(),
                category.getName(),
                category.getDescription(),
                category.getUnits(),
                category.getTags(),
                category.getSortOrder(),
                category.getCreatedAt(),
                message
        );
    }
}
