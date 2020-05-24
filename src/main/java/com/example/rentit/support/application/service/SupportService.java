package com.example.rentit.support.application.service;

import com.example.rentit.inventory.application.service.PlantInventoryItemAssembler;
import com.example.rentit.inventory.domain.model.EquipmentCondition;
import com.example.rentit.inventory.domain.model.PlantInventoryItem;
import com.example.rentit.inventory.domain.model.PlantReservation;
import com.example.rentit.inventory.domain.repository.PlantInventoryItemRepository;
import com.example.rentit.inventory.domain.repository.PlantReservationRepository;
import com.example.rentit.support.application.dto.MaintenancePlanDTO;
import com.example.rentit.support.application.dto.MaintenanceTaskDTO;
import com.example.rentit.support.application.exception.MaintenancePlanNotFoundException;
import com.example.rentit.support.application.exception.MaintenancePlanValidationException;
import com.example.rentit.support.application.exception.MaintenanceTaskScheduleException;
import com.example.rentit.support.application.exception.MaintenanceTaskValidationException;
import com.example.rentit.support.application.service.assemblers.MaintenancePlanAssembler;
import com.example.rentit.support.application.service.assemblers.MaintenanceTaskAssembler;
import com.example.rentit.support.application.service.validators.MaintenancePlanValidator;
import com.example.rentit.support.application.service.validators.MaintenanceTaskValidator;
import com.example.rentit.support.domain.model.MaintenancePlan;
import com.example.rentit.support.domain.model.MaintenanceTask;
import com.example.rentit.support.domain.model.TypeOfWork;
import com.example.rentit.support.domain.repository.MaintenancePlanRepository;
import com.example.rentit.support.domain.repository.MaintenanceTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SupportService {

    @Autowired
    MaintenancePlanRepository maintenancePlanRepository;

    @Autowired
    MaintenanceTaskRepository maintenanceTaskRepository;

    @Autowired
    PlantReservationRepository plantReservationRepository;

    @Autowired
    MaintenancePlanAssembler maintenancePlanAssembler;

    @Autowired
    PlantInventoryItemRepository plantInventoryItemRepository;

    @Autowired
    PlantInventoryItemAssembler plantInventoryItemAssembler;

    @Autowired
    MaintenanceTaskAssembler maintenanceTaskAssembler;

    public MaintenancePlanDTO findMaintenancePlan(Long planId) throws MaintenancePlanNotFoundException {
        MaintenancePlan plan = maintenancePlanRepository.findById(planId)
                .orElseThrow(() -> new MaintenancePlanNotFoundException(planId));

        return maintenancePlanAssembler.toResource(plan);
    }

    public MaintenancePlanDTO createMaintenancePlan(MaintenancePlanDTO planDTO) throws Exception {
        PlantInventoryItem item = plantInventoryItemRepository
                .findById(planDTO.getPlant().get_id())
                .orElse(null);

        MaintenancePlan plan = MaintenancePlan.of(planDTO.getYearOfAction(), item);
        List<MaintenanceTask> tasks = new ArrayList<>();

        DataBinder binder = new DataBinder(plan);
        binder.addValidators(new MaintenancePlanValidator());
        binder.validate();

        BindingResult bindingResult = binder.getBindingResult();

        if (bindingResult.hasFieldErrors()) {
            throw new MaintenancePlanValidationException(bindingResult);
        }

        plan = maintenancePlanRepository.saveAndFlush(plan);

        if (planDTO.getTasks() != null) {
            for (MaintenanceTaskDTO taskDTO: planDTO.getTasks()) {
                MaintenanceTask task = MaintenanceTask.of(taskDTO);
                PlantReservation res = PlantReservation.of(null, task.getTerm(), null, plan.getPlant(), plan);
                res = plantReservationRepository.saveAndFlush(res);
                task.setReservation(res);
                tasks.add(maintenanceTaskRepository.saveAndFlush(task));
            }
        }

        plan = addTasksToPlan(plan, tasks);
        plan = maintenancePlanRepository.saveAndFlush(plan);

        return maintenancePlanAssembler.toResource(plan);
    }

    public MaintenancePlanDTO updateMaintenancePlan(Long planId, MaintenancePlanDTO planDTO) throws Exception {
        MaintenancePlan plan = maintenancePlanRepository.findById(planId)
                .orElseThrow(() -> new MaintenancePlanNotFoundException(planId));

        PlantInventoryItem item = plantInventoryItemRepository.findById(planDTO.getPlant().get_id()).orElse(null);
        List<MaintenanceTask> tasks = new ArrayList<>();

        if (planDTO.getTasks() != null) {
            tasks = planDTO.getTasks().stream()
                    .map(MaintenanceTask::of)
                    .collect(Collectors.toList());
        }

        plan.setYearOfAction(planDTO.getYearOfAction());
        plan.setPlant(item);
        plan = addTasksToPlan(plan, tasks);

        DataBinder binder = new DataBinder(plan);
        binder.addValidators(new MaintenancePlanValidator());
        binder.validate();

        BindingResult bindingResult = binder.getBindingResult();

        if (bindingResult.hasFieldErrors()) {
            throw new MaintenancePlanValidationException(bindingResult);
        }

        plan = maintenancePlanRepository.saveAndFlush(plan);
        return maintenancePlanAssembler.toResource(plan);
    }

    public String isPossibleToAddTaskToPlan(MaintenanceTask task, MaintenancePlan plan) {
        EquipmentCondition condition = plan.getPlant().getEquipmentCondition();
        TypeOfWork type = task.getTypeOfWork();

        if (type == TypeOfWork.PREVENTIVE && condition != EquipmentCondition.SERVICEABLE) {
            return "Preventive maintenance task cannot be scheduled for a non-serviceable plant";
        }

        if (type == TypeOfWork.CORRECTIVE &&
                !(condition == EquipmentCondition.UNSERVICEABLEREPAIRABLE
                        || condition == EquipmentCondition.UNSERVICEABLEINCOMPLETE)) {
            return "Corrective maintenance task cannot be scheduled for an non-unserviceable repairable or an non-unserviceable complete plant";
        }

        if (type == TypeOfWork.OPERATIVE && condition == EquipmentCondition.UNSERVICEABLECONDEMNED) {
            return "Operative maintenance task cannot be scheduled for an unserviceable condemned plant";
        }

        return "";
    }

    public MaintenancePlan addTasksToPlan(MaintenancePlan plan, List<MaintenanceTask> tasks) throws Exception {
        for (MaintenanceTask task : tasks) {
            DataBinder binder = new DataBinder(task);
            binder.addValidators(new MaintenanceTaskValidator());
            binder.validate();

            BindingResult bindingResult = binder.getBindingResult();

            if (bindingResult.hasErrors()) {
                throw new MaintenanceTaskValidationException(bindingResult);
            }

            String reasonNotTo = isPossibleToAddTaskToPlan(task, plan);

            if (reasonNotTo.length() != 0) {
                throw new MaintenanceTaskScheduleException(reasonNotTo);
            }

            plan.addTask(task);
        }

        return plan;
    }
}
