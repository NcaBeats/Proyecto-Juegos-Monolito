package com.app.proyectojuegosmonolito.user.repository;

import com.app.proyectojuegosmonolito.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
