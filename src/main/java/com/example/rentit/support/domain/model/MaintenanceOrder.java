package com.example.rentit.support.domain.model;

import com.example.rentit.common.domain.model.BusinessPeriod;
import com.example.rentit.inventory.domain.model.PlantInventoryItem;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@AllArgsConstructor(staticName = "of")
public class MaintenanceOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

//    @ManyToOne
//    PlantInventoryItem plant;
    Long plantId;


    @OneToOne
    MaintenancePlan maintenancePlan;

    @Embedded
    BusinessPeriod schedule;

    @Enumerated(value=EnumType.STRING)
    MaintenanceOrderStatus status;

    String description;
}
