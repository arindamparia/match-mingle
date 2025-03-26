package com.arindamcreates.matchmingle.exception;

public class UnauthorisedException extends RuntimeException{
    public UnauthorisedException(String errorMessage){
        super(errorMessage);
    }
}
