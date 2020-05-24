package com.example.rentit.sales.application.exception;

import com.example.rentit.common.application.exception.ResourceNotFoundException;

public class PlantNotFoundException extends ResourceNotFoundException {
    private static final long serialVersionUID = 1L;

    public PlantNotFoundException(Long id) { super("Plant", id); }
}
