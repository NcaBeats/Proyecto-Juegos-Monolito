package com.app.proyectojuegosmonolito.purchase.repository;

import com.app.proyectojuegosmonolito.TestcontainersConfiguration;
import com.app.proyectojuegosmonolito.game.repository.GameRepository;
import com.app.proyectojuegosmonolito.purchase.model.PurchaseStatus;
import com.app.proyectojuegosmonolito.account.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static com.app.proyectojuegosmonolito.game.GameFixtures.*;
import static com.app.proyectojuegosmonolito.purchase.PurchaseFixtures.*;
import static com.app.proyectojuegosmonolito.account.user.UserFixtures.*;
import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PurchaseRepositoryIntegrationTest {

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameRepository gameRepository;

    @Test
    void save_shouldPersistWithGeneratedId() {
        var user = userRepository.save(user());
        var game = gameRepository.save(game());
        var item = item(game, 1);
        var purchase = purchase(user, PurchaseStatus.COMPLETED, List.of(item));

        var saved = purchaseRepository.save(purchase);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getItems()).hasSize(1);
    }

    @Test
    void findById_shouldReturnPurchaseWithItems() {
        var user = userRepository.save(user());
        var game = gameRepository.save(game());
        var item = item(game, 2);
        var purchase = purchaseRepository.save(purchase(user, PurchaseStatus.COMPLETED, List.of(item)));

        var found = purchaseRepository.findById(purchase.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getItems()).hasSize(1);
        assertThat(found.get().getItems().getFirst().getQuantity()).isEqualTo(2);
    }

    @Test
    void findAllWithPagination() {
        var user1 = userRepository.save(user("alpha", "alpha@test.com"));
        var user2 = userRepository.save(user("beta", "beta@test.com"));
        var game = gameRepository.save(game());
        purchaseRepository.save(purchase(user1, PurchaseStatus.COMPLETED, List.of(item(game, 1))));
        purchaseRepository.save(purchase(user2, PurchaseStatus.COMPLETED, List.of(item(game, 1))));

        var page = purchaseRepository.findAll(PageRequest.of(0, 1));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    void findByUserId_shouldReturnPage() {
        var user = userRepository.save(user());
        var game = gameRepository.save(game());
        purchaseRepository.save(purchase(user, PurchaseStatus.COMPLETED, List.of(item(game, 1))));

        var page = purchaseRepository.findByUser_Id(user.getId(), PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
    }
}
