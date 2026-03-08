/**
 * Filename: ScoreRepository.java
 * Author: Charles Bassani
 * Description: Repository for Score table queries
 */

//----------------------------------------------------------------------------------------------------
// Package
//----------------------------------------------------------------------------------------------------
package com.charble.backend.repository;

//----------------------------------------------------------------------------------------------------
// Imports
//----------------------------------------------------------------------------------------------------
import com.charble.backend.model.Category;
import com.charble.backend.model.Score;
import com.charble.backend.model.User;

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
public interface ScoreRepository extends JpaRepository<Score, UUID>
{
    //------------------------------------------------------------------------------------------------
    // Public Methods
    //------------------------------------------------------------------------------------------------
    Optional<Score> findFirstByCategoryAndUserOrderByScoreDesc(Category category, User user);

    Optional<Score> findFirstByCategoryAndUserOrderByScoreAsc(Category category, User user);

    void deleteAllByCategory(Category category);
}
