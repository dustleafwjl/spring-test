package com.thoughtworks.rslist.exception;


public class AmountIsLessException extends RuntimeException {
    private String error;

    public AmountIsLessException(String error) {
        this.error = error;
    }

    @Override
    public String getMessage() {
        return error;
    }
}