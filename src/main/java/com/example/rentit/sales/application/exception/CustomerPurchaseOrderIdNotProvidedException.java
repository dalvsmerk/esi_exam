package com.example.rentit.sales.application.exception;

import com.example.rentit.common.application.exception.ValidationException;

public class CustomerPurchaseOrderIdNotProvidedException extends ValidationException {
    public CustomerPurchaseOrderIdNotProvidedException(String resource, String violation) { super(resource, violation); }
}
