package com.example.rentit.support.domain.model;

import com.example.rentit.common.application.dto.BusinessPeriodDTO;
import com.example.rentit.common.domain.model.BusinessPeriod;
import com.example.rentit.inventory.application.dto.PlantInventoryEntryDTO;
import com.example.rentit.inventory.application.dto.PlantInventoryItemDTO;
import com.example.rentit.inventory.application.dto.PlantReservationDTO;
import com.example.rentit.inventory.domain.model.PlantInventoryEntry;
import com.example.rentit.inventory.domain.model.PlantInventoryItem;
import com.example.rentit.inventory.domain.model.PlantReservation;
import com.example.rentit.support.application.dto.MaintenanceTaskDTO;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor(force=true,access= AccessLevel.PROTECTED)
@AllArgsConstructor(staticName="of")
public class MaintenanceTask {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    Long id;

    String description;

    @Enumerated(value=EnumType.STRING)
    TypeOfWork typeOfWork;

    @ManyToOne
    MaintenancePlan plan;

    @Column(precision=8,scale=2)
    BigDecimal price;

    @Embedded
    BusinessPeriod term;

    @ManyToOne
    PlantReservation reservation;

    public static MaintenanceTask of(TypeOfWork type, BusinessPeriod term) {
        MaintenanceTask task = new MaintenanceTask();
        task.setTypeOfWork(type);
        task.setTerm(term);
        return task;
    }

    public static MaintenanceTask of(MaintenanceTaskDTO taskDTO) {
        MaintenanceTask task = new MaintenanceTask();
        BusinessPeriodDTO bpDTO = taskDTO.getTerm();

        task.setReservation(null);
        BusinessPeriod bp = BusinessPeriod.of(bpDTO.getStartDate(), bpDTO.getEndDate());
        task.setId(taskDTO.get_id());
        task.setPrice(taskDTO.getPrice());
        task.setTerm(bp);
        task.setTypeOfWork(taskDTO.getTypeOfWork());
        task.setDescription(taskDTO.getDescription());

        return task;
    }
}
