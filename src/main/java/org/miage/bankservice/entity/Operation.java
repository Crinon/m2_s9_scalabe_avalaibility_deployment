package org.miage.bankservice.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class Operation {
    public enum Category {
        SUBSCRIPTION, CARS, TAXES, EDUCATION, DAILYLIFE, HEALTH;
    }
    @Id
    private String id;
    private LocalDateTime localDateTime;
    private String wording;
    private Double amount;
    private Double conversionRate;
    private String recipientName;
    private Category category;
    private Account.Country countryShop;

    @ManyToOne()
    @JoinColumn(name="accountFrom_id", nullable=false)
    private Account accountFrom;
    @ManyToOne()
    @JoinColumn(name="accountTo_id", nullable=false)
    private Account accountTo;
}
