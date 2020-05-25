package com.example.rentit.final_exam.domain.repository;

import com.example.rentit.final_exam.domain.model.ReturnOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReturnOrderRepository extends JpaRepository<ReturnOrder, Long> {
}
