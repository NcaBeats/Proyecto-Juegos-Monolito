package com.app.proyectojuegosmonolito.account.profile.model;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RunValidator.class)
public @interface ValidRun {
    String message() default "RUN inválido. Formato esperado: 19011022K (7-9 caracteres, sin puntos ni guiones)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
