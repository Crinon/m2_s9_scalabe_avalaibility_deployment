package org.miage.bankservice.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

@Entity     // ORM: mapping des instances de la classe comme nuplet dans H2
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Account implements Serializable {


    public enum Country {
        FRANCE, GERMANY, ITALY, MOLDOVA, JAPAN, USA, UK;
    }

    @Id
    private String id;
    private String name;
    private String surname;
    private Country country;
    @Column(unique=true)
    private String passportNumber;
    @Column(unique=true)
    private String phoneGlobal;
    @Column(unique=true)
    private String iban;

    private String fkidcard;

    @OneToMany( targetEntity=Transfert.class, mappedBy="accountTo", cascade = CascadeType.ALL )
    private Set<Transfert> transfertsReceived;
    @OneToMany( targetEntity=Transfert.class, mappedBy="accountFrom", cascade = CascadeType.ALL )
    private Set<Transfert> transfertsSent;

    private String password;



}
