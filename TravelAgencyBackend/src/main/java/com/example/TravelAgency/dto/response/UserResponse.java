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
    String keycloakUserId;
    String fullName;
    String email;
    String phone;
    String documentId;
    String nationality;
    UserRole role;
    UserStatus status;
    boolean active;
    LocalDateTime createdAt;

    public static UserResponse from(UserEntity user) {
        return UserResponse.builder()
                .id(user.getId())
                .keycloakUserId(user.getKeycloakUserId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .documentId(user.getDocumentId())
                .nationality(user.getNationality())
                .role(user.getRole())
                .status(user.getStatus())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
