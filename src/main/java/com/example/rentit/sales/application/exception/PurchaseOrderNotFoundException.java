package com.example.rentit.sales.application.exception;

import com.example.rentit.common.application.exception.ResourceNotFoundException;

public class PurchaseOrderNotFoundException extends ResourceNotFoundException {
    public PurchaseOrderNotFoundException(Long id) { super("PurchaseOrder", id); }
}
