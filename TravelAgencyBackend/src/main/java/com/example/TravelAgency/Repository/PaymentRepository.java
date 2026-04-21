package com.example.TravelAgency.Repository;

import com.example.TravelAgency.Entity.BookingEntity;
import com.example.TravelAgency.Entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {


    // Obtiene el pago asociado a una reserva, si existe.

    Optional<PaymentEntity> findByBooking(BookingEntity booking);

    /**
    Indica si ya existe un pago para la reserva dada.
    Se usa para evitar pagos duplicados sobre la misma reserva.
     */
    boolean existsByBooking(BookingEntity booking);

    /**
    Lista pagos aprobados dentro de un rango de fechas,
    cargando ademas la reserva, el usuario y el paquete para uso en reportes.
     */
    @Query("""
        SELECT p FROM PaymentEntity p
        JOIN FETCH p.booking b
        JOIN FETCH b.user
        JOIN FETCH b.packageEntity
        WHERE p.status = 'APPROVED'
        AND p.paidAt BETWEEN :from AND :to
        ORDER BY p.paidAt DESC
    """)
    List<PaymentEntity> findApprovedPaymentsBetween(@Param("from") LocalDateTime from,
                                                    @Param("to") LocalDateTime to);

    /**
    Calcula un ranking de paquetes vendidos en base a pagos aprobados
    en el periodo indicado, considerando cantidad de pagos, pasajeros y montos.
    */
    @Query("""
        SELECT b.packageEntity.id,
               b.packageEntity.name,
               COUNT(p),
               SUM(b.passengersCount),
               SUM(p.amount)
        FROM PaymentEntity p
        JOIN p.booking b
        WHERE p.status = 'APPROVED'
        AND p.paidAt BETWEEN :from AND :to
        GROUP BY b.packageEntity.id, b.packageEntity.name
        ORDER BY COUNT(p) DESC, SUM(p.amount) DESC, b.packageEntity.name ASC
    """)
    List<Object[]> rankingPackagesBetween(@Param("from") LocalDateTime from,
                                          @Param("to") LocalDateTime to);
}
