/**
 * Filename: UserServiceUnitTest.java
 * Author: Wilson Jimenez
 * Description: Manual API-backed unit test helpers for UserService
 */

//----------------------------------------------------------------------------------------------------
// Package
//----------------------------------------------------------------------------------------------------
package com.statit.backend.unit_tests;

//----------------------------------------------------------------------------------------------------
// Imports
//----------------------------------------------------------------------------------------------------
import com.statit.backend.model.User;
import com.statit.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

//----------------------------------------------------------------------------------------------------
// Class Definition
//----------------------------------------------------------------------------------------------------
@RestController
@RequestMapping("api/unit_tests/user")
public class UserServiceUnitTest
{
    //------------------------------------------------------------------------------------------------
    // Constructors
    //------------------------------------------------------------------------------------------------
    public UserServiceUnitTest(UserService userService)
    {
        this.userService = userService;
    }

    //------------------------------------------------------------------------------------------------
    // Public Methods
    //------------------------------------------------------------------------------------------------
    @PostMapping("/generate-user")
    public ResponseEntity<Map<String, Object>> generateUser()
    {
        String token = UUID.randomUUID().toString().substring(0, 8);
        String username = "UnitTestUser_" + token;
        String email = "unit_user_" + token + "@myglobalranking.com";

        Map<String, String> demographics = new HashMap<>();
        demographics.put("region", "NORTH_AMERICA");
        demographics.put("sex", "MALE");

        User user = userService.createUser(
                username,
                email,
                "unit-test-password",
                LocalDate.of(2001, 1, 1),
                demographics
        );

        return ResponseEntity.ok(toUserSummary(user));
    }

    @GetMapping("/all-users")
    public ResponseEntity<Map<String, Object>> getAllUsers()
    {
        List<User> users = userService.getAllUsers();
        List<Map<String, Object>> summaries = new ArrayList<>();

        for(User user : users)
        {
            summaries.add(toUserSummary(user));
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("count", summaries.size());
        response.put("users", summaries);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete-user")
    public ResponseEntity<Map<String, Object>> deleteUser(@RequestParam UUID userId)
    {
        userService.deleteUser(userId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "User deleted.");
        response.put("userId", userId);
        return ResponseEntity.ok(response);
    }

    //------------------------------------------------------------------------------------------------
    // Private Methods
    //------------------------------------------------------------------------------------------------
    private Map<String, Object> toUserSummary(User user)
    {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("userId", user.getUserId());
        summary.put("username", user.getUsername());
        summary.put("email", user.getEmail());
        summary.put("birthday", user.getBirthday());
        summary.put("demographics", user.getDemographics());
        summary.put("createdAt", user.getCreatedAt());
        return summary;
    }

    //------------------------------------------------------------------------------------------------
    // Private Variables
    //------------------------------------------------------------------------------------------------
    private final UserService userService;
}
