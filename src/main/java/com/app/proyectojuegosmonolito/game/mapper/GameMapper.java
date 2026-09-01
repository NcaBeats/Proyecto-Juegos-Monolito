package com.app.proyectojuegosmonolito.game.mapper;

import com.app.proyectojuegosmonolito.game.model.Game;
import com.app.proyectojuegosmonolito.game.dto.GameRequest;
import com.app.proyectojuegosmonolito.game.dto.GameResponse;
import com.app.proyectojuegosmonolito.game.dto.CategoryResponse;
import org.springframework.stereotype.Component;

@Component
public class GameMapper {

    public Game toEntity(GameRequest request) {
        return Game.builder()
                .name(request.name())
                .originalPrice(request.originalPrice())
                .discountPercent(request.discountPercent())
                .description(request.description())
                .state(request.state())
                .launchDate(request.launchDate())
                .build();
    }

    public GameResponse toResponse(Game game) {
        var categoryResponses = game.getCategories().stream()
                .map(c -> new CategoryResponse(c.getId(), c.getName(), c.getCreatedAt()))
                .toList();
        return new GameResponse(
                game.getId(),
                game.getName(),
                game.getOriginalPrice(),
                game.getPrice(),
                game.getDiscountPercent(),
                game.getDescription(),
                game.getState(),
                game.getLaunchDate(),
                categoryResponses,
                game.getImageUrl(),
                game.getBannerUrl(),
                game.getCreatedAt()
        );
    }
}
