package com.example.TravelAgency.Entity;

import com.example.TravelAgency.enums.PaymentsStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private BookingEntity booking;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;

    // Solo guardamos últimos 4 dígitos
    @Column(name = "card_last4")
    private String cardLast4;

    @Column(name = "card_expiry")
    private String cardExpiry;

    @Column(name = "transaction_ref", unique = true)
    private String transactionRef;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentsStatus status;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @PrePersist
    public void prePersist() {
        this.status = PaymentsStatus.APPROVED;
        this.paidAt = LocalDateTime.now();
        // Genera referencia única
        this.transactionRef = "TXN-" + System.currentTimeMillis();
    }
}