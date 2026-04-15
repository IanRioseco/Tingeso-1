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
    @Query("""
        SELECT b FROM BookingEntity b
        WHERE b.bookingStatus = 'CONFIRMED'
        AND b.createdAt BETWEEN :from AND :to
        ORDER BY b.createdAt DESC
    """)
    List<BookingEntity> findConfirmedBetween(@Param("from") LocalDateTime from,
                                             @Param("to") LocalDateTime to);

    // Reporte: ranking de paquetes por periodo
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
}
