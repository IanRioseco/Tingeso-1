package com.example.TravelAgency.Entity;

import com.example.TravelAgency.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "booking")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false)
    private PackageEntity packageEntity;

    @Column(name = "passengers",nullable = false)
    private int passengersCount;

    @Column(name = "base_amount",nullable = false, precision = 10, scale = 2)
    private BigDecimal baseAmount;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "final_amount", precision = 10, scale = 2)
    private BigDecimal finalAmount;

    //JSON string describiendo descuentos aplicados: descuentos por grupo, cliente frecuente
    @Column(name = "discount_detail", columnDefinition = "TEXT")
    private String discountDetail;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status", nullable = false)
    private BookingStatus bookingStatus;

    //Para agrupar compras simultaneas (descuento multipaquete)
    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    //@OneToOne(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    //private PaymentEntity payment

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.bookingStatus == null)
            this.bookingStatus = BookingStatus.PENDING;
        if (this.expiresAt == null)
            this.expiresAt = LocalDateTime.now().plusDays(30);
        if (this.discountAmount == null)
            this.discountAmount = BigDecimal.ZERO;
    }





}
