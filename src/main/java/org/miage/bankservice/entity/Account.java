package org.miage.bankservice.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.io.Serializable;
import java.util.Set;

@Entity     // ORM: mapping des instances de la classe comme nuplet dans H2
@Getter
@Setter
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
