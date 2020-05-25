package com.example.rentit.final_exam.domain.model;

import com.example.rentit.sales.domain.model.PurchaseOrder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Entity
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@AllArgsConstructor(staticName = "of")
public class ReturnOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    LocalDate returnDate;

    @OneToMany
    List<PurchaseOrder> orders;

    BigDecimal fee;
    ReturnOrderStatus status;
}
