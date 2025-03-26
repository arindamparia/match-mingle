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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

@Getter
@Setter
@Builder
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

    public static User createUserFrom(UserRequest userRequest) {

        return User.builder()
                .firstName(userRequest.getFirstName())
                .lastName(userRequest.getLastName())
                .gender(userRequest.getGender().toUpperCase())
                .location(userRequest.getLocation())
                .email(userRequest.getEmail())
                .phone(userRequest.getPhone())
                .tagLine(userRequest.getTagLine())
                .summary(userRequest.getSummary())
                .imageUrl(userRequest.getImageUrl())
                .build();
    }

}
