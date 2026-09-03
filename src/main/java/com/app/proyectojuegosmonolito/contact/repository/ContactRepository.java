package com.app.proyectojuegosmonolito.contact.repository;

import com.app.proyectojuegosmonolito.contact.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactRepository extends JpaRepository<Contact, Long> {
}
