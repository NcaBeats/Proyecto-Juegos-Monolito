package com.app.proyectojuegosmonolito;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

import static org.assertj.core.api.Assertions.assertThat;

class ModulithTests {

    @Test
    void verifyModuleStructure() {
        var modules = ApplicationModules.of(ProyectoJuegosMonolitoApplication.class);
        modules.verify();
        assertThat(modules).isNotEmpty();
    }
}
