package com.arindamcreates.matchmingle.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

//Used for dto of sending request body
@Data
public class SentRequest {
    @NotNull
    private String senderId;

    @NotNull
    private String receiverId;
}
