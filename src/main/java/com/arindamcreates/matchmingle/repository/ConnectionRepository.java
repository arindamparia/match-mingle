package com.arindamcreates.matchmingle.repository;

import com.arindamcreates.matchmingle.model.Connection;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ConnectionRepository extends MongoRepository<Connection, ObjectId> {

  @Query(
      "{ $or: [ { $and: [ { 'user1': ?0 }, { 'user2': ?1 } ] }, { $and: [ { 'user1': ?1 }, { 'user2': ?0 } ] } ] }")
  Optional<Connection> findByUser1AndUser2(ObjectId user1, ObjectId user2);

  // Returns all connections for a user (where they are either user1 or user2)
  List<Connection> findByUser1OrUser2(ObjectId userId, ObjectId userIdAgain);
}
