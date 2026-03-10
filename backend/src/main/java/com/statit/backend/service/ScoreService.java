/**
 * Filename: ScoreService.java
 * Author: Charles Bassani
 * Description: Handles CRUD operations for scores and updates global baselines
 */

//----------------------------------------------------------------------------------------------------
// Package
//----------------------------------------------------------------------------------------------------
package com.statit.backend.service;

//----------------------------------------------------------------------------------------------------
// Imports
//----------------------------------------------------------------------------------------------------
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

//----------------------------------------------------------------------------------------------------
// Class Definition
//----------------------------------------------------------------------------------------------------
@Service
public class ScoreService
{
    //------------------------------------------------------------------------------------------------
    // Constructors
    //------------------------------------------------------------------------------------------------
    public ScoreService(ScoreRepository scoreRepository,
                        UserRepository userRepository,
                        CategoryRepository categoryRepository,
                        GlobalBaselineRepository globalBaselineRepository,
                        ObjectMapper objectMapper)
    {
        this.scoreRepository = scoreRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.globalBaselineRepository = globalBaselineRepository;
        this.objectMapper = objectMapper;
    }

    //------------------------------------------------------------------------------------------------
    // Public Methods
    //------------------------------------------------------------------------------------------------
    @Transactional
    public Score submitScore(UUID userId, UUID categoryId, Float scoreValue, Map<String, String> scoreTags, Boolean isAnonymous) {

        //Fetch the user and category
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found."));

        Score previousTopScore = getTopScoreForUser(category, user).orElse(null);

        //JSONB tags merge
        Map<String, String> finalTags = new HashMap<>();
        if (scoreTags != null) finalTags.putAll(scoreTags);
        if (user.getDemographics() != null) finalTags.putAll(user.getDemographics());
        finalTags.put("age_months", String.valueOf(user.getAgeMonths()));
        finalTags.put("age_years", String.valueOf(user.getAgeYears()));

        //Save the new score
        Score newScore = new Score(category, user, scoreValue, finalTags, Boolean.TRUE.equals(isAnonymous));
        scoreRepository.save(newScore);
        scoreRepository.flush();

        //Find new top score after saving
        Score newTopScore = getTopScoreForUser(category, user).orElse(null);

        //Update the global baseline
        if(previousTopScore == null)
        {
            //User has no score in specified category
            updateGlobalBaseline(category, newTopScore.getScore(), false);
        }
        else if(!previousTopScore.getScoreId().equals(newTopScore.getScoreId()))
        {
            //User set a new score, remove old and add new
            updateGlobalBaseline(category, previousTopScore.getScore(), true);
            updateGlobalBaseline(category, newTopScore.getScore(), false);
        }

        return newScore;
    }

    @Transactional
    public void deleteScore(UUID scoreId)
    {
        Score scoreToDelete = scoreRepository.findById(scoreId)
                .orElseThrow(() -> new IllegalArgumentException("Score not found."));


        Category category = scoreToDelete.getCategory();
        User user = scoreToDelete.getUser();

        //Find users top score before deleting
        Score currentTopScore = getTopScoreForUser(category, user).orElse(null);

        //Determine if deleted score was top score
        boolean wasTopScore = currentTopScore != null && currentTopScore.getScoreId().equals(scoreToDelete.getScoreId());

        //Remove the score
        scoreRepository.delete(scoreToDelete);
        scoreRepository.flush();

        //Update the global baseline
        if(wasTopScore)
        {
            //Remove the old score
            updateGlobalBaseline(category, scoreToDelete.getScore(), true);

            //Find new top score
            Score fallbackTopScore = getTopScoreForUser(category, user).orElse(null);

            //Update the baseline
            if(fallbackTopScore != null)
            {
                updateGlobalBaseline(category, fallbackTopScore.getScore(), false);
            }
        }
    }

    public Page<Score> getGlobalTopScores(UUID categoryId, int page, int size)
    {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found."));

        Pageable pageable = PageRequest.of(page, size);

        if(category.getSortOrder())
        {
            return scoreRepository.findTopScoresPerUserDesc(categoryId, pageable);
        }
        else
        {
            return scoreRepository.findTopScoresPerUserAsc(categoryId, pageable);
        }
    }

    public Page<Score> getFilteredTopScores(UUID categoryId, Map<String, String> tags, int page, int size)
    {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found."));

        // Serialize the filter tags to JSON for Postgres JSONB @> operator
        String tagsJson = serializeTagsToJson(tags);

        Pageable pageable = PageRequest.of(page, size);

        if(category.getSortOrder())
        {
            return scoreRepository.findFilteredTopScoresPerUserDesc(categoryId, tagsJson, pageable);
        }
        else
        {
            return scoreRepository.findFilteredTopScoresPerUserAsc(categoryId, tagsJson, pageable);
        }
    }


    //------------------------------------------------------------------------------------------------
    // Private Methods
    //------------------------------------------------------------------------------------------------
    private void updateGlobalBaseline(Category category, Float score, Boolean removal)
    {
        //Fetch existing baseline
        GlobalBaseline baseline = globalBaselineRepository.findByCategory(category)
                .orElseThrow(() -> new IllegalArgumentException("Category not found."));

        Integer oldN = baseline.getSampleSize();
        if(oldN == null) oldN = 0;

        //Fail if removing and no entries
        if(oldN == 0 && removal) return;

        Float oldMean = baseline.getMean();
        if(oldMean == null) oldMean = 0.0f;

        Float oldStdDev = baseline.getStandardDeviation();
        if(oldStdDev == null) oldStdDev = 0.0f;

        //Generate new values
        int newN = removal ? oldN - 1 : oldN + 1;
        float newMean;
        float newStdDev;

        if(removal)
        {
            if(newN == 0)
            {
                //No entries, no mean or std dev
                newMean = 0.0f;
                newStdDev = 0.0f;
            }
            else if(newN == 1)
            {
                //1 Entry, update mean, no std dev
                newMean = (oldMean * oldN - score) / newN;
                newStdDev = 0.0f;
            }
            else
            {
                //Find new mean
                newMean = ((oldMean * oldN) - score) / newN;

                //Get old variance
                float oldVariance = oldStdDev * oldStdDev;

                //Get sum squared difference
                float oldM2 = oldVariance * (oldN - 1);

                //Find new sum squared difference
                float newM2 = oldM2 - ((score - oldMean) * (score - newMean));

                //Ensure no negative
                newM2 = Math.max(0.0f, newM2);

                //Find new std dev
                float newVariance = newM2 / (newN - 1);
                newStdDev = (float)Math.sqrt(newVariance);
            }
        }
        else
        {
            if(newN == 1)
            {
                newMean = score;
                newStdDev = 0.0f;
            }
            else
            {
                //Find new mean
                newMean = oldMean + ((score - oldMean) / newN);

                //Find old variance
                float oldVariance = oldStdDev * oldStdDev;

                //Find old sum squared difference
                float oldM2 = oldVariance * (oldN - 1);

                //Find new sum squared difference
                float newM2 = oldM2 + ((score - oldMean) * (score - newMean));

                //Calculate new std dev
                float newVariance = newM2 / (newN - 1);
                newStdDev = (float) Math.sqrt(newVariance);
            }
        }

        //Update baseline record
        baseline.setMean(newMean);
        baseline.setStandardDeviation(newStdDev);
        baseline.setSampleSize(newN);

        //Save record
        globalBaselineRepository.save(baseline);
    }

    private Optional<Score> getTopScoreForUser(Category category, User user)
    {
        if(category.getSortOrder())
        {
            return scoreRepository.findFirstByCategoryAndUserOrderByScoreDesc(category, user);
        }
        else
        {
            return scoreRepository.findFirstByCategoryAndUserOrderByScoreAsc(category, user);
        }
    }

    private String serializeTagsToJson(Map<String, String> tags)
    {
        if(tags == null || tags.isEmpty()) return "{}";
        try
        {
            return objectMapper.writeValueAsString(tags);
        }
        catch(JacksonException e)
        {
            throw new IllegalArgumentException("Failed to serialize tags to JSON.", e);
        }
    }

    //------------------------------------------------------------------------------------------------
    // Private Variables
    //------------------------------------------------------------------------------------------------
    private final ScoreRepository scoreRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final GlobalBaselineRepository globalBaselineRepository;
    private final ObjectMapper objectMapper;
}
