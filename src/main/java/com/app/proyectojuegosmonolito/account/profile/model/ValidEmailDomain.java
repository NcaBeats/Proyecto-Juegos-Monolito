package com.app.proyectojuegosmonolito.account.profile.model;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EmailDomainValidator.class)
public @interface ValidEmailDomain {
    String message() default "Solo se permiten correos @duoc.cl, @profesor.duoc.cl y @gmail.com";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
