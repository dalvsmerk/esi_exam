package com.example.rentit.support.application.exception;

import com.example.rentit.common.application.exception.ResourceNotFoundException;

public class MaintenanceOrderNotFoundException extends ResourceNotFoundException {
    public MaintenanceOrderNotFoundException(Long id) { super("Maintenance order", id); }
}
