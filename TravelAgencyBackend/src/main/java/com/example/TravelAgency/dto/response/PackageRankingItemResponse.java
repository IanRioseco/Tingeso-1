package com.example.TravelAgency.dto.response;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class PackageRankingItemResponse {
    Long packageId;
    String packageName;
    long reservationsCount;
    long passengersCount;
    BigDecimal totalRevenue;

    public static PackageRankingItemResponse from(Object[] row) {
        return PackageRankingItemResponse.builder()
                .packageId((Long) row[0])
                .packageName((String) row[1])
                .reservationsCount(((Number) row[2]).longValue())
                .passengersCount(((Number) row[3]).longValue())
                .totalRevenue((BigDecimal) row[4])
                .build();
    }
}
