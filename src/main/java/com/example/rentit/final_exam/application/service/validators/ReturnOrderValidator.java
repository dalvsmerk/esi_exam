package com.example.rentit.final_exam.application.service.validators;

import com.example.rentit.final_exam.domain.model.ReturnOrder;
import com.example.rentit.sales.domain.model.PurchaseOrder;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReturnOrderValidator implements Validator {
    @Override
    public boolean supports(Class<?> aClass) {
        return ReturnOrder.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        ReturnOrder returnOrder = (ReturnOrder) o;

        List<PurchaseOrder> orders = returnOrder.getOrders();
        LocalDate returnDate = returnOrder.getReturnDate();

        for (int i = 0; i < orders.size(); i++) {
            String attr = String.format("orders[%d]", i);

            PurchaseOrder order = orders.get(i);
            LocalDate orderStartDate = order.getRentalPeriod().getStartDate();
            LocalDate orderEndDate = order.getRentalPeriod().getEndDate();

            Boolean plantsWereDispatched = LocalDate.now().isAfter(orderStartDate);
            Boolean returnDateWithinPeriod =
                    returnDate.isAfter(orderStartDate) && returnDate.isBefore(orderEndDate);

            if (!plantsWereDispatched) {
                errors.rejectValue(attr, "Plant hasn't been dispatched yet");
            }

            if (!returnDateWithinPeriod) {
                errors.rejectValue(attr, "Return Order date isn't within Purchase Order period");
            }
        }
    }
}
