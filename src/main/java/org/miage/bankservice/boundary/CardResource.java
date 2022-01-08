package org.miage.bankservice.boundary;

import org.miage.bankservice.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CardResource extends JpaRepository<Card, String>{
    Optional<Card> findByNumberEqualsIgnoreCase(String number);
}
