package com.example.rentit.sales.application.exception;

import com.example.rentit.common.application.exception.RelationViolationException;

public class InvoiceStatusViolationException extends RelationViolationException {
    public InvoiceStatusViolationException(String reason) {
        super(reason);
    }
}
