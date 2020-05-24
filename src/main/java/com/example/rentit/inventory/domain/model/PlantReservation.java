package com.example.rentit.inventory.domain.model;

import com.example.rentit.common.domain.model.BusinessPeriod;
import com.example.rentit.sales.domain.model.PurchaseOrder;
import com.example.rentit.support.domain.model.MaintenancePlan;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor(force=true,access= AccessLevel.PROTECTED)
@AllArgsConstructor(staticName="of")
public class PlantReservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Embedded
    BusinessPeriod schedule;

    @ManyToOne
    PurchaseOrder rental;

    @ManyToOne
    PlantInventoryItem plant;

    @ManyToOne
    MaintenancePlan maintenance;

    public static PlantReservation of(PlantInventoryItem item, BusinessPeriod schedule) {
        PlantReservation res = new PlantReservation();
        res.setPlant(item);
        res.setSchedule(schedule);
        return res;
    }
}
