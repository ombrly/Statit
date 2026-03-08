/**
 * Filename: UserRepository.java
 * Author: Charles Bassani
 * Description: Repository for User table queries
 */

//----------------------------------------------------------------------------------------------------
// Package
//----------------------------------------------------------------------------------------------------
package com.charble.backend.repository;

//----------------------------------------------------------------------------------------------------
// Imports
//----------------------------------------------------------------------------------------------------
import com.charble.backend.model.Category;
import com.charble.backend.model.User;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

//----------------------------------------------------------------------------------------------------
// Interface Definition
//----------------------------------------------------------------------------------------------------
@Repository
public interface UserRepository extends JpaRepository<User, UUID>
{
    //------------------------------------------------------------------------------------------------
    // Public Methods
    //------------------------------------------------------------------------------------------------
    @Query("SELECT u FROM User u WHERE u.username = ?1")
    Optional<User> findByUsername(String username);
}
