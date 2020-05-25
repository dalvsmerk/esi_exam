package com.example.rentit.sales.domain.repository;

import com.example.rentit.sales.domain.model.PurchaseOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import java.util.List;

@Service
public class PurchaseOrderRepositoryImpl implements CustomPurchaseOrderRepository {
    @Autowired
    EntityManager em;

    public PurchaseOrder findOneByPlantId(Long plantId) {
        List<PurchaseOrder> orders = em.createQuery(
                "select po.id from PurchaseOrder po " +
                        "where po.plant.id = ?1 and po.status = 'ACCEPTED'")
                .setParameter(1, plantId)
                .getResultList();

        if (orders.isEmpty()) return null;

        return orders.get(0);
    }
}
