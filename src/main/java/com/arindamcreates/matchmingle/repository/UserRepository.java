package com.arindamcreates.matchmingle.repository;

import com.arindamcreates.matchmingle.model.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository
        extends MongoRepository<User, ObjectId> {
    Optional<List<User>> findByEmailOrPhone(String userEmail, String userPhone);
    Optional<User> findByEmail(String userEmail);
}

