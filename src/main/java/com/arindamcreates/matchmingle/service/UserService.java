package com.arindamcreates.matchmingle.service;

import com.arindamcreates.matchmingle.constant.Constants;
import com.arindamcreates.matchmingle.dto.*;
import com.arindamcreates.matchmingle.exception.DataAlreadyExistException;
import com.arindamcreates.matchmingle.exception.DataNotFoundException;
import com.arindamcreates.matchmingle.model.Connection;
import com.arindamcreates.matchmingle.model.User;
import com.arindamcreates.matchmingle.model.VisibilityRequest;
import com.arindamcreates.matchmingle.repository.ConnectionRepository;
import com.arindamcreates.matchmingle.repository.UserRepository;
import com.arindamcreates.matchmingle.repository.VisibilityRequestRepository;
import com.arindamcreates.matchmingle.utils.AuthUtil;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

  private static final int BATCH_SIZE = 100;
  private final AuthUtil authUtil;
  private final ConnectionRepository connectionRepository;
  private final UserRepository userRepository;
  private final VisibilityRequestRepository visibilityRequestRepository;

  public User addUserDetails(UserRequest userRequest) {
    String loggedInUserEmail = authUtil.getCurrentUserEmail();
    Optional<List<User>> existingUserOptional =
        userRepository.findByEmailOrPhone(loggedInUserEmail, userRequest.getPhone());
    if (existingUserOptional.isEmpty()) {
      throw new DataNotFoundException("User doesn't exist");
    }
    if (!existingUserOptional.get().isEmpty() && existingUserOptional.get().size() > 1) {
      throw new DataAlreadyExistException("Different two users exist with same email or phone");
    }
    User savedUser = updateAndSaveUser(existingUserOptional.get().getFirst(), userRequest);
    log.info("User details for {} added successfully", savedUser.getId());
    return savedUser;
  }

  public User findUserByEmail(String email) {
    try {
      return userRepository
          .findByEmail(email)
          .orElseThrow(() -> new DataNotFoundException("No user exists for requested input"));
    } catch (DataAccessResourceFailureException ex) {
      log.error("Error occurred while fetching user data");
      throw ex;
    }
  }

  public User saveUser(User user) {
    try {
      return userRepository.save(user);
    } catch (Exception ex) {
      log.error("Error occurred while saving user");
      throw new DataAccessResourceFailureException(Constants.CANNOT_SAVE_USER, ex);
    }
  }

  public User checkUserDoesExist(ObjectId id) {
    try {
      return userRepository
          .findById(id)
          .orElseThrow(() -> new DataNotFoundException(Constants.USER_NOT_FOUND));
    } catch (Exception ex) {
      log.error(Constants.USER_NOT_FOUND);
      throw new DataAccessResourceFailureException("Constants.USER_NOT_FOUND", ex);
    }
  }

  public UserResponse findUserById(@Valid IdRequest id) {
    User targetedUser = checkUserDoesExist(new ObjectId(id.getId()));
    User loggedInUser = findUserByEmail(authUtil.getCurrentUserEmail());
    UserResponse userResponse =
        UserResponse.builder()
            .firstName(targetedUser.getFirstName())
            .lastName(targetedUser.getLastName())
            .location(targetedUser.getLocation())
            .gender(targetedUser.getGender())
            .tagLine(targetedUser.getTagLine())
            .summary(targetedUser.getSummary())
            .imageUrl(targetedUser.getImageUrl())
            .build();
    Connection connection =
        findConnectionByUser1AndUser2(loggedInUser.getId(), targetedUser.getId());
    if (Boolean.TRUE.equals(connection.getEmailShow())) {
      userResponse = userResponse.toBuilder().email(targetedUser.getEmail()).build();
    }
    return userResponse;
  }

  public void sendRequest(String id) {
    handleRequestForConnection(id, RequestAction.SEND);
  }

  public void acceptRequest(String id) {
    handleRequestForConnection(id, RequestAction.ACCEPT);
  }

  public void denyRequest(String id) {
    handleRequestForConnection(id, RequestAction.DENY);
  }

  public void removeConnection(String id) {
    handleRequestForConnection(id, RequestAction.REMOVE);
  }

  public void showEmail(String id) {
    handleRequestForPermission(id, RequestAction.SHOW_EMAIL);
  }

  public void showNumber(String id) {
    handleRequestForPermission(id, RequestAction.SHOW_NUMBER);
  }

  public void requestEmail(String id) {
    handleRequestForPermission(id, RequestAction.REQUEST_EMAIL);
  }

  public void requestNumber(String id) {
    handleRequestForPermission(id, RequestAction.REQUEST_NUMBER);
  }

  private void handleRequestForConnection(String id, RequestAction action) {
    try {
      String loggedInUserEmail = authUtil.getCurrentUserEmail();
      User targetedUser = checkUserDoesExist(new ObjectId(id));
      if (loggedInUserEmail.equals(targetedUser.getEmail())) {
        throw new DataNotFoundException(getSelfActionErrorMessage(action));
      }
      User loggedInUser = findUserByEmail(loggedInUserEmail);

      switch (action) {
        case SEND -> processSendRequest(loggedInUser, targetedUser);
        case ACCEPT -> processAcceptRequest(targetedUser, loggedInUser);
        case DENY -> processDenyRequest(targetedUser, loggedInUser);
        case REMOVE -> processRemoveConnection(loggedInUser, targetedUser);
      }
    } catch (Exception ex) {
      log.error("Error occurred while processing request action: {}", action);
      throw new DataAccessResourceFailureException(
          "Error occurred while processing request action: " + action, ex);
    }
  }

  private void handleRequestForPermission(String id, RequestAction action) {
    try {
      String loggedInUserEmail = authUtil.getCurrentUserEmail();
      User targetedUser = checkUserDoesExist(new ObjectId(id));
      if (loggedInUserEmail.equals(targetedUser.getEmail())) {
        throw new DataNotFoundException(getSelfActionErrorMessage(action));
      }
      User loggedInUser = findUserByEmail(loggedInUserEmail);

      if (loggedInUser.getEmail().equals(targetedUser.getEmail())) {
        throw new DataNotFoundException(getSelfActionErrorMessage(action));
      }

      switch (action) {
        case REQUEST_EMAIL -> processRequestPermission(loggedInUser, targetedUser, true);
        case REQUEST_NUMBER -> processRequestPermission(loggedInUser, targetedUser, false);
        case SHOW_EMAIL -> processShowPermission(targetedUser, loggedInUser, true);
        case SHOW_NUMBER -> processShowPermission(targetedUser, loggedInUser, false);
      }
    } catch (Exception ex) {
      log.error("Error occurred while processing requested permission: {}", action);
      throw new DataAccessResourceFailureException(
          "Error occurred while processing requested permission: " + action, ex);
    }
  }

  private void processSendRequest(User sender, User receiver) {
    if (sender.getConnections().contains(receiver.getId())
        || receiver.getConnections().contains(sender.getId())) {
      throw new DataAlreadyExistException("Connection already exists");
    }

    if (sender.getOutgoingRequests().contains(receiver.getId())
        || receiver.getIncomingRequests().contains(sender.getId())) {
      throw new DataAlreadyExistException("Request already sent");
    }

    if (sender.getIncomingRequests().contains(receiver.getId())
        || receiver.getOutgoingRequests().contains(sender.getId())) {
      throw new DataAlreadyExistException("Request already received");
    }

    sender.getOutgoingRequests().add(receiver.getId());
    receiver.getIncomingRequests().add(sender.getId());
    userRepository.save(sender);
    userRepository.save(receiver);
  }

  private void processAcceptRequest(User sender, User receiver) {
    if (!sender.getOutgoingRequests().contains(receiver.getId())
        || !receiver.getIncomingRequests().contains(sender.getId())) {
      throw new DataNotFoundException(Constants.REQUEST_NOT_FOUND);
    }

    sender.getOutgoingRequests().remove(receiver.getId());
    receiver.getIncomingRequests().remove(sender.getId());
    sender.getConnections().add(receiver.getId());
    receiver.getConnections().add(sender.getId());
    Connection connection = Connection.createConnection(sender.getId(), receiver.getId());
    connectionRepository.save(connection);
    userRepository.save(sender);
    userRepository.save(receiver);
  }

  private void processDenyRequest(User sender, User receiver) {
    if (!sender.getOutgoingRequests().contains(receiver.getId())
        || !receiver.getIncomingRequests().contains(sender.getId())) {
      throw new DataNotFoundException(Constants.REQUEST_NOT_FOUND);
    }

    sender.getOutgoingRequests().remove(receiver.getId());
    receiver.getIncomingRequests().remove(sender.getId());
    userRepository.save(sender);
    userRepository.save(receiver);
  }

  private void processRemoveConnection(User sender, User receiver) {
    if (!sender.getConnections().contains(receiver.getId())
        || !receiver.getConnections().contains(sender.getId())) {
      throw new DataNotFoundException(Constants.CONNECTION_NOT_FOUND);
    }

    sender.getConnections().remove(receiver.getId());
    receiver.getConnections().remove(sender.getId());
    Connection connection = findConnectionByUser1AndUser2(sender.getId(), receiver.getId());
    try {
      Optional<List<VisibilityRequest>> visibilityRequestOptional =
          findVisibilityRequestsByUser1AndUser2(sender.getId(), receiver.getId());
      if (visibilityRequestOptional.isPresent() && !visibilityRequestOptional.get().isEmpty())
        visibilityRequestRepository.deleteAll(visibilityRequestOptional.get());
      connectionRepository.delete(connection);
      userRepository.save(sender);
      userRepository.save(receiver);
    } catch (DataAccessResourceFailureException ex) {
      log.error("Error occurred while deleting connection");
      throw ex;
    }
  }

  private void processShowPermission(User sender, User receiver, boolean isEmail) {
    VisibilityRequest visibilityRequest =
        visibilityRequestRepository
            .findBySenderAndReceiverAndType(
                sender.getId(), receiver.getId(), isEmail ? "EMAIL" : "PHONE")
            .orElseThrow(() -> new DataNotFoundException(Constants.REQUEST_NOT_FOUND));
    Connection connection = findConnectionByUser1AndUser2(sender.getId(), receiver.getId());
    if (isEmail) {
      connection.setEmailShow(true);
    } else {
      connection.setNumberShow(true);
    }
    visibilityRequestRepository.delete(visibilityRequest);
    connectionRepository.save(connection);
  }

  private void processRequestPermission(User sender, User receiver, boolean isEmail) {
    Connection connection = findConnectionByUser1AndUser2(sender.getId(), receiver.getId());
    if (isEmail) {
      if (Boolean.TRUE.equals(connection.getEmailShow())) {
        throw new DataAlreadyExistException("Email already shared");
      }
    } else {
      if (Boolean.TRUE.equals(connection.getNumberShow())) {
        throw new DataAlreadyExistException("Phone number already shared");
      }
    }
    if (getVisibilityRequest(sender.getId(), receiver.getId(), isEmail ? "EMAIL" : "PHONE")
        .isPresent()) {
      throw new DataAlreadyExistException("Request already exists");
    }
    VisibilityRequest visibilityRequest =
        VisibilityRequest.builder()
            .sender(sender.getId())
            .receiver(receiver.getId())
            .type(
                isEmail ? VisibilityRequest.RequestType.EMAIL : VisibilityRequest.RequestType.PHONE)
            .status(VisibilityRequest.RequestStatus.PENDING)
            .requestTime(LocalDateTime.now())
            .build();
    visibilityRequestRepository.save(visibilityRequest);
  }

  private String getSelfActionErrorMessage(RequestAction action) {
    return switch (action) {
      case SEND -> Constants.CANNOT_SELF_REQUEST;
      case ACCEPT -> Constants.CANNOT_SELF_ACCEPT;
      case DENY -> Constants.CANNOT_SELF_DENY;
      case REMOVE -> Constants.CANNOT_SELF_REMOVE;
      case SHOW_EMAIL, SHOW_NUMBER, REQUEST_EMAIL, REQUEST_NUMBER ->
          Constants.CANNOT_SELF_PERMISSION;
    };
  }

  private Connection findConnectionByUser1AndUser2(ObjectId user1, ObjectId user2) {
    try {
      return connectionRepository
          .findByUser1AndUser2(user1, user2)
          .orElseThrow(() -> new DataNotFoundException(Constants.CONNECTION_NOT_FOUND));
    } catch (DataAccessResourceFailureException ex) {
      log.error("Error occurred while fetching connection data");
      throw ex;
    }
  }

  private Optional<List<VisibilityRequest>> findVisibilityRequestsByUser1AndUser2(
      ObjectId sender, ObjectId receiver) {
    return visibilityRequestRepository.findBySenderAndReceiverOrReceiverAndSender(
        sender, receiver, receiver, sender);
  }

  private void checkUserDoesExist(String email, String phone) {
    Optional<List<User>> existingUserOptional = userRepository.findByEmailOrPhone(email, phone);
    if (existingUserOptional.isPresent() && !existingUserOptional.get().isEmpty()) {
      throw new DataAlreadyExistException("User already exists");
    }
  }

  private Optional<VisibilityRequest> getVisibilityRequest(
      ObjectId sender, ObjectId receiver, String type) {
    return visibilityRequestRepository.findBySenderAndReceiverAndType(sender, receiver, type);
  }

  private User updateAndSaveUser(User user, UserRequest userRequest) {
    try {
      return userRepository.save(User.updateUserFrom(user, userRequest));
    } catch (Exception ex) {
      log.error("Error occurred while saving user");
      throw new DataAccessResourceFailureException("Constants.CANNOT_SAVE_USER", ex);
    }
  }

  private enum RequestAction {
    SEND,
    ACCEPT,
    DENY,
    REMOVE,
    SHOW_EMAIL,
    SHOW_NUMBER,
    REQUEST_EMAIL,
    REQUEST_NUMBER
  }
}
