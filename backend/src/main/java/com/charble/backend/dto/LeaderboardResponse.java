/**
 * Filename: LeaderboardResponse.java
 * Author: Charles Bassani
 * Description: DTO for leaderboard responses
 */

//----------------------------------------------------------------------------------------------------
// Package
//----------------------------------------------------------------------------------------------------
package com.charble.backend.dto;

import com.charble.backend.model.Category;
import com.charble.backend.model.Score;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//----------------------------------------------------------------------------------------------------
// Record Definition
//----------------------------------------------------------------------------------------------------
public record LeaderboardResponse(
        UUID categoryId,
        String categoryName,
        String units,
        Boolean sortOrder,
        List<LeaderboardEntry> scores,
        int page,
        int totalPages,
        long totalElements
)
{
    public static LeaderboardResponse fromPage(Category category, Page<Score> scorePage)
    {
        List<LeaderboardEntry> entries = new ArrayList<>();
        int topRank = scorePage.getNumber() * scorePage.getSize() + 1;

        for (int i = 0; i < scorePage.getContent().size(); i++)
        {
            Score s = scorePage.getContent().get(i);
            entries.add(new LeaderboardEntry(
                    topRank + i,
                    s.getScoreId(),
                    s.getAnonymous() ? "Anonymous" : s.getUser().getUsername(),
                    s.getScore(),
                    s.getTags(),
                    s.getAnonymous(),
                    s.getSubmittedAt()
            ));
        }

        return new LeaderboardResponse(
                category.getCategoryId(),
                category.getName(),
                category.getUnits(),
                category.getSortOrder(),
                entries,
                scorePage.getNumber(),
                scorePage.getTotalPages(),
                scorePage.getTotalElements()
        );
    }
}