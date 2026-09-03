package com.app.proyectojuegosmonolito.contact.service;

import com.app.proyectojuegosmonolito.contact.model.Contact;
import com.app.proyectojuegosmonolito.contact.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;

    @Transactional
    public Contact create(Contact contact) {
        contact.setCreatedAt(Instant.now());
        var saved = contactRepository.save(contact);
        log.info("Created contact from: {} (id={})", saved.getEmail(), saved.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    public Page<Contact> findAll(Pageable pageable) {
        log.info("Fetching all contacts with pageable: {}", pageable);
        return contactRepository.findAll(pageable);
    }
}
