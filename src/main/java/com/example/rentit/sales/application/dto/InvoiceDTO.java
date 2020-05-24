package com.example.rentit.sales.application.dto;

import com.example.rentit.common.rest.ResourceSupport;
import com.example.rentit.sales.domain.model.InvoiceStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(staticName = "of")
public class InvoiceDTO extends ResourceSupport {
    Long _id;
    BigDecimal total;
    PurchaseOrderDTO purchaseOrderDTO;
    InvoiceStatus status;
}