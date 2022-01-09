package org.miage.bankservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class TransfertInput {
    @NotNull
    @DecimalMin("0.01")
    @DecimalMax("5000.00")
    private Double amount;
    @NotNull
    @NotBlank
    private String idaccountFrom;
    @NotNull
    @NotBlank
    private String idaccountTo;
}
