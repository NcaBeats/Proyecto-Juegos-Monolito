package com.app.proyectojuegosmonolito.game.service;

import com.app.proyectojuegosmonolito.game.model.Game;
import com.app.proyectojuegosmonolito.game.model.GameState;
import com.app.proyectojuegosmonolito.game.repository.GameRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;

    @Transactional
    public Game create(Game game) {
        game.setCreatedAt(Instant.now());
        var saved = gameRepository.save(game);
        log.info("Created game: {} (id={})", saved.getName(), saved.getId());
        return saved;
    }

    public Game findById(Long id) {
        log.info("Fetching game by id: {}", id);
        return gameRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Game not found: {}", id);
                    return new EntityNotFoundException("Game not found: " + id);
                });
    }

    public long count() {
        return gameRepository.count();
    }

    public Page<Game> findAll(Pageable pageable) {
        log.info("Fetching all games with pageable: {}", pageable);
        return gameRepository.findAll(pageable);
    }

    @Transactional
    public Game update(Long id, String name, BigDecimal price, String description, GameState state, LocalDate launchDate) {
        log.info("Updating game {}: name={}, price={}", id, name, price);
        var game = findById(id);
        game.update(name, price, description, state, launchDate);
        var saved = gameRepository.save(game);
        log.info("Updated game {}", saved.getId());
        return saved;
    }

    @Transactional
    public void delete(Long id) {
        if (!gameRepository.existsById(id)) {
            log.warn("Attempted to delete non-existent game: {}", id);
            throw new EntityNotFoundException("Game not found: " + id);
        }
        gameRepository.deleteById(id);
        log.info("Deleted game {}", id);
    }
}
