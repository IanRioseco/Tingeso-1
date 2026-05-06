package com.example.TravelAgency.Repository;

import com.example.TravelAgency.Entity.PromotionEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PromotionRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private PromotionRepository promotionRepository;

    @Test
    void findActivePromotions_filtersByActiveAndDateRange() {
        LocalDate today = LocalDate.now();
        PromotionEntity active = em.persist(PromotionEntity.builder()
                .name("Promo")
                .discountPct(new BigDecimal("10"))
                .validFrom(today.minusDays(1))
                .validTo(today.plusDays(1))
                .active(true)
                .build());
        em.persist(PromotionEntity.builder()
                .name("Inactive")
                .discountPct(new BigDecimal("10"))
                .validFrom(today.minusDays(1))
                .validTo(today.plusDays(1))
                .active(false)
                .build());
        em.flush();

        assertThat(promotionRepository.findActivePromotions(today))
                .extracting(PromotionEntity::getId)
                .contains(active.getId());
    }
}

