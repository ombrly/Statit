/**
 * Filename: ScoreController.java
 * Author: Wilson Jimenez
 * Description: API controller for score submission.
 */

//----------------------------------------------------------------------------------------------------
// Package
//----------------------------------------------------------------------------------------------------
package com.charble.backend.controller;

//----------------------------------------------------------------------------------------------------
// Imports
//----------------------------------------------------------------------------------------------------
import com.charble.backend.dto.ScoreResponse;
import com.charble.backend.dto.ScoreSubmitRequest;
import com.charble.backend.model.Category;
import com.charble.backend.model.Score;
import com.charble.backend.model.User;
import com.charble.backend.repository.CategoryRepository;
import com.charble.backend.repository.UserRepository;
import com.charble.backend.service.ScoreService;
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
    public ScoreController(ScoreService scoreService,
                           UserRepository userRepository,
                           CategoryRepository categoryRepository)
    {
        this.scoreService = scoreService;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    //------------------------------------------------------------------------------------------------
    // Public Methods
    //------------------------------------------------------------------------------------------------
    @PostMapping
    public ResponseEntity<ScoreResponse> submitScore(@RequestBody ScoreSubmitRequest request)
    {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        Category category = categoryRepository.findByCategoryName(request.categoryName())
                .orElseThrow(() -> new IllegalArgumentException("Category not found."));

        Score newScore = scoreService.submitScore(
                user.getUserId(),
                category.getCategoryId(),
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
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
}
