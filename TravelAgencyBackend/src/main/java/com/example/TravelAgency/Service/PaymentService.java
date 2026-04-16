package com.example.TravelAgency.Service;

import com.example.TravelAgency.Entity.BookingEntity;
import com.example.TravelAgency.Entity.PaymentEntity;
import com.example.TravelAgency.Exceptions.BusinessException;
import com.example.TravelAgency.Exceptions.ResourceNotFoundException;
import com.example.TravelAgency.Repository.PaymentRepository;
import com.example.TravelAgency.enums.BookingStatus;
import com.example.TravelAgency.enums.PaymentsStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    public record PaymentPreview(
            Long bookingId,
            String packageName,
            int passengers,
            BigDecimal baseAmount,
            BigDecimal discountAmount,
            BigDecimal totalAmount,
            String discountDetail
    ) {}

    private final PaymentRepository paymentRepository;
    private final BookingService bookingService;

    @Transactional
    public PaymentEntity processPayment(Long bookingId, String cardNumber,
                                        String cardExpiry, String cvv) {

        BookingEntity booking = bookingService.findById(bookingId);

        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new BusinessException("No se puede pagar una reserva cancelada");
        }
        if (booking.getBookingStatus() == BookingStatus.EXPIRED) {
            throw new BusinessException("La reserva ha expirado");
        }
        if (booking.getBookingStatus() == BookingStatus.CONFIRMED) {
            throw new BusinessException("Esta reserva ya fue pagada");
        }
        if (paymentRepository.existsByBooking(booking)) {
            throw new BusinessException("Ya existe un pago para esta reserva");
        }
        if (booking.getFinalAmount() == null || booking.getFinalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El monto total de la reserva debe ser mayor que cero");
        }

        String sanitizedCard = cardNumber == null ? "" : cardNumber.replaceAll("\\s", "");
        if (sanitizedCard.length() < 4) {
            throw new BusinessException("El numero de tarjeta es invalido");
        }
        String last4 = sanitizedCard.substring(sanitizedCard.length() - 4);

        PaymentEntity payment = PaymentEntity.builder()
                .booking(booking)
                .amount(booking.getFinalAmount())
                .paymentMethod("CREDIT_CARD")
                .cardLast4(last4)
                .cardExpiry(cardExpiry)
                .status(PaymentsStatus.APPROVED)
                .build();

        paymentRepository.save(payment);
        bookingService.confirm(bookingId);

        return payment;
    }

    public PaymentPreview previewPayment(Long bookingId) {
        BookingEntity booking = bookingService.findById(bookingId);
        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new BusinessException("No se puede pagar una reserva cancelada");
        }
        if (booking.getBookingStatus() == BookingStatus.EXPIRED) {
            throw new BusinessException("La reserva ha expirado");
        }
        return new PaymentPreview(
                booking.getId(),
                booking.getPackageEntity().getName(),
                booking.getPassengersCount(),
                booking.getBaseAmount(),
                booking.getDiscountAmount(),
                booking.getFinalAmount(),
                booking.getDiscountDetail()
        );
    }

    public PaymentEntity findByBooking(Long bookingId) {
        BookingEntity booking = bookingService.findById(bookingId);
        return paymentRepository.findByBooking(booking)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado para esta reserva"));
    }

    public List<PaymentEntity> findApprovedPaymentsBetween(LocalDateTime from, LocalDateTime to) {
        return paymentRepository.findApprovedPaymentsBetween(from, to);
    }

    public List<Object[]> rankingPackagesBetween(LocalDateTime from, LocalDateTime to) {
        return paymentRepository.rankingPackagesBetween(from, to);
    }
}
