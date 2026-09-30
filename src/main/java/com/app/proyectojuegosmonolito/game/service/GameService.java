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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
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
    private final R2StorageService r2StorageService;

    @Transactional
    public Game create(Game game) {
        game.setCreatedAt(Instant.now());
        var saved = gameRepository.save(game);
        log.info("Created game: {} (id={})", saved.getName(), saved.getId());
        return saved;
    }

    /**
     * Persiste en bloque entidades posiblemente detached (merge). Lo usa el runner
     * de migracion para reescribir URLs de media de todos los juegos de una pasada.
     */
    @Transactional
    public List<Game> saveAll(List<Game> games) {
        if (games.isEmpty()) {
            return games;
        }
        var saved = gameRepository.saveAll(games);
        log.info("Saved {} games", saved.size());
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
    public java.util.List<Game> findAllWithGallery() {
        return gameRepository.findAllWithGallery();
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

        String slug = slugify(saved.getName());
        saved.setImageUrl(r2StorageService.storeImage(image, slug, "card"));
        if (banner != null && !banner.isEmpty()) {
            saved.setBannerUrl(r2StorageService.storeImage(banner, slug, "banner"));
        }
        if (video != null && !video.isEmpty()) {
            saved.setVideoUrl(storeVideo(video, saved.getName()));
        }
        if (gallery != null && !gallery.isEmpty()) {
            List<String> urls = new ArrayList<>(gallery.size());
            for (MultipartFile file : gallery) {
                if (file == null || file.isEmpty()) continue;
                urls.add(r2StorageService.storeImage(file, slug, "gallery"));
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
        // Se lee antes de update(): current y game son la misma instancia gestionada,
        // asi que leerlo despues devolveria la URL nueva y no habria trailer previo.
        var previousVideoUrl = current.getVideoUrl();
        var game = update(id, request, categories);

        String effectiveVideoUrl = request.videoUrl();
        if (video != null && !video.isEmpty()) {
            effectiveVideoUrl = storeVideo(video, request.name());
            // update() solo puede descartar trailers cuando la URL viene en el request.
            // Aqui la URL cambia despues, asi que el descarte se pide en este camino.
            discardReplacedTrailer(previousVideoUrl, effectiveVideoUrl);
        }
        if (effectiveVideoUrl == null || effectiveVideoUrl.isBlank()) {
            effectiveVideoUrl = current.getVideoUrl();
        }
        game.setVideoUrl(effectiveVideoUrl);

        String slug = slugify(game.getName());
        if (image != null && !image.isEmpty()) {
            game.setImageUrl(r2StorageService.storeImage(image, slug, "card"));
        }
        if (banner != null && !banner.isEmpty()) {
            game.setBannerUrl(r2StorageService.storeImage(banner, slug, "banner"));
        }
        if (gallery != null && !gallery.isEmpty()) {
            List<String> urls = new ArrayList<>(gallery.size());
            for (MultipartFile file : gallery) {
                if (file == null || file.isEmpty()) continue;
                urls.add(r2StorageService.storeImage(file, slug, "gallery"));
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
     * Sube el trailer a Cloudflare R2 y devuelve la ruta relativa que se persiste en
     * la entidad; el frontend la resuelve contra el host publico del bucket. La clave
     * la elige el servicio de storage e incluye la huella del contenido, asi que
     * re-subir el trailer produce una URL distinta sin tocar nada mas.
     */
    private String storeVideo(MultipartFile video, String name) throws IOException {
        var key = r2StorageService.storeVideo(video, slugify(name));
        log.info("Stored video to R2 key={}", key);
        return r2StorageService.publicPath(key);
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
        var previousVideoUrl = game.getVideoUrl();
        var effectiveVideoUrl = request.videoUrl();
        if (effectiveVideoUrl == null || effectiveVideoUrl.isBlank()) {
            effectiveVideoUrl = game.getVideoUrl();
        }
        game.update(request.name(), request.originalPrice(), request.discountPercent(),
                request.description(), request.state(), request.launchDate(), categories,
                game.getImageUrl(), game.getBannerUrl(), effectiveVideoUrl,
                request.minimumSpecs(), request.recommendedSpecs());
        discardReplacedTrailer(previousVideoUrl, effectiveVideoUrl);
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

    /**
     * Registra las URLs de media que el navegador ya subio directo a Cloudinary / R2.
     * Los campos ausentes o vacios se dejan intactos, de modo que una edicion parcial
     * no borra la portada ni la galeria que ya tenia el juego.
     */
    @Transactional
    public Game applyMediaUrls(Long id, String imageUrl, String bannerUrl, List<String> galleryUrls) {
        var game = findById(id);
        if (imageUrl != null && !imageUrl.isBlank()) {
            game.setImageUrl(imageUrl);
        }
        if (bannerUrl != null && !bannerUrl.isBlank()) {
            game.setBannerUrl(bannerUrl);
        }
        if (galleryUrls != null && !galleryUrls.isEmpty()) {
            game.getGallery().clear();
            for (int i = 0; i < galleryUrls.size(); i++) {
                game.getGallery().add(GameImage.builder()
                        .game(game)
                        .url(galleryUrls.get(i))
                        .position(i)
                        .createdAt(Instant.now())
                        .build());
            }
        }
        log.info("Applied media urls to game {}: image={} banner={} gallery={}",
                id, imageUrl, bannerUrl, galleryUrls == null ? 0 : galleryUrls.size());
        return game;
    }

    @Transactional
    public void delete(Long id) {
        if (!gameRepository.existsById(id)) {
            log.warn("Attempted to delete non-existent game: {}", id);
            throw new EntityNotFoundException("Game not found: " + id);
        }
        // Las URLs se leen antes del borrado: despues la entidad ya no esta y
        // no habria forma de saber que archivos limpiar.
        var game = gameRepository.findById(id).orElseThrow();
        var imageUrl = game.getImageUrl();
        var bannerUrl = game.getBannerUrl();
        var videoUrl = game.getVideoUrl();
        var galleryUrls = game.getGallery().stream().map(GameImage::getUrl).toList();

        gameRepository.deleteById(id);

        // R2 no participa de la transaccion: si el borrado externo se hiciera
        // aqui y luego la transaccion fallara, los archivos ya habrian
        // desaparecido sin que el juego se borrara. Por eso se difiere al commit
        // y, si falla, se loguea sin revivir el borrado.
        afterCommit(() -> {
            deleteQuietly(imageUrl, "image");
            deleteQuietly(bannerUrl, "banner");
            galleryUrls.forEach(url -> deleteQuietly(url, "gallery"));
            deleteTrailerQuietly(videoUrl);
        });

        log.info("Deleted game {}", id);
    }

    /**
     * Borra el trailer al que apunta una URL, sea absoluta o relativa y tenga o no
     * query string. La clave se deriva de la URL persistida y no del slug, porque
     * con huella del contenido el trailer de un juego ya no vive en una clave fija.
     */
    private void deleteTrailerQuietly(String videoUrl) {
        var key = r2StorageService.keyOf(videoUrl);
        if (key == null) {
            return;
        }
        try {
            r2StorageService.deleteTrailer(key);
        } catch (RuntimeException e) {
            log.warn("Could not delete trailer {}: {}", key, e.getMessage());
        }
    }

    /**
     * Limpia el trailer que quedo reemplazado. Las claves llevan la huella del
     * contenido, asi que cada re-subida deja el objeto anterior huerfano: sin esto
     * el bucket acumularia copias de 90 MB de cada trailer que se haya editado.
     * Se difiere al commit porque R2 no participa de la transaccion: borrar el
     * archivo viejo antes de confirmar dejaria la fila apuntando al peor.
     */
    private void discardReplacedTrailer(String previousVideoUrl, String newVideoUrl) {
        var previousKey = r2StorageService.keyOf(previousVideoUrl);
        var newKey = r2StorageService.keyOf(newVideoUrl);
        if (previousKey == null || previousKey.equals(newKey)) {
            return;
        }
        log.info("Discarding replaced trailer {} -> {}", previousKey, newKey);
        afterCommit(() -> deleteTrailerQuietly(previousVideoUrl));
    }

    private void afterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }

    private void deleteQuietly(String url, String kind) {
        if (url == null || url.isBlank()) {
            return;
        }
        try {
            r2StorageService.deleteImage(url);
        } catch (RuntimeException e) {
            log.warn("Could not delete {} image {}: {}", kind, url, e.getMessage());
        }
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
