package com.app.proyectojuegosmonolito.user.mapper;

import com.app.proyectojuegosmonolito.user.model.Wallet;
import com.app.proyectojuegosmonolito.user.dto.WalletResponse;
import org.springframework.stereotype.Component;

@Component
public class WalletMapper {

    public WalletResponse toResponse(Wallet wallet) {
        return new WalletResponse(
                wallet.getUserId(),
                wallet.getBalance(),
                wallet.getUpdatedAt()
        );
    }
}
