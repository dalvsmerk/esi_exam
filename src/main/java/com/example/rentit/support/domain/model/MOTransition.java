package com.example.rentit.support.domain.model;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(force = true, access = AccessLevel.PUBLIC)
@AllArgsConstructor(staticName = "of")
public class MOTransition {
    MaintenanceOrder maintenanceOrder;
    MaintenanceOrderStatus transitionTo;
}
