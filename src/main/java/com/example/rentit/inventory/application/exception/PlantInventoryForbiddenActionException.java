package com.example.rentit.inventory.application.exception;

import com.example.rentit.inventory.domain.model.PlantStatus;

public class PlantInventoryForbiddenActionException  extends Exception {
    public  PlantInventoryForbiddenActionException(PlantStatus statusFrom, PlantStatus statusTo) {
        super(String.format("Impossible to perform the action. Forbidden transition: %s -> %s", statusFrom, statusTo));
    }
}
