package com.app.proyectojuegosmonolito.account.profile.model;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

public class EmailDomainValidator implements ConstraintValidator<ValidEmailDomain, String> {

    private static final Set<String> ALLOWED_DOMAINS = Set.of(
            "duoc.cl",
            "profesor.duoc.cl",
            "gmail.com"
    );

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.isBlank()) {
            return false;
        }

        int atIndex = email.lastIndexOf('@');
        if (atIndex < 0 || atIndex == email.length() - 1) {
            return false;
        }

        String domain = email.substring(atIndex + 1).toLowerCase();
        return ALLOWED_DOMAINS.contains(domain);
    }
}
