package org.miage.bankservice.boundary;

import org.miage.bankservice.entity.Operation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperationResource extends JpaRepository<Operation, String> {
    Operation findFirstByOrderByIdDesc();

}