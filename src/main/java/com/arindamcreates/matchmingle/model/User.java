package com.arindamcreates.matchmingle.model;

import com.arindamcreates.matchmingle.dto.UserRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Document("User")
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
    private String password;
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
