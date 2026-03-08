/**
 * Filename: CategoryService.java
 * Author: Charles Bassani
 * Description: Handles CRUD operations for Categories
 */

//----------------------------------------------------------------------------------------------------
// Package
//----------------------------------------------------------------------------------------------------
package com.charble.backend.service;

//----------------------------------------------------------------------------------------------------
// Imports
//----------------------------------------------------------------------------------------------------
import com.charble.backend.model.Category;
import com.charble.backend.model.GlobalBaseline;
import com.charble.backend.model.User;
import com.charble.backend.repository.CategoryRepository;
import com.charble.backend.repository.GlobalBaselineRepository;
import com.charble.backend.repository.ScoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

//----------------------------------------------------------------------------------------------------
// Class Definition
//----------------------------------------------------------------------------------------------------
public class CategoryService
{
    //------------------------------------------------------------------------------------------------
    // Constructors
    //------------------------------------------------------------------------------------------------
    public CategoryService(CategoryRepository categoryRepository,
                           GlobalBaselineRepository globalBaselineRepository,
                           ScoreRepository scoreRepository)
    {
        this.categoryRepository = categoryRepository;
        this.globalBaselineRepository = globalBaselineRepository;
        this.scoreRepository = scoreRepository;
    }

    //------------------------------------------------------------------------------------------------
    // Public Methods
    //------------------------------------------------------------------------------------------------
    @Transactional
    public Category createCategory(String name,
                                   String description,
                                   List<String> tags,
                                   String units,
                                   Boolean sortOrder,
                                   User foundingUser)
    {
        //Check if category exists already
        if(categoryRepository.findByName(name).isPresent())
        {
            throw new IllegalArgumentException("Category already exists.");
        }

        //Create and save the category
        Category category = new Category(
                name,
                description,
                tags,
                units,
                sortOrder,
                foundingUser
        );

        //Re-assign to ensure get UUID from database
        category = categoryRepository.save(category);

        //Generate the baseline for the category
        generateAndSaveGlobalBaseline(category);

        return category;
    }

    @Transactional
    public void deleteCategory(UUID categoryId)
    {
        //Get the category to delete
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found."));

        //Delete all scores in category
        scoreRepository.deleteAllByCategory(category);

        //Delete all baselines in category
        globalBaselineRepository.deleteAllByCategory(category);

        //Delete the category itself
        categoryRepository.delete(category);
    }

    //------------------------------------------------------------------------------------------------
    // Private Methods
    //------------------------------------------------------------------------------------------------
    private void generateAndSaveGlobalBaseline(Category category)
    {
        GlobalBaseline baseline = new GlobalBaseline(
                category,
                new HashMap<>(),
                0.0f,
                0.0f,
                0.0f,
                null,
                null,
                null,
                0,
                "My Global Ranking Team"
        );

        globalBaselineRepository.save(baseline);
    }

    //------------------------------------------------------------------------------------------------
    // Private Variables
    //------------------------------------------------------------------------------------------------
    private final CategoryRepository categoryRepository;
    private final GlobalBaselineRepository globalBaselineRepository;
    private final ScoreRepository scoreRepository;
}