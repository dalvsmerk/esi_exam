package com.example.rentit.support.rest;

import com.example.rentit.common.application.dto.SimpleErrorDTO;
import com.example.rentit.common.application.dto.ValidationErrorDTO;
import com.example.rentit.common.application.exception.ResourceNotFoundException;
import com.example.rentit.common.application.exception.ValidationException;
import com.example.rentit.support.application.dto.MaintenancePlanDTO;
import com.example.rentit.support.application.exception.MaintenanceTaskScheduleException;
import com.example.rentit.support.application.service.SupportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/api/support")
public class SupportRestController {

    @Autowired
    SupportService supportService;

    @GetMapping("/maintenance-plan/{planId}")
    public MaintenancePlanDTO getMaintenancePlan(@PathVariable Long planId) throws Exception {
        return supportService.findMaintenancePlan(planId);
    }

    @PostMapping("/maintenance-plan")
    public ResponseEntity<MaintenancePlanDTO> createMaintenancePlan(@RequestBody MaintenancePlanDTO partialPlan)
            throws Exception {
        MaintenancePlanDTO planDTO = supportService.createMaintenancePlan(partialPlan);
        return new ResponseEntity<>(planDTO, HttpStatus.CREATED);
    }

    @PutMapping("/maintenance-plan/{planId}")
    public MaintenancePlanDTO updateMaintenancePlan(
            @PathVariable Long planId,
            @RequestBody MaintenancePlanDTO updatedPlan)
        throws Exception {
        return supportService.updateMaintenancePlan(planId, updatedPlan);
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

    @ExceptionHandler(MaintenanceTaskScheduleException.class)
    public ResponseEntity<SimpleErrorDTO> handleResourceNotFoundException(MaintenanceTaskScheduleException ex) {
        SimpleErrorDTO scheduleError = SimpleErrorDTO.of(ex.getMessage());

        return new ResponseEntity<>(scheduleError, HttpStatus.BAD_REQUEST);
    }
}
