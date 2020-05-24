package com.example.rentit.sales.application.service;


import com.example.rentit.common.domain.model.BusinessPeriod;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Service
public class ExtendPOEndDateValidator implements Validator {

    @Override
    public boolean supports(Class<?> aClass) {
        return BusinessPeriod.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        BusinessPeriod bp = (BusinessPeriod) o;

        if(bp.getEndDate() == null)
            errors.rejectValue("endDate", "endDate cannot be null");

        if (bp.getEndDate() != null) {
            if(bp.getEndDate().isBefore(bp.getStartDate()))
                errors.rejectValue("endDate", "end date cannot be set before the current end date");
        }
    }
}
