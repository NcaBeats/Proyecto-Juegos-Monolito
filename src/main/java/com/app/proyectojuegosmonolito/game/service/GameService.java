package com.app.proyectojuegosmonolito.game.service;

import com.app.proyectojuegosmonolito.game.model.Category;
import com.app.proyectojuegosmonolito.game.model.Game;
import com.app.proyectojuegosmonolito.game.model.GameState;
import com.app.proyectojuegosmonolito.game.repository.GameRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

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

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public Page<Game> findAll(Pageable pageable) {
        log.info("Fetching all games with pageable: {}", pageable);
        return gameRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Game> findDiscounted(Pageable pageable) {
        log.info("Fetching discounted games with pageable: {}", pageable);
        return gameRepository.findByDiscountPercentGreaterThan(0, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Game> findBannerGames(Pageable pageable) {
        log.info("Fetching banner games with pageable: {}", pageable);
        return gameRepository.findByBannerUrlIsNotNull(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Game> findByCategory(String categoryName, Pageable pageable) {
        log.info("Fetching games by category '{}' with pageable: {}", categoryName, pageable);
        return gameRepository.findByCategories_Name(categoryName, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Game> findByName(String name, Pageable pageable) {
        log.info("Fetching games by name '{}' with pageable: {}", name, pageable);
        return gameRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    @Transactional
    public Game update(Long id, String name, BigDecimal originalPrice, Integer discountPercent, String description, GameState state, LocalDate launchDate, List<Category> categories, String minimumSpecs, String recommendedSpecs) {
        log.info("Updating game {}: name={}, originalPrice={}, discountPercent={}", id, name, originalPrice, discountPercent);
        var game = findById(id);
        game.update(name, originalPrice, discountPercent, description, state, launchDate, categories, game.getImageUrl(), game.getBannerUrl(), minimumSpecs, recommendedSpecs);
        log.info("Updated game {}", game.getId());
        return game;
    }

    @Transactional
    public Game updateImage(Long id, String imageUrl) {
        log.info("Updating image for game {}: {}", id, imageUrl);
        var game = findById(id);
        game.update(game.getName(), game.getOriginalPrice(), game.getDiscountPercent(),
                game.getDescription(), game.getState(), game.getLaunchDate(),
                game.getCategories(), imageUrl, game.getBannerUrl(),
                game.getMinimumSpecs(), game.getRecommendedSpecs());
        log.info("Updated image for game {}", game.getId());
        return game;
    }

    @Transactional
    public Game updateBannerUrl(Long id, String bannerUrl) {
        log.info("Updating banner for game {}: {}", id, bannerUrl);
        var game = findById(id);
        game.update(game.getName(), game.getOriginalPrice(), game.getDiscountPercent(),
                game.getDescription(), game.getState(), game.getLaunchDate(),
                game.getCategories(), game.getImageUrl(), bannerUrl,
                game.getMinimumSpecs(), game.getRecommendedSpecs());
        log.info("Updated banner for game {}", game.getId());
        return game;
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
