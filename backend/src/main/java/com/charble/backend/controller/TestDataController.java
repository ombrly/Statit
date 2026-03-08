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
import java.util.List;

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
        User founder = userRepository.findByUsername("testFounder")
                .orElseGet(() -> {
                    User newUser = new User(
                            "testFounder",
                            "test@charble.com",
                            "hashed_pw",
                            LocalDateTime.of(1990, 1, 1, 0, 0),
                            21,
                            Region.NORTH_AMERICA,
                            Sex.MALE
                    );
                    return userRepository.save(newUser);
                });

        List<Category> dummyCategories = List.of(
                new Category("Height", "cm", true, true, true, true, founder),
                new Category("Weight", "kg", true, true, true, false, founder),
                new Category("Typing Speed", "wpm", false, false, true, true, founder),
                new Category("Bench Press Max", "lbs", true, true, true, true, founder),
                new Category("Reaction Time", "ms", false, false, true, false, founder)
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