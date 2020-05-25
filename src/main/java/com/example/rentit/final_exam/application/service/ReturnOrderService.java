package com.example.rentit.final_exam.application.service;

import com.example.rentit.common.application.exception.ResourceNotFoundException;
import com.example.rentit.common.application.exception.ValidationException;
import com.example.rentit.final_exam.application.dto.ReturnOrderDTO;
import com.example.rentit.final_exam.application.dto.ReturnOrderRequestDTO;
import com.example.rentit.final_exam.application.service.assemblers.ReturnOrderAssembler;
import com.example.rentit.final_exam.application.service.validators.ReturnOrderValidator;
import com.example.rentit.final_exam.domain.model.ReturnOrder;
import com.example.rentit.final_exam.domain.model.ReturnOrderStatus;
import com.example.rentit.final_exam.domain.repository.ReturnOrderRepository;
import com.example.rentit.inventory.application.exception.PlantInventoryEntryValidationException;
import com.example.rentit.inventory.application.service.PlantInventoryEntryValidator;
import com.example.rentit.sales.domain.model.PurchaseOrder;
import com.example.rentit.sales.domain.repository.PurchaseOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReturnOrderService {
    @Autowired
    ReturnOrderAssembler returnOrderAssembler;

    @Autowired
    PurchaseOrderRepository poRepository;

    @Autowired
    ReturnOrderRepository returnOrderRepository;

    public ReturnOrderDTO createReturnOrder(ReturnOrderRequestDTO requestDTO)
            throws ResourceNotFoundException, ValidationException {
        List<Long> items = requestDTO.getPlantItems();

        Pair<List<Long>, List<PurchaseOrder>> verificationResult = verifyPOExistence(items);
        List<Long> nonExistingItems = verificationResult.getFirst();
        List<PurchaseOrder> orders = verificationResult.getSecond();

        if (!nonExistingItems.isEmpty()) {
            throw new ResourceNotFoundException("Purchase Order", nonExistingItems);
        }

        BigDecimal fee = ReturnOrder.computeFee(orders);

        ReturnOrder order = ReturnOrder.of(
                null, requestDTO.getReturnDate(), orders, fee, ReturnOrderStatus.PENDING);

        DataBinder binder = new DataBinder(order);
        binder.addValidators(new ReturnOrderValidator());
        binder.validate();

        BindingResult bindingResult = binder.getBindingResult();

        if (bindingResult.hasFieldErrors()) {
            throw new ValidationException("Return Order", bindingResult);
        }

        order = returnOrderRepository.saveAndFlush(order);

        return returnOrderAssembler.toResource(order);
    }

    private Pair<List<Long>, List<PurchaseOrder>> verifyPOExistence(List<Long> itemIds) {
        List<Long> nonExistingItems = new ArrayList<>();
        List<PurchaseOrder> orders = new ArrayList<>();

        for (Long id : itemIds) {
            PurchaseOrder order = poRepository.findOneByPlantId(id);

            if (order == null) {
                nonExistingItems.add(id);
            } else {
                orders.add(order);
            }
        }

        return Pair.of(nonExistingItems, orders);
    }
}
