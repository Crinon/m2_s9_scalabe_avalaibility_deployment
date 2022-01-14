package org.miage.bankservice.entity;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import org.springframework.stereotype.Service;

@Service
public class TransfertValidator {

    private Validator validator;

    TransfertValidator(Validator validator) {
        this.validator = validator;
    }

    public void validate(TransfertInput transfertInput) {
        Set<ConstraintViolation<TransfertInput>> violations = validator.validate(transfertInput);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}
