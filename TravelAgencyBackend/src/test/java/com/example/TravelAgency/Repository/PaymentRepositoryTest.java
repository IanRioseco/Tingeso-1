package com.example.TravelAgency.Repository;

import com.example.TravelAgency.Entity.BookingEntity;
import com.example.TravelAgency.Entity.PackageEntity;
import com.example.TravelAgency.Entity.PaymentEntity;
import com.example.TravelAgency.Entity.UserEntity;
import com.example.TravelAgency.enums.BookingStatus;
import com.example.TravelAgency.enums.PackageStatus;
import com.example.TravelAgency.enums.PaymentsStatus;
import com.example.TravelAgency.enums.UserRole;
import com.example.TravelAgency.enums.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PaymentRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    void existsByBooking_returnsTrueWhenPaymentExists() {
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
        BookingEntity booking = new BookingEntity();
        booking.setUser(user);
        booking.setPackageEntity(pkg);
        booking.setPassengersCount(1);
        booking.setBaseAmount(new BigDecimal("10.00"));
        booking.setFinalAmount(new BigDecimal("10.00"));
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        em.persist(booking);

        PaymentEntity payment = PaymentEntity.builder()
                .booking(booking)
                .amount(new BigDecimal("10.00"))
                .paymentMethod("CREDIT_CARD")
                .status(PaymentsStatus.APPROVED)
                .paidAt(LocalDateTime.now())
                .transactionRef("TXN")
                .cardLast4("1111")
                .cardExpiry("12/30")
                .build();
        em.persist(payment);
        em.flush();

        assertThat(paymentRepository.existsByBooking(booking)).isTrue();
    }
}

