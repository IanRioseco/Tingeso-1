package com.example.TravelAgency.dto.response;

import com.example.TravelAgency.Entity.BookingEntity;
import com.example.TravelAgency.Entity.PaymentEntity;
import com.example.TravelAgency.enums.BookingStatus;
import com.example.TravelAgency.enums.PaymentsStatus;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Value
@Builder
public class BookingReceiptResponse {
    Long bookingId;
    String clientName;
    String clientEmail;
    String packageName;
    String destination;
    LocalDate startDate;
    LocalDate endDate;
    int passengers;
    BigDecimal baseAmount;
    BigDecimal discountAmount;
    BigDecimal finalAmount;
    String discountDetail;
    BookingStatus bookingStatus;
    Long paymentId;
    PaymentsStatus paymentStatus;
    String transactionRef;
    LocalDateTime paidAt;

    public static BookingReceiptResponse from(BookingEntity booking, PaymentEntity payment) {
        return BookingReceiptResponse.builder()
                .bookingId(booking.getId())
                .clientName(booking.getUser().getFullName())
                .clientEmail(booking.getUser().getEmail())
                .packageName(booking.getPackageEntity().getName())
                .destination(booking.getPackageEntity().getDestination())
                .startDate(booking.getPackageEntity().getStartDate())
                .endDate(booking.getPackageEntity().getEndDate())
                .passengers(booking.getPassengersCount())
                .baseAmount(booking.getBaseAmount())
                .discountAmount(booking.getDiscountAmount())
                .finalAmount(booking.getFinalAmount())
                .discountDetail(booking.getDiscountDetail())
                .bookingStatus(booking.getBookingStatus())
                .paymentId(payment.getId())
                .paymentStatus(payment.getStatus())
                .transactionRef(payment.getTransactionRef())
                .paidAt(payment.getPaidAt())
                .build();
    }
}
