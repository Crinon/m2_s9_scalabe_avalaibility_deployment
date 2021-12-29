package org.miage.bankservice.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.mapping.Collection;
import org.springframework.data.rest.core.annotation.RestResource;

@Entity     // ORM: mapping des instances de la classe comme nuplet dans H2
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Account implements Serializable {
    private static final long serialVersionUID = 765432234567L;
    public enum Country {
        FRANCE, GERMANY, ITALY, MOLDOVA, JAPAN, USA, UK;
    }
//    @JoinColumn(name = "idcard", referencedColumnName = "idcard") // name : nom de la colonne de la FK à créer et referencedColumnName = id de la table card

    @Id
    private String id;
    private String name;
    private String surname;
    private Country country;
    private String passportNumber;
    private String phoneGlobal;
    private String iban;

    private String fkidcard;

    @OneToMany( targetEntity=Transfert.class, mappedBy="accountTo" )
    private Set<Transfert> transfertsReceived;
    @OneToMany( targetEntity=Transfert.class, mappedBy="accountFrom" )
    private Set<Transfert> transfertsSent;


}
