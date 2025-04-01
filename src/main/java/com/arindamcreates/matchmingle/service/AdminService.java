package com.arindamcreates.matchmingle.service;

import com.arindamcreates.matchmingle.constant.Constants;
import com.arindamcreates.matchmingle.dto.IdRequest;
import com.arindamcreates.matchmingle.dto.UserResponseForAdmin;
import com.arindamcreates.matchmingle.model.User;
import com.arindamcreates.matchmingle.utils.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminService {
  private final UserService userService;
  private final AuthUtil authUtil;

  public UserResponseForAdmin findUserByEmail(String email) {
    User targetedUser = userService.findUserByEmail(email);
    return UserResponseForAdmin.builder()
        .firstName(targetedUser.getFirstName())
        .lastName(targetedUser.getLastName())
        .location(targetedUser.getLocation())
        .gender(targetedUser.getGender())
        .tagLine(targetedUser.getTagLine())
        .summary(targetedUser.getSummary())
        .imageUrl(targetedUser.getImageUrl())
        .phone(targetedUser.getPhone())
        .email(targetedUser.getEmail())
        .role(targetedUser.getRole())
        .connections(targetedUser.getConnections())
        .userDetailsProvided(targetedUser.getUserDetailsProvided())
        .userLocked(targetedUser.getUserLocked())
        .incomingRequests(targetedUser.getIncomingRequests())
        .outgoingRequests(targetedUser.getOutgoingRequests())
        .build();
  }

  public void lockUser(@Valid IdRequest idRequest) {
    updateUserLockStatus(idRequest, true);
  }

  public void unLockUser(@Valid IdRequest idRequest) {
    updateUserLockStatus(idRequest, false);
  }

  private void updateUserLockStatus(@Valid IdRequest idRequest, boolean lockStatus) {
    try {
      String loggedInUserEmail = authUtil.getCurrentUserEmail();
      User user = userService.checkUserDoesExist(new ObjectId(idRequest.getId()));

      if (user.getEmail().equals(loggedInUserEmail)) {
        String errorMessage =
            lockStatus ? Constants.CANNOT_SELF_LOCK : Constants.CANNOT_SELF_UNLOCK;
        throw new IllegalArgumentException(errorMessage);
      }

      boolean currentLockStatus = Boolean.TRUE.equals(user.getUserLocked());
      if (currentLockStatus == lockStatus) {
        String errorMessage =
            lockStatus ? Constants.USER_ALREADY_LOCKED : Constants.USER_ALREADY_UNLOCKED;
        throw new IllegalArgumentException(errorMessage);
      }

      user.setUserLocked(lockStatus);
      userService.saveUser(user);

      String action = lockStatus ? "locked" : "unlocked";
      log.info("User with ID {} has been {}", idRequest.getId(), action);

    } catch (Exception ex) {
      String action = lockStatus ? "locking" : "unlocking";
      String errorMessage =
          String.format("Error occurred while %s user: %s", action, idRequest.getId());
      log.error(errorMessage, ex);
      throw new DataAccessResourceFailureException(errorMessage, ex);
    }
  }
}
