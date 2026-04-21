package com.example.TravelAgency.Service;

import com.example.TravelAgency.Entity.BookingEntity;
import com.example.TravelAgency.Exceptions.BusinessException;
import com.example.TravelAgency.Repository.BookingRepository;
import com.example.TravelAgency.Repository.PaymentRepository;
import com.example.TravelAgency.dto.response.PackageRankingItemResponse;
import com.example.TravelAgency.dto.response.SalesReportItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final PaymentService paymentService;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;

    public List<SalesReportItemResponse> salesByPeriod(LocalDate from, LocalDate to) {
        DateRange range = validateRange(from, to);
        // Se arma el reporte a partir de reservas relevantes al rango.
        // La query hace fetch de relaciones para evitar N+1 cuando se construyen los DTOs.
        List<BookingEntity> bookings = bookingRepository.findForSalesReport(range.from(), range.to());
        return bookings.stream()
                // Para cada reserva se adjunta (si existe) el pago asociado; en este modelo hay a lo más 1 pago por reserva.
                .map(b -> SalesReportItemResponse.from(b, paymentRepository.findByBooking(b)))
                .sorted(Comparator.comparing(SalesReportItemResponse::getOperationDate).reversed())
                .toList();
    }

    public List<PackageRankingItemResponse> rankingPackagesByPeriod(LocalDate from, LocalDate to) {
        DateRange range = validateRange(from, to);
        // El ranking se basa en pagos aprobados (no en reservas creadas), para reflejar ventas efectivas.
        return paymentService.rankingPackagesBetween(range.from(), range.to()).stream()
                .map(PackageRankingItemResponse::from)
                .toList();
    }

    private DateRange validateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new BusinessException("La fecha de inicio y termino son obligatorias");
        }
        if (from.isAfter(to)) {
            throw new BusinessException("La fecha de inicio no puede ser posterior a la fecha de termino");
        }
        // Se normaliza a [inicio del día, fin del día] para que el rango sea inclusivo por fecha.
        return new DateRange(from.atStartOfDay(), to.atTime(23, 59, 59));
    }

    private record DateRange(LocalDateTime from, LocalDateTime to) {}
}
