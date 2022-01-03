package org.miage.bankservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.miage.bankservice.miscellaneous.ToolBox;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Card {
    private static final long serialVersionUID = 767432234567L;

    @Id
    private String idcard = UUID.randomUUID().toString();
    private String number = ToolBox.generate(16);
    private int code = Integer.parseInt(ToolBox.generate(4));
    private int cryptogram = Integer.parseInt(ToolBox.generate(3));
    private boolean blocked = false;
    private boolean gps = true;
    private int slidinglimit = 5000;
    private boolean contactless = true;
    // Obligation de placer 300€ à l'ouverture d'un compte
    private String cash = "300.00";

//    private Account account; // mapped by card : card est le nom de la variable dans Account
}
