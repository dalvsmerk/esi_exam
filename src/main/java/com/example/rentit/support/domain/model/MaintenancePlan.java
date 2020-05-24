package com.example.rentit.support.domain.model;

import com.example.rentit.inventory.domain.model.PlantInventoryItem;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@AllArgsConstructor(staticName = "of")
public class MaintenancePlan {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    Long id;

    Integer yearOfAction;

    @OneToMany(fetch = FetchType.EAGER)
    List<MaintenanceTask> tasks;

    @ManyToOne
    PlantInventoryItem plant;

    Long orderId;

    public static MaintenancePlan of(Integer yearOfAction, PlantInventoryItem plant) {
        MaintenancePlan mp = new MaintenancePlan();
        mp.setYearOfAction(yearOfAction);
        mp.setPlant(plant);
        mp.tasks = new ArrayList<MaintenanceTask>();
        return mp;
    }

    public void addTask(MaintenanceTask task) {
        tasks.add(task);
    }
}
