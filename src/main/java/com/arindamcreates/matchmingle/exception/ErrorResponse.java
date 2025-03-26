package com.arindamcreates.matchmingle.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;
@Getter
@AllArgsConstructor
public class ErrorResponse {

    private int errorCode;
    private String errorMessage;
    private List<String> errors;

    public ErrorResponse(int errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
