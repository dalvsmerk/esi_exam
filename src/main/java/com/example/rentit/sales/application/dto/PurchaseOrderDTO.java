package com.example.rentit.sales.application.dto;

import com.example.rentit.common.application.dto.BusinessPeriodDTO;
import com.example.rentit.inventory.application.dto.PlantInventoryItemDTO;
import com.example.rentit.sales.domain.model.POStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.ResourceSupport;

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor(staticName = "of")
public class PurchaseOrderDTO extends ResourceSupport {
    Long _id;
    BusinessPeriodDTO rentalPeriod;
    PlantInventoryItemDTO plant;
    POStatus status;
    Boolean plantReplaced;
    Long customerOrderId;


    public BusinessPeriodDTO getRentalPeriod() {
        return rentalPeriod;
    }

    public void setRentalPeriod(BusinessPeriodDTO rentalPeriod) {
        this.rentalPeriod = rentalPeriod;
    }



    public PlantInventoryItemDTO getPlant() {
        return plant;
    }

    public void setPlant(PlantInventoryItemDTO plant) {
        this.plant = plant;
    }

    public Long get_id() {
        return _id;
    }

    public void set_id(Long _id) {
        this._id = _id;
    }

    public POStatus getStatus() {
        return status;
    }

    public void setStatus(POStatus status) {
        this.status = status;
    }
}
