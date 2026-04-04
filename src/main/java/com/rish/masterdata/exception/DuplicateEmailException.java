package com.rish.masterdata.exception;

public class DuplicateEmailException extends RuntimeException{
    public DuplicateEmailException(String message) {
        super(message);
    }
}
