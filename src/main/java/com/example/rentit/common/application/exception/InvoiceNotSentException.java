package com.example.rentit.common.application.exception;

import com.example.rentit.sales.domain.model.Invoice;

public class InvoiceNotSentException extends Exception {
    public InvoiceNotSentException(Invoice invoice) { super("Invoice " + invoice.getId().toString() + " for Purchase Order " + invoice.getPurchaseOrder().getId() + " is not sent"); }
}