package org.miage.bankservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.miage.bankservice.miscellaneous.ToolBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;

import javax.persistence.Column;
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
    @Column(unique=true)
    private String number = ToolBox.generateCardNumber(16);
    private int code = Integer.parseInt(ToolBox.generateCardNumber(4));
    private int cryptogram = Integer.parseInt(ToolBox.generateCardNumber(3));
    private boolean blocked = false;
    private boolean regionLocked = true;
    private int slidinglimit = 5000;
    private boolean contactless = true;
    // Obligation de placer 300€ à l'ouverture d'un compte
    private String cash = "300.00";
}
