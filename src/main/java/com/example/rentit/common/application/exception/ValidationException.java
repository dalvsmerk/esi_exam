package com.example.rentit.common.application.exception;

import org.springframework.validation.BindingResult;

public class ValidationException extends Exception {
    BindingResult bindingResult;

    public ValidationException(String resource, BindingResult br) {
        super(String.format("Constraint violation of %s", resource));
        bindingResult = br;
    }

    public ValidationException(String resource, String violation) {
        super(String.format("Constraint violation of %s: %s", resource, violation));
    }

    public BindingResult getBindingResult() {
        return bindingResult;
    }
}
