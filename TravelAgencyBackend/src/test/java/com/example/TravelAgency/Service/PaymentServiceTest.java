package com.example.TravelAgency.Service;

import com.example.TravelAgency.Entity.BookingEntity;
import com.example.TravelAgency.Entity.PackageEntity;
import com.example.TravelAgency.Entity.PaymentEntity;
import com.example.TravelAgency.Entity.UserEntity;
import com.example.TravelAgency.Exceptions.BusinessException;
import com.example.TravelAgency.Repository.PaymentRepository;
import com.example.TravelAgency.enums.BookingStatus;
import com.example.TravelAgency.enums.PaymentsStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
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
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private BookingService bookingService;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void processPayment_happyPath_savesPayment_andConfirmsBooking() {
        BookingEntity booking = new BookingEntity();
        booking.setId(1L);
        booking.setBookingStatus(BookingStatus.PENDING);
        booking.setPassengersCount(2);
        booking.setFinalAmount(new BigDecimal("100.00"));
        booking.setBaseAmount(new BigDecimal("100.00"));
        booking.setDiscountAmount(BigDecimal.ZERO);
        booking.setDiscountDetail("Sin descuentos");
        booking.setUser(UserEntity.builder().id(2L).fullName("U").email("u@u.com").documentId("D").nationality("CL").build());
        booking.setPackageEntity(PackageEntity.builder().id(3L).name("P").destination("D").startDate(LocalDate.now()).endDate(LocalDate.now()).build());

        when(bookingService.findById(1L)).thenReturn(booking);
        when(paymentRepository.existsByBooking(booking)).thenReturn(false);
        when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentEntity payment = paymentService.processPayment(1L, "4111 1111 1111 1111", "12/30", "123");

        ArgumentCaptor<PaymentEntity> captor = ArgumentCaptor.forClass(PaymentEntity.class);
        verify(paymentRepository).save(captor.capture());
        PaymentEntity saved = captor.getValue();
        assertThat(saved.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(saved.getCardLast4()).isEqualTo("1111");
        assertThat(saved.getPaymentMethod()).isEqualTo("CREDIT_CARD");
        assertThat(saved.getStatus()).isEqualTo(PaymentsStatus.APPROVED);
        verify(bookingService).confirm(1L);
        assertThat(payment).isSameAs(saved);
    }

    @Test
    void processPayment_whenCardTooShort_throws() {
        BookingEntity booking = new BookingEntity();
        booking.setId(1L);
        booking.setBookingStatus(BookingStatus.PENDING);
        booking.setFinalAmount(new BigDecimal("10.00"));

        when(bookingService.findById(1L)).thenReturn(booking);
        when(paymentRepository.existsByBooking(booking)).thenReturn(false);

        assertThrows(BusinessException.class,
                () -> paymentService.processPayment(1L, "1", "12/30", "123"));
        verify(paymentRepository, never()).save(any());
        verify(bookingService, never()).confirm(anyLong());
    }

    @Test
    void findByBooking_whenNotFound_throws() {
        BookingEntity booking = new BookingEntity();
        booking.setId(1L);
        when(bookingService.findById(1L)).thenReturn(booking);
        when(paymentRepository.findByBooking(booking)).thenReturn(Optional.empty());
        assertThrows(com.example.TravelAgency.Exceptions.ResourceNotFoundException.class,
                () -> paymentService.findByBooking(1L));
    }

    @Test
    void previewPayment_whenCancelled_throws() {
        BookingEntity booking = new BookingEntity();
        booking.setId(1L);
        booking.setBookingStatus(BookingStatus.CANCELLED);
        when(bookingService.findById(1L)).thenReturn(booking);

        assertThatThrownBy(() -> paymentService.previewPayment(1L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void previewPayment_happyPath_returnsData() {
        BookingEntity booking = new BookingEntity();
        booking.setId(1L);
        booking.setBookingStatus(BookingStatus.PENDING);
        booking.setPassengersCount(2);
        booking.setBaseAmount(new BigDecimal("200.00"));
        booking.setDiscountAmount(new BigDecimal("10.00"));
        booking.setFinalAmount(new BigDecimal("190.00"));
        booking.setDiscountDetail("D");
        booking.setPackageEntity(PackageEntity.builder().id(3L).name("PKG").build());
        when(bookingService.findById(1L)).thenReturn(booking);

        PaymentService.PaymentPreview preview = paymentService.previewPayment(1L);

        assertThat(preview.bookingId()).isEqualTo(1L);
        assertThat(preview.packageName()).isEqualTo("PKG");
        assertThat(preview.passengers()).isEqualTo(2);
        assertThat(preview.totalAmount()).isEqualByComparingTo(new BigDecimal("190.00"));
    }

    @Test
    void processPayment_whenBookingAlreadyConfirmed_throws() {
        BookingEntity booking = new BookingEntity();
        booking.setId(1L);
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        booking.setFinalAmount(new BigDecimal("10.00"));
        when(bookingService.findById(1L)).thenReturn(booking);

        assertThrows(BusinessException.class,
                () -> paymentService.processPayment(1L, "4111111111111111", "12/30", "123"));
        verify(paymentRepository, never()).save(any());
        verify(bookingService, never()).confirm(anyLong());
    }

    @Test
    void processPayment_whenPaymentAlreadyExists_throws() {
        BookingEntity booking = new BookingEntity();
        booking.setId(1L);
        booking.setBookingStatus(BookingStatus.PENDING);
        booking.setFinalAmount(new BigDecimal("10.00"));
        when(bookingService.findById(1L)).thenReturn(booking);
        when(paymentRepository.existsByBooking(booking)).thenReturn(true);

        assertThrows(BusinessException.class,
                () -> paymentService.processPayment(1L, "4111111111111111", "12/30", "123"));
        verify(paymentRepository, never()).save(any());
        verify(bookingService, never()).confirm(anyLong());
    }

    @Test
    void findApprovedPaymentsBetween_delegates() {
        when(paymentRepository.findApprovedPaymentsBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

        paymentService.findApprovedPaymentsBetween(LocalDateTime.now().minusDays(1), LocalDateTime.now());

        verify(paymentRepository).findApprovedPaymentsBetween(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void rankingPackagesBetween_delegates() {
        when(paymentRepository.rankingPackagesBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

        paymentService.rankingPackagesBetween(LocalDateTime.now().minusDays(1), LocalDateTime.now());

        verify(paymentRepository).rankingPackagesBetween(any(LocalDateTime.class), any(LocalDateTime.class));
    }
}
