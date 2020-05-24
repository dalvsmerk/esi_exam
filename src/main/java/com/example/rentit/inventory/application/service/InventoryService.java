package com.example.rentit.inventory.application.service;

import com.example.rentit.common.application.service.ExternalAPICommunicator;
import com.example.rentit.inventory.application.dto.PlantInventoryEntryDTO;
import com.example.rentit.inventory.application.dto.PlantInventoryItemDTO;
import com.example.rentit.inventory.application.exception.*;
import com.example.rentit.inventory.domain.model.*;
import com.example.rentit.inventory.domain.repository.InventoryRepository;
import com.example.rentit.inventory.domain.repository.PlantInventoryEntryRepository;
import com.example.rentit.inventory.domain.repository.PlantInventoryItemRepository;
import com.example.rentit.inventory.domain.repository.PlantReservationRepository;
import com.example.rentit.sales.application.service.SalesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    @Autowired
    InventoryRepository inventoryRepository;

    @Autowired
    PlantInventoryEntryAssembler plantInventoryEntryAssembler;

    @Autowired
    PlantReservationRepository plantReservationRepository;

    @Autowired
    PlantInventoryEntryRepository plantInventoryEntryRepository;

    @Autowired
    PlantInventoryItemRepository plantInventoryItemRepository;

    @Autowired
    PlantInventoryItemAssembler plantInventoryItemAssembler;

    @Autowired
    ExternalAPICommunicator externalAPICommunicator;

    @Autowired
    SalesService salesService;

    //   Plant Items actions

    public Boolean verifyPlantActionTransition(PlantStatus from, PlantStatus to) {
        if (from == null) return true;

        switch (from) {
            case AVAILABLE: return to == PlantStatus.DISPATCHED;
            case DISPATCHED: return to == PlantStatus.REJECTED_BY_CUSTOMER || to == PlantStatus.DELIVERED;
            case REJECTED_BY_CUSTOMER:
            case DELIVERED: return to == PlantStatus.AVAILABLE;

            default: return false;
        }
    }

    public PlantInventoryItemDTO changePlantStatus(Long plantId, PlantStatus status) throws PlantInventoryItemNotFoundException, PlantInventoryForbiddenActionException {
        PlantInventoryItem item = plantInventoryItemRepository.findById(plantId).orElseThrow(() -> new PlantInventoryItemNotFoundException(plantId));

        if (!this.verifyPlantActionTransition(item.getStatus(), status)) throw new PlantInventoryForbiddenActionException(item.getStatus(), status);

        item.setStatus(status);
        plantInventoryItemRepository.save(item);
        return  plantInventoryItemAssembler.toResource(item);
    }

    public PlantInventoryItemDTO dispatchPlant(Long plantId) throws PlantInventoryItemNotFoundException, PlantInventoryForbiddenActionException {
        return this.changePlantStatus(plantId, PlantStatus.DISPATCHED);
    }

    public PlantInventoryItemDTO deliverPlant(Long plantId) throws PlantInventoryItemNotFoundException, PlantInventoryForbiddenActionException {
        return this.changePlantStatus(plantId, PlantStatus.DELIVERED);
    }

    public PlantInventoryItemDTO rejectPlant(Long plantId) throws PlantInventoryItemNotFoundException, PlantInventoryForbiddenActionException {
        return this.changePlantStatus(plantId, PlantStatus.REJECTED_BY_CUSTOMER);
    }

    public PlantInventoryItemDTO returnPlant(Long plantId) throws PlantInventoryItemNotFoundException, PlantInventoryForbiddenActionException {
        return this.changePlantStatus(plantId, PlantStatus.AVAILABLE);
    }

    public boolean checkPlantIsAvailable(Long plantId, LocalDate startDate, LocalDate endDate) throws PlantInventoryItemNotFoundException {
        PlantInventoryItem item = plantInventoryItemRepository.findById(plantId).orElseThrow(() -> new PlantInventoryItemNotFoundException(plantId));
        return item.getEquipmentCondition() != EquipmentCondition.UNSERVICEABLECONDEMNED && inventoryRepository.checkPlantIsAvailable(plantId, startDate, endDate);
    }

    public List<PlantInventoryEntryDTO> findAvailablePlants(String name, LocalDate startDate, LocalDate endDate) {
        List<PlantInventoryEntry> entries = inventoryRepository.findAvailablePlants(name, startDate, endDate);
        return plantInventoryEntryAssembler.toResources(entries);
    }

    public PlantInventoryItemDTO createPlantInventoryItem (PlantInventoryItemDTO itemDTO) throws Exception {
        PlantInventoryEntry entry = getPlantInventoryEntry(itemDTO);
        PlantInventoryItem item = PlantInventoryItem.of(null, itemDTO.getSerialNumber(), itemDTO.getEquipmentCondition(), entry, PlantStatus.AVAILABLE);
        validatePlantInventoryItem(item);
        plantInventoryItemRepository.save(item);
        return plantInventoryItemAssembler.toResource(item);
    }

    public PlantInventoryItemDTO updatePlantInventoryItem(Long itemId, PlantInventoryItemDTO itemDTO) throws Exception {
        PlantInventoryItem item = plantInventoryItemRepository.findById(itemId)
                .orElseThrow(() -> new PlantInventoryItemNotFoundException(itemId));

        if (itemDTO.getPlantInfo() != null) {
            item.setPlantInfo(getPlantInventoryEntry(itemDTO));
        }
        if (itemDTO.getEquipmentCondition() != null) {
            item.setEquipmentCondition(itemDTO.getEquipmentCondition());
        }
        if (itemDTO.getSerialNumber() != null) {
            item.setSerialNumber(itemDTO.getSerialNumber());
        }

        validatePlantInventoryItem(item);

        item = plantInventoryItemRepository.saveAndFlush(item);
        return plantInventoryItemAssembler.toResource(item);
    }

    public BigDecimal getPlantInventoryItemPrice(Long itemId, LocalDate startDate, LocalDate endDate) throws Exception {
        if (!startDate.isBefore(endDate)) {
            return BigDecimal.valueOf(0);
        }

        PlantInventoryItem item = plantInventoryItemRepository.findById(itemId)
                .orElseThrow(() -> new PlantInventoryItemNotFoundException(itemId));

        Period period = Period.between(startDate, endDate);
        int diff = period.getDays();

        return item.getPlantInfo().getPrice().multiply(BigDecimal.valueOf(diff));
    }

    private PlantInventoryEntry getPlantInventoryEntry(PlantInventoryItemDTO itemDTO) throws Exception {
        PlantInventoryEntry entry = null;
        if (itemDTO.getPlantInfo() != null) {
            entry = plantInventoryEntryRepository
                    .findById(itemDTO.getPlantInfo().get_id())
                    .orElseThrow(() -> new PlantInventoryEntryNotFoundException(itemDTO.getPlantInfo().get_id()));
        }
        return entry;
    }

    public PlantInventoryEntryDTO updatePlantInventoryEntry(Long entryId, PlantInventoryEntryDTO entryDTO) throws Exception {
        PlantInventoryEntry entry = plantInventoryEntryRepository.findById(entryId)
                .orElseThrow(() -> new PlantInventoryEntryNotFoundException(entryId));

        if (entryDTO.getName() != null) {
            entry.setName(entryDTO.getName());
        }
        if (entryDTO.getDescription() != null) {
            entry.setDescription(entryDTO.getDescription());
        }
        if (entryDTO.getPrice() != null) {
            entry.setPrice(entryDTO.getPrice());
        }

        validatePlantInventoryEntry(entry);

        entry = plantInventoryEntryRepository.saveAndFlush(entry);
        return plantInventoryEntryAssembler.toResource(entry);
    }

    private void validatePlantInventoryItem(PlantInventoryItem item) throws Exception {
        DataBinder binder = new DataBinder(item);
        binder.addValidators(new PlantInventoryItemValidator());
        binder.validate();

        BindingResult bindingResult = binder.getBindingResult();

        if (bindingResult.hasFieldErrors()) {
            throw new PlantInventoryItemValidationException(bindingResult);
        }
    }

    public PlantInventoryEntryDTO createPlantInventoryEntry (PlantInventoryEntryDTO entryDTO) throws Exception {
        PlantInventoryEntry e = PlantInventoryEntry.of(null, entryDTO.getName(), entryDTO.getDescription(), entryDTO.getPrice());

        validatePlantInventoryEntry(e);
        plantInventoryEntryRepository.save(e);
        return plantInventoryEntryAssembler.toResource(e);
    }

    public List<PlantInventoryItemDTO> findAvailableItems(LocalDate startDate, LocalDate endDate) {
        List<Pair<PlantInventoryItem, BigDecimal>> items =
                plantInventoryItemRepository.findAvailableItems(startDate, endDate);

        List<PlantInventoryItemDTO> itemsDTO = new ArrayList<>();

        for (Pair<PlantInventoryItem, BigDecimal> itemToPrice : items) {
            PlantInventoryItemDTO itemDTO = plantInventoryItemAssembler.toResource(itemToPrice.getFirst());
            BigDecimal price = itemToPrice.getSecond();
            itemDTO.setPrice(price);

            itemsDTO.add(itemDTO);
        }

        return itemsDTO;
    }

    private void validatePlantInventoryEntry(PlantInventoryEntry entry) throws Exception {
        DataBinder binder = new DataBinder(entry);
        binder.addValidators(new PlantInventoryEntryValidator());
        binder.validate();

        BindingResult bindingResult = binder.getBindingResult();

        if (bindingResult.hasFieldErrors()) {
            throw new PlantInventoryEntryValidationException(bindingResult);
        }
    }
}