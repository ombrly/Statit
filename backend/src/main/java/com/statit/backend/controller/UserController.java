/**
 * Filename: .java
 * Author:
 * Description:
 */

//----------------------------------------------------------------------------------------------------
// Package
//----------------------------------------------------------------------------------------------------
package com.statit.backend.controller;

//----------------------------------------------------------------------------------------------------
// Imports
//----------------------------------------------------------------------------------------------------

import com.statit.backend.dto.UserCreateRequest;
import com.statit.backend.dto.UserResponse;
import com.statit.backend.model.User;
import com.statit.backend.service.UserService;
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
    public ResponseEntity<UserResponse> createUser(@RequestBody UserCreateRequest request)
    {
        User newUser = userService.createUser(
                request.username(),
                request.email(),
                request.passwordHash(),
                request.birthday(),
                request.demographics()
        );

        UserResponse userResponse = UserResponse.fromUser(newUser, "User created successfully");
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username)
    {
        User user = userService.getUser(username);

        UserResponse response = UserResponse.fromUser(user, null);
        return ResponseEntity.ok(response);
    }


    //------------------------------------------------------------------------------------------------
    // Private Variables
    //------------------------------------------------------------------------------------------------
    private final UserService userService;
}
