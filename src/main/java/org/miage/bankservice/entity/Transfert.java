package org.miage.bankservice.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity     // ORM: mapping des instances de la classe comme nuplet dans H2
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class Transfert {
    @Id
    private String idtransfert;
    private LocalDateTime localdatetime;
    private Double amount;
    @JsonBackReference
    @ManyToOne()
    @JoinColumn(name="accountFrom_id", nullable=false)
    private Account accountFrom;
    @ManyToOne()
    @JoinColumn(name="accountTo_id", nullable=false)
    private Account accountTo;
}
