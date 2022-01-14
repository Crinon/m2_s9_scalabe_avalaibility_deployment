package org.miage.bankservice.security;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class JwtAuthenticationInput {
    private String passportNumber;
    private String password;
}
