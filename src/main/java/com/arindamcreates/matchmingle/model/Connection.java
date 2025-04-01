package com.arindamcreates.matchmingle.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document("Connection")
public class Connection {

  @Field("_id")
  @Id
  private ObjectId id;

  private ObjectId user1;
  private ObjectId user2;
  private LocalDateTime connectionTime;
  private Boolean numberShow;
  private Boolean emailShow;

  public static Connection createConnection(ObjectId user1, ObjectId user2) {

    return Connection.builder()
        .user1(user1)
        .user2(user2)
        .connectionTime(LocalDateTime.now())
        .emailShow(false)
        .numberShow(false)
        .build();
  }
}
