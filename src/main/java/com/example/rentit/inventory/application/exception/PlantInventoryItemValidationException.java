package com.example.rentit.inventory.application.exception;

import com.example.rentit.common.application.exception.ValidationException;
import org.springframework.validation.BindingResult;

public class PlantInventoryItemValidationException extends ValidationException {
    public PlantInventoryItemValidationException(BindingResult br) {
        super("Plant inventory item", br);
    }
}
