package com.example.rentit.support.application.dto;

import com.example.rentit.common.application.dto.BusinessPeriodDTO;
import com.example.rentit.common.rest.ResourceSupport;
//import com.example.rentit.inventory.application.dto.PlantInventoryItemDTO;
import com.example.rentit.support.domain.model.MaintenanceOrderStatus;
import lombok.Data;

@Data
public class MaintenanceOrderDTO extends ResourceSupport {
    Long _id;
//    PlantInventoryItemDTO plant;
    Long plantId;
    BusinessPeriodDTO schedule;
    MaintenanceOrderStatus status;
    String description;
    MaintenancePlanDTO maintenancePlan;
}
