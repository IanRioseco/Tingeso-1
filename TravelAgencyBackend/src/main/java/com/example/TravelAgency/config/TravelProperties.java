package com.example.TravelAgency.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@Data
@ConfigurationProperties(prefix = "travel")
public class TravelProperties {

    private Booking booking = new Booking();
    private Discount discount = new Discount();

    @Data
    public static class Booking {
        /**
         * Plazo en dias para pagar una reserva pendiente antes de expirar y liberar cupos.
         */
        private int pendingExpiresDays = 30;
    }

    @Data
    public static class Discount {
        private int groupMinPassengers = 4;
        private BigDecimal groupPercent = new BigDecimal("10");
        private int frequentMinPaidBookings = 3;
        private BigDecimal frequentPercent = new BigDecimal("5");
        private BigDecimal multiPackagePercent = new BigDecimal("5");
        /**
         * Ventana en dias para considerar otra reserva del mismo cliente como compra multiple.
         */
        private int multiPackageLookbackDays = 7;
        private BigDecimal maxTotalPercent = new BigDecimal("20");
    }
}
