package org.miage.bankservice.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@NamedQueries({
        @NamedQuery(name = "Transfert.findByAccountFrom_IdEqualsAndAccountTo_IdEquals",
                query = "select t from Transfert t where t.accountFrom.id = :id1 and t.accountTo.id = :id2")
})

@Entity
// ORM: mapping des instances de la classe comme nuplet dans H2
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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="accountFrom_id", nullable=false)
    private Account accountFrom;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="accountTo_id", nullable=false)
    private Account accountTo;
}

