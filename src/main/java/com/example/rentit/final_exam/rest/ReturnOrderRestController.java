package com.example.rentit.final_exam.rest;

import com.example.rentit.common.application.dto.SimpleErrorDTO;
import com.example.rentit.common.application.exception.ResourceNotFoundException;
import com.example.rentit.final_exam.application.dto.ReturnOrderDTO;
import com.example.rentit.final_exam.application.dto.ReturnOrderRequestDTO;
import com.example.rentit.final_exam.application.service.ReturnOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;

@RestController
@RequestMapping("/api/returns")
public class ReturnOrderRestController {
    @Autowired
    ReturnOrderService returnOrderService;

    @PostMapping("/return-order")
    @ResponseStatus(HttpStatus.CREATED)
    public ReturnOrderDTO createReturnOrder(@RequestBody ReturnOrderRequestDTO request)
        throws ResourceNotFoundException {
        return returnOrderService.createReturnOrder(request);
    }

    @PatchMapping("/return-order/{id}/accept")
    @ResponseStatus(HttpStatus.OK)
    public ReturnOrderDTO acceptReturnOrder(@PathParam("id") Long id) {
        return null;
    }

    @PatchMapping("/return-order/{id}/reject")
    @ResponseStatus(HttpStatus.OK)
    public ReturnOrderDTO rejectReturnOrder(@PathParam("id") Long id) {
        return null;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<SimpleErrorDTO> handleResourceNotFoundException(ResourceNotFoundException ex) {
        SimpleErrorDTO notFoundError = SimpleErrorDTO.of(ex.getMessage());

        return new ResponseEntity<>(notFoundError, HttpStatus.NOT_FOUND);
    }
}
