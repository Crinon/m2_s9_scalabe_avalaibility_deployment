package org.miage.bankservice.miscellaneous;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class CustomErrorHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public Map<String, Map<String, String>> handleValidationExceptions(final ConstraintViolationException ex) {
        final Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(error -> {
            final StringJoiner fieldName = new StringJoiner(".")
                    .add(ex.getMessage())
                    .add("validation")
                    .add(error.getPropertyPath().toString())
                    .add(error.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName());
            final String errorMessage = error.getMessage();
            errors.put(fieldName.toString().toLowerCase(), errorMessage);
        });
        return new HashMap<>() {{
            put("Requête(s) malformée(s)", errors);
        }};
    }

    public static String currentUsername() {
        final User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getUsername();
    }

}
