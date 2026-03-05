package com.charble.backend.repository;

import com.charble.backend.model.Score;
import com.charble.backend.model.User;
import com.charble.backend.model.Category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScoreRepository extends JpaRepository<Score, UUID>
{
    @Query("SELECT s FROM Score s WHERE s.user = ?1 AND s.category = ?2 ORDER BY s.score DESC LIMIT 1")
    Optional<Score> findTopByUserAndCategoryOrderByScoreDesc(User user, Category category);

    @Query("SELECT s FROM Score s WHERE s.user = ?1 AND s.category = ?2 ORDER BY s.submittedAt DESC LIMIT 1")
    Optional<Score> findTopByUserAndCategoryOrderBySubmittedAtDesc(User user, Category category);

    @Query("SELECT COUNT(DISTINCT s.user) FROM Score s WHERE s.category = ?1")
    long countDistinctUsersByCategory(Category category);

    @Query("SELECT MAX(s.score) FROM Score s WHERE s.category = ?1 GROUP BY s.user")
    List<Float> findHighestScoresPerUserByCategory(Category category);
}
