package com.example.rentit.support.application.service.validators;
import com.example.rentit.common.application.service.BusinessPeriodValidator;
import com.example.rentit.support.domain.model.MaintenanceTask;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class MaintenanceTaskValidator implements Validator {
    @Override
    public boolean supports(Class<?> aClass) {
        return MaintenanceTask.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        MaintenanceTask t = (MaintenanceTask) o;

        if (t.getId() == null)
            errors.rejectValue("id", "id cannot be null");

        if (t.getTypeOfWork() == null)
            errors.rejectValue("typeOfWork",  "typeOfWork cannot be null");

        if (t.getDescription() == null)
            errors.rejectValue("description", "description cannot be null");

        if (t.getDescription().length() == 0)
            errors.rejectValue("description", "description cannot be empty");

        if (t.getPrice() == null)
            errors.rejectValue("price", "price cannot be null");

        if (t.getPrice().compareTo(BigDecimal.ZERO) < 0)
            errors.rejectValue("price", "price cannot be less than 0");

        DataBinder binder = new DataBinder(t.getTerm());
        binder.addValidators(new BusinessPeriodValidator());
        binder.validate();

        if (binder.getBindingResult().hasErrors()) {
            String error = binder.getBindingResult().getFieldError().getCode();
            errors.rejectValue("term", error);
        }

//        if (t.getReservation() == null)
//            errors.rejectValue("reservation", "reservation cannot be null");
    }
}
