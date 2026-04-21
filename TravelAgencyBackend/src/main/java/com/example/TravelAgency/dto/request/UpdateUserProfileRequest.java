package com.example.TravelAgency.dto.request;

import lombok.Data;

@Data
public class UpdateUserProfileRequest {
    private String fullName;
    private String phone;
    private String documentId;
    private String nationality;
}
