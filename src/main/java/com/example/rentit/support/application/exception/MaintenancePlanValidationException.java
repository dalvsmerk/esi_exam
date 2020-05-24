package com.example.rentit.support.application.exception;

import com.example.rentit.common.application.exception.ValidationException;
import org.springframework.validation.BindingResult;

public class MaintenancePlanValidationException extends ValidationException {
    public MaintenancePlanValidationException(BindingResult br) {
        super("Maintenance Plan", br);
    }
}
