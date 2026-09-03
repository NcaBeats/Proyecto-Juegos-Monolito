package com.app.proyectojuegosmonolito.account.profile.model;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RunValidator implements ConstraintValidator<ValidRun, String> {

    @Override
    public boolean isValid(String run, ConstraintValidatorContext context) {
        if (run == null || run.isBlank()) {
            return false;
        }

        String cleaned = run.trim().toUpperCase();

        if (cleaned.length() < 7 || cleaned.length() > 9) {
            return false;
        }

        if (!cleaned.matches("[0-9]+[0-9K]")) {
            return false;
        }

        String body = cleaned.substring(0, cleaned.length() - 1);
        char expectedDigit = cleaned.charAt(cleaned.length() - 1);

        char computedDigit = computeCheckDigit(body);
        return expectedDigit == computedDigit;
    }

    private char computeCheckDigit(String body) {
        int sum = 0;
        int multiplier = 2;

        for (int i = body.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(body.charAt(i));
            sum += digit * multiplier;
            multiplier = multiplier == 7 ? 2 : multiplier + 1;
        }

        int remainder = 11 - (sum % 11);

        if (remainder == 11) return '0';
        if (remainder == 10) return 'K';
        return (char) (remainder + '0');
    }
}
