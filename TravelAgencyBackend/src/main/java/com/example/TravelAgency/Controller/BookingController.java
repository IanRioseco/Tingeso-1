package com.example.TravelAgency.Controller;

import com.example.TravelAgency.Entity.BookingEntity;
import com.example.TravelAgency.Entity.UserEntity;
import com.example.TravelAgency.Exceptions.BusinessException;
import com.example.TravelAgency.Service.AccessControlService;
import com.example.TravelAgency.Service.BookingService;
import com.example.TravelAgency.Service.PaymentService;
import com.example.TravelAgency.dto.request.BookingRequest;
import com.example.TravelAgency.dto.response.BookingReceiptResponse;
import com.example.TravelAgency.dto.response.BookingResponse;
import com.example.TravelAgency.enums.BookingStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

 // Controlador REST para gestionar reservas de paquetes turisticos.
 // Expone endpoints para crear, consultar, cancelar reservas
 // y obtener comprobantes de una reserva confirmada

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class BookingController {

    private final BookingService bookingService;
    private final PaymentService paymentService;
    private final AccessControlService accessControlService;

    /**
    Crea una nueva reserva para el usuario autenticado.
    Valida que el identificador del usuario del request coincida
    con el encabezado `X-User-Id` y delega la logica al servicio.
     */
    @PostMapping
    public ResponseEntity<BookingResponse> create(@Valid @RequestBody BookingRequest req,
                                                  @RequestHeader("X-User-Id") Long userId) {
        if (!userId.equals(req.getUserId())) {
            throw new BusinessException("El usuario autenticado no coincide con el usuario de la reserva");
        }
        UserEntity user = accessControlService.requireActiveUser(userId);
        BookingEntity booking = bookingService.create(
                user, req.getPackageId(), req.getPassengers(), req.getSessionId());
        return ResponseEntity.status(HttpStatus.CREATED).body(BookingResponse.from(booking));
    }

    /**
    Obtiene el detalle de una reserva por su identificador.
    Solo el dueno de la reserva o un administrador pueden acceder.
     */
    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<BookingResponse> findById(@RequestHeader("X-User-Id") Long userId,
                                                    @PathVariable Long id) {
        BookingEntity booking = bookingService.findById(id);
        accessControlService.requireSameUserOrAdmin(userId, booking.getUser().getId());
        return ResponseEntity.ok(BookingResponse.from(booking));
    }

     // Lista las reservas asociadas al usuario autenticado.

    @GetMapping("/my")
    public ResponseEntity<List<BookingResponse>> myBookings(
            @RequestHeader("X-User-Id") Long userId) {
        UserEntity user = accessControlService.requireActiveUser(userId);
        return ResponseEntity.ok(
                bookingService.findByUser(user).stream().map(BookingResponse::from).toList()
        );
    }

    /**
    Lista todas las reservas del sistema.
    Solo accesible para usuarios con rol administrador.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingResponse>> findAll(@RequestHeader("X-User-Id") Long userId) {
        accessControlService.requireAdmin(userId);
        return ResponseEntity.ok(
                bookingService.findAll().stream().map(BookingResponse::from).toList()
        );
    }


     // Cancela una reserva pendiente del usuario o de un tercero si es administrador.

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Map<String, String>> cancel(@RequestHeader("X-User-Id") Long userId,
                                                      @PathVariable Long id) {
        BookingEntity booking = bookingService.findById(id);
        accessControlService.requireSameUserOrAdmin(userId, booking.getUser().getId());
        bookingService.cancel(id);
        return ResponseEntity.ok(Map.of("message", "Reserva cancelada correctamente"));
    }

    /**
    Genera el comprobante de una reserva confirmada,
    incluyendo los datos de los pagos asociados.
     */
    @GetMapping("/{id}/receipt")
    public ResponseEntity<BookingReceiptResponse> receipt(@RequestHeader("X-User-Id") Long userId,
                                                          @PathVariable Long id) {
        BookingEntity booking = bookingService.findById(id);
        accessControlService.requireSameUserOrAdmin(userId, booking.getUser().getId());
        if (booking.getBookingStatus() != BookingStatus.CONFIRMED) {
            throw new BusinessException(
                    "El comprobante solo puede emitirse para reservas confirmadas con pago registrado");
        }
        return ResponseEntity.ok(
                BookingReceiptResponse.from(booking, paymentService.findByBooking(id))
        );
    }
}
