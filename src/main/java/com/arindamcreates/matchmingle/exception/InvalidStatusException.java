package com.arindamcreates.matchmingle.exception;

import lombok.Getter;

public class InvalidStatusException extends RuntimeException{
    @Getter
    private final String errorMessage;
    public InvalidStatusException(String msg) {
        super(msg);
        this.errorMessage = msg;
    }
}
