package com.example.rentit.support.application.exception;

import com.example.rentit.common.application.exception.ValidationException;
import org.springframework.validation.BindingResult;

public class MOTransitionValidationException extends ValidationException {
    public MOTransitionValidationException(BindingResult br) {
        super("Maintenance transition", br);
    }
    public MOTransitionValidationException() {
        super("Maintenance transition", "Forbidden transition");
    }
}
