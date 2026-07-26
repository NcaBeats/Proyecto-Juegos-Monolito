package com.app.proyectojuegosmonolito.game.mapper;

import com.app.proyectojuegosmonolito.game.model.Game;
import com.app.proyectojuegosmonolito.game.dto.GameRequest;
import com.app.proyectojuegosmonolito.game.dto.GameResponse;
import org.springframework.stereotype.Component;

@Component
public class GameMapper {

    public Game toEntity(GameRequest request) {
        return Game.builder()
                .name(request.name())
                .price(request.price())
                .description(request.description())
                .state(request.state())
                .launchDate(request.launchDate())
                .build();
    }

    public GameResponse toResponse(Game game) {
        return new GameResponse(
                game.getId(),
                game.getName(),
                game.getPrice(),
                game.getDescription(),
                game.getState(),
                game.getLaunchDate(),
                game.getCreatedAt()
        );
    }
}
