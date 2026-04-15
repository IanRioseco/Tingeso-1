package com.example.TravelAgency.dto.response;

import com.example.TravelAgency.Entity.PackageEntity;
import com.example.TravelAgency.enums.PackageStatus;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Value
@Builder
public class PackageResponse {
    Long id;
    String name;
    String destination;
    String description;
    LocalDate startDate;
    LocalDate endDate;
    int durationDays;
    BigDecimal price;
    int totalSlots;
    int availableSlots;
    String travelType;
    String season;
    PackageStatus status;
    String servicesIncluded;
    String restrictions;
    LocalDateTime createdAt;

    public static PackageResponse from(PackageEntity pkg) {
        return PackageResponse.builder()
                .id(pkg.getId())
                .name(pkg.getName())
                .destination(pkg.getDestination())
                .description(pkg.getDescription())
                .startDate(pkg.getStartDate())
                .endDate(pkg.getEndDate())
                .durationDays(pkg.getDurationDays())
                .price(pkg.getPrice())
                .totalSlots(pkg.getTotalSlots())
                .availableSlots(pkg.getAvailableSlots())
                .travelType(pkg.getTravelType())
                .season(pkg.getSeason())
                .status(pkg.getStatus())
                .servicesIncluded(pkg.getServicesIncluded())
                .restrictions(pkg.getRestrictions())
                .createdAt(pkg.getCreatedAt())
                .build();
    }
}
