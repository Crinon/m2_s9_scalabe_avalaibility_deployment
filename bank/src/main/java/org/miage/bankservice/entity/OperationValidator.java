package org.miage.bankservice.entity;

import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.util.Set;

@Service
public class OperationValidator {

    private Validator validator;

    OperationValidator(Validator validator) {
        this.validator = validator;
    }

    public void validate(OperationInput operationInput) {
        Set<ConstraintViolation<OperationInput>> violations = validator.validate(operationInput);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}
