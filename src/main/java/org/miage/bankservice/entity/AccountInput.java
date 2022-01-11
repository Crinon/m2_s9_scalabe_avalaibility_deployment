package org.miage.bankservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.validation.constraints.Email;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountInput {

    @NotNull
    @NotBlank
    @Size(min=2, max=20)
    @Pattern(regexp = "(?i)^(?:(?![×Þß÷þø])[-'a-zÀ-ÿ])+$") // only alphabets + accent
    private String name;
    @NotNull
    @NotBlank
    @Size(min=2, max=20)
    @Pattern(regexp = "(?i)^(?:(?![×Þß÷þø])[-'a-zÀ-ÿ])+$") // only alphabets + accents
    private String surname;
    @NotNull
    private Account.Country country;
    @NotNull
    @NotBlank
    @Size(min=3, max=20)
    @Pattern(regexp = "^(?!^0+$)[a-zA-Z0-9]{3,20}$")
    private String passportNumber;
    @NotNull
    @NotBlank
    @Pattern(regexp = "^\\+(?:[0-9]●?){6,14}[0-9]$")
    private String phoneGlobal;
//    @NotNull
//    @NotBlank
//    @Pattern(regexp = "\\b[A-Z]{2}[0-9]{2}(?:[ ]?[0-9]{4}){4}(?!(?:[ ]?[0-9]){3})(?:[ ]?[0-9]{1,2})?\\b")
    private String iban;

    private Card card;

    private Set<Transfert> transfertsReceived;
    private Set<Transfert> transfertsSent;

    @NotNull
    @NotBlank
    private String password;

}


