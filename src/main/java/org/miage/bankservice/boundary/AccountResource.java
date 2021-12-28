package org.miage.bankservice.boundary;

import org.miage.bankservice.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountResource extends JpaRepository<Account, String>{
}
