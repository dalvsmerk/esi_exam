package com.example.rentit.sales.domain.repository;

import com.example.rentit.sales.domain.model.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseOrderRepository
        extends JpaRepository<PurchaseOrder, Long>, CustomPurchaseOrderRepository {
}
