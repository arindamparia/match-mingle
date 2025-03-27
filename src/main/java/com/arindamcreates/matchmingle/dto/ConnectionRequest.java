package com.arindamcreates.matchmingle.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import org.bson.types.ObjectId;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConnectionRequest {

    @NotEmpty
    private ObjectId user1;

    @NotEmpty
    private ObjectId user2;

    private Boolean numberShow = false;

    private  Boolean emailShow = false;

}
