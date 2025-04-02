package com.arindamcreates.matchmingle.repository;

import com.arindamcreates.matchmingle.model.User;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, ObjectId> {
  Optional<List<User>> findByEmailOrPhone(String userEmail, String userPhone);

  Optional<User> findByEmail(String userEmail);

  @Query("{ 'connections': ?0 }")
  @Update("{ $pull: { 'connections': ?0 }}")
  void removeFromConnections(ObjectId userId);

  @Query("{ 'connections': ?0 }")
  @Update("{ $pull: { 'connections': ?0 }}")
  void removeFromIncomingRequests(ObjectId userId);

  @Query("{ 'connections': ?0 }")
  @Update("{ $pull: { 'connections': ?0 }}")
  void removeFromOutgoingRequests(ObjectId userId);
}
