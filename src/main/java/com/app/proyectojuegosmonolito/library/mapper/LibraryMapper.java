package com.app.proyectojuegosmonolito.library.mapper;

import com.app.proyectojuegosmonolito.library.Library;
import com.app.proyectojuegosmonolito.library.dto.LibraryResponse;
import org.springframework.stereotype.Component;

@Component
public class LibraryMapper {

    public LibraryResponse toResponse(Library library) {
        return new LibraryResponse(
                library.getId(),
                library.getUser().getId(),
                library.getGame().getId(),
                library.getAcquiredAt()
        );
    }
}
