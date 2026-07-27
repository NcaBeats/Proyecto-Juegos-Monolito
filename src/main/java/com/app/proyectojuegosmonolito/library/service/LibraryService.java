package com.app.proyectojuegosmonolito.library.service;

import com.app.proyectojuegosmonolito.game.service.GameService;
import com.app.proyectojuegosmonolito.library.model.Library;
import com.app.proyectojuegosmonolito.library.repository.LibraryRepository;
import com.app.proyectojuegosmonolito.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class LibraryService {

    private final LibraryRepository libraryRepository;
    private final UserService userService;
    private final GameService gameService;

    @Transactional
    public Library add(Long userId, Long gameId) {
        log.info("Adding game {} to user {}'s library", gameId, userId);
        if (isGameOwned(userId, gameId)) {
            log.warn("Game {} already in library for user {}", gameId, userId);
            throw new IllegalArgumentException("Game already in library: " + gameId);
        }

        var user = userService.findById(userId);
        var game = gameService.findById(gameId);

        var library = Library.builder()
                .user(user)
                .game(game)
                .acquiredAt(Instant.now())
                .build();
        var saved = libraryRepository.save(library);
        log.info("Added game {} to user {}'s library (entry id={})", gameId, userId, saved.getId());
        return saved;
    }

    public Page<Library> findByUserId(Long userId, Pageable pageable) {
        log.info("Fetching library for user {} with pageable: {}", userId, pageable);
        return libraryRepository.findByUser_Id(userId, pageable);
    }

    public boolean isGameOwned(Long userId, Long gameId) {
        log.info("Checking if user {} owns game {}", userId, gameId);
        return libraryRepository.existsByUser_IdAndGame_Id(userId, gameId);
    }

    @Transactional
    public void remove(Long id, Long userId) {
        log.info("Removing library entry {} for user {}", id, userId);
        var lib = libraryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Library entry not found: {}", id);
                    return new EntityNotFoundException("Library entry not found: " + id);
                });
        if (!lib.getUser().getId().equals(userId)) {
            log.warn("User {} attempted to remove library entry {} belonging to user {}", userId, id, lib.getUser().getId());
            throw new IllegalArgumentException("Library entry does not belong to user");
        }
        libraryRepository.delete(lib);
        log.info("Removed library entry {} for user {}", id, userId);
    }
}
