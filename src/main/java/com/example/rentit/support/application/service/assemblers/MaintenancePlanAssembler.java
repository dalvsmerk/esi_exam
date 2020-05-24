package com.example.rentit.support.application.service.assemblers;

import com.example.rentit.inventory.application.dto.PlantInventoryItemDTO;
import com.example.rentit.inventory.application.service.PlantInventoryItemAssembler;
import com.example.rentit.support.application.dto.MaintenancePlanDTO;
import com.example.rentit.support.application.dto.MaintenanceTaskDTO;
import com.example.rentit.support.domain.model.MaintenancePlan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MaintenancePlanAssembler extends ResourceAssemblerSupport<MaintenancePlan, MaintenancePlanDTO> {

    @Autowired
    MaintenanceTaskAssembler maintenanceTaskAssembler;

    @Autowired
    PlantInventoryItemAssembler plantInventoryItemAssembler;

    @Autowired
    MaintenanceOrderAssembler maintenanceOrderAssembler;

    public MaintenancePlanAssembler() { super(MaintenancePlan.class, MaintenancePlanDTO.class); }

    @Override
    public MaintenancePlanDTO toResource(MaintenancePlan plan) {
        if (plan == null) return null;
        MaintenancePlanDTO planDTO = new MaintenancePlanDTO();

        List<MaintenanceTaskDTO> tasks = new ArrayList<>();
        plan.getTasks().forEach(maintenanceTask ->
                tasks.add(maintenanceTaskAssembler.toResource(maintenanceTask)));

        PlantInventoryItemDTO itemDTO = plantInventoryItemAssembler.toResource(plan.getPlant());

        planDTO.set_id(plan.getId());
        planDTO.setYearOfAction(plan.getYearOfAction());
        planDTO.setTasks(tasks);
        planDTO.setPlant(itemDTO);
        planDTO.setOrderId(plan.getOrderId());

        return planDTO;
    }
}
