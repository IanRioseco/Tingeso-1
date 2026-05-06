package com.example.TravelAgency.Repository;

import com.example.TravelAgency.Entity.PackageEntity;
import com.example.TravelAgency.enums.PackageStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PackageRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private PackageRepository packageRepository;

    @Test
    void findPublicPackages_returnsOnlyAvailableFutureWithSlots() {
        LocalDate today = LocalDate.now();

        PackageEntity ok = em.persist(PackageEntity.builder()
                .name("Ok")
                .destination("D")
                .description("Desc")
                .startDate(today.plusDays(1))
                .endDate(today.plusDays(2))
                .price(new BigDecimal("10.00"))
                .totalSlots(10)
                .availableSlots(5)
                .status(PackageStatus.AVAILABLE)
                .servicesIncluded("S")
                .conditions("C")
                .build());

        em.persist(PackageEntity.builder()
                .name("SoldOut")
                .destination("D")
                .description("Desc")
                .startDate(today.plusDays(1))
                .endDate(today.plusDays(2))
                .price(new BigDecimal("10.00"))
                .totalSlots(10)
                .availableSlots(0)
                .status(PackageStatus.AVAILABLE)
                .servicesIncluded("S")
                .conditions("C")
                .build());

        em.flush();

        assertThat(packageRepository.findPublicPackages())
                .extracting(PackageEntity::getId)
                .contains(ok.getId());
    }
}

