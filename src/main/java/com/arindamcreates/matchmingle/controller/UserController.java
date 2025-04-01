package com.arindamcreates.matchmingle.controller;

import com.arindamcreates.matchmingle.dto.*;
import com.arindamcreates.matchmingle.model.User;
import com.arindamcreates.matchmingle.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
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

    @PostMapping("/addDetails")
    @ResponseStatus(HttpStatus.OK)
    public User addUserDetails(@RequestBody @Valid UserRequest userRequest) {
        return userService.addUserDetails(userRequest);
    }

    @GetMapping("/get-user")
    @ResponseStatus(HttpStatus.OK)
    public UserResponse getUserById(@RequestBody @Valid IdRequest id) {
        return userService.findUserById(id);
    }

    @PostMapping("/send-request")
    @ResponseStatus(HttpStatus.OK)
    public void sentRequest(@RequestBody @Valid IdRequest id) {
        userService.sendRequest(id.getId());
    }
    @PostMapping("/accept-request")
    @ResponseStatus(HttpStatus.OK)
    public void acceptRequest(@RequestBody @Valid IdRequest id) {
        userService.acceptRequest(id.getId());
    }
    @PostMapping("/deny-request")
    @ResponseStatus(HttpStatus.OK)
    public void denyRequest(@RequestBody @Valid IdRequest id) {
        userService.denyRequest(id.getId());
    }
    @PostMapping("/remove-connection")
    @ResponseStatus(HttpStatus.OK)
    public void removeConnection(@RequestBody @Valid IdRequest id) {
        userService.removeConnection(id.getId());
    }
    @PostMapping("/show-email")
    @ResponseStatus(HttpStatus.OK)
    public void showEmail(@RequestBody @Valid String id) {
        userService.showEmail(id);
    }
    @PostMapping("/show-number")
    @ResponseStatus(HttpStatus.OK)
    public void showNumber(@RequestBody @Valid String id) {
        userService.showNumber(id);
    }

}

