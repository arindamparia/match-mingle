package com.arindamcreates.matchmingle.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {

    @NotEmpty
    @Pattern(
            regexp = "^[A-Za-z]+$",
            message = "First name cannot be blank or contain characters other than letters")
    private String firstName;

    @Pattern(
            regexp = "^[A-Za-z]*$",
            message = "Last Name cannot contain characters other than letters")
    private String lastName;

    @NotEmpty
    @Pattern(
            regexp = "^[mMfF]$",
            message = "Gender cannot contain characters other than M or F")
    private String gender;

    //@NotEmpty(message = "Location cannot be blank")
    private String location="Location";

    @NotEmpty(message = "Email cannot be blank")
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            message = "Invalid email format")
    private String email;

    @NotEmpty(message = "Mobile Number cannot be blank")
    @Pattern(regexp = "^[1-9][0-9]{9}$", message = "Mobile must contain 10 digits")
    private String phone;

    private String imageUrl = "IMAGE.URL";

    @Pattern(
            regexp = "^[A-Za-z0-9.,!?\\s]*$",
            message = "TagLine can only contain letters, numbers, spaces, and common punctuation marks (.,!?).")
    @Size(
            min = 10,
            max = 100,
            message = "TagLine must be between 10 and 100 characters")
    private String tagLine;

    @Pattern(
            regexp = "^[A-Za-z0-9.,!?\\s]*$",
            message = "Summary can only contain letters, numbers, spaces, and common punctuation marks (.,!?).")
    @Size(
            min = 50,
            max = 500,
            message = "Summary must be between 50 and 500 characters")
    private String summary;

}
