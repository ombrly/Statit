/**
 * Filename: CategoryRepository.java
 * Author: Charles Bassani
 * Description: Repository for Category table queries
 */

//----------------------------------------------------------------------------------------------------
// Package
//----------------------------------------------------------------------------------------------------
package com.charble.backend.repository;

//----------------------------------------------------------------------------------------------------
// Imports
//----------------------------------------------------------------------------------------------------
import com.charble.backend.model.Category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

//----------------------------------------------------------------------------------------------------
// Interface Definition
//----------------------------------------------------------------------------------------------------
@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID>
{
    //------------------------------------------------------------------------------------------------
    // Public Methods
    //------------------------------------------------------------------------------------------------
    @Query("SELECT c FROM Category c WHERE c.categoryName = ?1")
    Optional<Category> findByName(String name);

    @Query("SELECT c FROM Category c ORDER BY c.categoryName ASC")
    List<Category> findAllByAlphabetical();
}