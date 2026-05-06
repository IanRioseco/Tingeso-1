package com.example.TravelAgency.Service;

import com.example.TravelAgency.Entity.PromotionEntity;
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

import static org.assertj.core.api.Assertions.assertThat;
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
}

