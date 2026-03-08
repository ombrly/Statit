/**
 * Filename: ScoreService.java
 * Author: Charles Bassani
 * Description: Handles CRUD operations for scores and updates global baselines
 */

//----------------------------------------------------------------------------------------------------
// Package
//----------------------------------------------------------------------------------------------------
package com.charble.backend.service;

//----------------------------------------------------------------------------------------------------
// Imports
//----------------------------------------------------------------------------------------------------
import com.charble.backend.model.Category;
import com.charble.backend.model.GlobalBaseline;
import com.charble.backend.model.Score;
import com.charble.backend.model.User;
import com.charble.backend.repository.CategoryRepository;
import com.charble.backend.repository.GlobalBaselineRepository;
import com.charble.backend.repository.ScoreRepository;
import com.charble.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

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
                        GlobalBaselineRepository globalBaselineRepository)
    {
        this.scoreRepository = scoreRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.globalBaselineRepository = globalBaselineRepository;
    }

    //------------------------------------------------------------------------------------------------
    // Public Methods
    //------------------------------------------------------------------------------------------------
    @Transactional
    public Score submitScore(UUID userId, UUID categoryId, Float scoreValue, Boolean isAnonymous) {

        //Fetch the user and category
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found."));

        Score previousTopScore = getTopScoreForUser(category, user).orElse(null);

        //Save the new score
        Score newScore = new Score(category, user, scoreValue, isAnonymous);
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
        if(oldN == 0 && removal == true) return;

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

    //------------------------------------------------------------------------------------------------
    // Private Variables
    //------------------------------------------------------------------------------------------------
    private final ScoreRepository scoreRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final GlobalBaselineRepository globalBaselineRepository;
}