package com.example.TravelAgency.Service;

import com.example.TravelAgency.Entity.BookingEntity;
import com.example.TravelAgency.Entity.PackageEntity;
import com.example.TravelAgency.Entity.UserEntity;
import com.example.TravelAgency.Exceptions.BusinessException;
import com.example.TravelAgency.Exceptions.ResourceNotFoundException;
import com.example.TravelAgency.Repository.BookingRepository;
import com.example.TravelAgency.config.TravelProperties;
import com.example.TravelAgency.enums.BookingStatus;
import com.example.TravelAgency.enums.PackageStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final PackageService packageService;
    private final PromotionService promotionService;
    private final TravelProperties travelProperties;

    /**
    Crea una nueva reserva para el usuario y el paquete indicados.
    Aplica las reglas de negocio de cupos, estado del paquete y descuentos/promociones
    y deja la reserva en estado PENDING hasta que exista un pago confirmado.
     */
    @Transactional
    public BookingEntity create(UserEntity user, Long packageId, int passengers, String sessionId) {
        // La reserva solo tiene sentido con al menos un pasajero.
        if (passengers <= 0) {
            throw new BusinessException("La cantidad de pasajeros debe ser mayor a 0");
        }

        PackageEntity packageEntity = packageService.findById(packageId);

        // Se bloquean paquetes que ya no aceptan nuevas reservas.
        if (packageEntity.getStatus() == PackageStatus.CANCELED) {
            throw new BusinessException("No se puede reservar un paquete cancelado");
        }
        if (packageEntity.getStatus() == PackageStatus.EXPIRED) {
            throw new BusinessException("No se puede reservar un paquete no vigente");
        }
        if (packageEntity.getStatus() == PackageStatus.SOLD_OUT) {
            throw new BusinessException("El paquete no tiene cupos disponibles");
        }
        if (packageEntity.getAvailableSlots() < passengers) {
            throw new BusinessException("Solo hay " + packageEntity.getAvailableSlots() + " cupos disponibles");
        }

        BigDecimal baseAmount = packageEntity.getPrice().multiply(BigDecimal.valueOf(passengers));
        PromotionService.DiscountResult discountResult = promotionService.calculate(
                baseAmount, passengers, user, sessionId
        );
        // `sessionId` se usa para reglas tipo “multi-paquete en una misma sesión” (p. ej. carrito/flujo).
        // No es un sessionId de Spring Security: es un identificador funcional que el frontend envía.

        // Los cupos se descuentan dentro de la misma transaccion para evitar sobreventa.
        packageService.decreaseSlots(packageEntity, passengers);

        // La reserva nace como pendiente hasta que un pago o validacion la confirme.
        BookingEntity booking = new BookingEntity();
        booking.setUser(user);
        booking.setPackageEntity(packageEntity);
        booking.setPassengersCount(passengers);
        booking.setBaseAmount(baseAmount);
        booking.setDiscountAmount(discountResult.discountAmount());
        booking.setFinalAmount(discountResult.finalAmount());
        booking.setDiscountDetail(discountResult.discountDetail());
        booking.setBookingStatus(BookingStatus.PENDING);
        booking.setSessionId(sessionId);
        // La expiración permite “reservar cupo” por un tiempo acotado.
        // Si no hay pago antes de este vencimiento, una tarea programada expira la reserva y libera cupos.
        booking.setExpiresAt(LocalDateTime.now().plusDays(
                travelProperties.getBooking().getPendingExpiresDays()));

        return bookingRepository.save(booking);
    }


    // Obtiene una reserva por su identificador o lanza excepcion si no existe.

    public BookingEntity findById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));
    }


    // Obtiene todas las reservas asociadas al usuario entregado.

    public List<BookingEntity> findByUser(UserEntity user) {
        // Se consulta por id para depender de una clave estable, no del estado del objeto en memoria.
        return bookingRepository.findByUserId(user.getId());
    }


     // Obtiene todas las reservas registradas en el sistema.

    public List<BookingEntity> findAll() {
        return bookingRepository.findAll();
    }


    // Obtiene las reservas confirmadas dentro del rango de fechas indicado.
    // Utilizado principalmente para reportes de ventas.

    public List<BookingEntity> findConfirmedBetween(LocalDateTime from, LocalDateTime to) {
        return bookingRepository.findConfirmedBetween(from, to);
    }


    // Confirma una reserva existente.
    // No vuelve a modificar cupos, ya que estos se descuentan al crear la reserva.

    @Transactional
    public void confirm(Long bookingId) {
        BookingEntity booking = findById(bookingId);
        if (booking.getFinalAmount() == null || booking.getFinalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("La reserva debe tener un monto final mayor que cero para confirmarse");
        }
        // Confirmar no descuenta cupos otra vez; eso ya ocurrio al crear la reserva.
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
    }


    // Cancela una reserva pendiente, validando que no haya sido confirmada ni expirada.
    // Al cancelar se devuelven los cupos al paquete asociado.

    @Transactional
    public void cancel(Long bookingId) {
        BookingEntity booking = findById(bookingId);

        if (booking.getBookingStatus() == BookingStatus.CONFIRMED) {
            throw new BusinessException("No se puede cancelar una reserva ya confirmada con pago");
        }
        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new BusinessException("La reserva ya esta cancelada");
        }
        if (booking.getBookingStatus() == BookingStatus.EXPIRED) {
            throw new BusinessException("La reserva ya esta expirada");
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        // Al cancelar una reserva pendiente, los cupos vuelven al paquete.
        packageService.releaseSlots(booking.getPackageEntity(), booking.getPassengersCount());
    }

    // Libera automaticamente los cupos de reservas pendientes que superaron su vencimiento.

    // Tarea programada que marca como expiradas las reservas pendientes cuyo plazo vencio
    // y devuelve sus cupos a los paquetes correspondientes.
     
    @Scheduled(fixedRate = 300000)
    @Transactional
    public void expireUnpaidBookings() {
        // Se ejecuta cada 5 minutos: busca reservas PENDING vencidas y las pasa a EXPIRED.
        // Importante: esto “revierte” el descuento de cupos hecho al crear la reserva, para que el stock
        // no quede bloqueado indefinidamente.
        List<BookingEntity> expired = bookingRepository
                .findByBookingStatusAndExpiresAtBefore(BookingStatus.PENDING, LocalDateTime.now());

        for (BookingEntity booking : expired) {
            // Solo se expiran reservas pendientes cuyo plazo ya termino.
            booking.setBookingStatus(BookingStatus.EXPIRED);
            bookingRepository.save(booking);
            packageService.releaseSlots(booking.getPackageEntity(), booking.getPassengersCount());
        }
    }
}
