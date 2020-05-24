package com.example.rentit.support.application.service.validators;

import com.example.rentit.inventory.domain.model.PlantInventoryItem;
import com.example.rentit.inventory.domain.model.PlantStatus;
import com.example.rentit.support.domain.model.MaintenancePlan;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.LocalDate;

@Service
public class MaintenancePlanValidator implements Validator {
    @Override
    public boolean supports(Class<?> aClass) {
        return MaintenancePlan.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        MaintenancePlan plan = (MaintenancePlan) o;
        PlantInventoryItem plant = plan.getPlant();

        if (plan.getPlant() == null) {
            errors.rejectValue("plant", "plant cannot be null");
        }

        if (plan.getYearOfAction() == null) {
            errors.rejectValue("yearOfAction", "yearOfAction cannot be null");
        }

        if (plan.getYearOfAction() < LocalDate.now().getYear()) {
            errors.rejectValue("yearOfAction", "yearOfAction cannot be past");
        }

        if (plant.getStatus() != PlantStatus.AVAILABLE) {
            errors.rejectValue("plant", "Maintenance plan cannot be scheduled for a non-available plant");
        }
    }
}
