package com.example.rentit.support.application.service.assemblers;

import com.example.rentit.common.application.dto.BusinessPeriodDTO;
import com.example.rentit.common.application.service.BusinessPeriodAssembler;
import com.example.rentit.inventory.application.dto.PlantReservationDTO;
import com.example.rentit.inventory.application.service.PlantReservationAssembler;
import com.example.rentit.support.application.dto.MaintenanceTaskDTO;
import com.example.rentit.support.domain.model.MaintenanceTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Service;

@Service
public class MaintenanceTaskAssembler extends ResourceAssemblerSupport<MaintenanceTask, MaintenanceTaskDTO> {

    @Autowired
    PlantReservationAssembler plantReservationAssembler;

    @Autowired
    BusinessPeriodAssembler businessPeriodAssembler;

    public MaintenanceTaskAssembler() { super(MaintenanceTask.class, MaintenanceTaskDTO.class); }

    public MaintenanceTaskDTO toResource(MaintenanceTask task) {
        MaintenanceTaskDTO taskDTO = new MaintenanceTaskDTO();

        PlantReservationDTO plantReservationDTO = plantReservationAssembler.toResource(task.getReservation());
        BusinessPeriodDTO businessPeriodDTO = businessPeriodAssembler.toResource(task.getTerm());

        taskDTO.set_id(task.getId());
        taskDTO.setDescription(task.getDescription());
        taskDTO.setPrice(task.getPrice());
        taskDTO.setReservation(plantReservationDTO);
        taskDTO.setTerm(businessPeriodDTO);
        taskDTO.setTypeOfWork(task.getTypeOfWork());

        return taskDTO;
    }
}
