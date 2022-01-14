package org.miage.bankservice.boundary;

import org.miage.bankservice.entity.Transfert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import java.util.List;

@Component
public interface TransfertResource extends JpaRepository<Transfert, String> {
    List<Transfert> findByAccountFrom_IdEqualsIgnoreCaseOrAccountTo_IdEqualsIgnoreCase(String id, String id1);

}
