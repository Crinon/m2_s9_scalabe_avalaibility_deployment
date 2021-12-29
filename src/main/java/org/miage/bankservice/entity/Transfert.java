package org.miage.bankservice.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;
import org.springframework.data.rest.core.annotation.RestResource;

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
