package com.example.TravelAgency.dto.response;

import com.example.TravelAgency.Entity.UserEntity;
import com.example.TravelAgency.enums.UserRole;
import com.example.TravelAgency.enums.UserStatus;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class UserResponse {
    Long id;
    String fullName;
    String email;
    String phone;
    String documentId;
    String nationality;
    UserRole role;
    UserStatus status;
    boolean active;
    LocalDateTime lockedUntil;
    LocalDateTime createdAt;

    public static UserResponse from(UserEntity user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .documentId(user.getDocumentId())
                .nationality(user.getNationality())
                .role(user.getRole())
                .status(user.getStatus())
                .active(user.isActive())
                .lockedUntil(user.getLockedUntil())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
