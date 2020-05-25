package com.example.rentit.final_exam.application.dto;

import com.example.rentit.common.rest.ResourceSupport;
import com.example.rentit.final_exam.domain.model.ReturnOrderStatus;
import com.example.rentit.sales.application.dto.PurchaseOrderDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor(staticName = "of")
public class ReturnOrderDTO extends ResourceSupport {
    Long _id;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate returnDate;

    List<PurchaseOrderDTO> orders;
    BigDecimal fee;
    ReturnOrderStatus status;
}
