package com.example.rentit.inventory.domain.repository;

import com.example.rentit.inventory.domain.model.PlantInventoryItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class PlantInventoryItemRepositoryImpl implements CustomPlantInventoryItemRepository {
    @Autowired
    EntityManager em;

    public List<Pair<PlantInventoryItem, BigDecimal>> findAvailableItems(LocalDate startDate, LocalDate endDate) {
        List<Object> results = em.createQuery(
                "select it, p.price from PlantInventoryItem it " +
                        "inner join PlantInventoryEntry p on p.id = it.plantInfo.id " +
                        "where it not in" +
                        "(select r.plant from PlantReservation r " +
                        "where r.schedule.startDate < ?2 " +
                        "and r.schedule.endDate > ?1)")
                .setParameter(1, startDate)
                .setParameter(2, endDate)
                .getResultList();

        Iterator it = results.iterator();
        List<Pair<PlantInventoryItem, BigDecimal>> out = new ArrayList<>();

        while (it.hasNext()) {
            Object[] obj = (Object[]) it.next();
            PlantInventoryItem item = (PlantInventoryItem) obj[0];
            BigDecimal price = (BigDecimal) obj[1];
            Pair<PlantInventoryItem, BigDecimal> itemToPrice = Pair.of(item, price);

            out.add(itemToPrice);
        }

        return out;
    }
}
