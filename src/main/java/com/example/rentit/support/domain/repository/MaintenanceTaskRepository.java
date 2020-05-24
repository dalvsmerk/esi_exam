package com.example.rentit.support.domain.repository;

import com.example.rentit.support.domain.model.MaintenanceTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaintenanceTaskRepository
        extends JpaRepository<MaintenanceTask, Long>, CustomMaintenanceTaskRepository {
}
