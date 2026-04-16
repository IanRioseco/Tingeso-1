package com.example.TravelAgency.dto.response;

import com.example.TravelAgency.Entity.PaymentEntity;
import com.example.TravelAgency.enums.PaymentsStatus;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
@Builder
public class PaymentResponse {
    Long id;
    Long bookingId;
    BigDecimal amount;
    String paymentMethod;
    String cardLast4;
    String cardExpiry;
    String transactionRef;
    PaymentsStatus status;
    LocalDateTime paidAt;

    public static PaymentResponse from(PaymentEntity payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .bookingId(payment.getBooking().getId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .cardLast4(payment.getCardLast4())
                .cardExpiry(payment.getCardExpiry())
                .transactionRef(payment.getTransactionRef())
                .status(payment.getStatus())
                .paidAt(payment.getPaidAt())
                .build();
    }
}
