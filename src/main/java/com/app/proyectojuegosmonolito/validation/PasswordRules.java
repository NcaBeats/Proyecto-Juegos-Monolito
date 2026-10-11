package com.app.proyectojuegosmonolito.validation;

/**
 * Regla de password del backend, declarada UNA sola vez.
 *
 * Antes estaba copiada en cinco DTOs ({@code @Size(min = 4, max = 10)} en
 * RegisterRequest, LoginRequest, UserRequestCreate, AdminUserUpdateRequest y
 * UserUpdatePassword). Los valores son constantes de compilacion justamente para
 * poder usarlas desde las anotaciones; cambiar la regla es cambiar estos dos
 * numeros, no cinco archivos.
 *
 * El valor debe coincidir con {@code PASSWORD_MIN}/{@code PASSWORD_MAX} del
 * frontend ({@code schemas/password.schema.ts}); el backend es la autoridad y
 * responde 400 si el frontend deja pasar algo.
 *
 * Ojo al subir el minimo: {@code DataInitializer} siembra los usuarios de prueba
 * con {@code pass123} (7 caracteres), asi que un min de 8 los dejaria sin poder
 * iniciar sesion.
 */
public final class PasswordRules {

    public static final int MIN = 4;
    public static final int MAX = 10;

    private PasswordRules() {
    }
}
