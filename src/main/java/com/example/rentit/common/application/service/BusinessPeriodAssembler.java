package com.example.rentit.common.application.service;

import com.example.rentit.common.application.dto.BusinessPeriodDTO;
import com.example.rentit.common.domain.model.BusinessPeriod;
import org.springframework.stereotype.Service;

@Service
public class BusinessPeriodAssembler {
    public BusinessPeriodDTO toResource(BusinessPeriod businessPeriod) {
        BusinessPeriodDTO businessPeriodDTO = new BusinessPeriodDTO();

        businessPeriodDTO.setStartDate(businessPeriod.getStartDate());
        businessPeriodDTO.setEndDate(businessPeriod.getEndDate());

        return businessPeriodDTO;
    }
}
