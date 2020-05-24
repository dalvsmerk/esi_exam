package com.example.rentit.inventory.domain.repository;

import com.example.rentit.inventory.domain.model.PlantInventoryEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlantInventoryEntryRepository extends JpaRepository<PlantInventoryEntry, Long> {
    List<PlantInventoryEntry> findByNameContaining(String mini);

    @Query("select p from PlantInventoryEntry p where p.name like %?1%")
    List<PlantInventoryEntry> finderMethod(String name);

    @Query(value="select * from plant_inventory_entry where name like %?1%", nativeQuery=true)
    List<PlantInventoryEntry> finderMethodV2(String name);
}
