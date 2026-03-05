package com.charble.backend.repository;

import com.charble.backend.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID>
{
    @Query("SELECT c FROM Category c WHERE c.categoryName = ?1")
    Optional<Category> findByName(String name);
}
