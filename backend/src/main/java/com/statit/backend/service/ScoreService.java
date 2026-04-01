package com.statit.backend.service;
import com.statit.backend.model.Category;
import com.statit.backend.model.GlobalBaseline;
import com.statit.backend.model.Score;
import com.statit.backend.model.User;
import com.statit.backend.repository.CategoryRepository;
import com.statit.backend.repository.GlobalBaselineRepository;
import com.statit.backend.repository.ScoreRepository;
import com.statit.backend.repository.UserRepository;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class ScoreService {
    private final ScoreRepository scoreRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final GlobalBaselineRepository globalBaselineRepository;
    private final ObjectMapper objectMapper;

    public ScoreService(ScoreRepository scoreRepository, UserRepository userRepository, CategoryRepository categoryRepository, GlobalBaselineRepository globalBaselineRepository, ObjectMapper objectMapper) {
        this.scoreRepository = scoreRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.globalBaselineRepository = globalBaselineRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void updateUserPrivacy(UUID userId, boolean anonymous) {
        scoreRepository.updateUserPrivacy(userId, anonymous);
    }

    @Transactional
    public Score submitScore(UUID userId, UUID categoryId, Float scoreValue, Map<String, String> scoreTags, Boolean isAnonymous) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found."));
        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new IllegalArgumentException("Category not found."));
        Score previousTopScore = getTopScoreForUser(category, user).orElse(null);

        Map<String, String> finalTags = new HashMap<>();
        if (scoreTags != null) finalTags.putAll(scoreTags);
        if (user.getDemographics() != null) finalTags.putAll(user.getDemographics());
        finalTags.put("age_months", String.valueOf(user.getAgeMonths()));
        finalTags.put("age_years", String.valueOf(user.getAgeYears()));

        Score newScore = new Score(category, user, scoreValue, finalTags, Boolean.TRUE.equals(isAnonymous));
        scoreRepository.save(newScore);
        scoreRepository.flush();

        Score newTopScore = getTopScoreForUser(category, user).orElse(null);
        if(previousTopScore == null) {
            updateGlobalBaseline(category, newTopScore.getScore(), false);
        } else if(!previousTopScore.getScoreId().equals(newTopScore.getScoreId())) {
            updateGlobalBaseline(category, previousTopScore.getScore(), true);
            updateGlobalBaseline(category, newTopScore.getScore(), false);
        }
        return newScore;
    }

    @Transactional
    public void deleteScore(UUID scoreId) {
        Score scoreToDelete = scoreRepository.findById(scoreId).orElseThrow(() -> new IllegalArgumentException("Score not found."));
        Category category = scoreToDelete.getCategory();
        User user = scoreToDelete.getUser();
        Score currentTopScore = getTopScoreForUser(category, user).orElse(null);
        boolean wasTopScore = currentTopScore != null && currentTopScore.getScoreId().equals(scoreToDelete.getScoreId());
        scoreRepository.delete(scoreToDelete);
        scoreRepository.flush();
        if(wasTopScore) {
            updateGlobalBaseline(category, scoreToDelete.getScore(), true);
            Score fallbackTopScore = getTopScoreForUser(category, user).orElse(null);
            if(fallbackTopScore != null) updateGlobalBaseline(category, fallbackTopScore.getScore(), false);
        }
    }

    public Page<Score> getGlobalTopScores(UUID categoryId, int page, int size) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new IllegalArgumentException("Category not found."));
        Pageable pageable = PageRequest.of(page, size);
        return category.getSortOrder() ? scoreRepository.findTopScoresPerUserDesc(categoryId, pageable) : scoreRepository.findTopScoresPerUserAsc(categoryId, pageable);
    }

    public Page<Score> getFilteredTopScores(UUID categoryId, Map<String, String> tags, int page, int size) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new IllegalArgumentException("Category not found."));
        String tagsJson = serializeTagsToJson(tags);
        Pageable pageable = PageRequest.of(page, size);
        return category.getSortOrder() ? scoreRepository.findFilteredTopScoresPerUserDesc(categoryId, tagsJson, pageable) : scoreRepository.findFilteredTopScoresPerUserAsc(categoryId, tagsJson, pageable);
    }

    private void updateGlobalBaseline(Category category, Float score, Boolean removal) {
        GlobalBaseline baseline = globalBaselineRepository.findByCategory(category).orElseThrow(() -> new IllegalArgumentException("Category not found."));
        Integer oldN = baseline.getSampleSize() == null ? 0 : baseline.getSampleSize();
        if(oldN == 0 && removal) return;
        Float oldMean = baseline.getMean() == null ? 0.0f : baseline.getMean();
        Float oldStdDev = baseline.getStandardDeviation() == null ? 0.0f : baseline.getStandardDeviation();
        int newN = removal ? oldN - 1 : oldN + 1;
        float newMean, newStdDev;
        if(removal) {
            if(newN == 0) { newMean = 0.0f; newStdDev = 0.0f; }
            else if(newN == 1) { newMean = (oldMean * oldN - score) / newN; newStdDev = 0.0f; }
            else {
                newMean = ((oldMean * oldN) - score) / newN;
                float oldM2 = (oldStdDev * oldStdDev) * (oldN - 1);
                float newM2 = Math.max(0.0f, oldM2 - ((score - oldMean) * (score - newMean)));
                newStdDev = (float)Math.sqrt(newM2 / (newN - 1));
            }
        } else {
            if(newN == 1) { newMean = score; newStdDev = 0.0f; }
            else {
                newMean = oldMean + ((score - oldMean) / newN);
                float oldM2 = (oldStdDev * oldStdDev) * (oldN - 1);
                float newM2 = oldM2 + ((score - oldMean) * (score - newMean));
                newStdDev = (float) Math.sqrt(newM2 / (newN - 1));
            }
        }
        baseline.setMean(newMean);
        baseline.setStandardDeviation(newStdDev);
        baseline.setSampleSize(newN);
        globalBaselineRepository.save(baseline);
    }
    private Optional<Score> getTopScoreForUser(Category category, User user) {
        return category.getSortOrder() ? scoreRepository.findFirstByCategoryAndUserOrderByScoreDesc(category, user) : scoreRepository.findFirstByCategoryAndUserOrderByScoreAsc(category, user);
    }
    private String serializeTagsToJson(Map<String, String> tags) {
        if(tags == null || tags.isEmpty()) return "{}";
        try { return objectMapper.writeValueAsString(tags); }
        catch(JacksonException e) { throw new IllegalArgumentException("Failed to serialize tags to JSON.", e); }
    }
}
