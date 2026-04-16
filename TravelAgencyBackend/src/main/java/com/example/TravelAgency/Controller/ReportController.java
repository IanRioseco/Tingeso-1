package com.example.TravelAgency.Controller;

import com.example.TravelAgency.Service.AccessControlService;
import com.example.TravelAgency.Service.ReportService;
import com.example.TravelAgency.dto.response.PackageRankingItemResponse;
import com.example.TravelAgency.dto.response.SalesReportItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final AccessControlService accessControlService;

    @GetMapping("/sales")
    public ResponseEntity<List<SalesReportItemResponse>> sales(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        accessControlService.requireAdmin(userId);
        return ResponseEntity.ok(reportService.salesByPeriod(from, to));
    }

    @GetMapping("/packages-ranking")
    public ResponseEntity<List<PackageRankingItemResponse>> packagesRanking(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        accessControlService.requireAdmin(userId);
        return ResponseEntity.ok(reportService.rankingPackagesByPeriod(from, to));
    }
}
