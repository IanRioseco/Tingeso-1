package com.example.TravelAgency.dto.response;

import com.example.TravelAgency.Service.PromotionService;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class PromotionCalculationResponse {
    BigDecimal discountAmount;
    BigDecimal finalAmount;
    String discountDetail;

    public static PromotionCalculationResponse from(PromotionService.DiscountResult result) {
        return PromotionCalculationResponse.builder()
                .discountAmount(result.discountAmount())
                .finalAmount(result.finalAmount())
                .discountDetail(result.discountDetail())
                .build();
    }
}
