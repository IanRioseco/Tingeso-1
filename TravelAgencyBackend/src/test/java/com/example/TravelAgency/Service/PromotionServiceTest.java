package com.example.TravelAgency.Service;

import com.example.TravelAgency.Entity.PromotionEntity;
import com.example.TravelAgency.Entity.BookingEntity;
import com.example.TravelAgency.Entity.UserEntity;
import com.example.TravelAgency.Exceptions.BusinessException;
import com.example.TravelAgency.Repository.BookingRepository;
import com.example.TravelAgency.Repository.PromotionRepository;
import com.example.TravelAgency.config.TravelProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromotionServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private PromotionRepository promotionRepository;

    private TravelProperties travelProperties;
    private PromotionService promotionService;

    @BeforeEach
    void setUp() {
        travelProperties = new TravelProperties();
        travelProperties.getDiscount().setGroupMinPassengers(4);
        travelProperties.getDiscount().setGroupPercent(new BigDecimal("10"));
        travelProperties.getDiscount().setFrequentMinPaidBookings(3);
        travelProperties.getDiscount().setFrequentPercent(new BigDecimal("5"));
        travelProperties.getDiscount().setMultiPackagePercent(new BigDecimal("5"));
        travelProperties.getDiscount().setMultiPackageLookbackDays(7);
        travelProperties.getDiscount().setMaxTotalPercent(new BigDecimal("20"));
        promotionService = new PromotionService(bookingRepository, promotionRepository, travelProperties);
    }

    @Test
    void calculate_appliesGroupAndFrequentAndPromotion_andRespectsMaxCap() {
        UserEntity user = UserEntity.builder()
                .id(1L)
                .fullName("U")
                .email("u@u.com")
                .documentId("DOC-1")
                .nationality("CL")
                .build();

        when(bookingRepository.countConfirmedByUser(user)).thenReturn(10L);
        when(bookingRepository.findBySessionId("S1")).thenReturn(List.of()); // no session discount
        when(bookingRepository.countActiveBookingsSince(eq(user), any(LocalDateTime.class))).thenReturn(0L);
        when(promotionRepository.findActivePromotions(any(LocalDate.class)))
                .thenReturn(List.of(
                        PromotionEntity.builder().name("Promo").discountPct(new BigDecimal("15")).active(true)
                                .validFrom(LocalDate.now().minusDays(1)).validTo(LocalDate.now().plusDays(1)).build()
                ));

        PromotionService.DiscountResult result = promotionService.calculate(
                new BigDecimal("100.00"), 4, user, "S1"
        );

        // group 10 + frequent 5 + promo 15 => 30, capped at 20
        assertThat(result.discountAmount()).isEqualByComparingTo(new BigDecimal("20.00"));
        assertThat(result.finalAmount()).isEqualByComparingTo(new BigDecimal("80.00"));
        assertThat(result.discountDetail()).contains("tope acumulado");
    }

    @Test
    void create_whenInvalid_throws() {
        PromotionEntity invalid = PromotionEntity.builder()
                .name(" ")
                .discountPct(new BigDecimal("0"))
                .active(true)
                .build();
        assertThrows(BusinessException.class, () -> promotionService.create(invalid));
        verifyNoInteractions(promotionRepository);
    }

    @Test
    void findAll_sortsByValidFromDesc() {
        PromotionEntity p1 = PromotionEntity.builder()
                .name("Old").discountPct(new BigDecimal("5")).active(true)
                .validFrom(LocalDate.of(2026, 1, 1)).validTo(LocalDate.of(2026, 1, 2)).build();
        PromotionEntity p2 = PromotionEntity.builder()
                .name("New").discountPct(new BigDecimal("5")).active(true)
                .validFrom(LocalDate.of(2026, 2, 1)).validTo(LocalDate.of(2026, 2, 2)).build();
        when(promotionRepository.findAll()).thenReturn(List.of(p1, p2));

        List<PromotionEntity> sorted = promotionService.findAll();

        assertThat(sorted).extracting(PromotionEntity::getName).containsExactly("New", "Old");
    }

    @Test
    void update_whenMissing_throws() {
        when(promotionRepository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(BusinessException.class,
                () -> promotionService.update(10L, PromotionEntity.builder().build()));
    }

    @Test
    void changeStatus_updatesAndSaves() {
        PromotionEntity existing = PromotionEntity.builder()
                .id(1L)
                .name("P")
                .discountPct(new BigDecimal("5"))
                .validFrom(LocalDate.now().minusDays(1))
                .validTo(LocalDate.now().plusDays(1))
                .active(true)
                .build();
        when(promotionRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(promotionRepository.save(any(PromotionEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        PromotionEntity updated = promotionService.changeStatus(1L, false);

        assertThat(updated.isActive()).isFalse();
        verify(promotionRepository).save(existing);
    }

    @Test
    void calculate_whenNoDiscounts_returnsNoDiscountDetail() {
        UserEntity user = UserEntity.builder()
                .id(1L).fullName("U").email("u@u.com").documentId("DOC-1").nationality("CL").build();
        when(bookingRepository.countConfirmedByUser(user)).thenReturn(0L);
        when(bookingRepository.findBySessionId("S1")).thenReturn(List.of());
        when(bookingRepository.countActiveBookingsSince(eq(user), any(LocalDateTime.class))).thenReturn(0L);
        when(promotionRepository.findActivePromotions(any(LocalDate.class))).thenReturn(List.of());

        PromotionService.DiscountResult result = promotionService.calculate(
                new BigDecimal("100.00"), 1, user, "S1"
        );

        assertThat(result.discountAmount()).isEqualByComparingTo(new BigDecimal("0.00"));
        assertThat(result.finalAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(result.discountDetail()).contains("Sin descuentos");
    }

    @Test
    void calculate_whenMultiBySession_addsMultiDiscountDetail() {
        UserEntity user = UserEntity.builder()
                .id(1L).fullName("U").email("u@u.com").documentId("DOC-1").nationality("CL").build();

        when(bookingRepository.countConfirmedByUser(user)).thenReturn(0L);
        when(bookingRepository.findBySessionId("S1")).thenReturn(List.of(new BookingEntity())); // triggers multiBySession
        when(bookingRepository.countActiveBookingsSince(eq(user), any(LocalDateTime.class))).thenReturn(0L);
        when(promotionRepository.findActivePromotions(any(LocalDate.class))).thenReturn(List.of());

        PromotionService.DiscountResult result = promotionService.calculate(
                new BigDecimal("100.00"), 1, user, "S1"
        );

        assertThat(result.discountDetail()).contains("Compra multiple");
    }

    @Test
    void create_whenInvalidDateRange_throws() {
        PromotionEntity invalid = PromotionEntity.builder()
                .name("P")
                .discountPct(new BigDecimal("1"))
                .validFrom(LocalDate.of(2026, 5, 10))
                .validTo(LocalDate.of(2026, 5, 1))
                .active(true)
                .build();

        assertThatThrownBy(() -> promotionService.create(invalid))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("fecha de termino");
    }
}
