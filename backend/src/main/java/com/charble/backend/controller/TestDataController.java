/**
 * Filename: TestDataController.java
 * Author: Charles Bassani
 * Description: Controller for generating dummy test data and verifying sorting logic
 */

//----------------------------------------------------------------------------------------------------
// Package
//----------------------------------------------------------------------------------------------------
package com.charble.backend.controller;

//----------------------------------------------------------------------------------------------------
// Imports
//----------------------------------------------------------------------------------------------------
import com.charble.backend.model.Category;
import com.charble.backend.model.User;
import com.charble.backend.model.enums.Region;
import com.charble.backend.model.enums.Sex;
import com.charble.backend.repository.CategoryRepository;
import com.charble.backend.repository.UserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//----------------------------------------------------------------------------------------------------
// Class Definition
//----------------------------------------------------------------------------------------------------
@RestController
@RequestMapping("/api/test")
public class TestDataController
{
    //------------------------------------------------------------------------------------------------
    // Constructors
    //------------------------------------------------------------------------------------------------
    public TestDataController(CategoryRepository categoryRepository,
                              UserRepository userRepository)
    {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    //------------------------------------------------------------------------------------------------
    // Public Methods
    //------------------------------------------------------------------------------------------------
    @PostMapping("/generate-categories")
    public ResponseEntity<String> generateDummyCategories()
    {
        Map<String, String> dummyDemographics = new HashMap<>();

        User founder = userRepository.findByUsername("testFounder")
                .orElseGet(() -> {

                    // --- THE NEW JSONB DEMOGRAPHICS SETUP ---
                    dummyDemographics.put("region", "North America");
                    dummyDemographics.put("sex", "Male");

                    // Age is no longer passed as an integer, it calculates automatically from the birthday!
                    User newUser = new User(
                            "testFounder",
                            "test@charble.com",
                            "hashed_pw",
                            LocalDateTime.of(1990, 1, 1, 0, 0),
                            dummyDemographics
                    );
                    return userRepository.save(newUser);
                });

        List<Category> dummyCategories = List.of(
                new Category("Height", "Physical height measurements", null, "cm", true, founder),
                new Category("Weight", "Physical body weight", null, "kg", false, founder),
                new Category("Typing Speed", "Standard 60-second typing test", null, "wpm", true, founder),
                new Category("Bench Press Max", "1 Rep Max on flat bench", null, "lbs", true, founder),
                new Category("Reaction Time", "Visual click reaction time", null, "ms", false, founder)
        );

        for(Category cat : dummyCategories)
        {
            if(categoryRepository.findByName(cat.getName()).isEmpty())
            {
                categoryRepository.save(cat);
            }
        }

        return ResponseEntity.ok("Dummy categories generated");
    }

    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getPopularCategories()
    {
        List<Category> categories = categoryRepository.findAllByAlphabetical();
        return ResponseEntity.ok(categories);
    }

    //------------------------------------------------------------------------------------------------
    // Private Variables
    //------------------------------------------------------------------------------------------------
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
}