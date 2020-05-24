package com.example.rentit.sales.application.service;

import com.example.rentit.common.application.dto.BusinessPeriodDTO;
import com.example.rentit.common.rest.ExtendedLink;
import com.example.rentit.inventory.application.service.PlantInventoryEntryAssembler;
import com.example.rentit.inventory.application.service.PlantInventoryItemAssembler;
import com.example.rentit.sales.application.dto.PurchaseOrderDTO;
import com.example.rentit.sales.domain.model.PurchaseOrder;
import com.example.rentit.sales.rest.SalesRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Service;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
import static org.springframework.http.HttpMethod.*;

@Service
public class PurchaseOrderAssembler extends ResourceAssemblerSupport<PurchaseOrder, PurchaseOrderDTO> {

    @Autowired
    PlantInventoryItemAssembler plantInventoryItemAssembler;

    public PurchaseOrderAssembler() {
        super(SalesRestController.class, PurchaseOrderDTO.class);
    }

    @Override
    public PurchaseOrderDTO toResource(PurchaseOrder po) {
        if (po == null) return null;

        PurchaseOrderDTO dto = createResourceWithId(po.getId(), po);
        dto.setStatus(po.getStatus());
        dto.set_id(po.getId());
        dto.setRentalPeriod(BusinessPeriodDTO.of(po.getRentalPeriod().getStartDate(), po.getRentalPeriod().getEndDate()));
        dto.setPlant(plantInventoryItemAssembler.toResource(po.getPlant()));
        dto.setPlantReplaced(po.getPlantReplaced());
        dto.setCustomerOrderId(po.getCustomerOrderId());

        return dto;
    }
}