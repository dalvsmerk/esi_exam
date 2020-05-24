package com.example.rentit.inventory.application.exception;

import com.example.rentit.common.application.exception.ResourceNotFoundException;

public class PlantInventoryItemNotFoundException extends ResourceNotFoundException {
    public  PlantInventoryItemNotFoundException(Long id) {
        super("Plant inventory item", id);
    }
}
