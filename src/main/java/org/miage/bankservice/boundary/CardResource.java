package org.miage.bankservice.boundary;

import org.miage.bankservice.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardResource extends JpaRepository<Card, String>{
}
