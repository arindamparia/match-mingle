package com.arindamcreates.matchmingle.repository;

import com.arindamcreates.matchmingle.model.Connection;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface ConnectionRepository
        extends MongoRepository<Connection, ObjectId> {
    Optional<List<Connection>> findByUser1AndUser2 (ObjectId user1, ObjectId user2);
}

