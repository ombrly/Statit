/**
 * Filename: UserCreateRequest.java
 * Author: Charles Bassani
 * Description: DTO for user creation requests
 */

//----------------------------------------------------------------------------------------------------
// Package
//----------------------------------------------------------------------------------------------------
package com.statit.backend.dto;

import java.time.LocalDate;
import java.util.Map;

//----------------------------------------------------------------------------------------------------
// Record Definition
//----------------------------------------------------------------------------------------------------
public record UserCreateRequest(String username,
                                String email,
                                String passwordHash,
                                LocalDate birthday,
                                Map<String, String> demographics) {}
