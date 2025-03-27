package com.arindamcreates.matchmingle.service;

import com.arindamcreates.matchmingle.dto.*;
import com.arindamcreates.matchmingle.exception.DataAlreadyExistException;
import com.arindamcreates.matchmingle.exception.DataNotFoundException;
import com.arindamcreates.matchmingle.model.Connection;
import com.arindamcreates.matchmingle.model.User;
import com.arindamcreates.matchmingle.repository.ConnectionRepository;
import com.arindamcreates.matchmingle.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@AllArgsConstructor
@Slf4j
public class UserService {
    private static final String USER_NOT_FOUND = "No user exists for the given Id";
    private static final String CONNECTION_NOT_FOUND = "No such connection exists";
    private static final String REQUEST_NOT_FOUND = "Request not found";
    private static final String CANNOT_SELF_REQUEST = "Cannot send request to self";
    private static final String CANNOT_SELF_ACCEPT = "Cannot accept request from self";
    private static final String CANNOT_SELF_DENY = "Cannot deny request from self";
    private static final String CANNOT_SELF_REMOVE = "Cannot remove request from self";
    private static final String CANNOT_SELF_PERMISSION = "Cannot give permission to self";

    private ConnectionRepository connectionRepository;
    private UserRepository userRepository;

    public User addUser(UserRequest userRequest) {
        checkUserDoesNotExist(userRequest.getEmail(),userRequest.getPhone());
        User savedUser = checkSavedUser(userRequest);
        log.info("User {} added successfully for User {}", savedUser.getId());
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

    public void sendRequest(SentRequest sentRequest) {
        handleRequestForConnection(sentRequest, RequestAction.SEND);
    }

    public void acceptRequest(SentRequest sentRequest) {
        handleRequestForConnection(sentRequest, RequestAction.ACCEPT);
    }

    public void denyRequest(SentRequest sentRequest) {
        handleRequestForConnection(sentRequest, RequestAction.DENY);
    }

    public void removeConnection(SentRequest sentRequest) {
        handleRequestForConnection(sentRequest, RequestAction.REMOVE);
    }

    public void showEmail(SentRequest sentRequest) {
        handleRequestForPermission(sentRequest, RequestAction.SHOW_EMAIL);
    }

    public void showNumber(SentRequest sentRequest) {
        handleRequestForPermission(sentRequest, RequestAction.SHOW_NUMBER);
    }

    private void handleRequestForConnection (SentRequest sentRequest, RequestAction action) {
        try {
            if (sentRequest.getSenderId().equals(sentRequest.getReceiverId())) {
                throw new DataNotFoundException(getSelfActionErrorMessage(action));
            }

            User sender = checkUserDoesNotExist(new ObjectId(sentRequest.getSenderId()));
            User receiver = checkUserDoesNotExist(new ObjectId(sentRequest.getReceiverId()));

            switch (action) {
                case SEND -> processSendRequest(sender, receiver, sentRequest);
                case ACCEPT -> processAcceptRequest(sender, receiver, sentRequest);
                case DENY -> processDenyRequest(sender, receiver, sentRequest);
                case REMOVE -> processRemoveConnection(sender, receiver, sentRequest);
                case SHOW_EMAIL -> processShowPermission(sentRequest, true);
                case SHOW_NUMBER -> processShowPermission(sentRequest, false);
            }
        } catch (Exception ex) {
            log.error("Error occurred while processing request action: {}", action);
            throw new DataAccessResourceFailureException(
                    "Error occurred while processing request action: " + action, ex);
        }
    }

    private void handleRequestForPermission (SentRequest sentRequest, RequestAction action) {
        try {
            if (sentRequest.getSenderId().equals(sentRequest.getReceiverId())) {
                throw new DataNotFoundException(getSelfActionErrorMessage(action));
            }

            switch (action) {
                case SHOW_EMAIL -> processShowPermission(sentRequest, true);
                case SHOW_NUMBER -> processShowPermission(sentRequest, false);
            }
        } catch (Exception ex) {
            log.error("Error occurred while processing requested permission: {}", action);
            throw new DataAccessResourceFailureException(
                    "Error occurred while processing requested permission: " + action, ex);
        }
    }

    private void processSendRequest(User sender, User receiver, SentRequest sentRequest) {
        if (sender.getConnections().contains(new ObjectId(sentRequest.getReceiverId())) ||
                receiver.getConnections().contains(new ObjectId(sentRequest.getSenderId()))) {
            throw new DataAlreadyExistException("Connection already exists");
        }

        if (sender.getOutgoingRequests().contains(new ObjectId(sentRequest.getReceiverId())) ||
                receiver.getIncomingRequests().contains(new ObjectId(sentRequest.getSenderId()))) {
            throw new DataAlreadyExistException("Request already sent");
        }

        if (sender.getIncomingRequests().contains(new ObjectId(sentRequest.getReceiverId())) ||
                receiver.getOutgoingRequests().contains(new ObjectId(sentRequest.getSenderId()))) {
            throw new DataAlreadyExistException("Request already received");
        }

        sender.getOutgoingRequests().add(new ObjectId(sentRequest.getReceiverId()));
        receiver.getIncomingRequests().add(new ObjectId(sentRequest.getSenderId()));
        userRepository.save(sender);
        userRepository.save(receiver);
    }

    private void processAcceptRequest(User sender, User receiver, SentRequest sentRequest) {
        if (!sender.getOutgoingRequests().contains(new ObjectId(sentRequest.getReceiverId())) ||
                !receiver.getIncomingRequests().contains(new ObjectId(sentRequest.getSenderId()))) {
            throw new DataNotFoundException(REQUEST_NOT_FOUND);
        }

        sender.getOutgoingRequests().remove(new ObjectId(sentRequest.getReceiverId()));
        receiver.getIncomingRequests().remove(new ObjectId(sentRequest.getSenderId()));
        sender.getConnections().add(new ObjectId(sentRequest.getReceiverId()));
        receiver.getConnections().add(new ObjectId(sentRequest.getSenderId()));
        Connection connection = Connection.createConnection(new ObjectId(sentRequest.getSenderId()), new ObjectId(sentRequest.getReceiverId()));
        connectionRepository.save(connection);
        userRepository.save(sender);
        userRepository.save(receiver);
    }

    private void processDenyRequest(User sender, User receiver, SentRequest sentRequest) {
        if (!sender.getOutgoingRequests().contains(new ObjectId(sentRequest.getReceiverId())) ||
                !receiver.getIncomingRequests().contains(new ObjectId(sentRequest.getSenderId()))) {
            throw new DataNotFoundException(REQUEST_NOT_FOUND);
        }

        sender.getOutgoingRequests().remove(new ObjectId(sentRequest.getReceiverId()));
        receiver.getIncomingRequests().remove(new ObjectId(sentRequest.getSenderId()));
        userRepository.save(sender);
        userRepository.save(receiver);
    }

    private void processRemoveConnection(User sender, User receiver, SentRequest sentRequest) {
        if (!sender.getConnections().contains(new ObjectId(sentRequest.getReceiverId())) ||
                !receiver.getConnections().contains(new ObjectId(sentRequest.getSenderId()))) {
            throw new DataNotFoundException(CONNECTION_NOT_FOUND);
        }

        sender.getConnections().remove(new ObjectId(sentRequest.getReceiverId()));
        receiver.getConnections().remove(new ObjectId(sentRequest.getSenderId()));
        List<Connection> connections = getConnectionsFromUsers(new ObjectId(sentRequest.getSenderId()), new ObjectId(sentRequest.getReceiverId()));
        for (Connection conn : connections) {
            connectionRepository.delete(conn);
        }
        userRepository.save(sender);
        userRepository.save(receiver);
    }

    private void processShowPermission(SentRequest sentRequest, boolean isEmail) {
        boolean doesConnectionExist = false;
        List<Connection> connections = getConnectionsFromUsers(new ObjectId(sentRequest.getSenderId()), new ObjectId(sentRequest.getReceiverId()));
        for (Connection conn : connections) {
            doesConnectionExist = true;
            if (isEmail) {
                conn.setEmailShow(true);
            } else {
                conn.setNumberShow(true);
            }
            connectionRepository.save(conn);
        }
        if (!doesConnectionExist) {
            throw new DataNotFoundException(CONNECTION_NOT_FOUND);
        }
    }

    private String getSelfActionErrorMessage(RequestAction action) {
        return switch (action) {
            case SEND -> CANNOT_SELF_REQUEST;
            case ACCEPT -> CANNOT_SELF_ACCEPT;
            case DENY -> CANNOT_SELF_DENY;
            case REMOVE -> CANNOT_SELF_REMOVE;
            case SHOW_EMAIL, SHOW_NUMBER -> CANNOT_SELF_PERMISSION;
        };
    }

    private Optional<List<Connection>> findByUser1AndUser2(ObjectId user1, ObjectId user2) {
        try {
            return connectionRepository
                    .findByUser1AndUser2(user1,user2);
        } catch (DataAccessResourceFailureException ex) {
            log.error("Error occurred while fetching connection data");
            throw ex;
        }
    }

    private List<Connection> getConnectionsFromUsers(ObjectId user1, ObjectId user2) {
        List<Connection> connections = new ArrayList<>();
        Optional<List<Connection>> connections1 = findByUser1AndUser2(user1, user2);
        Optional<List<Connection>> connections2 = findByUser1AndUser2(user2, user1);
        if (connections1.isPresent() && !connections1.get().isEmpty()) connections.addAll(connections1.get());
        if (connections2.isPresent() && !connections2.get().isEmpty()) connections.addAll(connections2.get());
        return connections;
    }

    private void checkUserDoesNotExist(String email,String phone) {
        Optional<List<User>> existingUserOptional = userRepository.findByEmailOrPhone(email,phone);
        if (existingUserOptional.isPresent() && !existingUserOptional.get().isEmpty()) {
            throw new DataAlreadyExistException("User already exists");
        }
    }

    private User checkUserDoesNotExist(ObjectId id) {
        try {
            return userRepository
                    .findById(id)
                    .orElseThrow(() -> new DataNotFoundException(USER_NOT_FOUND));
        } catch (Exception ex) {
            log.error(USER_NOT_FOUND);
            throw new DataAccessResourceFailureException(
                    "Error occurred while saving user data", ex);
        }
    }

    private User checkSavedUser(UserRequest userRequest) {
        try {
            return userRepository.save(User.createUserFrom(userRequest));
        } catch (Exception ex) {
            log.error("Error occurred while saving user");
            throw new DataAccessResourceFailureException(
                    "Error occurred while saving user data", ex);
        }
    }

    private enum RequestAction {
        SEND, ACCEPT, DENY, REMOVE, SHOW_EMAIL, SHOW_NUMBER
    }

}

