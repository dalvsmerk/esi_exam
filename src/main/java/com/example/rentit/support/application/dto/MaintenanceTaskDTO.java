package com.example.rentit.support.application.dto;

import com.example.rentit.common.application.dto.BusinessPeriodDTO;
import com.example.rentit.common.rest.ResourceSupport;
import com.example.rentit.inventory.application.dto.PlantReservationDTO;
import com.example.rentit.support.domain.model.TypeOfWork;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MaintenanceTaskDTO extends ResourceSupport {
    Long _id;
    String description;
    TypeOfWork typeOfWork;
    BigDecimal price;
    BusinessPeriodDTO term;
    PlantReservationDTO reservation;
}
