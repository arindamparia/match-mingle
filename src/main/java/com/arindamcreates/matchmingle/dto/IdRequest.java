package com.arindamcreates.matchmingle.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IdRequest {
    @Pattern(regexp = "^[0-9a-f]{24}$", message = "Invalid ID format")
    private String id;
}