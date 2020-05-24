package com.example.rentit.inventory.application.service;

import com.example.rentit.inventory.domain.model.PlantInventoryItem;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Service
public class PlantInventoryItemValidator implements Validator {
    @Override
    public boolean supports(Class<?> aClass) {
        return PlantInventoryItem.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        PlantInventoryItem item = (PlantInventoryItem) o;

        if (item.getPlantInfo() == null) {
            errors.rejectValue("plantInfo","plant info cannot be null");
        }
        if (item.getEquipmentCondition() == null) {
            errors.rejectValue("equipmentCondition","equipment condition cannot be null");
        }
        if (item.getSerialNumber() == null) {
            errors.rejectValue("serialNumber","serial number cannot be null");
        }
    }
}
