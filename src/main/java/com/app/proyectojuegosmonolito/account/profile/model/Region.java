package com.app.proyectojuegosmonolito.account.profile.model;

import lombok.Getter;

@Getter
public enum Region {
    ARICA_Y_PARINACOTA("Arica y Parinacota"),
    TARAPACA("Tarapacá"),
    ANTOFAGASTA("Antofagasta"),
    ATACAMA("Atacama"),
    COQUIMBO("Coquimbo"),
    VALPARAISO("Valparaíso"),
    METROPOLITANA_DE_SANTIAGO("Metropolitana de Santiago"),
    O_HIGGINS("O'Higgins"),
    MAULE("Maule"),
    NUBLE("Ñuble"),
    BIOBIO("Biobío"),
    ARAUCANIA("Araucanía"),
    LOS_RIOS("Los Ríos"),
    LOS_LAGOS("Los Lagos"),
    AYSEN("Aysén"),
    MAGALLANES("Magallanes");

    private final String displayName;

    Region(String displayName) {
        this.displayName = displayName;
    }

}
