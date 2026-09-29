package com.app.proyectojuegosmonolito.game.dto;

import com.app.proyectojuegosmonolito.game.model.GameState;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Variante JSON de {@link GameRequest}: el cliente subio la media por su cuenta
 * (directo a Cloudinary y R2) y solo manda las URLs resultantes.
 * El videoUrl viaja en {@link GameRequest}, igual que en el flujo multipart.
 */
public record GameWithMediaRequest(
    @NotBlank @Size(max = 50) String name,
    @NotNull @DecimalMin("0") BigDecimal originalPrice,
    @NotNull @Min(0) @Max(100) Integer discountPercent,
    @NotBlank String description,
    @NotNull GameState state,
    @NotNull LocalDate launchDate,
    @NotNull @NotEmpty List<String> categoryNames,
    String minimumSpecs,
    String recommendedSpecs,
    String videoUrl,
    Long sellerId,
    String imageUrl,
    String bannerUrl,
    List<String> galleryUrls
) {

    public GameRequest toGameRequest() {
        return new GameRequest(
                name,
                originalPrice,
                discountPercent,
                description,
                state,
                launchDate,
                categoryNames,
                minimumSpecs,
                recommendedSpecs,
                videoUrl,
                sellerId
        );
    }
}
