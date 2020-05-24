package com.example.rentit.inventory.application.dto;

import com.example.rentit.common.rest.ResourceSupport;
import com.example.rentit.inventory.domain.model.EquipmentCondition;
import com.example.rentit.inventory.domain.model.PlantStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor(staticName = "of")
public class PlantInventoryItemDTO extends ResourceSupport {
    Long _id;
    String serialNumber;
    EquipmentCondition equipmentCondition;
    PlantInventoryEntryDTO plantInfo;
    PlantStatus status;
    BigDecimal price;

    public static PlantInventoryItemDTO of(Long id) {
        PlantInventoryItemDTO dto = new PlantInventoryItemDTO();
        dto.set_id(id);
        return dto;
    }
}

