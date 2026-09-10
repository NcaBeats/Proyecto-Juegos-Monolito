package com.app.proyectojuegosmonolito.game.service;

import com.app.proyectojuegosmonolito.account.user.model.Role;
import com.app.proyectojuegosmonolito.account.user.model.User;
import com.app.proyectojuegosmonolito.common.RepositoryUtils;
import com.app.proyectojuegosmonolito.game.storage.R2StorageService;
import com.app.proyectojuegosmonolito.game.dto.GameRequest;
import com.app.proyectojuegosmonolito.game.mapper.GameMapper;
import com.app.proyectojuegosmonolito.game.model.Category;
import com.app.proyectojuegosmonolito.game.model.Game;
import com.app.proyectojuegosmonolito.game.model.GameImage;
import com.app.proyectojuegosmonolito.game.model.GameState;
import com.app.proyectojuegosmonolito.game.repository.GameRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final GameMapper gameMapper;
    private final ImageService imageService;
    private final R2StorageService r2StorageService;

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
        return RepositoryUtils.findOrThrow(gameRepository, id, "Game");
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

    @Transactional(readOnly = true)
    public java.util.Optional<Game> findByName(String name) {
        log.info("Fetching game by exact name '{}'", name);
        return gameRepository.findByName(name);
    }

    @Transactional(readOnly = true)
    public Page<Game> findBySeller(Long sellerId, String name, Pageable pageable) {
        if (name != null && !name.isBlank()) {
            log.info("Fetching games of seller {} by name '{}' with pageable: {}", sellerId, name, pageable);
            return gameRepository.findBySeller_IdAndNameContainingIgnoreCase(sellerId, name, pageable);
        }
        log.info("Fetching games of seller {} with pageable: {}", sellerId, pageable);
        return gameRepository.findBySeller_Id(sellerId, pageable);
    }

    @Transactional(readOnly = true)
    public boolean isSellerOfGame(Long gameId, Long sellerId) {
        return gameRepository.existsByIdAndSeller_Id(gameId, sellerId);
    }

    @Transactional
    public Game assignSeller(Long gameId, User seller) {
        var game = findById(gameId);
        game.setSeller(seller);
        log.info("Assigned seller {} to game {} (id={})", seller.getId(), game.getName(), game.getId());
        return game;
    }

    @Transactional
    public Game createWithFiles(GameRequest request, List<Category> categories, User seller,
                                MultipartFile image, MultipartFile banner, MultipartFile video,
                                List<MultipartFile> gallery) throws IOException {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("La imagen principal (image) es obligatoria");
        }
        var game = gameMapper.toEntity(request);
        game.setCategories(categories);
        if (seller != null) {
            game.setSeller(seller);
        }
        var saved = create(game);

        String folderBase = "games/" + slugify(saved.getName());
        saved.setImageUrl(imageService.store(image, folderBase + "/card"));
        if (banner != null && !banner.isEmpty()) {
            saved.setBannerUrl(imageService.store(banner, folderBase + "/banner"));
        }
        if (video != null && !video.isEmpty()) {
            saved.setVideoUrl(storeVideo(video, saved.getName()));
        }
        if (gallery != null && !gallery.isEmpty()) {
            int position = 0;
            List<String> urls = new ArrayList<>(gallery.size());
            for (MultipartFile file : gallery) {
                if (file == null || file.isEmpty()) continue;
                urls.add(imageService.store(file, folderBase + "/gallery"));
            }
            replaceGallery(saved.getId(), urls);
            saved = findById(saved.getId());
        }
        log.info("Created game with files: {} (id={})", saved.getName(), saved.getId());
        return saved;
    }

    @Transactional
    public Game updateWithFiles(Long id, GameRequest request, List<Category> categories,
                                MultipartFile image, MultipartFile banner, MultipartFile video,
                                List<MultipartFile> gallery) throws IOException {
        var current = findById(id);
        var game = update(id, request, categories);

        String effectiveVideoUrl = request.videoUrl();
        if (video != null && !video.isEmpty()) {
            effectiveVideoUrl = storeVideo(video, request.name());
        }
        if (effectiveVideoUrl == null || effectiveVideoUrl.isBlank()) {
            effectiveVideoUrl = current.getVideoUrl();
        }
        game.setVideoUrl(effectiveVideoUrl);

        String folderBase = "games/" + slugify(game.getName());
        if (image != null && !image.isEmpty()) {
            game.setImageUrl(imageService.store(image, folderBase + "/card"));
        }
        if (banner != null && !banner.isEmpty()) {
            game.setBannerUrl(imageService.store(banner, folderBase + "/banner"));
        }
        if (gallery != null && !gallery.isEmpty()) {
            List<String> urls = new ArrayList<>(gallery.size());
            for (MultipartFile file : gallery) {
                if (file == null || file.isEmpty()) continue;
                urls.add(imageService.store(file, folderBase + "/gallery"));
            }
            replaceGallery(id, urls);
        }
        log.info("Updated game with files: {} (id={})", game.getName(), game.getId());
        return findById(id);
    }

    public String slugify(String name) {
        if (name == null) return "game";
        String slug = name.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return slug.isBlank() ? "game" : slug;
    }

    /**
     * Store the trailer video to Cloudflare R2 (primary).
     * The relative /uploads path is kept in the DB; the frontend resolves it to R2.
     */
    private String storeVideo(MultipartFile video, String name) throws IOException {
        String slug = slugify(name);
        String url = r2StorageService.storeVideo(video, slug);
        log.info("Stored video to R2: {}", url);
        return "/uploads/games/" + slug + "/trailer.mp4";
    }

    @Transactional(readOnly = true)
    public long countByState(GameState state) {
        return gameRepository.countByState(state);
    }

    @Transactional(readOnly = true)
    public java.math.BigDecimal sumDiscountedPriceByState(GameState state) {
        return gameRepository.sumDiscountedPriceByState(state);
    }

    @Transactional
    public Game update(Long id, GameRequest request, List<Category> categories) {
        log.info("Updating game {}: name={}, originalPrice={}, discountPercent={}", id, request.name(), request.originalPrice(), request.discountPercent());
        var game = findById(id);
        game.update(request.name(), request.originalPrice(), request.discountPercent(),
                request.description(), request.state(), request.launchDate(), categories,
                game.getImageUrl(), game.getBannerUrl(), request.videoUrl(),
                request.minimumSpecs(), request.recommendedSpecs());
        log.info("Updated game {}", game.getId());
        return game;
    }

    @Transactional
    public Game updateImage(Long id, String imageUrl) {
        log.info("Updating image for game {}: {}", id, imageUrl);
        var game = findById(id);
        game.setImageUrl(imageUrl);
        log.info("Updated image for game {}", game.getId());
        return game;
    }

    @Transactional
    public Game updateBannerUrl(Long id, String bannerUrl) {
        log.info("Updating banner for game {}: {}", id, bannerUrl);
        var game = findById(id);
        game.setBannerUrl(bannerUrl);
        log.info("Updated banner for game {}", game.getId());
        return game;
    }

    @Transactional
    public Game updateVideoUrl(Long id, String videoUrl) {
        log.info("Updating video for game {}: {}", id, videoUrl);
        var game = findById(id);
        game.setVideoUrl(videoUrl);
        log.info("Updated video for game {}", game.getId());
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

    @Transactional
    public Game assignBanner(Long id, String bannerUrl) {
        log.info("Assigning banner to game {}: {}", id, bannerUrl);
        var game = findById(id);
        game.setBannerUrl(bannerUrl);
        return game;
    }

    @Transactional
    public Game addGalleryImage(Long id, String url, Integer position) {
        log.info("Adding gallery image to game {} at position {}: {}", id, position, url);
        var game = findById(id);
        var image = GameImage.builder()
                .game(game)
                .url(url)
                .position(position)
                .createdAt(Instant.now())
                .build();
        game.getGallery().add(image);
        return game;
    }

    @Transactional
    public Game replaceGallery(Long id, List<String> urls) {
        log.info("Replacing gallery for game {} with {} images", id, urls.size());
        var game = findById(id);
        game.getGallery().clear();
        for (int i = 0; i < urls.size(); i++) {
            var image = GameImage.builder()
                    .game(game)
                    .url(urls.get(i))
                    .position(i)
                    .createdAt(Instant.now())
                    .build();
            game.getGallery().add(image);
        }
        return game;
    }

    public void assertCanModify(Game game, User user) {
        if (user.getRole() == Role.ADMIN) {
            return;
        }
        if (game.getSeller() == null || !game.getSeller().getId().equals(user.getId())) {
            throw new SecurityException("User " + user.getId() + " is not authorized to modify game " + game.getId());
        }
    }
}
