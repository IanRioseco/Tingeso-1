package com.example.TravelAgency.Repository;

import com.example.TravelAgency.Entity.BookingEntity;
import com.example.TravelAgency.Entity.UserEntity;
import com.example.TravelAgency.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, Long> {

    // Reservas de un cliente especifico
    List<BookingEntity> findByUserId(Long id);

    // Reservas por estado
    List<BookingEntity> findByBookingStatus(BookingStatus bookingStatus);

    // Reservas expiradas que aun estan pendientes (para liberal cupos)
    List<BookingEntity> findByBookingStatusAndExpiresAtBefore(BookingStatus bookingStatus, LocalDateTime now);

    // Cuenta reservas pagadas/confirmadas de un usuario (para cliente frecuente)
    @Query("""
        SELECT COUNT(b) FROM BookingEntity b
        WHERE b.user = :user
        AND b.bookingStatus = 'CONFIRMED'
    """)
    long countConfirmedByUser(@Param("user") UserEntity user);

    // Reporte: listado de ventas por periodo
    // Nota: se filtra por `createdAt` de la reserva. En este proyecto la confirmación está asociada al pago,
    // pero la fecha de operación puede variar según cómo armes el reporte (creación vs pago).
    @Query("""
        SELECT b FROM BookingEntity b
        WHERE b.bookingStatus = 'CONFIRMED'
        AND b.createdAt BETWEEN :from AND :to
        ORDER BY b.createdAt DESC
    """)
    List<BookingEntity> findConfirmedBetween(@Param("from") LocalDateTime from,
                                             @Param("to") LocalDateTime to);

    // Reporte: ranking de paquetes por periodo
    // Este ranking usa reservas confirmadas (no pagos). En paralelo existe otro ranking basado en pagos aprobados
    // (`PaymentRepository.rankingPackagesBetween`) que refleja ventas efectivas.
    @Query("""
        SELECT b.packageEntity.id, b.packageEntity.name,
               COUNT(b),
               SUM(b.passengersCount),
               SUM(b.finalAmount)
        FROM BookingEntity b
        WHERE b.bookingStatus = 'CONFIRMED'
        AND b.createdAt BETWEEN :from AND :to
        GROUP BY b.packageEntity.id, b.packageEntity.name
        ORDER BY COUNT(b) DESC, b.packageEntity.name ASC
    """)
    List<Object[]> rankingPackagesBetween(@Param("from") LocalDateTime from,
                                          @Param("to") LocalDateTime to);

    // Reservas de la misma sesion (para descuento multi-paquete)
    List<BookingEntity> findBySessionId(String sessionId);


    // Reservas pendientes o confirmadas del usuario creadas desde {@code since} (compra multiple en periodo).

    @Query("""
        SELECT COUNT(b) FROM BookingEntity b
        WHERE b.user = :user
        AND b.bookingStatus IN ('PENDING', 'CONFIRMED')
        AND b.createdAt >= :since
        """)
    long countActiveBookingsSince(@Param("user") UserEntity user, @Param("since") LocalDateTime since);

     // Reporte de ventas: registro de reserva o pago aprobado dentro del rango; excluye canceladas.
     // La condición OR permite que una venta aparezca si:
     // - la reserva fue creada en el rango (útil para “movimiento de reservas”), o
     // - existe un pago APPROVED en el rango (útil para “ventas efectivas”).
     // Se hace `DISTINCT` porque el EXISTS puede coincidir con pagos y evitar duplicados.

    @Query("""
        SELECT DISTINCT b FROM BookingEntity b
        LEFT JOIN FETCH b.user
        LEFT JOIN FETCH b.packageEntity
        WHERE b.bookingStatus <> 'CANCELLED'
        AND (
          (b.createdAt BETWEEN :from AND :to)
          OR EXISTS (
            SELECT 1 FROM PaymentEntity p
            WHERE p.booking = b
            AND p.paidAt BETWEEN :from AND :to
            AND p.status = 'APPROVED'
          )
        )
        """)
    List<BookingEntity> findForSalesReport(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
