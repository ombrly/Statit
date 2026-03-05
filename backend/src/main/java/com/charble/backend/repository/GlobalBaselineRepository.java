package com.charble.backend.repository;

import com.charble.backend.model.Category;
import com.charble.backend.model.GlobalBaseline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GlobalBaselineRepository extends JpaRepository<GlobalBaseline, UUID>
{
    @Query("SELECT gb FROM GlobalBaseline gb WHERE gb.category = ?1")
    Optional<GlobalBaseline> findByCategory(Category category);
}
