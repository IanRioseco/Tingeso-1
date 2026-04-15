package com.example.TravelAgency.Controller;

import com.example.TravelAgency.Entity.PackageEntity;
import com.example.TravelAgency.Service.PackageService;
import com.example.TravelAgency.dto.request.PackageRequest;
import com.example.TravelAgency.dto.response.PackageResponse;
import com.example.TravelAgency.enums.PackageStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @GetMapping
    public ResponseEntity<List<PackageResponse>> findAll() {
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
            @RequestParam(required = false) String travelType) {
        return ResponseEntity.ok(
                packageService.search(destination, minPrice, maxPrice, startDate, travelType)
                        .stream()
                        .map(PackageResponse::from)
                        .toList()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<PackageResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(PackageResponse.from(packageService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<PackageResponse> create(@Valid @RequestBody PackageRequest req) {
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
                .build();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PackageResponse.from(packageService.create(pkg)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PackageResponse> update(@PathVariable Long id,
                                                  @RequestBody PackageRequest req) {
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
                .build();
        return ResponseEntity.ok(PackageResponse.from(packageService.update(id, updated)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String, String>> changeStatus(@PathVariable Long id,
                                                            @RequestParam PackageStatus status) {
        packageService.changeStatus(id, status);
        return ResponseEntity.ok(Map.of("message", "Estado actualizado correctamente"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        packageService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Paquete eliminado correctamente"));
    }
}
