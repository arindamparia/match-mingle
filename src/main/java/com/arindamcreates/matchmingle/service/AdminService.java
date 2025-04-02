package com.arindamcreates.matchmingle.service;

import com.arindamcreates.matchmingle.constant.Constants;
import com.arindamcreates.matchmingle.dto.IdRequest;
import com.arindamcreates.matchmingle.dto.UserResponseForAdmin;
import com.arindamcreates.matchmingle.model.Connection;
import com.arindamcreates.matchmingle.model.User;
import com.arindamcreates.matchmingle.model.VisibilityRequest;
import com.arindamcreates.matchmingle.repository.ConnectionRepository;
import com.arindamcreates.matchmingle.repository.UserRepository;
import com.arindamcreates.matchmingle.repository.VisibilityRequestRepository;
import com.arindamcreates.matchmingle.utils.AuthUtil;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminService {
  private final UserService userService;
  private final AuthUtil authUtil;
  private final MongoTemplate mongoTemplate;

  private static final int BATCH_SIZE = 100;
  private final ConnectionRepository connectionRepository;
  private final UserRepository userRepository;
  private final VisibilityRequestRepository visibilityRequestRepository;

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

  public void deleteUserWithBatchProcessing(String uId) {
    ObjectId userId = new ObjectId(uId);
    // 1. Find the user to delete
    User userToDelete = userService.checkUserDoesExist(userId);
    if (userToDelete.getEmail().equals(authUtil.getCurrentUserEmail())) {
      throw new IllegalArgumentException(Constants.CANNOT_SELF_DELETE);
    }
    List<Connection> connections = connectionRepository.findByUser1OrUser2(userId, userId);
    if (connections != null && !connections.isEmpty()) {
      connectionRepository.deleteAll(connections);
    }

    List<VisibilityRequest> visibilityRequests =
        visibilityRequestRepository.findBySenderOrReceiver(userId, userId);
    if (visibilityRequests != null && !visibilityRequests.isEmpty()) {
      visibilityRequestRepository.deleteAll(visibilityRequests);
    }

    // 2. Collect all relationships that need to be updated
    Set<ObjectId> connectionsToUpdate = new HashSet<>(userToDelete.getConnections());
    Set<ObjectId> incomingToUpdate = new HashSet<>(userToDelete.getIncomingRequests());
    Set<ObjectId> outgoingToUpdate = new HashSet<>(userToDelete.getOutgoingRequests());

    // 3. Process relationship updates in batches
    processRelationshipBatches(connectionsToUpdate, userId, "connections");
    processRelationshipBatches(incomingToUpdate, userId, "outgoingRequests");
    processRelationshipBatches(outgoingToUpdate, userId, "incomingRequests");

    // 4. Delete the user
    userRepository.delete(userToDelete);

    // 5. Log summary of operation
    int totalRelationshipsProcessed =
        connectionsToUpdate.size() + incomingToUpdate.size() + outgoingToUpdate.size();

    System.out.println(
        "Successfully deleted user "
            + userId
            + ". Cleaned up "
            + totalRelationshipsProcessed
            + " relationship references.");
  }

  private void processRelationshipBatches(
      Set<ObjectId> userIds, ObjectId userIdToRemove, String fieldName) {
    if (userIds.isEmpty()) {
      return;
    }

    List<ObjectId> batch = new ArrayList<>(BATCH_SIZE);
    int totalProcessed = 0;

    for (ObjectId id : userIds) {
      batch.add(id);

      // When batch is full or this is the last item, process the batch
      if (batch.size() >= BATCH_SIZE || totalProcessed == userIds.size() - 1) {
        updateBatch(batch, userIdToRemove, fieldName);
        totalProcessed += batch.size();
        System.out.println(
            "Processed batch of "
                + batch.size()
                + " updates for "
                + fieldName
                + ". Progress: "
                + totalProcessed
                + "/"
                + userIds.size());
        batch.clear();
      }
    }

    // Process any remaining items
    if (!batch.isEmpty()) {
      updateBatch(batch, userIdToRemove, fieldName);
    }
  }

  /** Updates a batch of user documents by pulling the specified user ID from the given field */
  private void updateBatch(List<ObjectId> userIds, ObjectId userIdToRemove, String fieldName) {
    Query query = new Query(Criteria.where("_id").in(userIds));
    Update update = new Update().pull(fieldName, userIdToRemove);
    mongoTemplate.updateMulti(query, update, User.class);
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
