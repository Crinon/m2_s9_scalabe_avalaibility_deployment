package org.miage.bankservice.boundary;

import org.miage.bankservice.entity.Operation;
import org.miage.bankservice.entity.Transfert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OperationResource extends JpaRepository<Operation, String> {
    Operation findFirstByOrderByIdDesc();

    List<Operation> findByAccountFrom_IdEqualsIgnoreCaseOrAccountTo_IdEqualsIgnoreCase(String id, String id1);

}