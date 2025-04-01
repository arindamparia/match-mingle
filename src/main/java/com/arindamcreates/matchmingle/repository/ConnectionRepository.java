package com.arindamcreates.matchmingle.repository;

import com.arindamcreates.matchmingle.model.Connection;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConnectionRepository extends MongoRepository<Connection, ObjectId> {

  Optional<Connection> findByUser1AndUser2OrUser2AndUser1(
      ObjectId user1, ObjectId user2, ObjectId user2Again, ObjectId user1Again);

  // Returns all connections for a user (where they are either user1 or user2)
  List<Connection> findByUser1OrUser2(ObjectId userId, ObjectId userIdAgain);
}
