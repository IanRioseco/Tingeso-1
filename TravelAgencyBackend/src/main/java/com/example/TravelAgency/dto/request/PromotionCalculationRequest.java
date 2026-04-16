package com.example.TravelAgency.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PromotionCalculationRequest {

    @NotNull(message = "El monto base es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto base debe ser mayor que cero")
    private BigDecimal baseAmount;

    @Min(value = 1, message = "La cantidad de pasajeros debe ser mayor a 0")
    private int passengers;

    @NotNull(message = "El id del usuario es obligatorio")
    private Long userId;

    private String sessionId;
}
