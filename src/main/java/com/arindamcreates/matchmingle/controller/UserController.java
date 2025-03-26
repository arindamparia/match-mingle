package com.arindamcreates.matchmingle.controller;

import com.arindamcreates.matchmingle.dto.*;
import com.arindamcreates.matchmingle.model.User;
import com.arindamcreates.matchmingle.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@Validated
@RequestMapping("/v1/user")
public class UserController {

    private UserService userService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public User addUser(@RequestBody @Valid UserRequest userRequest) {
        return userService.addUser(userRequest);
    }

    @GetMapping("/{email}")
    public User getUserByEmail(
            @PathVariable @Email(message = "Invalid email format") String email) {
        return userService.findUserByEmail(email);
    }

}

