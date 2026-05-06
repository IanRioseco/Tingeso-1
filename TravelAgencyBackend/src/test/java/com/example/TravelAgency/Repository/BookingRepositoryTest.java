package com.example.TravelAgency.Repository;

import com.example.TravelAgency.Entity.BookingEntity;
import com.example.TravelAgency.Entity.PackageEntity;
import com.example.TravelAgency.Entity.UserEntity;
import com.example.TravelAgency.enums.BookingStatus;
import com.example.TravelAgency.enums.PackageStatus;
import com.example.TravelAgency.enums.UserRole;
import com.example.TravelAgency.enums.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BookingRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void countConfirmedByUser_countsOnlyConfirmed() {
        UserEntity user = em.persist(UserEntity.builder()
                .fullName("U")
                .email("u@u.com")
                .documentId("DOC-1")
                .nationality("CL")
                .role(UserRole.CLIENT)
                .status(UserStatus.ACTIVE)
                .active(true)
                .build());
        PackageEntity pkg = em.persist(PackageEntity.builder()
                .name("P")
                .destination("D")
                .description("Desc")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .price(new BigDecimal("10.00"))
                .totalSlots(10)
                .availableSlots(10)
                .status(PackageStatus.AVAILABLE)
                .servicesIncluded("S")
                .conditions("C")
                .build());

        BookingEntity confirmed = new BookingEntity();
        confirmed.setUser(user);
        confirmed.setPackageEntity(pkg);
        confirmed.setPassengersCount(1);
        confirmed.setBaseAmount(new BigDecimal("10.00"));
        confirmed.setFinalAmount(new BigDecimal("10.00"));
        confirmed.setBookingStatus(BookingStatus.CONFIRMED);
        em.persist(confirmed);

        BookingEntity pending = new BookingEntity();
        pending.setUser(user);
        pending.setPackageEntity(pkg);
        pending.setPassengersCount(1);
        pending.setBaseAmount(new BigDecimal("10.00"));
        pending.setFinalAmount(new BigDecimal("10.00"));
        pending.setBookingStatus(BookingStatus.PENDING);
        em.persist(pending);

        em.flush();

        long count = bookingRepository.countConfirmedByUser(user);
        assertThat(count).isEqualTo(1L);
    }

    @Test
    void findByBookingStatusAndExpiresAtBefore_findsExpiredPending() {
        UserEntity user = em.persist(UserEntity.builder()
                .fullName("U")
                .email("u2@u.com")
                .documentId("DOC-2")
                .nationality("CL")
                .role(UserRole.CLIENT)
                .status(UserStatus.ACTIVE)
                .active(true)
                .build());
        PackageEntity pkg = em.persist(PackageEntity.builder()
                .name("P2")
                .destination("D")
                .description("Desc")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .price(new BigDecimal("10.00"))
                .totalSlots(10)
                .availableSlots(10)
                .status(PackageStatus.AVAILABLE)
                .servicesIncluded("S")
                .conditions("C")
                .build());

        BookingEntity pendingExpired = new BookingEntity();
        pendingExpired.setUser(user);
        pendingExpired.setPackageEntity(pkg);
        pendingExpired.setPassengersCount(1);
        pendingExpired.setBaseAmount(new BigDecimal("10.00"));
        pendingExpired.setFinalAmount(new BigDecimal("10.00"));
        pendingExpired.setBookingStatus(BookingStatus.PENDING);
        pendingExpired.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        em.persist(pendingExpired);

        em.flush();

        List<BookingEntity> found = bookingRepository.findByBookingStatusAndExpiresAtBefore(
                BookingStatus.PENDING, LocalDateTime.now()
        );
        assertThat(found).extracting(BookingEntity::getId).contains(pendingExpired.getId());
    }
}

