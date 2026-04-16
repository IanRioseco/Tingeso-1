package com.example.TravelAgency.dto.response;

import com.example.TravelAgency.Service.PaymentService;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class PaymentSummaryResponse {
    Long bookingId;
    String packageName;
    int passengers;
    BigDecimal baseAmount;
    BigDecimal discountAmount;
    BigDecimal totalAmount;
    String discountDetail;

    public static PaymentSummaryResponse from(PaymentService.PaymentPreview preview) {
        return PaymentSummaryResponse.builder()
                .bookingId(preview.bookingId())
                .packageName(preview.packageName())
                .passengers(preview.passengers())
                .baseAmount(preview.baseAmount())
                .discountAmount(preview.discountAmount())
                .totalAmount(preview.totalAmount())
                .discountDetail(preview.discountDetail())
                .build();
    }
}
