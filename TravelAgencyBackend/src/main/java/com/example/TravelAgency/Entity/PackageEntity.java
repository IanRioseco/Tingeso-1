package com.example.TravelAgency.Entity;

import com.example.TravelAgency.enums.PackageStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Table(name = "packages")
public class PackageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String destination;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "duration_days")
    private int durationDays;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "total_slots", nullable = false)
    private int totalSlots;

    @Column(name = "available_slots", nullable = false)
    private int availableSlots;

    @Column(name = "travel_type")
    private String travelType;

    private String season;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PackageStatus status;

    @Column(name = "services_included", columnDefinition = "TEXT")
    private String servicesIncluded;

    @Column(columnDefinition = "TEXT")
    private String restrictions;

    /** Condiciones comerciales y legales del paquete (epica 2). */
    @Column(name = "commercial_conditions", columnDefinition = "TEXT")
    private String conditions;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = PackageStatus.AVAILABLE;
        }
        if (this.availableSlots == 0 && this.totalSlots > 0) {
            this.availableSlots = this.totalSlots;
        }
        updateDurationDays();
    }

    @PreUpdate
    public void preUpdate() {
        if (this.availableSlots == 0) {
            this.status = PackageStatus.SOLD_OUT;
        }
        updateDurationDays();
    }

    private void updateDurationDays() {
        if (this.startDate != null && this.endDate != null) {
            this.durationDays = (int) (this.endDate.toEpochDay() - this.startDate.toEpochDay());
        }
    }
}
