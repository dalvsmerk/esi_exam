package com.example.rentit.sales.rest;

import com.example.rentit.common.application.dto.SimpleErrorDTO;
import com.example.rentit.common.application.dto.ValidationErrorDTO;
import com.example.rentit.common.application.exception.RelationViolationException;
import com.example.rentit.common.application.exception.ResourceNotFoundException;
import com.example.rentit.common.application.exception.ValidationException;
import com.example.rentit.sales.application.dto.InvoiceDTO;
import com.example.rentit.sales.application.exception.*;
import com.example.rentit.inventory.application.service.InventoryService;
import com.example.rentit.sales.application.dto.PurchaseOrderDTO;
import com.example.rentit.sales.application.service.SalesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/sales")
public class SalesRestController {
    @Autowired
    InventoryService inventoryService;

    @Autowired
    SalesService salesService;

    @GetMapping("/orders")
    @ResponseStatus(HttpStatus.OK)
    public List<PurchaseOrderDTO> fetchPurchaseOrders() {
        return salesService.findPOs();
    }

    @GetMapping("/orders/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PurchaseOrderDTO fetchPurchaseOrder(@PathVariable("id") Long id) {
        return salesService.findPO(id);
    }

    @PostMapping("/orders")
    public ResponseEntity<PurchaseOrderDTO> createPurchaseOrder(@RequestBody PurchaseOrderDTO partialPODTO)
            throws Exception {
        PurchaseOrderDTO newlyCreatePODTO = salesService.createPO(partialPODTO);

        return new ResponseEntity<>(newlyCreatePODTO, HttpStatus.CREATED);
    }

    @PatchMapping("/orders/{id}")
    public PurchaseOrderDTO editPurchaseOrderPeriod(
            @PathVariable Long id,
            @RequestParam(name = "startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) throws PurchaseOrderNotFoundException {
        return salesService.editPOPeriod(id, startDate, endDate);
    }

    @PostMapping("/orders/{id}/extend")
    public PurchaseOrderDTO extendPurchaseOrder(
            @PathVariable Long id,
            @RequestParam(name = "endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) throws PurchaseOrderNotFoundException, ValidationException, PlantNotAvailableException {
        return salesService.extendPO(id, endDate);
    }

    @PatchMapping("/orders/{id}/accept")
    public PurchaseOrderDTO acceptPurchaseOrder(@PathVariable Long id) throws Exception {
        return salesService.acceptPO(id);
    }

    @PatchMapping("/orders/{id}/reject")
    public PurchaseOrderDTO rejectPurchaseOrder(
            @PathVariable Long id,
            @RequestParam(name = "rejectReason") String rejectReason
    ) throws Exception {
        return salesService.rejectPO(id, rejectReason);
    }

    @PatchMapping("/orders/{id}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public PurchaseOrderDTO cancelPurchaseOrder(@PathVariable Long id) throws Exception {
        return salesService.cancelPO(id);
    }

    @GetMapping("/invoices/{id}")
    @ResponseStatus(HttpStatus.OK)
    public InvoiceDTO getInvoice(@PathVariable Long id) throws InvoiceNotFoundException {
        return salesService.getInvoice(id);
    }

    @PostMapping("/invoices/{id}/pay")
    @ResponseStatus(HttpStatus.OK)
    public InvoiceDTO payInvoice(@PathVariable Long id) throws InvoiceNotFoundException, InvoiceStatusViolationException {
        return salesService.payInvoice(id);
    }

    @PostMapping("/invoices/{id}/reject")
    @ResponseStatus(HttpStatus.OK)
    public InvoiceDTO rejectInvoice(@PathVariable Long id) throws InvoiceNotFoundException, InvoiceStatusViolationException {
        return salesService.rejectInvoice(id);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<SimpleErrorDTO> handleResourceNotFoundException(Exception ex) {
        SimpleErrorDTO scheduleError = SimpleErrorDTO.of(ex.getMessage());

        return new ResponseEntity<>(scheduleError, HttpStatus.NOT_FOUND);
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

    @ExceptionHandler({PlantNotAvailableException.class, RelationViolationException.class})
    public ResponseEntity<SimpleErrorDTO> handlePlantNotAvailableException(Exception ex) {
        SimpleErrorDTO scheduleError = SimpleErrorDTO.of(ex.getMessage());

        return new ResponseEntity<>(scheduleError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CustomerPurchaseOrderIdNotProvidedException.class)
    public ResponseEntity<SimpleErrorDTO> handleValidationPurchaseOrderException(Exception ex) {
        SimpleErrorDTO scheduleError = SimpleErrorDTO.of(ex.getMessage());
        return new ResponseEntity<>(scheduleError, HttpStatus.BAD_REQUEST);
    }
}