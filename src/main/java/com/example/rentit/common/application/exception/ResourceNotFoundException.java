package com.example.rentit.common.application.exception;

public abstract class ResourceNotFoundException extends Exception {
    public ResourceNotFoundException(String resource, Long id) {
        super(String.format("%s not found (id: %d)", resource, id));
    }
}
