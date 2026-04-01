package com.statit.backend.repository;
import com.statit.backend.model.Category;
import com.statit.backend.model.Score;
import com.statit.backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScoreRepository extends JpaRepository<Score, UUID> {
    Optional<Score> findFirstByCategoryAndUserOrderByScoreDesc(Category category, User user);
    Optional<Score> findFirstByCategoryAndUserOrderByScoreAsc(Category category, User user);
    List<Score> findAllByUser(User user);
    Page<Score> findByUserOrderBySubmittedAtDesc(User user, Pageable pageable);
    long countByCategoryAndRejectedFalse(Category category);
    void deleteAllByCategory(Category category);

    @Modifying
    @Transactional
    @Query("UPDATE Score s SET s.anonymous = :anon WHERE s.user.userId = :userId")
    void updateUserPrivacy(@Param("userId") UUID userId, @Param("anon") boolean anon);

    @Query(value = "SELECT * FROM (SELECT DISTINCT ON (s.user_id) s.* FROM scores s WHERE s.category_id = :categoryId AND s.rejected = false ORDER BY s.user_id, s.score_value DESC) best ORDER BY best.score_value DESC", countQuery = "SELECT COUNT(DISTINCT s.user_id) FROM scores s WHERE s.category_id = :categoryId AND s.rejected = false", nativeQuery = true)
    Page<Score> findTopScoresPerUserDesc(@Param("categoryId") UUID categoryId, Pageable pageable);

    @Query(value = "SELECT * FROM (SELECT DISTINCT ON (s.user_id) s.* FROM scores s WHERE s.category_id = :categoryId AND s.rejected = false ORDER BY s.user_id, s.score_value ASC) best ORDER BY best.score_value ASC", countQuery = "SELECT COUNT(DISTINCT s.user_id) FROM scores s WHERE s.category_id = :categoryId AND s.rejected = false", nativeQuery = true)
    Page<Score> findTopScoresPerUserAsc(@Param("categoryId") UUID categoryId, Pageable pageable);

    @Query(value = "SELECT * FROM (SELECT DISTINCT ON (s.user_id) s.* FROM scores s WHERE s.category_id = :categoryId AND s.rejected = false AND s.tags @> CAST(:tags AS jsonb) ORDER BY s.user_id, s.score_value DESC) best ORDER BY best.score_value DESC", countQuery = "SELECT COUNT(DISTINCT s.user_id) FROM scores s WHERE s.category_id = :categoryId AND s.rejected = false AND s.tags @> CAST(:tags AS jsonb)", nativeQuery = true)
    Page<Score> findFilteredTopScoresPerUserDesc(@Param("categoryId") UUID categoryId, @Param("tags") String tagsJson, Pageable pageable);

    @Query(value = "SELECT * FROM (SELECT DISTINCT ON (s.user_id) s.* FROM scores s WHERE s.category_id = :categoryId AND s.rejected = false AND s.tags @> CAST(:tags AS jsonb) ORDER BY s.user_id, s.score_value ASC) best ORDER BY best.score_value ASC", countQuery = "SELECT COUNT(DISTINCT s.user_id) FROM scores s WHERE s.category_id = :categoryId AND s.rejected = false AND s.tags @> CAST(:tags AS jsonb)", nativeQuery = true)
    Page<Score> findFilteredTopScoresPerUserAsc(@Param("categoryId") UUID categoryId, @Param("tags") String tagsJson, Pageable pageable);
}
