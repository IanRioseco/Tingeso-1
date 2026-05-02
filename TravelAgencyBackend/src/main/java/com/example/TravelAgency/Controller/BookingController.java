package com.example.TravelAgency.Controller;

import com.example.TravelAgency.Entity.BookingEntity;
import com.example.TravelAgency.Entity.UserEntity;
import com.example.TravelAgency.Exceptions.BusinessException;
import com.example.TravelAgency.Service.AccessControlService;
import com.example.TravelAgency.Service.BookingService;
import com.example.TravelAgency.Service.PaymentService;
import com.example.TravelAgency.Service.UserService;
import com.example.TravelAgency.dto.request.BookingRequest;
import com.example.TravelAgency.dto.response.BookingReceiptResponse;
import com.example.TravelAgency.dto.response.BookingResponse;
import com.example.TravelAgency.enums.BookingStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
    private final UserService userService;

    /**
    Crea una nueva reserva para el usuario autenticado.
    El usuario se resuelve desde el JWT para evitar confiar en datos enviados por el cliente.
     */
    @PostMapping
    public ResponseEntity<BookingResponse> create(@Valid @RequestBody BookingRequest req,
                                                  @AuthenticationPrincipal Jwt jwt) {
        UserEntity currentUser = userService.getOrCreateFromJwt(jwt);
        UserEntity user = accessControlService.requireActiveUser(currentUser.getId());
        BookingEntity booking = bookingService.create(
                user, req.getPackageId(), req.getPassengers(), req.getSessionId());
        return ResponseEntity.status(HttpStatus.CREATED).body(BookingResponse.from(booking));
    }

    /**
    Obtiene el detalle de una reserva por su identificador.
    Solo el dueno de la reserva o un administrador pueden acceder.
     */
    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<BookingResponse> findById(@AuthenticationPrincipal Jwt jwt,
                                                    @PathVariable Long id) {
        UserEntity currentUser = userService.getOrCreateFromJwt(jwt);
        BookingEntity booking = bookingService.findById(id);
        accessControlService.requireSameUserOrAdmin(currentUser.getId(), booking.getUser().getId());
        return ResponseEntity.ok(BookingResponse.from(booking));
    }

     // Lista las reservas asociadas al usuario autenticado.

    @GetMapping("/my")
    public ResponseEntity<List<BookingResponse>> myBookings(
            @AuthenticationPrincipal Jwt jwt) {
        UserEntity currentUser = userService.getOrCreateFromJwt(jwt);
        UserEntity user = accessControlService.requireActiveUser(currentUser.getId());
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
    public ResponseEntity<List<BookingResponse>> findAll(@AuthenticationPrincipal Jwt jwt) {
        UserEntity currentUser = userService.getOrCreateFromJwt(jwt);
        accessControlService.requireAdmin(currentUser.getId());
        return ResponseEntity.ok(
                bookingService.findAll().stream().map(BookingResponse::from).toList()
        );
    }


     // Cancela una reserva pendiente del usuario o de un tercero si es administrador.

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Map<String, String>> cancel(@AuthenticationPrincipal Jwt jwt,
                                                      @PathVariable Long id) {
        UserEntity currentUser = userService.getOrCreateFromJwt(jwt);
        BookingEntity booking = bookingService.findById(id);
        accessControlService.requireSameUserOrAdmin(currentUser.getId(), booking.getUser().getId());
        bookingService.cancel(id);
        return ResponseEntity.ok(Map.of("message", "Reserva cancelada correctamente"));
    }

    /**
    Genera el comprobante de una reserva confirmada,
    incluyendo los datos de los pagos asociados.
     */
    @GetMapping("/{id}/receipt")
    public ResponseEntity<BookingReceiptResponse> receipt(@AuthenticationPrincipal Jwt jwt,
                                                          @PathVariable Long id) {
        UserEntity currentUser = userService.getOrCreateFromJwt(jwt);
        BookingEntity booking = bookingService.findById(id);
        accessControlService.requireSameUserOrAdmin(currentUser.getId(), booking.getUser().getId());
        if (booking.getBookingStatus() != BookingStatus.CONFIRMED) {
            throw new BusinessException(
                    "El comprobante solo puede emitirse para reservas confirmadas con pago registrado");
        }
        return ResponseEntity.ok(
                BookingReceiptResponse.from(booking, paymentService.findByBooking(id))
        );
    }
}
