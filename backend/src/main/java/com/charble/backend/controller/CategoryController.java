/**
 * Filename: CategoryController.java
 * Author: Wilson Jimenez
 * Description: API controller for creating categories and listing available categories.
 */

//----------------------------------------------------------------------------------------------------
// Package
//----------------------------------------------------------------------------------------------------
package com.charble.backend.controller;

//----------------------------------------------------------------------------------------------------
// Imports
//----------------------------------------------------------------------------------------------------
import com.charble.backend.dto.CategoryCreateRequest;
import com.charble.backend.dto.CategoryListResponse;
import com.charble.backend.dto.CategoryResponse;
import com.charble.backend.model.Category;
import com.charble.backend.model.User;
import com.charble.backend.service.CategoryService;
import com.charble.backend.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

//----------------------------------------------------------------------------------------------------
// Class Definition
//----------------------------------------------------------------------------------------------------
@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController
{
    //------------------------------------------------------------------------------------------------
    // Constructors
    //------------------------------------------------------------------------------------------------
    public CategoryController(CategoryService categoryService,
                              UserService userService)
    {
        this.categoryService = categoryService;
        this.userService = userService;
    }

    //------------------------------------------------------------------------------------------------
    // Public Methods
    //------------------------------------------------------------------------------------------------
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody CategoryCreateRequest request)
    {
        User foundingUser = userService.getUser(request.foundingUserId());
        List<String> tags = request.tags() != null ? request.tags() : new ArrayList<>();

        Category category = categoryService.createCategory(
                request.name(),
                request.description(),
                request.units(),
                tags,
                request.sortOrder(),
                foundingUser
        );

        CategoryResponse response = CategoryResponse.fromCategory(category, "Category created successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<CategoryListResponse> getAllCategories(@RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "25") int size)
    {
        Pageable pageable = PageRequest.of(page, size);
        Page<Category> categoryPage = categoryService.getAllCategories(pageable);

        List<CategoryResponse> categories = new ArrayList<>();
        for(Category category : categoryPage.getContent())
        {
            categories.add(CategoryResponse.fromCategory(category, null));
        }

        CategoryListResponse response = new CategoryListResponse(
                categories,
                categoryPage.getNumber(),
                categoryPage.getTotalPages(),
                categoryPage.getTotalElements()
        );

        return ResponseEntity.ok(response);
    }

    //------------------------------------------------------------------------------------------------
    // Private Variables
    //------------------------------------------------------------------------------------------------
    private final CategoryService categoryService;
    private final UserService userService;
}
