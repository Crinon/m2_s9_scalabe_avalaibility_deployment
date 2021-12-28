package org.miage.bankservice.entity;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import org.springframework.stereotype.Service;

@Service
public class CardValidator {

    private Validator validator;

    CardValidator(Validator validator) {
        this.validator = validator;
    }

    public void validate(CardInput account) {
        Set<ConstraintViolation<CardInput>> violations = validator.validate(account);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}