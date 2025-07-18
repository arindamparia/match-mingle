package com.arindamcreates.matchmingle.model;

import com.arindamcreates.matchmingle.dto.UserRequest;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Document("User")
@CompoundIndexes({
  @CompoundIndex(name = "connections_index", def = "{'connections': 1}", sparse = true),
  @CompoundIndex(name = "incoming_requests_index", def = "{'incomingRequests': 1}", sparse = true),
  @CompoundIndex(name = "outgoing_requests_index", def = "{'outgoingRequests': 1}", sparse = true)
})
public class User {

  @Field("_id")
  @Id
  private ObjectId id;

  private String firstName;
  private String lastName;
  private String gender;
  private String location;
  private String email;
  private String phone;
  private String tagLine;
  private String summary;
  private String imageUrl;
  private String role;
  private Boolean userLocked;
  private Boolean userDetailsProvided;
  private Set<ObjectId> incomingRequests = new HashSet<>();
  private Set<ObjectId> outgoingRequests = new HashSet<>();
  private Set<ObjectId> connections = new HashSet<>();

  public static User updateUserFrom(User user, UserRequest userRequest) {

    return user.toBuilder()
        .firstName(userRequest.getFirstName())
        .lastName(userRequest.getLastName())
        .gender(userRequest.getGender().toUpperCase())
        .location(userRequest.getLocation())
        .phone(userRequest.getPhone())
        .tagLine(userRequest.getTagLine())
        .summary(userRequest.getSummary())
        .imageUrl(userRequest.getImageUrl())
        .userDetailsProvided(true)
        .build();
  }
}
