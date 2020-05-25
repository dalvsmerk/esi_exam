package com.example.rentit.common.application.exception;

import java.util.List;

public class ResourceNotFoundException extends Exception {
    public ResourceNotFoundException(String resource, Long id) {
        super(String.format("%s not found (id: %d)", resource, id));
    }

    public ResourceNotFoundException(String resource, List<Long> ids) {
        super(String.format("%s not found (ids: %d)", resource, ids.toString()));
    }
}
