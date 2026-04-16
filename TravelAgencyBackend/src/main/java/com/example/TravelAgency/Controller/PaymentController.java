package com.example.TravelAgency.Controller;

import com.example.TravelAgency.Entity.BookingEntity;
import com.example.TravelAgency.Entity.PaymentEntity;
import com.example.TravelAgency.Service.AccessControlService;
import com.example.TravelAgency.Service.BookingService;
import com.example.TravelAgency.Service.PaymentService;
import com.example.TravelAgency.dto.request.PaymentRequest;
import com.example.TravelAgency.dto.response.PaymentResponse;
import com.example.TravelAgency.dto.response.PaymentSummaryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final BookingService bookingService;
    private final AccessControlService accessControlService;

    @PostMapping("/{bookingId}")
    public ResponseEntity<PaymentResponse> pay(@RequestHeader("X-User-Id") Long userId,
                                               @PathVariable Long bookingId,
                                               @Valid @RequestBody PaymentRequest req) {
        BookingEntity booking = bookingService.findById(bookingId);
        accessControlService.requireSameUserOrAdmin(userId, booking.getUser().getId());
        PaymentEntity payment = paymentService.processPayment(
                bookingId, req.getCardNumber(), req.getCardExpiry(), req.getCvv());
        return ResponseEntity.status(HttpStatus.CREATED).body(PaymentResponse.from(payment));
    }

    @GetMapping("/summary/{bookingId}")
    public ResponseEntity<PaymentSummaryResponse> preview(@RequestHeader("X-User-Id") Long userId,
                                                          @PathVariable Long bookingId) {
        BookingEntity booking = bookingService.findById(bookingId);
        accessControlService.requireSameUserOrAdmin(userId, booking.getUser().getId());
        return ResponseEntity.ok(PaymentSummaryResponse.from(paymentService.previewPayment(bookingId)));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<PaymentResponse> findByBooking(@RequestHeader("X-User-Id") Long userId,
                                                         @PathVariable Long bookingId) {
        BookingEntity booking = bookingService.findById(bookingId);
        accessControlService.requireSameUserOrAdmin(userId, booking.getUser().getId());
        return ResponseEntity.ok(PaymentResponse.from(paymentService.findByBooking(bookingId)));
    }
}
