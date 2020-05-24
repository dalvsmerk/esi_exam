package com.example.rentit.inventory.rest;

import com.example.rentit.common.application.dto.SimpleErrorDTO;
import com.example.rentit.common.application.dto.ValidationErrorDTO;
import com.example.rentit.common.application.exception.ResourceNotFoundException;
import com.example.rentit.common.application.exception.ValidationException;
import com.example.rentit.inventory.application.dto.PlantInventoryEntryDTO;
import com.example.rentit.inventory.application.dto.PlantInventoryItemDTO;
import com.example.rentit.inventory.application.exception.MissingParameterException;
import com.example.rentit.inventory.application.exception.PlantInventoryForbiddenActionException;
import com.example.rentit.inventory.application.exception.PlantInventoryItemNotFoundException;
import com.example.rentit.inventory.application.service.InventoryService;
import com.example.rentit.sales.application.service.SalesService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
public class InventoryRestController {
    @Autowired
    InventoryService inventoryService;

    @Autowired
    SalesService salesService;


    //   Plant Items

    @GetMapping("/plant")
    public ResponseEntity<List<PlantInventoryItemDTO>> listAvailablePlants(
            @RequestParam(name = "startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<PlantInventoryItemDTO> items = inventoryService.findAvailableItems(startDate, endDate);
        return new ResponseEntity<>(items, HttpStatus.OK);
    }

    @GetMapping("/plant/{itemId}")
    public ResponseEntity<String> checkPlantIsAvailable(
            @PathVariable Long itemId,
            @RequestParam(name = "startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) throws PlantInventoryItemNotFoundException {
        boolean result = inventoryService.checkPlantIsAvailable(itemId, startDate, endDate);

        Map<String, Boolean> resultMap = new HashMap<>();
        resultMap.put("available", result);

        String json = null;

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            json = objectMapper.writeValueAsString(resultMap);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<String>(json, HttpStatus.OK);
    }

    @GetMapping("/plant/{itemId}/price")
    public ResponseEntity<String> getPlantPrice(
            @PathVariable Long itemId,
            @RequestParam(name = "startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) throws Exception {
        BigDecimal price = inventoryService.getPlantInventoryItemPrice(itemId, startDate, endDate);

        Map<String, BigDecimal> resultMap = new HashMap<>();
        resultMap.put("price", price);

        String json = null;

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            json = objectMapper.writeValueAsString(resultMap);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<String>(json, HttpStatus.OK);
    }

    @PostMapping("/plant")
    public ResponseEntity<PlantInventoryItemDTO> createPlantInventoryItem(@RequestBody PlantInventoryItemDTO partialPlantInventoryItem) throws Exception {
        PlantInventoryItemDTO pIIDTO = inventoryService.createPlantInventoryItem(partialPlantInventoryItem);
        return new ResponseEntity<>(pIIDTO, HttpStatus.CREATED);
    }

    @PatchMapping("/plant/{itemId}")
    public PlantInventoryItemDTO updatePlantInventoryItem(@PathVariable Long itemId, @RequestBody PlantInventoryItemDTO edits) throws Exception {
        return inventoryService.updatePlantInventoryItem(itemId, edits);
    }

    @PatchMapping("/plant/{itemId}/{action}")
    public ResponseEntity<PlantInventoryItemDTO> performTheActionOnThePlant(
            @PathVariable Long itemId,
            @PathVariable String action,
            @RequestParam(name = "orderId", required = false) Long orderId)
            throws Exception {
        switch (action) {
            case "dispatch": return new ResponseEntity<PlantInventoryItemDTO>(inventoryService.dispatchPlant(itemId), HttpStatus.OK);
            case "deliver": return new ResponseEntity<PlantInventoryItemDTO>(inventoryService.deliverPlant(itemId), HttpStatus.OK);
            case "reject": return new ResponseEntity<PlantInventoryItemDTO>(inventoryService.rejectPlant(itemId), HttpStatus.OK);
            case "return": {
                if (orderId == null) {
                    throw new MissingParameterException("orderId");
                }

                salesService.createInvoice(orderId);
                return new ResponseEntity<PlantInventoryItemDTO>(inventoryService.returnPlant(itemId), HttpStatus.OK);
            }
            default: return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    //   Plant Entries

    @GetMapping("/plant-entries")
    public List<PlantInventoryEntryDTO> findAvailablePlants(
            @RequestParam(name = "name") String plantName,
            @RequestParam(name = "startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return inventoryService.findAvailablePlants(plantName.toLowerCase(), startDate, endDate);
    }

    @PostMapping("/plant-entries")
    public ResponseEntity<PlantInventoryEntryDTO> createPlant(@RequestBody PlantInventoryEntryDTO plantEntryDTO) throws Exception {
        PlantInventoryEntryDTO d = inventoryService.createPlantInventoryEntry(plantEntryDTO);
        return new ResponseEntity<>(d, HttpStatus.CREATED);
    }

    @PatchMapping("/plant-entries/{id}")
    public PlantInventoryEntryDTO updatePlant(@PathVariable Long id, @RequestBody PlantInventoryEntryDTO edits) throws Exception {
        return inventoryService.updatePlantInventoryEntry(id, edits);
    }


    @ExceptionHandler({PlantInventoryForbiddenActionException.class, MissingParameterException.class})
    public ResponseEntity<SimpleErrorDTO> handleResourceNotFoundException(PlantInventoryForbiddenActionException ex) {
        SimpleErrorDTO notFoundError = SimpleErrorDTO.of(ex.getMessage());
        return new ResponseEntity<>(notFoundError, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<SimpleErrorDTO> handleResourceNotFoundException(ResourceNotFoundException ex) {
        SimpleErrorDTO notFoundError = SimpleErrorDTO.of(ex.getMessage());

        return new ResponseEntity<>(notFoundError, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ValidationErrorDTO> handleValidationException(ValidationException ex) {
        HashMap<String, String> errors = new HashMap<>();
        BindingResult bindingResult = ex.getBindingResult();

        bindingResult.getFieldErrors().stream().forEach(
                fieldError -> errors.put(fieldError.getField(), fieldError.getCode()));

        ValidationErrorDTO validationErrorDTO = ValidationErrorDTO.of(errors);
        return new ResponseEntity<>(validationErrorDTO, HttpStatus.BAD_REQUEST);
    }
}
