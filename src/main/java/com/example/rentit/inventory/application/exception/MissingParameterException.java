package com.example.rentit.inventory.application.exception;

public class MissingParameterException extends Exception {
    public  MissingParameterException(String field) {
        super(String.format("Missing required query parameter: '%s'", field));
    }
}
