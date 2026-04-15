package com.example.TravelAgency.dto.response;

import com.example.TravelAgency.Entity.BookingEntity;
import com.example.TravelAgency.enums.BookingStatus;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
@Builder
public class BookingResponse {
    Long id;
    Long userId;
    String userName;
    Long packageId;
    String packageName;
    int passengers;
    BigDecimal baseAmount;
    BigDecimal discountAmount;
    BigDecimal finalAmount;
    String discountDetail;
    BookingStatus bookingStatus;
    String sessionId;
    LocalDateTime expiresAt;
    LocalDateTime createdAt;

    public static BookingResponse from(BookingEntity booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .userId(booking.getUser().getId())
                .userName(booking.getUser().getFullName())
                .packageId(booking.getPackageEntity().getId())
                .packageName(booking.getPackageEntity().getName())
                .passengers(booking.getPassengersCount())
                .baseAmount(booking.getBaseAmount())
                .discountAmount(booking.getDiscountAmount())
                .finalAmount(booking.getFinalAmount())
                .discountDetail(booking.getDiscountDetail())
                .bookingStatus(booking.getBookingStatus())
                .sessionId(booking.getSessionId())
                .expiresAt(booking.getExpiresAt())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}
