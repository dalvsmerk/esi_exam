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

        for (int i = 0; i < orders.size(); i++) {
            PurchaseOrder order = orders.get(i);
            LocalDate orderStartDate = order.getRentalPeriod().getStartDate();

            if (LocalDate.now().isBefore(orderStartDate)) {
                String attr = String.format("orders[%d]", i);
                errors.rejectValue(attr, "Plant hasn't been dispatched yet");
            }
        }
    }
}
