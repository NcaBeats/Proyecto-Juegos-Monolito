package com.app.proyectojuegosmonolito.account.wallet.mapper;

import com.app.proyectojuegosmonolito.account.wallet.model.Wallet;
import com.app.proyectojuegosmonolito.account.wallet.dto.WalletResponse;
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
