/**
 * Filename: GlobalBaselineRepository.java
 * Author: Charles Bassani
 * Description: Repository for GlobalBaseline table queries
 */

//----------------------------------------------------------------------------------------------------
// Package
//----------------------------------------------------------------------------------------------------
package com.statit.backend.repository;

//----------------------------------------------------------------------------------------------------
// Imports
//----------------------------------------------------------------------------------------------------
import com.statit.backend.model.Category;
import com.statit.backend.model.GlobalBaseline;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

//----------------------------------------------------------------------------------------------------
// Interface Definition
//----------------------------------------------------------------------------------------------------
@Repository
public interface GlobalBaselineRepository extends JpaRepository<GlobalBaseline, UUID>
{
    //------------------------------------------------------------------------------------------------
    // Single Baseline Lookups
    //------------------------------------------------------------------------------------------------
    Optional<GlobalBaseline> findByCategory(Category category);

    List<GlobalBaseline> findAllByCategory(Category category);

    //------------------------------------------------------------------------------------------------
    // Bulk Deletes
    //------------------------------------------------------------------------------------------------
    void deleteAllByCategory(Category category);
}
