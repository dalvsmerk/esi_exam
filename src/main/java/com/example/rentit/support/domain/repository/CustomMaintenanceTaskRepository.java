package com.example.rentit.support.domain.repository;

import com.example.rentit.support.domain.model.MaintenanceTask;

import java.time.LocalDate;
import java.util.List;

public interface CustomMaintenanceTaskRepository {
    List<MaintenanceTask> findMaintenanceUntilDate(Long plantId, LocalDate endDate);
}
