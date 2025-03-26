package com.arindamcreates.matchmingle.exception;

public class DataAlreadyExistException extends RuntimeException {

    public DataAlreadyExistException(String errorMessage){
        super(errorMessage);
    }
}
