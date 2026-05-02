package com.example.TravelAgency.Controller;

import com.example.TravelAgency.Entity.BookingEntity;
import com.example.TravelAgency.Entity.PaymentEntity;
import com.example.TravelAgency.Entity.UserEntity;
import com.example.TravelAgency.Service.AccessControlService;
import com.example.TravelAgency.Service.BookingService;
import com.example.TravelAgency.Service.PaymentService;
import com.example.TravelAgency.Service.UserService;
import com.example.TravelAgency.dto.request.PaymentRequest;
import com.example.TravelAgency.dto.response.PaymentResponse;
import com.example.TravelAgency.dto.response.PaymentSummaryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para operaciones relacionadas con pagos de reservas.
 * Permite procesar pagos, obtener un resumen previo y consultar el pago asociado a una reserva.
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class PaymentController {

    private final PaymentService paymentService;
    private final BookingService bookingService;
    private final AccessControlService accessControlService;
    private final UserService userService;

    /**
     * Procesa el pago de una reserva especifica del usuario autenticado
     * (o de un tercero si el usuario tiene rol administrador).
     */
    @PostMapping("/{bookingId}")
    public ResponseEntity<PaymentResponse> pay(@AuthenticationPrincipal Jwt jwt,
                                               @PathVariable Long bookingId,
                                               @Valid @RequestBody PaymentRequest req) {
        UserEntity currentUser = userService.getOrCreateFromJwt(jwt);
        BookingEntity booking = bookingService.findById(bookingId);
        accessControlService.requireSameUserOrAdmin(currentUser.getId(), booking.getUser().getId());
        PaymentEntity payment = paymentService.processPayment(
                bookingId, req.getCardNumber(), req.getCardExpiry(), req.getCvv());
        return ResponseEntity.status(HttpStatus.CREATED).body(PaymentResponse.from(payment));
    }

    /**
     * Entrega un resumen del pago (montos y descuentos) para una reserva
     * sin ejecutar aun la transaccion de pago.
     */
    @GetMapping("/summary/{bookingId}")
    public ResponseEntity<PaymentSummaryResponse> preview(@AuthenticationPrincipal Jwt jwt,
                                                          @PathVariable Long bookingId) {
        UserEntity currentUser = userService.getOrCreateFromJwt(jwt);
        BookingEntity booking = bookingService.findById(bookingId);
        accessControlService.requireSameUserOrAdmin(currentUser.getId(), booking.getUser().getId());
        return ResponseEntity.ok(PaymentSummaryResponse.from(paymentService.previewPayment(bookingId)));
    }

    /**
     * Obtiene el pago registrado para una reserva determinada.
     */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<PaymentResponse> findByBooking(@AuthenticationPrincipal Jwt jwt,
                                                         @PathVariable Long bookingId) {
        UserEntity currentUser = userService.getOrCreateFromJwt(jwt);
        BookingEntity booking = bookingService.findById(bookingId);
        accessControlService.requireSameUserOrAdmin(currentUser.getId(), booking.getUser().getId());
        return ResponseEntity.ok(PaymentResponse.from(paymentService.findByBooking(bookingId)));
    }
}
