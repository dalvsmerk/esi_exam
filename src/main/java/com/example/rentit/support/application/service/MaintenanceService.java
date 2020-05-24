package com.example.rentit.support.application.service;

import com.example.rentit.common.application.dto.BusinessPeriodDTO;
import com.example.rentit.common.domain.model.BusinessPeriod;
import com.example.rentit.support.application.dto.MaintenanceOrderDTO;
import com.example.rentit.support.application.dto.MaintenancePlanDTO;
import com.example.rentit.support.application.exception.MOTransitionValidationException;
import com.example.rentit.support.application.exception.MaintenanceOrderNotFoundException;
import com.example.rentit.support.application.exception.MaintenancePlanNotFoundException;
import com.example.rentit.support.application.service.assemblers.MaintenanceOrderAssembler;
import com.example.rentit.support.application.service.validators.MaintenanceTransitionValidator;
import com.example.rentit.support.domain.model.MOTransition;
import com.example.rentit.support.domain.model.MaintenanceOrder;
import com.example.rentit.support.domain.model.MaintenanceOrderStatus;
import com.example.rentit.support.domain.model.MaintenancePlan;
import com.example.rentit.support.domain.repository.MaintenanceOrderRepository;
import com.example.rentit.support.domain.repository.MaintenancePlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;

@Service
public class MaintenanceService {
    @Autowired
    SupportService supportService;

    @Autowired
    MaintenanceOrderRepository orderRepository;

    @Autowired
    MaintenancePlanRepository maintenancePlanRepository;

    @Autowired
    MaintenanceOrderAssembler orderAssembler;

    public MaintenanceOrderDTO createMaintenanceOrder(MaintenanceOrderDTO partialOrder) throws Exception {
        BusinessPeriodDTO orderSchedule = partialOrder.getSchedule();
        BusinessPeriod schedule = BusinessPeriod.of(orderSchedule.getStartDate(), orderSchedule.getEndDate());

        MaintenanceOrder order = MaintenanceOrder.of(
                partialOrder.get_id(), partialOrder.getPlantId(), null,
                schedule, MaintenanceOrderStatus.PENDING, partialOrder.getDescription());

        order = orderRepository.saveAndFlush(order);

        return orderAssembler.toResource(order);
    }

    public MaintenanceOrderDTO findMaintenanceOrder(Long id) throws MaintenanceOrderNotFoundException {
        MaintenanceOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new MaintenanceOrderNotFoundException(id));
        return orderAssembler.toResource(order);
    }

    public MaintenanceOrderDTO updateMaintenanceOrderStatus(Long id, MaintenanceOrderStatus newStatus) throws Exception {
        MaintenanceOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new MaintenanceOrderNotFoundException(id));
        MOTransition transition = MOTransition.of(order, newStatus);
        validateMOTransition(transition);

        order.setStatus(newStatus);
        order = orderRepository.saveAndFlush(order);

        return orderAssembler.toResource(order);
    }

    public MaintenanceOrderDTO acceptMaintenanceOrder(Long id, MaintenanceOrderDTO partialOrder) throws Exception {
        MaintenanceOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new MaintenanceOrderNotFoundException(id));
        BusinessPeriodDTO orderSchedule = partialOrder.getSchedule();
        BusinessPeriod schedule = BusinessPeriod.of(orderSchedule.getStartDate(), orderSchedule.getEndDate());

        MaintenancePlanDTO planDTO = supportService.createMaintenancePlan(partialOrder.getMaintenancePlan());
        MaintenancePlan plan = maintenancePlanRepository.findById(planDTO.get_id())
                .orElseThrow(() -> new MaintenancePlanNotFoundException(planDTO.get_id()));

        MOTransition transition = MOTransition.of(order, MaintenanceOrderStatus.ACCEPTED);
        validateMOTransition(transition);

        order.setMaintenancePlan(plan);
        order.setSchedule(schedule);
        order.setStatus(MaintenanceOrderStatus.ACCEPTED);
        order = orderRepository.saveAndFlush(order);

        return orderAssembler.toResource(order);
    }

    private void validateMOTransition(MOTransition transition) throws Exception {
        DataBinder binder = new DataBinder(transition);
        binder.addValidators(new MaintenanceTransitionValidator());
        binder.validate();

        BindingResult bindingResult = binder.getBindingResult();

        if (bindingResult.hasFieldErrors()) {
            throw new MOTransitionValidationException(bindingResult);
        }
    }
}
