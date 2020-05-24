package com.example.rentit.inventory.domain.repository;

import com.example.rentit.inventory.domain.model.PlantInventoryEntry;
import com.example.rentit.inventory.domain.model.PlantInventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlantInventoryItemRepository
        extends JpaRepository<PlantInventoryItem, Long>, CustomPlantInventoryItemRepository {
    PlantInventoryItem findOneByPlantInfo(PlantInventoryEntry entry);
}
