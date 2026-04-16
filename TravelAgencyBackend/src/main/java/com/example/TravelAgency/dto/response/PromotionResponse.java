package com.example.TravelAgency.dto.response;

import com.example.TravelAgency.Entity.PromotionEntity;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

@Value
@Builder
public class PromotionResponse {
    Long id;
    String name;
    BigDecimal discountPct;
    LocalDate validFrom;
    LocalDate validTo;
    boolean active;

    public static PromotionResponse from(PromotionEntity promotion) {
        return PromotionResponse.builder()
                .id(promotion.getId())
                .name(promotion.getName())
                .discountPct(promotion.getDiscountPct())
                .validFrom(promotion.getValidFrom())
                .validTo(promotion.getValidTo())
                .active(promotion.isActive())
                .build();
    }
}
