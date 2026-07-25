package com.app.proyectojuegosmonolito.library.service;

import com.app.proyectojuegosmonolito.game.service.GameService;
import com.app.proyectojuegosmonolito.library.Library;
import com.app.proyectojuegosmonolito.library.repository.LibraryRepository;
import com.app.proyectojuegosmonolito.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class LibraryService {

    private final LibraryRepository libraryRepository;
    private final UserService userService;
    private final GameService gameService;

    @Transactional
    public Library add(Long userId, Long gameId) {
        var user = userService.findById(userId);
        var game = gameService.findById(gameId);

        var library = Library.builder()
                .user(user)
                .game(game)
                .acquiredAt(Instant.now())
                .build();
        return libraryRepository.save(library);
    }

    public Page<Library> findByUserId(Long userId, Pageable pageable) {
        return libraryRepository.findByUser_Id(userId, pageable);
    }

    @Transactional
    public void remove(Long id) {
        if (!libraryRepository.existsById(id)) {
            throw new EntityNotFoundException("Library entry not found: " + id);
        }
        libraryRepository.deleteById(id);
    }
}
