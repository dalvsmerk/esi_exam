package com.example.rentit.sales.domain.repository;
import com.example.rentit.sales.domain.model.Invoice;
import com.example.rentit.sales.domain.model.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> getAllByStatus(InvoiceStatus status);
}
