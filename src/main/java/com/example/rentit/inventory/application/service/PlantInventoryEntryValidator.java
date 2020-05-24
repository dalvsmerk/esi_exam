package com.example.rentit.inventory.application.service;

import com.example.rentit.inventory.domain.model.PlantInventoryEntry;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Service
public class PlantInventoryEntryValidator implements Validator {
    @Override
    public boolean supports(Class<?> aClass) {
        return PlantInventoryEntry.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        PlantInventoryEntry entry = (PlantInventoryEntry) o;

        if (entry.getName() == null) {
            errors.rejectValue("name","name cannot be null");
        }
        if (entry.getDescription() == null) {
            errors.rejectValue("description","description cannot be null");
        }
        if (entry.getPrice() == null) {
            errors.rejectValue("price","price cannot be null");
        }
    }
}
