package com.app.proyectojuegosmonolito.contact.mapper;

import com.app.proyectojuegosmonolito.contact.model.Contact;
import com.app.proyectojuegosmonolito.contact.dto.ContactRequest;
import com.app.proyectojuegosmonolito.contact.dto.ContactResponse;
import org.springframework.stereotype.Component;

@Component
public class ContactMapper {

    public Contact toEntity(ContactRequest request) {
        return Contact.builder()
                .name(request.name())
                .email(request.email())
                .comment(request.comment())
                .build();
    }

    public ContactResponse toResponse(Contact contact) {
        return new ContactResponse(
                contact.getId(),
                contact.getName(),
                contact.getEmail(),
                contact.getComment(),
                contact.getCreatedAt()
        );
    }
}
