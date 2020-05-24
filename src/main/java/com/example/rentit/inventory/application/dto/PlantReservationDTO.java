package com.example.rentit.inventory.application.dto;

import com.example.rentit.common.application.dto.BusinessPeriodDTO;
import com.example.rentit.common.rest.ResourceSupport;
import com.example.rentit.sales.application.dto.PurchaseOrderDTO;
import com.example.rentit.support.application.dto.MaintenancePlanDTO;
import lombok.Data;

@Data
public class PlantReservationDTO extends ResourceSupport {
    Long _id;
    BusinessPeriodDTO schedule;
    PurchaseOrderDTO rental;
    PlantInventoryItemDTO plant;
    Long maintenancePlanId;
}
