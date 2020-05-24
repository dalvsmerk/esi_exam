package com.example.rentit.sales.application.exception;

import com.example.rentit.common.application.exception.RelationViolationException;
import com.example.rentit.inventory.domain.model.PlantStatus;

public class PurchaseOrderCancellationException extends RelationViolationException {
    private static String messageTemplate = "Purchase order %d cannot be cancelled because the plant was %s";

    public PurchaseOrderCancellationException(Long id, PlantStatus plantStatus) {
        super(String.format(messageTemplate, id, plantStatusToReason(plantStatus)));
    }

    private static String plantStatusToReason(PlantStatus status) {
        switch (status) {
            case DISPATCHED: return "dispatched";
            case DELIVERED: return "delivered";
            case REJECTED_BY_CUSTOMER: return "rejected by customer";
            default: return "available";
        }
    }
}
