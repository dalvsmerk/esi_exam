package com.example.rentit.inventory.domain.repository;

import com.example.rentit.inventory.domain.model.PlantInventoryEntry;
import com.example.rentit.inventory.domain.model.PlantInventoryItem;
import org.springframework.data.util.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface CustomInventoryRepository {
    List<PlantInventoryEntry> findAvailablePlants(String name, LocalDate startDate, LocalDate endDate);
    List<PlantInventoryItem> findAvailableItems(Long id, LocalDate startDate, LocalDate endDate);

    boolean checkPlantIsAvailable(Long id, LocalDate startDate, LocalDate endDate);

    List<Pair<String, Long>> query2(LocalDate startDate, LocalDate endDate);
    List<Pair<String, Long>> query3(String plantName, LocalDate startDate, LocalDate endDate);
    List<String> query4();
}
