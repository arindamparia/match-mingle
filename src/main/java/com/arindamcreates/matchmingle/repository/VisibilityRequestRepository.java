package com.arindamcreates.matchmingle.repository;

import com.arindamcreates.matchmingle.model.VisibilityRequest;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VisibilityRequestRepository extends MongoRepository<VisibilityRequest, ObjectId> {

  Optional<List<VisibilityRequest>> findBySenderAndReceiverOrReceiverAndSender(
      ObjectId sender, ObjectId receiver, ObjectId receiverAgain, ObjectId senderAgain);

  Optional<VisibilityRequest> findBySenderAndReceiverAndType(
      ObjectId sender, ObjectId receiver, String Type);

  List<VisibilityRequest> findBySenderOrReceiver(ObjectId userId, ObjectId userIdAgain);
}
