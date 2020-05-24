package com.example.rentit.sales.application.exception;

import com.example.rentit.common.application.exception.RelationViolationException;
import com.example.rentit.sales.domain.model.POStatus;

public class PurchaseOrderStatusException extends RelationViolationException {
    private static String messageTemplate = "Status of the purchase order %d cannot be changed to %s because the plant was %s";

    public PurchaseOrderStatusException(Long id, POStatus fromStatus, POStatus toStatus) {
        super(String.format(messageTemplate, id, toStatus, fromStatus));
    }
}
