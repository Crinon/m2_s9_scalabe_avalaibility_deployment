package org.miage.bankservice.entity;

import javax.validation.constraints.*;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class TransfertInput {
    private LocalDateTime localdatetime;
    @NotNull
    @DecimalMin("0.01")
    @DecimalMax("5000.00")
    private Double amount;
    @NotNull
    private String idaccountFrom;
    @NotNull
    private String idaccountTo;
}
