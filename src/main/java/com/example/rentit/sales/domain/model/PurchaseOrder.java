package com.example.rentit.sales.domain.model;

import com.example.rentit.common.domain.model.BusinessPeriod;
import com.example.rentit.inventory.domain.model.PlantInventoryEntry;
import com.example.rentit.inventory.domain.model.PlantInventoryItem;
import com.example.rentit.inventory.domain.model.PlantReservation;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor(force=true,access= AccessLevel.PROTECTED)
public class PurchaseOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToMany
    List<PlantReservation> reservations;

    @ManyToOne
    PlantInventoryItem plant;

    LocalDate issueDate;

    @Enumerated(EnumType.STRING)
    POStatus status;

    @Embedded
    BusinessPeriod rentalPeriod;

    Boolean plantReplaced;

    Long customerOrderId;

    public static PurchaseOrder of(PlantInventoryItem item, BusinessPeriod period, Long customerOrderId) {
        PurchaseOrder po = new PurchaseOrder();
        po.plant = item;
        po.rentalPeriod = period;
        po.reservations = new ArrayList<>();
        po.issueDate = LocalDate.now();
        po.status = POStatus.PENDING;
        po.plantReplaced = false;
        po.customerOrderId = customerOrderId;
        return po;
    }

    public void setStatus(POStatus newStatus) {
        status = newStatus;
    }

    public void addReservation(PlantReservation reservation) {
        reservations.add(reservation);
    }

}
