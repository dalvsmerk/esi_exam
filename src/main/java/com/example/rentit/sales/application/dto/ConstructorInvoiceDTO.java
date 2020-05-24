package com.example.rentit.sales.application.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(staticName = "of")
public class ConstructorInvoiceDTO {
    BigDecimal price;
    String supplierInvoiceLink;
    Long supplierPOId;
}
