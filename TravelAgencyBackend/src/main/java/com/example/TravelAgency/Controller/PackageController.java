package com.example.TravelAgency.Controller;

import com.example.TravelAgency.Entity.PackageEntity;
import com.example.TravelAgency.Service.AccessControlService;
import com.example.TravelAgency.Service.PackageService;
import com.example.TravelAgency.dto.request.PackageRequest;
import com.example.TravelAgency.dto.response.PackageResponse;
import com.example.TravelAgency.enums.PackageStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/packages")
@RequiredArgsConstructor
public class PackageController {

    private final PackageService packageService;
    private final AccessControlService accessControlService;

    @GetMapping
    public ResponseEntity<List<PackageResponse>> findAll() {
        return ResponseEntity.ok(packageService.findAvailable().stream().map(PackageResponse::from).toList());
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PackageResponse>> findAllForAdmin(
            @RequestHeader("X-User-Id") Long userId) {
        accessControlService.requireAdmin(userId);
        return ResponseEntity.ok(packageService.findAll().stream().map(PackageResponse::from).toList());
    }

    @GetMapping("/available")
    public ResponseEntity<List<PackageResponse>> findAvailable() {
        return ResponseEntity.ok(packageService.findAvailable().stream().map(PackageResponse::from).toList());
    }

    @GetMapping("/search")
    public ResponseEntity<List<PackageResponse>> search(
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String travelType,
            @RequestParam(required = false) String season,
            @RequestParam(required = false) Integer availableSlots,
            @RequestParam(required = false) Integer minDurationDays,
            @RequestParam(required = false) Integer maxDurationDays) {
        return ResponseEntity.ok(
                packageService.search(
                                destination, minPrice, maxPrice, startDate, endDate,
                    travelType, season, availableSlots, minDurationDays, maxDurationDays
                        )
                        .stream()
                        .map(PackageResponse::from)
                        .toList()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<PackageResponse> findById(@PathVariable Long id) {
        PackageEntity pkg = packageService.findById(id);
        if (!packageService.isPubliclyReservable(pkg)) {
            throw new com.example.TravelAgency.Exceptions.BusinessException(
                    "El paquete solicitado no esta disponible para clientes"
            );
        }
        return ResponseEntity.ok(PackageResponse.from(pkg));
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PackageResponse> findByIdForAdmin(@RequestHeader("X-User-Id") Long userId,
                                                            @PathVariable Long id) {
        accessControlService.requireAdmin(userId);
        return ResponseEntity.ok(PackageResponse.from(packageService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PackageResponse> create(@RequestHeader("X-User-Id") Long userId,
                                                  @Valid @RequestBody PackageRequest req) {
        accessControlService.requireAdmin(userId);
        PackageEntity pkg = PackageEntity.builder()
                .name(req.getName())
                .destination(req.getDestination())
                .description(req.getDescription())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .price(req.getPrice())
                .totalSlots(req.getTotalSlots())
                .travelType(req.getTravelType())
                .season(req.getSeason())
                .servicesIncluded(req.getServicesIncluded())
                .restrictions(req.getRestrictions())
                .conditions(req.getConditions())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PackageResponse.from(packageService.create(pkg)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PackageResponse> update(@RequestHeader("X-User-Id") Long userId,
                                                  @PathVariable Long id,
                                                  @Valid @RequestBody PackageRequest req) {
        accessControlService.requireAdmin(userId);
        PackageEntity updated = PackageEntity.builder()
                .name(req.getName())
                .destination(req.getDestination())
                .description(req.getDescription())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .price(req.getPrice())
                .totalSlots(req.getTotalSlots())
                .travelType(req.getTravelType())
                .season(req.getSeason())
                .servicesIncluded(req.getServicesIncluded())
                .restrictions(req.getRestrictions())
                .conditions(req.getConditions())
                .build();
        return ResponseEntity.ok(PackageResponse.from(packageService.update(id, updated)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> changeStatus(@RequestHeader("X-User-Id") Long userId,
                                                            @PathVariable Long id,
                                                            @RequestParam PackageStatus status) {
        accessControlService.requireAdmin(userId);
        packageService.changeStatus(id, status);
        return ResponseEntity.ok(Map.of("message", "Estado actualizado correctamente"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@RequestHeader("X-User-Id") Long userId,
                                                      @PathVariable Long id) {
        accessControlService.requireAdmin(userId);
        packageService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Paquete eliminado correctamente"));
    }
}
