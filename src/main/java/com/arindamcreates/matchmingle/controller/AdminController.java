package com.arindamcreates.matchmingle.controller;

import com.arindamcreates.matchmingle.dto.IdRequest;
import com.arindamcreates.matchmingle.dto.UserResponseForAdmin;
import com.arindamcreates.matchmingle.service.AdminService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@Validated
@RequestMapping("/v1/admin")
public class AdminController {
  private AdminService adminService;

  @PostMapping("/lock-user")
  @ResponseStatus(HttpStatus.OK)
  public void lockUser(@RequestBody @Valid IdRequest idRequest) {
    adminService.lockUser(idRequest);
  }

  @PostMapping("/unlock-user")
  @ResponseStatus(HttpStatus.OK)
  public void unLockUser(@RequestBody @Valid IdRequest idRequest) {
    adminService.unLockUser(idRequest);
  }

  @GetMapping("/get-user")
  @ResponseStatus(HttpStatus.OK)
  public UserResponseForAdmin getUserByEmail(
      @RequestParam @Email(message = "Invalid email format") String email) {
    return adminService.findUserByEmail(email);
  }

  @DeleteMapping("/delete-user")
  @ResponseStatus(HttpStatus.OK)
  public void deleteUser(@RequestBody @Valid IdRequest idRequest) {
    adminService.deleteUserWithBatchProcessing(idRequest.getId());
  }
}
