/**
 * Filename: ScoreController.java
 * Author: Wilson Jimenez
 * Description: API controller for score submission.
 */

//----------------------------------------------------------------------------------------------------
// Package
//----------------------------------------------------------------------------------------------------
package com.statit.backend.controller;

//----------------------------------------------------------------------------------------------------
// Imports
//----------------------------------------------------------------------------------------------------
import com.statit.backend.dto.ScoreResponse;
import com.statit.backend.dto.ScoreSubmitRequest;
import com.statit.backend.model.Score;
import com.statit.backend.service.ScoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//----------------------------------------------------------------------------------------------------
// Class Definition
//----------------------------------------------------------------------------------------------------
@RestController
@RequestMapping("/api/v1/scores")
public class ScoreController
{
    //------------------------------------------------------------------------------------------------
    // Constructors
    //------------------------------------------------------------------------------------------------
    public ScoreController(ScoreService scoreService)
    {
        this.scoreService = scoreService;
    }

    //------------------------------------------------------------------------------------------------
    // Public Methods
    //------------------------------------------------------------------------------------------------
    @PostMapping
    public ResponseEntity<ScoreResponse> submitScore(@RequestBody ScoreSubmitRequest request)
    {
        Score newScore = scoreService.submitScore(
                request.userId(),
                request.categoryId(),
                request.score(),
                request.tags(),
                request.anonymous()
        );

        ScoreResponse response = ScoreResponse.fromScore(newScore, "Score submitted successfully");
        return ResponseEntity.ok(response);
    }

    //------------------------------------------------------------------------------------------------
    // Private Variables
    //------------------------------------------------------------------------------------------------
    private final ScoreService scoreService;
}
