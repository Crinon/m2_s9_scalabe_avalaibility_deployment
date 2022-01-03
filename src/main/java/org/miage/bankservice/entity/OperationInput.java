package org.miage.bankservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.env.Environment;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperationInput {
    private Environment env;

    @NotNull
    @NotBlank
    private String wording;
    @NotNull
    @DecimalMin("0.01")
    // @DecimalMax("${app.operation.max-amount}")
    @DecimalMax("5000")
    private Double amount;
    @DecimalMin("0.01")
    @DecimalMax("1.00")
    private Double conversionRate;

    // Il se peut que la catégorie ne soit pas connue.
    private Operation.Category category;


    @NotNull
    @NotBlank
    private String idaccountCustomer;
    @NotNull
    @NotBlank
    private String idaccountShop;
}
