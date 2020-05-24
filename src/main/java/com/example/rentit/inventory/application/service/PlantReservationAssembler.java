package com.example.rentit.inventory.application.service;

import com.example.rentit.common.application.dto.BusinessPeriodDTO;
import com.example.rentit.common.application.service.BusinessPeriodAssembler;
import com.example.rentit.inventory.application.dto.PlantInventoryItemDTO;
import com.example.rentit.inventory.application.dto.PlantReservationDTO;
import com.example.rentit.inventory.domain.model.PlantReservation;
import com.example.rentit.sales.application.dto.PurchaseOrderDTO;
import com.example.rentit.sales.application.service.PurchaseOrderAssembler;
import com.example.rentit.support.application.dto.MaintenancePlanDTO;
import com.example.rentit.support.application.service.assemblers.MaintenancePlanAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Service;

@Service
public class PlantReservationAssembler extends ResourceAssemblerSupport<PlantReservation, PlantReservationDTO> {

    @Autowired
    MaintenancePlanAssembler maintenancePlanAssembler;

    @Autowired
    PlantInventoryItemAssembler plantInventoryItemAssembler;

    @Autowired
    PurchaseOrderAssembler purchaseOrderAssembler;

    @Autowired
    BusinessPeriodAssembler businessPeriodAssembler;

    public PlantReservationAssembler() { super(PlantReservation.class, PlantReservationDTO.class); }

    public PlantReservationDTO toResource(PlantReservation reservation) {
        if (reservation == null) {
            return null;
        }

        PlantReservationDTO plantReservationDTO = new PlantReservationDTO();

        PlantInventoryItemDTO plantInventoryItemDTO = plantInventoryItemAssembler.toResource(reservation.getPlant());
        PurchaseOrderDTO purchaseOrderDTO = purchaseOrderAssembler.toResource(reservation.getRental());
        BusinessPeriodDTO scheduleDTO = businessPeriodAssembler.toResource(reservation.getSchedule());

        plantReservationDTO.set_id(reservation.getId());
        plantReservationDTO.setMaintenancePlanId(reservation.getMaintenance().getId());
        plantReservationDTO.setPlant(plantInventoryItemDTO);
        plantReservationDTO.setRental(purchaseOrderDTO);
        plantReservationDTO.setSchedule(scheduleDTO);

        return plantReservationDTO;
    }
}
