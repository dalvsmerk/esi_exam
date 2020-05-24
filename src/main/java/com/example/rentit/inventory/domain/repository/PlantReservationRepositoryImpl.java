package com.example.rentit.inventory.domain.repository;

import com.example.rentit.inventory.domain.model.PlantReservation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;

@Service
public class PlantReservationRepositoryImpl implements CustomPlantReservationRepository {
    @Autowired
    EntityManager em;

    public List<PlantReservation> findPlantItemReservations(Long plantId, LocalDate startDate, LocalDate endDate) {
        return em.createQuery(
                "select r from PlantReservation r " +
                "where r.plant.id = ?1 " +
                "and " +
                "(r.schedule.startDate <= ?3 and r.schedule.endDate >= ?3 or " +
                "r.schedule.startDate <= ?2 and r.schedule.endDate >= ?2 or " +
                "r.schedule.startDate >= ?2 and r.schedule.endDate <= ?3 or " +
                "r.schedule.startDate <= ?2 and r.schedule.endDate >= ?3)", PlantReservation.class)
                .setParameter(1, plantId)
                .setParameter(2, startDate)
                .setParameter(3, endDate)
                .getResultList();
    }
}
