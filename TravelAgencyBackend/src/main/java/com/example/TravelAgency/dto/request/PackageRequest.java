package com.example.TravelAgency.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PackageRequest {

    @NotBlank(message = "El nombre del paquete es obligatorio")
    private String name;

    @NotBlank(message = "El destino es obligatorio")
    private String destination;

    @NotBlank(message = "La descripcion es obligatoria")
    private String description;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @Future(message = "La fecha de inicio debe ser futura")
    private LocalDate startDate;

    @NotNull(message = "La fecha de termino es obligatoria")
    @Future(message = "La fecha de termino debe ser futura")
    private LocalDate endDate;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor que cero")
    private BigDecimal price;

    @Min(value = 1, message = "Los cupos deben ser mayores que cero")
    private int totalSlots;

    private String travelType;

    private String season;

    @NotBlank(message = "Los servicios incluidos son obligatorios")
    private String servicesIncluded;

    private String restrictions;

    @NotBlank(message = "Las condiciones del paquete son obligatorias")
    private String conditions;
}
