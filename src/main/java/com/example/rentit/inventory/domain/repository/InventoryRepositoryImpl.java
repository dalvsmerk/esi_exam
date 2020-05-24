package com.example.rentit.inventory.domain.repository;

import com.example.rentit.inventory.domain.model.PlantInventoryEntry;
import com.example.rentit.inventory.domain.model.PlantInventoryItem;
import org.springframework.data.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;

@Service
public class InventoryRepositoryImpl implements CustomInventoryRepository {

    @Autowired
    EntityManager em;

    // One item is considered available if it is not rented during the period
    // and if it is serviceable at the moment of the query
    // or the requested period is at least three weeks later in the future and the
    // plant is programed for maintenance at least one week before the rental. Plants marked
    // as UnserviceableCondemned cannot be available as they cannot be fixed
    public boolean checkPlantIsAvailable(Long plantId, LocalDate startDate, LocalDate endDate) {
        String query =
                "select count(p) from PlantReservation p " +
                        "where p.plant.id = ?1 " +
                        "and (p.schedule.startDate > ?2 and p.schedule.startDate < ?3 " +
                        "or p.schedule.endDate > ?2 and p.schedule.endDate < ?3 " +
                        "or p.plant.equipmentCondition != 'SERVICEABLE' " +
                            "and p.plant.equipmentCondition != 'UNSERVICEABLECONDEMNED' " +
                            "and p.maintenance is null " +
                                "or p.maintenance is not null " +
                                    "and (p.schedule.startDate > ?4 " +
                                        "or ?5 > ?6))";
        Long numberOfReservationsWithIntersectingSchedule = em.createQuery(query,Long.class)
                .setParameter(1, plantId)
                .setParameter(2, startDate)
                .setParameter(3, endDate)
                .setParameter(4, startDate.minusWeeks(1))
                .setParameter(5, LocalDate.now())
                .setParameter(6, startDate.minusWeeks(3))
                .getSingleResult();
        return numberOfReservationsWithIntersectingSchedule == 0;
    }

    public List<PlantInventoryEntry> findAvailablePlants(String name, LocalDate startDate, LocalDate endDate) {
        return em.createQuery("select p.plantInfo from PlantInventoryItem p where p.plantInfo.name like concat('%', ?1, '%') and p not in" +
                        "(select r.plant from PlantReservation r where ?2 < r.schedule.endDate and ?3 > r.schedule.startDate)",
                PlantInventoryEntry.class)
                .setParameter(1, name)
                .setParameter(2, startDate)
                .setParameter(3, endDate)
                .getResultList();
    }

    public List<PlantInventoryItem> findAvailableItems(Long id, LocalDate startDate, LocalDate endDate) {
        return em.createQuery("select p from PlantInventoryItem p where p.plantInfo.id=?1 and p not in " +
                        "(select r.plant from PlantReservation r where ?2 < r.schedule.endDate and ?3 > r.schedule.startDate)",
                PlantInventoryItem.class)
                .setParameter(1, id)
                .setParameter(2, startDate)
                .setParameter(3, endDate)
                .getResultList();
    }

    @Override
    public List<Pair<String, Long>> query3(String plantName, LocalDate startDate, LocalDate endDate) {
        List<Object> selectResult = em.createQuery(
                "select e.name, count(*) from PlantInventoryEntry e \n" +
                        "join PlantInventoryItem i on e.id = i.plantInfo \n" +
                        "left join PlantReservation r on i.id = r.plant \n" +
                        "left join MaintenanceTask mt on mt.reservation = r.id \n" +
                        "where lower(e.name) LIKE lower(?1) and \n" +
                        "(r.id is null or r.schedule.startDate >= ?3 or r.schedule.endDate <= ?2) and \n" +
                        "i.equipmentCondition <> 'UNSERVICEABLECONDEMNED' and \n" +
                        "(i.equipmentCondition = 'SERVICEABLE' or (datediff(week, current_timestamp(), ?2) >= 3 and datediff(week, mt.term.startDate, ?2) >= 1)) \n" +
                        "group by e.id"
        )
                .setParameter(1, '%' + plantName + '%')
                .setParameter(2, startDate)
                .setParameter(3, endDate)
                .getResultList();

        Iterator itr = selectResult.iterator();
        List<Pair<String, Long>> result = new ArrayList<>();
        while (itr.hasNext()) {
            Object[] obj = (Object[]) itr.next();
            String name = String.valueOf(obj[0]);
            Long amount = Long.parseLong(String.valueOf(obj[1]));
            result.add(Pair.of(name, amount));
        }

        return result;
    }

    @Override
    public List<String> query4() {
        return em.createQuery("SELECT i.serialNumber\n" +
                "FROM PlantInventoryItem i\n" +
                "JOIN PlantReservation r ON i.id = r.plant.id\n" +
                "JOIN MaintenanceTask t ON r.id = t.reservation.id\n" +
                "WHERE YEAR(t.term.startDate) = YEAR(CURRENT_DATE) - 1\n" +
                "GROUP BY i.serialNumber\n" +
                "ORDER BY COUNT(*)  DESC, SUM(t.price) DESC")
                .setMaxResults(3).getResultList();
    }

    public List<Pair<String, Long>> query2(LocalDate startDate, LocalDate endDate) {
        long daysDiff = DAYS.between(startDate, endDate);
        if (daysDiff <= 0) {
            return new ArrayList<>();
        }

        String reservationDuration = "DATEDIFF(day, r.schedule.startDate, r.schedule.endDate)";
        String inWorkDays = String.format("ISNULL(SUM(%s), 0 )", reservationDuration);
        String idleDays = String.format("?1 - %s", inWorkDays);

        List<Object> selectResult = em.createQuery(
                "SELECT e.name, " + idleDays + " FROM PlantInventoryEntry e \n" +
                        "JOIN PlantInventoryItem i ON e.id = i.plantInfo \n" +
                        "LEFT JOIN PlantReservation r ON i.id = r.plant \n" +
                        "GROUP BY e.id"
        )
                .setParameter(1, daysDiff)
                .getResultList();

        Iterator itr = selectResult.iterator();
        List<Pair<String, Long>> result = new ArrayList<>();
        while (itr.hasNext()) {
            Object[] obj = (Object[]) itr.next();
            String name = String.valueOf(obj[0]);
            Long idle = (long) Double.parseDouble(String.valueOf(obj[1]));
            result.add(Pair.of(name, idle));
        }

        return result;
    }
}
