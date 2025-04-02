package com.arindamcreates.matchmingle.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "visibility_requests")
@CompoundIndex(
    name = "request_lookup_idx",
    def = "{'requesterId': 1, 'requestedToId': 1, 'type': 1, 'status': 1}")
public class VisibilityRequest {
  @Field("_id")
  @Id
  private ObjectId id;

  private ObjectId sender; // User who requested
  private ObjectId receiver; // User who received request

  private RequestType type; // PHONE or EMAIL
  private RequestStatus status; // PENDING, ACCEPTED, REJECTED

  private LocalDateTime requestTime;
  private LocalDateTime responseTime;

  public enum RequestType {
    PHONE,
    EMAIL
  }

  public enum RequestStatus {
    PENDING,
    ACCEPTED,
    REJECTED
  }
}
