/**
 * Filename: .java
 * Author:
 * Description:
 */

//----------------------------------------------------------------------------------------------------
// Package
//----------------------------------------------------------------------------------------------------
package com.charble.backend.controller;

//----------------------------------------------------------------------------------------------------
// Imports
//----------------------------------------------------------------------------------------------------

import com.charble.backend.dto.UserResponse;
import com.charble.backend.model.User;
import com.charble.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

//----------------------------------------------------------------------------------------------------
// Class Definition
//----------------------------------------------------------------------------------------------------
@RestController
@RequestMapping("/api/v1/users")
public class UserController
{
    //------------------------------------------------------------------------------------------------
    // Constructors
    //------------------------------------------------------------------------------------------------
    public UserController(UserService userService)
    {
        this.userService = userService;
    }

    //------------------------------------------------------------------------------------------------
    // Public Methods
    //------------------------------------------------------------------------------------------------
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody Map<String, Object> request)
    {
        String username = (String)request.get("username");
        String email = (String)request.get("email");
        String passwordHash = (String)request.get("password_hash");
        LocalDate birthday = LocalDate.parse((String)request.get("birthdate"));

        @SuppressWarnings("unchecked")
        Map<String, String> demographics = (Map<String, String>)request.get("demographics");

        User newUser = userService.createUser(username, email, passwordHash, birthday, demographics);

        UserResponse userResponse = UserResponse.fromUser(newUser, "User created successfully");
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID userId)
    {
        User user = userService.getUser(userId);

        UserResponse response = UserResponse.fromUser(user, null);
        return ResponseEntity.ok(response);
    }

    //------------------------------------------------------------------------------------------------
    // Private Variables
    //------------------------------------------------------------------------------------------------
    private final UserService userService;
}
