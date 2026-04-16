package com.example.TravelAgency.dto.response;

import com.example.TravelAgency.Entity.BookingEntity;
import com.example.TravelAgency.Entity.PaymentEntity;
import com.example.TravelAgency.enums.BookingStatus;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Value
@Builder
public class SalesReportItemResponse {
    LocalDateTime operationDate;
    Long bookingId;
    Long paymentId;
    String clientName;
    String clientEmail;
    String packageName;
    int passengers;
    BigDecimal bookingTotal;
    BigDecimal amountPaid;
    BookingStatus bookingStatus;

    /**
     * Fecha de operacion: pago aprobado si existe; si no, fecha de registro de la reserva.
     */
    public static SalesReportItemResponse from(BookingEntity booking, Optional<PaymentEntity> payment) {
        LocalDateTime operationDate = payment.map(PaymentEntity::getPaidAt).orElse(booking.getCreatedAt());
        return SalesReportItemResponse.builder()
                .operationDate(operationDate)
                .bookingId(booking.getId())
                .paymentId(payment.map(PaymentEntity::getId).orElse(null))
                .clientName(booking.getUser().getFullName())
                .clientEmail(booking.getUser().getEmail())
                .packageName(booking.getPackageEntity().getName())
                .passengers(booking.getPassengersCount())
                .bookingTotal(booking.getFinalAmount())
                .amountPaid(payment.map(PaymentEntity::getAmount).orElse(BigDecimal.ZERO))
                .bookingStatus(booking.getBookingStatus())
                .build();
    }
}
