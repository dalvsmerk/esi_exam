package com.example.rentit.final_exam.domain.model;

import com.example.rentit.common.domain.model.BusinessPeriod;
import com.example.rentit.sales.domain.model.PurchaseOrder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Duration;
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

    @Enumerated(EnumType.STRING)
    ReturnOrderStatus status;

    public static BigDecimal computeFee(List<PurchaseOrder> orders) {
        BigDecimal fee = BigDecimal.ZERO;

        for (PurchaseOrder order : orders) {
            BusinessPeriod period = order.getRentalPeriod();
            Long daysBetween = Duration
                    .between(period.getStartDate().atStartOfDay(), period.getEndDate().atStartOfDay())
                    .toDays();
            BigDecimal orderFee = BigDecimal.valueOf(daysBetween).multiply(order.getPlant().getPlantInfo().getPrice());

            fee = fee.add(orderFee);
        }

        // 5% fee of total purchase orders price
        return fee.multiply(BigDecimal.valueOf(0.05));
    }
}
