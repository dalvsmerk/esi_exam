package com.example.rentit.inventory.application.exception;

import com.example.rentit.common.application.exception.ResourceNotFoundException;

public class PlantInventoryEntryNotFoundException extends ResourceNotFoundException {
    public  PlantInventoryEntryNotFoundException(Long id) {
        super("Plant inventory entry", id);
    }
}
