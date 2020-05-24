package com.example.rentit.support.rest;

import com.example.rentit.common.application.dto.SimpleErrorDTO;
import com.example.rentit.common.application.dto.ValidationErrorDTO;
import com.example.rentit.common.application.exception.ResourceNotFoundException;
import com.example.rentit.support.application.dto.MaintenanceOrderDTO;
import com.example.rentit.support.application.exception.MOTransitionValidationException;
import com.example.rentit.support.domain.model.MaintenanceOrderStatus;
import com.example.rentit.support.application.service.MaintenanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/api/support")
public class MaintenanceRestController {
    @Autowired
    MaintenanceService maintenanceService;

    @PostMapping("/maintenance-order")
    public ResponseEntity<MaintenanceOrderDTO> createMaintenanceOrder(@RequestBody MaintenanceOrderDTO partialOrderDTO)
            throws Exception {
        MaintenanceOrderDTO orderDTO = maintenanceService.createMaintenanceOrder(partialOrderDTO);
        return new ResponseEntity<>(orderDTO, HttpStatus.CREATED);
    }

    @GetMapping("/maintenance-order/{orderId}")
    public ResponseEntity<MaintenanceOrderDTO> getMaintenanceOrder(@PathVariable Long orderId)
            throws Exception {
        MaintenanceOrderDTO orderDTO = maintenanceService.findMaintenanceOrder(orderId);
        return new ResponseEntity<>(orderDTO, HttpStatus.OK);
    }

    @PatchMapping("/maintenance-order/{orderId}/cancel")
    public ResponseEntity<MaintenanceOrderDTO> cancelMaintenanceOrder(@PathVariable Long orderId)
        throws Exception {
        MaintenanceOrderStatus newStatus = MaintenanceOrderStatus.CANCELLED;
        MaintenanceOrderDTO orderDTO = maintenanceService.updateMaintenanceOrderStatus(orderId, newStatus);
        return new ResponseEntity<>(orderDTO, HttpStatus.OK);
    }

    @PatchMapping("/maintenance-order/{orderId}/complete")
    public ResponseEntity<MaintenanceOrderDTO> completeMaintenanceOrder(@PathVariable Long orderId)
            throws Exception {
        MaintenanceOrderStatus newStatus = MaintenanceOrderStatus.COMPLETED;
        MaintenanceOrderDTO orderDTO = maintenanceService.updateMaintenanceOrderStatus(orderId, newStatus);
        return new ResponseEntity<>(orderDTO, HttpStatus.OK);
    }

    @PatchMapping("/maintenance-order/{orderId}/accept")
    public ResponseEntity<MaintenanceOrderDTO> acceptMaintenanceOrder(
            @PathVariable Long orderId,
            @RequestBody MaintenanceOrderDTO partialOrderDTO) throws Exception {
        MaintenanceOrderDTO orderDTO = maintenanceService.acceptMaintenanceOrder(orderId, partialOrderDTO);
        return new ResponseEntity<>(orderDTO, HttpStatus.OK);
    }

    @PatchMapping("/maintenance-order/{orderId}/reject")
    public ResponseEntity<MaintenanceOrderDTO> rejectMaintenanceOrder(@PathVariable Long orderId)
            throws Exception {
        MaintenanceOrderStatus newStatus = MaintenanceOrderStatus.REJECTED;
        MaintenanceOrderDTO orderDTO = maintenanceService.updateMaintenanceOrderStatus(orderId, newStatus);
        return new ResponseEntity<>(orderDTO, HttpStatus.OK);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<SimpleErrorDTO> handleResourceNotFoundException(ResourceNotFoundException ex) {
        SimpleErrorDTO notFoundError = SimpleErrorDTO.of(ex.getMessage());

        return new ResponseEntity<>(notFoundError, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MOTransitionValidationException.class)
    public ResponseEntity<ValidationErrorDTO> handleValidationException(MOTransitionValidationException ex) {
        HashMap<String, String> errors = new HashMap<>();
        BindingResult bindingResult = ex.getBindingResult();

        bindingResult.getFieldErrors().stream().forEach(
                fieldError -> errors.put(fieldError.getField(), fieldError.getCode()));

        ValidationErrorDTO validationErrorDTO = ValidationErrorDTO.of(errors);
        return new ResponseEntity<>(validationErrorDTO, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<SimpleErrorDTO> generalException(Exception ex) {
        SimpleErrorDTO errorDTO = SimpleErrorDTO.of(ex.getMessage());

        return new ResponseEntity<>(errorDTO, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
