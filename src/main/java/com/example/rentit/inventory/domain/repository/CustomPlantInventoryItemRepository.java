package com.example.rentit.inventory.domain.repository;

import com.example.rentit.inventory.domain.model.PlantInventoryItem;
import org.springframework.data.util.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface CustomPlantInventoryItemRepository {
    List<Pair<PlantInventoryItem, BigDecimal>> findAvailableItems(LocalDate startDate, LocalDate endDate);
}
