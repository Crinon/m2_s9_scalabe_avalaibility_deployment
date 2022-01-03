package org.miage.bankservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardInput {
    @NotNull
    @Digits(integer=16, fraction=0)
    private String number;
    @NotNull
    @Digits(integer=4, fraction=0)
    private int code;
    @NotNull
    @Digits(integer=3, fraction=0)
    private int cryptogram;
    @NotNull
    private boolean blocked;
    @NotNull
    private boolean gps;
    @NotNull
    @Digits(integer=4, fraction=0) // débit max : 9.999€
    private int slidinglimit;
    @NotNull
    private boolean contactless;
    @NotNull
    @NotBlank
    private String cash;
}
