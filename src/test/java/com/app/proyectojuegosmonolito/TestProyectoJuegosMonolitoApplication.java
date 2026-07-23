package com.app.proyectojuegosmonolito;

import org.springframework.boot.SpringApplication;

public class TestProyectoJuegosMonolitoApplication {

    public static void main(String[] args) {
        SpringApplication.from(ProyectoJuegosMonolitoApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
