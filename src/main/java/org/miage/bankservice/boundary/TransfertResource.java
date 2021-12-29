package org.miage.bankservice.boundary;

import org.miage.bankservice.entity.Transfert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface TransfertResource extends JpaRepository<Transfert, String> {
}
