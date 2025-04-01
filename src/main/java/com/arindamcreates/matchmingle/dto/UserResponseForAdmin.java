package com.arindamcreates.matchmingle.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseForAdmin {

    private String firstName;

    private String lastName;

    private String gender;

    private String location;

    private String phone;

    private String email;

    private String imageUrl ;

    private String tagLine;

    private String summary;

    private Boolean userDetailsProvided;

    private Boolean userLocked;

    private String role;

    private Set<ObjectId> incomingRequests = new HashSet<>();

    private Set<ObjectId> outgoingRequests = new HashSet<>();

    private Set<ObjectId> connections = new HashSet<>();

}
