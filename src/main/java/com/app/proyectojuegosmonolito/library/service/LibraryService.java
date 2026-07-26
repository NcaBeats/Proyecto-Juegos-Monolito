package com.app.proyectojuegosmonolito.library.service;

import com.app.proyectojuegosmonolito.game.service.GameService;
import com.app.proyectojuegosmonolito.library.model.Library;
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
        if (isGameOwned(userId, gameId)) {
            throw new IllegalArgumentException("Game already in library: " + gameId);
        }

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

    public boolean isGameOwned(Long userId, Long gameId) {
        return libraryRepository.existsByUser_IdAndGame_Id(userId, gameId);
    }

    @Transactional
    public void remove(Long id, Long userId) {
        var lib = libraryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Library entry not found: " + id));
        if (!lib.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Library entry does not belong to user");
        }
        libraryRepository.delete(lib);
    }
}
