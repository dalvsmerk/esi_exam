package com.example.rentit.inventory.domain.repository;

import com.example.rentit.inventory.domain.model.PlantReservation;

import java.time.LocalDate;
import java.util.List;

public interface CustomPlantReservationRepository {
    List<PlantReservation> findPlantItemReservations(Long plantId, LocalDate startDate, LocalDate endDate);
}
