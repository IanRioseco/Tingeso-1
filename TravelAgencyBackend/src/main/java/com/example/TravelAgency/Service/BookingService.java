package com.example.TravelAgency.Service;

import com.example.TravelAgency.Entity.BookingEntity;
import com.example.TravelAgency.Entity.PackageEntity;
import com.example.TravelAgency.Entity.UserEntity;
import com.example.TravelAgency.Exceptions.BusinessException;
import com.example.TravelAgency.Exceptions.ResourceNotFoundException;
import com.example.TravelAgency.Repository.BookingRepository;
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

        // Por ahora se registra la reserva sin motor de descuentos.
        BigDecimal baseAmount = packageEntity.getPrice().multiply(BigDecimal.valueOf(passengers));
        // Cuando exista DiscountService, aqui se puede calcular descuento y monto final.

        // Los cupos se descuentan dentro de la misma transaccion para evitar sobreventa.
        packageService.decreaseSlots(packageEntity, passengers);

        // La reserva nace como pendiente hasta que un pago o validacion la confirme.
        BookingEntity booking = new BookingEntity();
        booking.setUser(user);
        booking.setPackageEntity(packageEntity);
        booking.setPassengersCount(passengers);
        booking.setBaseAmount(baseAmount);
        booking.setDiscountAmount(BigDecimal.ZERO);
        booking.setFinalAmount(baseAmount);
        booking.setDiscountDetail("Sin descuentos aplicados");
        booking.setBookingStatus(BookingStatus.PENDING);
        booking.setSessionId(sessionId);

        return bookingRepository.save(booking);
    }

    public BookingEntity findById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));
    }

    public List<BookingEntity> findByUser(UserEntity user) {
        // Se consulta por id para depender de una clave estable, no del estado del objeto en memoria.
        return bookingRepository.findByUserId(user.getId());
    }

    public List<BookingEntity> findAll() {
        return bookingRepository.findAll();
    }

    @Transactional
    public void confirm(Long bookingId) {
        BookingEntity booking = findById(bookingId);
        // Confirmar no descuenta cupos otra vez; eso ya ocurrio al crear la reserva.
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
    }

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
    @Scheduled(fixedRate = 300000)
    @Transactional
    public void expireUnpaidBookings() {
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
