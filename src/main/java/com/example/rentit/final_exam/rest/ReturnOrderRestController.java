package com.example.rentit.final_exam.rest;

import com.example.rentit.common.application.dto.SimpleErrorDTO;
import com.example.rentit.common.application.dto.ValidationErrorDTO;
import com.example.rentit.common.application.exception.ResourceNotFoundException;
import com.example.rentit.common.application.exception.ValidationException;
import com.example.rentit.final_exam.application.dto.ReturnOrderDTO;
import com.example.rentit.final_exam.application.dto.ReturnOrderRequestDTO;
import com.example.rentit.final_exam.application.service.ReturnOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;
import java.util.HashMap;

@RestController
@RequestMapping("/api/returns")
public class ReturnOrderRestController {
    @Autowired
    ReturnOrderService returnOrderService;

    @PostMapping("/return-order")
    @ResponseStatus(HttpStatus.CREATED)
    public ReturnOrderDTO createReturnOrder(@RequestBody ReturnOrderRequestDTO request)
        throws ResourceNotFoundException, ValidationException {
        return returnOrderService.createReturnOrder(request);
    }

    @PatchMapping("/return-order/{id}/accept")
    @ResponseStatus(HttpStatus.OK)
    public ReturnOrderDTO acceptReturnOrder(@PathParam("id") Long id) {
        return null;
    }

//    @PatchMapping("/return-order/{id}/reject")
//    @ResponseStatus(HttpStatus.OK)
//    public ReturnOrderDTO rejectReturnOrder(@PathParam("id") Long id) {
//        return null;
//    }

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
        return new ResponseEntity<>(validationErrorDTO, HttpStatus.CONFLICT);
    }
}
