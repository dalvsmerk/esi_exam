package com.example.rentit.support.application.service.validators;
import com.example.rentit.support.domain.model.MOTransition;
import com.example.rentit.support.domain.model.MaintenanceOrderStatus;
import com.example.rentit.support.domain.model.MaintenancePlan;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.LocalDate;

@Service
public class MaintenanceTransitionValidator implements Validator {
    @Override
    public boolean supports(Class<?> aClass) {
        return MOTransition.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        MOTransition t = (MOTransition) o;

        if (t.getTransitionTo() == null)
            errors.rejectValue("transitionTo", "transitionTo cannot be null");

        if (t.getMaintenanceOrder() == null)
            errors.rejectValue("maintenanceOrder", "maintenanceOrder cannot be null");

        MaintenanceOrderStatus from = t.getMaintenanceOrder().getStatus();
        MaintenanceOrderStatus to = t.getTransitionTo();


        switch (from){
            case PENDING: {
                if (to == MaintenanceOrderStatus.ACCEPTED) return;
                if (to == MaintenanceOrderStatus.REJECTED) return;
                if (to == MaintenanceOrderStatus.CANCELLED) return;

                errors.rejectValue("transitionTo", "Forbidden transition");
            }
            case ACCEPTED:{
                if (to == MaintenanceOrderStatus.CANCELLED && !maintenancePlanStarted(t.getMaintenanceOrder().getMaintenancePlan())) return;
                if (to == MaintenanceOrderStatus.COMPLETED) return;

                errors.rejectValue("transitionTo", "Forbidden transition");
            }

            default:{
                errors.rejectValue("transitionTo", "Forbidden transition");
            }
        }
    }

    private boolean maintenancePlanStarted(MaintenancePlan plan) {
        boolean isStarted =  plan != null
                && plan.getTasks() != null
                && plan.getTasks().stream().anyMatch(maintenanceTask ->
                        maintenanceTask.getTerm().getStartDate().isBefore(LocalDate.now()));
        return isStarted;
    }
}
