package com.example.TravelAgency.Controller;

import com.example.TravelAgency.Entity.PromotionEntity;
import com.example.TravelAgency.Entity.UserEntity;
import com.example.TravelAgency.Service.AccessControlService;
import com.example.TravelAgency.Service.PromotionService;
import com.example.TravelAgency.Service.UserService;
import com.example.TravelAgency.dto.request.PromotionCalculationRequest;
import com.example.TravelAgency.dto.request.PromotionRequest;
import com.example.TravelAgency.dto.response.PromotionCalculationResponse;
import com.example.TravelAgency.dto.response.PromotionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;
    private final UserService userService;
    private final AccessControlService accessControlService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PromotionResponse>> findAll(@RequestHeader("X-User-Id") Long userId) {
        accessControlService.requireAdmin(userId);
        return ResponseEntity.ok(
                promotionService.findAll().stream().map(PromotionResponse::from).toList()
        );
    }

    @GetMapping("/active")
    public ResponseEntity<List<PromotionResponse>> findActive() {
        return ResponseEntity.ok(
                promotionService.findActivePromotions(LocalDate.now()).stream()
                        .map(PromotionResponse::from)
                        .toList()
        );
    }

    @PostMapping("/calculate")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<PromotionCalculationResponse> calculate(
            @Valid @RequestBody PromotionCalculationRequest request) {
        UserEntity user = userService.findById(request.getUserId());
        PromotionService.DiscountResult result = promotionService.calculate(
                request.getBaseAmount(),
                request.getPassengers(),
                user,
                request.getSessionId()
        );
        return ResponseEntity.ok(PromotionCalculationResponse.from(result));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PromotionResponse> create(@RequestHeader("X-User-Id") Long userId,
                                                    @Valid @RequestBody PromotionRequest request) {
        accessControlService.requireAdmin(userId);
        PromotionEntity promotion = PromotionEntity.builder()
                .name(request.getName())
                .discountPct(request.getDiscountPct())
                .validFrom(request.getValidFrom())
                .validTo(request.getValidTo())
                .active(request.isActive())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PromotionResponse.from(promotionService.create(promotion)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PromotionResponse> update(@RequestHeader("X-User-Id") Long userId,
                                                    @PathVariable Long id,
                                                    @Valid @RequestBody PromotionRequest request) {
        accessControlService.requireAdmin(userId);
        PromotionEntity updated = PromotionEntity.builder()
                .name(request.getName())
                .discountPct(request.getDiscountPct())
                .validFrom(request.getValidFrom())
                .validTo(request.getValidTo())
                .active(request.isActive())
                .build();
        return ResponseEntity.ok(PromotionResponse.from(promotionService.update(id, updated)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PromotionResponse> changeStatus(@RequestHeader("X-User-Id") Long userId,
                                                          @PathVariable Long id,
                                                          @RequestParam boolean active) {
        accessControlService.requireAdmin(userId);
        return ResponseEntity.ok(PromotionResponse.from(promotionService.changeStatus(id, active)));
    }
}
