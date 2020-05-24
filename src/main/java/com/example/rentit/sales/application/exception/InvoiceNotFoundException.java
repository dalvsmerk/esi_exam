package com.example.rentit.sales.application.exception;

import com.example.rentit.common.application.exception.ResourceNotFoundException;

public class InvoiceNotFoundException extends ResourceNotFoundException {
    public InvoiceNotFoundException(Long id) { super("Invoice", id); }
}
