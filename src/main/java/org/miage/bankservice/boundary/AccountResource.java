package org.miage.bankservice.boundary;

import org.miage.bankservice.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountResource extends JpaRepository<Account, String>{
    Optional<Account> findByPassportNumberEqualsIgnoreCase(String passportNumber);
    Optional<Account> findByPhoneGlobalEqualsIgnoreCase(String phoneGlobal);

    Account findByFkidcardEqualsIgnoreCase(String fkidcard);

}
