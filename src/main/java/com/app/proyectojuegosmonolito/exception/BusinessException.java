package com.app.proyectojuegosmonolito.exception;

public class BusinessException extends RuntimeException {

    public static final String INVALID_DEPOSIT_AMOUNT = "INVALID_DEPOSIT_AMOUNT";
    public static final String INSUFFICIENT_BALANCE = "INSUFFICIENT_BALANCE";
    public static final String DUPLICATE_PURCHASE = "DUPLICATE_PURCHASE";
    public static final String PROFILE_REQUIRED = "PROFILE_REQUIRED";
    public static final String INVALID_PRICE_RANGE = "INVALID_PRICE_RANGE";

    private final String code;

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}