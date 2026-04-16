package com.example.TravelAgency.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PaymentRequest {

    @NotBlank(message = "El numero de tarjeta es obligatorio")
    @Pattern(regexp = "^[0-9\\s]{12,19}$", message = "El numero de tarjeta debe contener entre 12 y 19 digitos")
    private String cardNumber;

    @NotBlank(message = "La fecha de expiracion es obligatoria")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/[0-9]{2}$", message = "La expiracion debe tener formato MM/YY")
    private String cardExpiry;

    @NotBlank(message = "El CVV es obligatorio")
    @Pattern(regexp = "^[0-9]{3,4}$", message = "El CVV debe tener 3 o 4 digitos")
    private String cvv;
}
