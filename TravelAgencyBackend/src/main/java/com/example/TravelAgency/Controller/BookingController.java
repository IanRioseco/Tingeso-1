package com.example.TravelAgency.Controller;

import com.example.TravelAgency.dto.request.BookingRequest;
import com.example.TravelAgency.dto.response.BookingResponse;
import com.example.TravelAgency.Entity.BookingEntity;
import com.example.TravelAgency.Entity.UserEntity;
import com.example.TravelAgency.Service.BookingService;
import com.example.TravelAgency.Service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final UserService userService;

    // POST /api/bookings
    @PostMapping
    public ResponseEntity<BookingResponse> create(@Valid @RequestBody BookingRequest req,
                                                  @RequestHeader("X-User-Id") Long userId) {
        UserEntity user = userService.findById(userId);
        BookingEntity booking = bookingService.create(
                user, req.getPackageId(), req.getPassengers(), req.getSessionId());
        return ResponseEntity.status(HttpStatus.CREATED).body(BookingResponse.from(booking));
    }

    // GET /api/bookings/{id}
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(BookingResponse.from(bookingService.findById(id)));
    }

    // GET /api/bookings/my  (reservas del usuario autenticado)
    @GetMapping("/my")
    public ResponseEntity<List<BookingResponse>> myBookings(
            @RequestHeader("X-User-Id") Long userId) {
        UserEntity user = userService.findById(userId);
        return ResponseEntity.ok(
                bookingService.findByUser(user).stream().map(BookingResponse::from).toList()
        );
    }

    // GET /api/bookings  (solo admin)
    @GetMapping
    public ResponseEntity<List<BookingResponse>> findAll() {
        return ResponseEntity.ok(
                bookingService.findAll().stream().map(BookingResponse::from).toList()
        );
    }

    // PATCH /api/bookings/{id}/cancel
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Map<String, String>> cancel(@PathVariable Long id) {
        bookingService.cancel(id);
        return ResponseEntity.ok(Map.of("message", "Reserva cancelada correctamente"));
    }
}