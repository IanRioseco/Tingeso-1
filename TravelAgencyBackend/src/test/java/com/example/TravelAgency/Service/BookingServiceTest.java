package com.example.TravelAgency.Service;

import com.example.TravelAgency.Entity.BookingEntity;
import com.example.TravelAgency.Entity.PackageEntity;
import com.example.TravelAgency.Entity.UserEntity;
import com.example.TravelAgency.Exceptions.BusinessException;
import com.example.TravelAgency.Repository.BookingRepository;
import com.example.TravelAgency.config.TravelProperties;
import com.example.TravelAgency.enums.BookingStatus;
import com.example.TravelAgency.enums.PackageStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private PackageService packageService;
    @Mock
    private PromotionService promotionService;

    private TravelProperties travelProperties;
    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        travelProperties = new TravelProperties();
        travelProperties.getBooking().setPendingExpiresDays(10);
        bookingService = new BookingService(bookingRepository, packageService, promotionService, travelProperties);
    }

    @Test
    void create_whenPassengersInvalid_throws() {
        UserEntity user = UserEntity.builder().id(1L).build();
        assertThrows(BusinessException.class, () -> bookingService.create(user, 10L, 0, "S"));
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void create_happyPath_savesPendingBooking_andDecreasesSlots() {
        UserEntity user = UserEntity.builder().id(1L).fullName("U").email("u@u.com").documentId("D").nationality("CL").build();
        PackageEntity pkg = PackageEntity.builder()
                .id(10L)
                .name("PKG")
                .destination("D")
                .startDate(LocalDate.now().plusDays(10))
                .endDate(LocalDate.now().plusDays(12))
                .price(new BigDecimal("100.00"))
                .availableSlots(5)
                .totalSlots(5)
                .status(PackageStatus.AVAILABLE)
                .build();

        when(bookingRepository.existsByUserIdAndPackageEntityIdAndBookingStatusIn(eq(1L), eq(10L), anyList()))
                .thenReturn(false);
        when(packageService.findById(10L)).thenReturn(pkg);
        when(promotionService.calculate(any(BigDecimal.class), eq(2), eq(user), eq("S1")))
                .thenReturn(new PromotionService.DiscountResult(new BigDecimal("10.00"), new BigDecimal("190.00"), "D"));
        when(bookingRepository.save(any(BookingEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingEntity saved = bookingService.create(user, 10L, 2, "S1");

        verify(packageService).decreaseSlots(pkg, 2);
        ArgumentCaptor<BookingEntity> captor = ArgumentCaptor.forClass(BookingEntity.class);
        verify(bookingRepository).save(captor.capture());

        BookingEntity booking = captor.getValue();
        assertThat(booking.getBookingStatus()).isEqualTo(BookingStatus.PENDING);
        assertThat(booking.getPassengersCount()).isEqualTo(2);
        assertThat(booking.getBaseAmount()).isEqualByComparingTo(new BigDecimal("200.00"));
        assertThat(booking.getDiscountAmount()).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(booking.getFinalAmount()).isEqualByComparingTo(new BigDecimal("190.00"));
        assertThat(booking.getSessionId()).isEqualTo("S1");
        assertThat(saved).isSameAs(booking);
    }

    @Test
    void create_whenAlreadyHasActiveBooking_throws() {
        UserEntity user = UserEntity.builder().id(1L).build();
        when(bookingRepository.existsByUserIdAndPackageEntityIdAndBookingStatusIn(eq(1L), eq(10L), anyList()))
                .thenReturn(true);

        assertThatThrownBy(() -> bookingService.create(user, 10L, 1, "S"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("reserva activa");
        verifyNoInteractions(packageService, promotionService);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void create_whenPackageCanceled_throws() {
        UserEntity user = UserEntity.builder().id(1L).build();
        when(bookingRepository.existsByUserIdAndPackageEntityIdAndBookingStatusIn(eq(1L), eq(10L), anyList()))
                .thenReturn(false);
        when(packageService.findById(10L)).thenReturn(PackageEntity.builder()
                .id(10L)
                .status(PackageStatus.CANCELED)
                .availableSlots(10)
                .price(new BigDecimal("10"))
                .build());

        assertThrows(BusinessException.class, () -> bookingService.create(user, 10L, 1, "S"));
        verifyNoInteractions(promotionService);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void create_whenPackageExpired_throws() {
        UserEntity user = UserEntity.builder().id(1L).build();
        when(bookingRepository.existsByUserIdAndPackageEntityIdAndBookingStatusIn(eq(1L), eq(10L), anyList()))
                .thenReturn(false);
        when(packageService.findById(10L)).thenReturn(PackageEntity.builder()
                .id(10L)
                .status(PackageStatus.EXPIRED)
                .availableSlots(10)
                .price(new BigDecimal("10"))
                .build());

        assertThrows(BusinessException.class, () -> bookingService.create(user, 10L, 1, "S"));
        verifyNoInteractions(promotionService);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void create_whenPackageSoldOut_throws() {
        UserEntity user = UserEntity.builder().id(1L).build();
        when(bookingRepository.existsByUserIdAndPackageEntityIdAndBookingStatusIn(eq(1L), eq(10L), anyList()))
                .thenReturn(false);
        when(packageService.findById(10L)).thenReturn(PackageEntity.builder()
                .id(10L)
                .status(PackageStatus.SOLD_OUT)
                .availableSlots(0)
                .price(new BigDecimal("10"))
                .build());

        assertThrows(BusinessException.class, () -> bookingService.create(user, 10L, 1, "S"));
        verifyNoInteractions(promotionService);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void create_whenNotEnoughSlots_throws() {
        UserEntity user = UserEntity.builder().id(1L).build();
        when(bookingRepository.existsByUserIdAndPackageEntityIdAndBookingStatusIn(eq(1L), eq(10L), anyList()))
                .thenReturn(false);
        when(packageService.findById(10L)).thenReturn(PackageEntity.builder()
                .id(10L)
                .status(PackageStatus.AVAILABLE)
                .availableSlots(1)
                .price(new BigDecimal("10"))
                .build());

        assertThatThrownBy(() -> bookingService.create(user, 10L, 2, "S"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("cupos disponibles");
        verifyNoInteractions(promotionService);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void cancel_whenConfirmed_throws() {
        BookingEntity booking = new BookingEntity();
        booking.setId(1L);
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        when(bookingRepository.findById(1L)).thenReturn(java.util.Optional.of(booking));

        assertThrows(BusinessException.class, () -> bookingService.cancel(1L));
    }

    @Test
    void cancel_whenPending_cancelsAndReleasesSlots() {
        PackageEntity pkg = PackageEntity.builder().id(9L).build();
        BookingEntity booking = new BookingEntity();
        booking.setId(1L);
        booking.setBookingStatus(BookingStatus.PENDING);
        booking.setPackageEntity(pkg);
        booking.setPassengersCount(2);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(BookingEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        bookingService.cancel(1L);

        assertThat(booking.getBookingStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(packageService).releaseSlots(pkg, 2);
        verify(bookingRepository).save(booking);
    }

    @Test
    void cancel_whenAlreadyCancelled_throws() {
        BookingEntity booking = new BookingEntity();
        booking.setId(1L);
        booking.setBookingStatus(BookingStatus.CANCELLED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(BusinessException.class, () -> bookingService.cancel(1L));
        verifyNoInteractions(packageService);
    }

    @Test
    void cancel_whenExpired_throws() {
        BookingEntity booking = new BookingEntity();
        booking.setId(1L);
        booking.setBookingStatus(BookingStatus.EXPIRED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(BusinessException.class, () -> bookingService.cancel(1L));
        verifyNoInteractions(packageService);
    }

    @Test
    void findById_whenMissing_throws() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(com.example.TravelAgency.Exceptions.ResourceNotFoundException.class,
                () -> bookingService.findById(99L));
    }

    @Test
    void confirm_whenFinalAmountInvalid_throws() {
        BookingEntity booking = new BookingEntity();
        booking.setId(1L);
        booking.setBookingStatus(BookingStatus.PENDING);
        booking.setFinalAmount(new BigDecimal("0"));
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(BusinessException.class, () -> bookingService.confirm(1L));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void confirm_happyPath_setsConfirmed_andSaves() {
        BookingEntity booking = new BookingEntity();
        booking.setId(1L);
        booking.setBookingStatus(BookingStatus.PENDING);
        booking.setFinalAmount(new BigDecimal("10.00"));
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(BookingEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        bookingService.confirm(1L);

        assertThat(booking.getBookingStatus()).isEqualTo(BookingStatus.CONFIRMED);
        verify(bookingRepository).save(booking);
    }

    @Test
    void findByUser_delegatesToRepositoryByUserId() {
        UserEntity user = UserEntity.builder().id(5L).build();
        when(bookingRepository.findByUserId(5L)).thenReturn(List.of());

        bookingService.findByUser(user);

        verify(bookingRepository).findByUserId(5L);
    }

    @Test
    void findAll_delegates() {
        when(bookingRepository.findAll()).thenReturn(List.of());
        bookingService.findAll();
        verify(bookingRepository).findAll();
    }

    @Test
    void findConfirmedBetween_delegates() {
        when(bookingRepository.findConfirmedBetween(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(List.of());
        bookingService.findConfirmedBetween(LocalDateTime.now().minusDays(1), LocalDateTime.now());
        verify(bookingRepository).findConfirmedBetween(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void expireUnpaidBookings_marksExpired_andReleasesSlots() {
        PackageEntity pkg = PackageEntity.builder().id(9L).availableSlots(0).totalSlots(10).status(PackageStatus.SOLD_OUT).build();
        BookingEntity b1 = new BookingEntity();
        b1.setId(1L);
        b1.setPackageEntity(pkg);
        b1.setPassengersCount(3);
        b1.setBookingStatus(BookingStatus.PENDING);
        b1.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        when(bookingRepository.findByBookingStatusAndExpiresAtBefore(eq(BookingStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of(b1));
        when(bookingRepository.save(any(BookingEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        bookingService.expireUnpaidBookings();

        assertThat(b1.getBookingStatus()).isEqualTo(BookingStatus.EXPIRED);
        verify(packageService).releaseSlots(pkg, 3);
        verify(bookingRepository).save(b1);
    }
}
