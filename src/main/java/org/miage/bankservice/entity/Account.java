package org.miage.bankservice.entity;

import java.io.Serializable;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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
}
