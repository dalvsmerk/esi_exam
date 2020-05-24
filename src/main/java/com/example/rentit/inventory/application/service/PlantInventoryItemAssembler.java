package com.example.rentit.inventory.application.service;

import com.example.rentit.inventory.application.dto.PlantInventoryEntryDTO;
import com.example.rentit.inventory.application.dto.PlantInventoryItemDTO;
import com.example.rentit.inventory.domain.model.PlantInventoryItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Service;

@Service
public class PlantInventoryItemAssembler extends ResourceAssemblerSupport<PlantInventoryItem, PlantInventoryItemDTO> {

    @Autowired
    PlantInventoryEntryAssembler plantInventoryEntryAssembler;

    public PlantInventoryItemAssembler() { super(PlantInventoryItem.class, PlantInventoryItemDTO.class); }

    public PlantInventoryItemDTO toResource(PlantInventoryItem item) {
        PlantInventoryItemDTO itemDTO = new PlantInventoryItemDTO();

        PlantInventoryEntryDTO entryDTO = plantInventoryEntryAssembler.toResource(item.getPlantInfo());

        itemDTO.set_id(item.getId());
        itemDTO.setEquipmentCondition(item.getEquipmentCondition());
        itemDTO.setPlantInfo(entryDTO);
        itemDTO.setSerialNumber(item.getSerialNumber());
        itemDTO.setStatus(item.getStatus());

        return itemDTO;
    }
}
