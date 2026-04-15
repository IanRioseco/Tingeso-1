package com.example.TravelAgency.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingRequest {

    @NotNull(message = "El id del usuario es obligatorio")
    private Long userId;

    @NotNull(message = "El id del paquete es obligatorio")
    private Long packageId;

    @Min(value = 1, message = "La cantidad de pasajeros debe ser mayor a 0")
    private int passengers;

    private String sessionId;
}
