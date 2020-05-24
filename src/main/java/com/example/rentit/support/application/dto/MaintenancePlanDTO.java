package com.example.rentit.support.application.dto;

import com.example.rentit.common.rest.ResourceSupport;
import com.example.rentit.inventory.application.dto.PlantInventoryItemDTO;
import lombok.Data;

import java.util.List;

@Data
public class MaintenancePlanDTO extends ResourceSupport {
    Long _id;
    Integer yearOfAction;
    List<MaintenanceTaskDTO> tasks;
    PlantInventoryItemDTO plant;
    Long orderId;
}
