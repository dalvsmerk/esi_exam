package com.example.rentit.final_exam.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor(staticName = "of")
public class ReturnOrderRequestDTO {
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate returnDate;

    List<Long> plantItems;
}
