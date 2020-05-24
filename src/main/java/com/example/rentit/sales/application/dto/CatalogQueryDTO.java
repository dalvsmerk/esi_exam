package com.example.rentit.sales.application.dto;

import com.example.rentit.common.application.dto.BusinessPeriodDTO;
import lombok.Data;

@Data
public class CatalogQueryDTO {
    String name;
    BusinessPeriodDTO rentalPeriod;
}
