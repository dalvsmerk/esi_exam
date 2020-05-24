package com.example.rentit.support.domain.repository;

import com.example.rentit.support.domain.model.MaintenanceTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;

@Service
public class MaintenanceTaskRepositoryImpl implements CustomMaintenanceTaskRepository {
    @Autowired
    EntityManager em;

    public List<MaintenanceTask> findMaintenanceUntilDate(Long plantId, LocalDate endDate) {
        return em.createQuery(
                "select mt from MaintenanceTask mt " +
                "inner join MaintenancePlan p on mt.plan.id = p.id " +
                "where p.plant.id = ?1 " +
                "and mt.term.endDate > ?2")
                .setParameter(1, plantId)
                .setParameter(2, endDate)
                .getResultList();
    }
}
