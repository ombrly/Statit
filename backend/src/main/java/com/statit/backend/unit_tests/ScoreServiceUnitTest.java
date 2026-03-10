/**
 * Filename: .java
 * Author:
 * Description:
 */

//----------------------------------------------------------------------------------------------------
// Package
//----------------------------------------------------------------------------------------------------
package com.statit.backend.unit_tests;

//----------------------------------------------------------------------------------------------------
// Imports
//----------------------------------------------------------------------------------------------------

import com.statit.backend.dto.LeaderboardResponse;
import com.statit.backend.model.*;

import com.statit.backend.repository.*;
import com.statit.backend.service.ScoreService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;


//----------------------------------------------------------------------------------------------------
// Class Definition
//----------------------------------------------------------------------------------------------------
@RestController
@RequestMapping("api/unit_tests/score")
public class ScoreServiceUnitTest
{
    //------------------------------------------------------------------------------------------------
    // Constructors
    //------------------------------------------------------------------------------------------------
    public ScoreServiceUnitTest(CategoryRepository categoryRepository,
                                UserRepository userRepository,
                                GlobalBaselineRepository globalBaselineRepository,
                                ScoreService scoreService)
    {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.globalBaselineRepository = globalBaselineRepository;
        this.scoreService = scoreService;
    }

    //------------------------------------------------------------------------------------------------
    // Public Methods
    //------------------------------------------------------------------------------------------------
    @PostMapping("/generate-scores")
    public ResponseEntity<String> generateTestScores()
    {

        User testUser = userRepository.findByUsername("TestUser")
                .orElseGet(() -> {
                    Map<String, String> testDemographics = new HashMap<>();
                    testDemographics.put("region", "North America");
                    testDemographics.put("sex", "Male");
                    testDemographics.put("age", "20");

                    User newTestUser = new User(
                            "TestUser",
                            "tuser@myglobalranking.com",
                            "browns",
                            LocalDate.now(),
                            testDemographics
                    );

                    return userRepository.save(newTestUser);
                });

        Category testCategory = categoryRepository.findByCategoryName("Test Category")
                .orElseGet(() -> {
                    List<String> testTags = List.of(
                            "North America",
                            "Male"
                    );

                    Category newTestCategory = new Category(
                            "Test Category",
                            "Category used for unit tests",
                            "Test Units",
                            testTags,
                            true,
                            testUser
                    );

                    return categoryRepository.save(newTestCategory);
                });

        GlobalBaseline testBaseline = globalBaselineRepository.findByCategory(testCategory)
                .orElseGet(() -> {
                    GlobalBaseline newTestBaseline = new GlobalBaseline(
                            testCategory,
                            null,
                            0.0f,
                            0.0f,
                            0.0f,
                            null,
                            null,
                            null,
                            0,
                            "My Global Ranking Unit Test"
                    );

                    return globalBaselineRepository.save(newTestBaseline);
                });

        //Generate test scores and load through Score Service
        Random rand = new Random();
        for(int i = 0; i < 100; ++i)
        {
            scoreService.submitScore(
                    testUser.getUserId(),
                    testCategory.getCategoryId(),
                    rand.nextFloat() * 100.0f,
                    testUser.getDemographics(),
                    false
            );
        }

        return ResponseEntity.ok("Test scores generated");

    }

    @GetMapping("/top-scores")
    public ResponseEntity<LeaderboardResponse> getScores(
            @RequestParam UUID categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size)
    {
        Category category = categoryRepository.findById(categoryId).orElse(null);

        if(category != null)
        {
            Page<Score> scores = scoreService.getGlobalTopScores(categoryId, page, size);
            return ResponseEntity.ok(LeaderboardResponse.fromPage(category, scores));
        }
        else
        {
            return ResponseEntity.notFound().build();
        }
    }

    //------------------------------------------------------------------------------------------------
    // Private Variables
    //------------------------------------------------------------------------------------------------
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final GlobalBaselineRepository globalBaselineRepository;
    private final ScoreService scoreService;
}