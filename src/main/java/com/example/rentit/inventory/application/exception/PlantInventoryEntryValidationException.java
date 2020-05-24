package com.example.rentit.inventory.application.exception;

import com.example.rentit.common.application.exception.ValidationException;
import org.springframework.validation.BindingResult;

public class PlantInventoryEntryValidationException extends ValidationException {
    public PlantInventoryEntryValidationException(BindingResult br) {
            super("Plant inventory entry", br);
        }
}
