package com.example.TravelAgency.Controller;

import com.example.TravelAgency.Entity.PromotionEntity;
import com.example.TravelAgency.Entity.UserEntity;
import com.example.TravelAgency.Service.PromotionService;
import com.example.TravelAgency.Service.UserService;
import com.example.TravelAgency.dto.request.PromotionCalculationRequest;
import com.example.TravelAgency.dto.request.PromotionRequest;
import com.example.TravelAgency.dto.response.PromotionCalculationResponse;
import com.example.TravelAgency.dto.response.PromotionResponse;
import com.example.TravelAgency.Service.AccessControlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;
        private final UserService userService;
        private final com.example.TravelAgency.Service.AccessControlService accessControlService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PromotionResponse>> findAll(@AuthenticationPrincipal Jwt jwt) {
        userService.getOrCreateFromJwt(jwt);
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
            @Valid @RequestBody PromotionCalculationRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UserEntity currentUser = userService.getOrCreateFromJwt(jwt);
        UserEntity user = accessControlService.requireActiveUser(currentUser.getId());
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
    public ResponseEntity<PromotionResponse> create(@AuthenticationPrincipal Jwt jwt,
                                                    @Valid @RequestBody PromotionRequest request) {
        userService.getOrCreateFromJwt(jwt);
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
    public ResponseEntity<PromotionResponse> update(@AuthenticationPrincipal Jwt jwt,
                                                    @PathVariable Long id,
                                                    @Valid @RequestBody PromotionRequest request) {
        userService.getOrCreateFromJwt(jwt);
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
    public ResponseEntity<PromotionResponse> changeStatus(@AuthenticationPrincipal Jwt jwt,
                                                          @PathVariable Long id,
                                                          @RequestParam boolean active) {
                userService.getOrCreateFromJwt(jwt);
        return ResponseEntity.ok(PromotionResponse.from(promotionService.changeStatus(id, active)));
    }
}
