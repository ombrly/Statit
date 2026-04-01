/**
 * Filename: LeaderboardController.java
 * Author: Wilson Jimenez
 * Description: API controller for category leaderboards and baseline statistics retrieval.
 */

//----------------------------------------------------------------------------------------------------
// Package
//----------------------------------------------------------------------------------------------------
package com.charble.backend.controller;

//----------------------------------------------------------------------------------------------------
// Imports
//----------------------------------------------------------------------------------------------------
import com.charble.backend.dto.BaselineStatsResponse;
import com.charble.backend.dto.LeaderboardResponse;
import com.charble.backend.dto.LeaderboardSnapshotResponse;
import com.charble.backend.dto.ScoreFilterRequest;
import com.charble.backend.model.Category;
import com.charble.backend.model.GlobalBaseline;
import com.charble.backend.model.Score;
import com.charble.backend.service.BaselineManagementService;
import com.charble.backend.service.CategoryService;
import com.charble.backend.service.ScoreService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//----------------------------------------------------------------------------------------------------
// Class Definition
//----------------------------------------------------------------------------------------------------
@RestController
@RequestMapping("/api/v1/leaderboards")
public class LeaderboardController
{
    //------------------------------------------------------------------------------------------------
    // Constructors
    //------------------------------------------------------------------------------------------------
    public LeaderboardController(ScoreService scoreService,
                                 CategoryService categoryService,
                                 BaselineManagementService baselineManagementService)
    {
        this.scoreService = scoreService;
        this.categoryService = categoryService;
        this.baselineManagementService = baselineManagementService;
    }

    //------------------------------------------------------------------------------------------------
    // Public Methods
    //------------------------------------------------------------------------------------------------
    @GetMapping("/{categoryId}/top")
    public ResponseEntity<LeaderboardResponse> getTopScores(@PathVariable UUID categoryId,
                                                            @RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "25") int size)
    {
        Category category = categoryService.getCategory(categoryId);
        Page<Score> scores = scoreService.getGlobalTopScores(categoryId, page, size);

        LeaderboardResponse response = LeaderboardResponse.fromPage(category, scores);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{categoryId}/snapshot")
    public ResponseEntity<LeaderboardSnapshotResponse> getLeaderboardSnapshot(@PathVariable UUID categoryId,
                                                                              @RequestParam(defaultValue = "0") int page,
                                                                              @RequestParam(defaultValue = "25") int size)
    {
        Category category = categoryService.getCategory(categoryId);
        Page<Score> scores = scoreService.getGlobalTopScores(categoryId, page, size);
        LeaderboardResponse leaderboardResponse = LeaderboardResponse.fromPage(category, scores);

        List<GlobalBaseline> baselines = baselineManagementService.getBaselinesByCategory(categoryId);
        List<BaselineStatsResponse> baselineResponses = new ArrayList<>();
        for(GlobalBaseline baseline : baselines)
        {
            baselineResponses.add(BaselineStatsResponse.fromBaseline(baseline));
        }

        LeaderboardSnapshotResponse response = new LeaderboardSnapshotResponse(
                categoryId,
                leaderboardResponse,
                baselineResponses
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{categoryId}/filtered")
    public ResponseEntity<LeaderboardResponse> getFilteredTopScores(@PathVariable UUID categoryId,
                                                                    @RequestParam(defaultValue = "0") int page,
                                                                    @RequestParam(defaultValue = "25") int size,
                                                                    @RequestBody ScoreFilterRequest request)
    {
        Category category = categoryService.getCategory(categoryId);
        Page<Score> scores = scoreService.getFilteredTopScores(categoryId, request.tags(), page, size);

        LeaderboardResponse response = LeaderboardResponse.fromPage(category, scores);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{categoryId}/baselines")
    public ResponseEntity<List<BaselineStatsResponse>> getBaselineStats(@PathVariable UUID categoryId)
    {
        List<GlobalBaseline> baselines = baselineManagementService.getBaselinesByCategory(categoryId);
        List<BaselineStatsResponse> responses = new ArrayList<>();

        for(GlobalBaseline baseline : baselines)
        {
            responses.add(BaselineStatsResponse.fromBaseline(baseline));
        }

        return ResponseEntity.ok(responses);
    }

    //------------------------------------------------------------------------------------------------
    // Private Variables
    //------------------------------------------------------------------------------------------------
    private final ScoreService scoreService;
    private final CategoryService categoryService;
    private final BaselineManagementService baselineManagementService;
}
