package com.example.rentit.sales.domain.repository;

import com.example.rentit.sales.domain.model.PurchaseOrder;

public interface CustomPurchaseOrderRepository {
    PurchaseOrder findOneByPlantId(Long id);
}
