package com.example.TravelAgency.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PromotionRequest {

    @NotBlank(message = "El nombre de la promocion es obligatorio")
    private String name;

    @NotNull(message = "El porcentaje de descuento es obligatorio")
    @DecimalMin(value = "0.01", message = "El porcentaje de descuento debe ser mayor que cero")
    private BigDecimal discountPct;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate validFrom;

    @NotNull(message = "La fecha de termino es obligatoria")
    private LocalDate validTo;

    private boolean active = true;
}
