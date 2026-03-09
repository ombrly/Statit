/**
 * Filename: LeaderboardSnapshotResponse.java
 * Author: Wilson Jimenez
 * Description: DTO for combined leaderboard and baseline snapshot responses.
 */

//----------------------------------------------------------------------------------------------------
// Package
//----------------------------------------------------------------------------------------------------
package com.statit.backend.dto;

//----------------------------------------------------------------------------------------------------
// Imports
//----------------------------------------------------------------------------------------------------
import java.util.List;
import java.util.UUID;

//----------------------------------------------------------------------------------------------------
// Record Definition
//----------------------------------------------------------------------------------------------------
public record LeaderboardSnapshotResponse(UUID categoryId,
                                          LeaderboardResponse leaderboard,
                                          List<GlobalBaselineResponse> baselines)
{
}
