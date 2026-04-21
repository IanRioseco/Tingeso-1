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

/**
Servicio de dominio para la gestion de pagos de reservas.
Valida el estado de la reserva, crea el pago y coordina la confirmacion de la reserva.
*/
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

    /**
    Procesa el pago de una reserva validando su estado y los datos basicos de la tarjeta.
    Marca el pago como aprobado y confirma la reserva asociada.
    */
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

        // En esta entrega no existe un gateway real; se valida lo mínimo y se “aprueba” de inmediato.
        // El objetivo es modelar el flujo (reservar → pagar → confirmar) y evitar duplicidad de pagos.
        String sanitizedCard = cardNumber == null ? "" : cardNumber.replaceAll("\\s", "");
        if (sanitizedCard.length() < 4) {
            throw new BusinessException("El numero de tarjeta es invalido");
        }
        // Se persiste solo el last4 para evitar almacenar PAN completo en base de datos.
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
        // La confirmación de la reserva se amarra a “pago aprobado” para que los reportes/receipt
        // se basen en un estado consistente (CONFIRMED).
        bookingService.confirm(bookingId);

        return payment;
    }

    /**
    Entrega un resumen de la informacion relevante de la reserva
    antes de efectuar el pago (previsualizacion en el frontend).
    */
    public PaymentPreview previewPayment(Long bookingId) {
        BookingEntity booking = bookingService.findById(bookingId);
        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new BusinessException("No se puede pagar una reserva cancelada");
        }
        if (booking.getBookingStatus() == BookingStatus.EXPIRED) {
            throw new BusinessException("La reserva ha expirado");
        }
        // Preview no muta estado: solo devuelve datos para mostrar al usuario (total, descuentos, detalle).
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

    
    // Obtiene el pago asociado a una reserva, o lanza excepcion si no existe.
     
    public PaymentEntity findByBooking(Long bookingId) {
        BookingEntity booking = bookingService.findById(bookingId);
        return paymentRepository.findByBooking(booking)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado para esta reserva"));
    }

    
    // Lista pagos aprobados en el periodo indicado para uso en reportes de ventas.
    
    public List<PaymentEntity> findApprovedPaymentsBetween(LocalDateTime from, LocalDateTime to) {
        return paymentRepository.findApprovedPaymentsBetween(from, to);
    }

    
    // Obtiene datos crudos para el ranking de paquetes vendidos en un periodo.
     
    public List<Object[]> rankingPackagesBetween(LocalDateTime from, LocalDateTime to) {
        // Este método devuelve filas “crudas” (Object[]) para no acoplar el ranking a una entidad.
        // Luego se transforma a DTO en `PackageRankingItemResponse.from`.
        return paymentRepository.rankingPackagesBetween(from, to);
    }
}
