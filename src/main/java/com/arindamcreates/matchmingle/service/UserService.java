package com.arindamcreates.matchmingle.service;

import com.arindamcreates.matchmingle.dto.*;
import com.arindamcreates.matchmingle.exception.DataAlreadyExistException;
import com.arindamcreates.matchmingle.exception.DataNotFoundException;
import com.arindamcreates.matchmingle.model.User;
import com.arindamcreates.matchmingle.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class UserService {
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



    private void checkUserDoesNotExist(String email,String phone) {
        Optional<List<User>> existingUserOptional = userRepository.findByEmailOrPhone(email,phone);
        if (existingUserOptional.isPresent() && !existingUserOptional.get().isEmpty()) {
            throw new DataAlreadyExistException("User already exists");
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

}

