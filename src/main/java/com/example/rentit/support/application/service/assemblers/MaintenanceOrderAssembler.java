package com.example.rentit.support.application.service.assemblers;

import com.example.rentit.common.application.dto.BusinessPeriodDTO;
import com.example.rentit.common.application.service.BusinessPeriodAssembler;
import com.example.rentit.common.rest.ExtendedLink;
import com.example.rentit.inventory.application.service.PlantInventoryItemAssembler;
import com.example.rentit.support.application.dto.MaintenanceOrderDTO;
import com.example.rentit.support.domain.model.MaintenanceOrder;
import com.example.rentit.support.rest.MaintenanceRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Service;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
import static org.springframework.http.HttpMethod.PATCH;

@Service
public class MaintenanceOrderAssembler extends ResourceAssemblerSupport<MaintenanceOrder, MaintenanceOrderDTO> {
    @Autowired
    PlantInventoryItemAssembler itemAssembler;

    @Autowired
    BusinessPeriodAssembler scheduleAssembler;

    @Autowired
    MaintenancePlanAssembler maintenancePlanAssembler;

    public MaintenanceOrderAssembler() {
        super(MaintenanceOrder.class, MaintenanceOrderDTO.class);
    }

    @Override
    public MaintenanceOrderDTO toResource(MaintenanceOrder order) {
        MaintenanceOrderDTO dto = new MaintenanceOrderDTO();
        BusinessPeriodDTO scheduleDTO = scheduleAssembler.toResource(order.getSchedule());

        dto.set_id(order.getId());
        dto.setPlantId(order.getPlantId());
        dto.setSchedule(scheduleDTO);
        dto.setDescription(order.getDescription());
        dto.setStatus(order.getStatus());

        try {
            switch (order.getStatus()) {
                case PENDING:
                    dto.add(new ExtendedLink(linkTo(methodOn(MaintenanceRestController.class).cancelMaintenanceOrder(dto.get_id())).toString(),"accept", PATCH));
                    dto.add(new ExtendedLink(linkTo(methodOn(MaintenanceRestController.class).cancelMaintenanceOrder(dto.get_id())).toString(),"reject", PATCH));
                    dto.add(new ExtendedLink(linkTo(methodOn(MaintenanceRestController.class).cancelMaintenanceOrder(dto.get_id())).toString(),"cancel", PATCH));
                    break;
                case ACCEPTED:
                    dto.add(new ExtendedLink(linkTo(methodOn(MaintenanceRestController.class).cancelMaintenanceOrder(dto.get_id())).toString(),"cancel", PATCH));
                    break;
                default:
                    break;
           }
        } catch (Exception e) {}

        return dto;
    }
}
