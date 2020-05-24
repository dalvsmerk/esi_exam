package com.example.rentit.support.application.exception;

import com.example.rentit.common.application.exception.ResourceNotFoundException;

public class MaintenancePlanNotFoundException extends ResourceNotFoundException {
    private static final long serialVersionUID = -1404L;

    public MaintenancePlanNotFoundException(Long id) {
        super("Maintenance plan", id);
    }
}
