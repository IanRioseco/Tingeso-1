package com.example.TravelAgency.Controller;

import com.example.TravelAgency.Service.AccessControlService;
import com.example.TravelAgency.Service.ReportService;
import com.example.TravelAgency.dto.response.PackageRankingItemResponse;
import com.example.TravelAgency.dto.response.SalesReportItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
Controlador REST para exponer reportes administrativos.
Incluye reportes de ventas por periodo y ranking de paquetes.
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final ReportService reportService;
    private final AccessControlService accessControlService;

    /**
    Reporte de ventas por periodo para administradores.
    Devuelve el detalle de ventas entre las fechas indicadas.
     */
    @GetMapping("/sales")
    public ResponseEntity<List<SalesReportItemResponse>> sales(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        accessControlService.requireAdmin(userId);
        return ResponseEntity.ok(reportService.salesByPeriod(from, to));
    }

    /**
    Reporte de ranking de paquetes vendidos por periodo.
    Solo accesible para usuarios con rol administrador.
     */
    @GetMapping("/packages-ranking")
    public ResponseEntity<List<PackageRankingItemResponse>> packagesRanking(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        accessControlService.requireAdmin(userId);
        return ResponseEntity.ok(reportService.rankingPackagesByPeriod(from, to));
    }
}
