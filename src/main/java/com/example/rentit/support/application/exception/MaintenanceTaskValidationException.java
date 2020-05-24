package com.example.rentit.support.application.exception;

import com.example.rentit.common.application.exception.ValidationException;
import org.springframework.validation.BindingResult;

public class MaintenanceTaskValidationException extends ValidationException {
    public MaintenanceTaskValidationException(BindingResult br) {
        super("Maintenance Task", br);
    }
}
