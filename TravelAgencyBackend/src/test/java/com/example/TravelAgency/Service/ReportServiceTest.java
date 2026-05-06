package com.example.TravelAgency.Service;

import com.example.TravelAgency.Entity.BookingEntity;
import com.example.TravelAgency.Entity.PackageEntity;
import com.example.TravelAgency.Entity.PaymentEntity;
import com.example.TravelAgency.Entity.UserEntity;
import com.example.TravelAgency.Exceptions.BusinessException;
import com.example.TravelAgency.Repository.BookingRepository;
import com.example.TravelAgency.Repository.PaymentRepository;
import com.example.TravelAgency.dto.response.PackageRankingItemResponse;
import com.example.TravelAgency.dto.response.SalesReportItemResponse;
import com.example.TravelAgency.enums.BookingStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private PaymentService paymentService;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private ReportService reportService;

    @Test
    void salesByPeriod_whenInvalidRange_throws() {
        LocalDate from = LocalDate.of(2026, 5, 10);
        LocalDate to = LocalDate.of(2026, 5, 1);
        assertThrows(BusinessException.class, () -> reportService.salesByPeriod(from, to));
    }

    @Test
    void salesByPeriod_buildsItems_andSortsByOperationDateDesc() {
        UserEntity user = UserEntity.builder()
                .id(1L).fullName("U").email("u@u.com").documentId("D").nationality("CL").build();
        PackageEntity pkg = PackageEntity.builder().id(2L).name("P").build();

        BookingEntity booking = new BookingEntity();
        booking.setId(10L);
        booking.setUser(user);
        booking.setPackageEntity(pkg);
        booking.setPassengersCount(1);
        booking.setFinalAmount(new BigDecimal("50.00"));
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        booking.setCreatedAt(LocalDateTime.of(2026, 5, 1, 10, 0));

        PaymentEntity payment = PaymentEntity.builder()
                .id(99L)
                .amount(new BigDecimal("50.00"))
                .paidAt(LocalDateTime.of(2026, 5, 2, 10, 0))
                .build();

        when(bookingRepository.findForSalesReport(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));
        when(paymentRepository.findByBooking(booking)).thenReturn(Optional.of(payment));

        List<SalesReportItemResponse> items = reportService.salesByPeriod(
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 3)
        );

        assertThat(items).hasSize(1);
        assertThat(items.get(0).getPaymentId()).isEqualTo(99L);
        assertThat(items.get(0).getOperationDate()).isEqualTo(LocalDateTime.of(2026, 5, 2, 10, 0));
    }

    @Test
    void rankingPackagesByPeriod_mapsRowsToDto() {
        when(paymentService.rankingPackagesBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.<Object[]>of(new Object[]{1L, "P", 2L, 5L, new BigDecimal("100.00")}));

        List<PackageRankingItemResponse> ranking = reportService.rankingPackagesByPeriod(
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 3)
        );

        assertThat(ranking).hasSize(1);
        assertThat(ranking.get(0).getPackageId()).isEqualTo(1L);
        assertThat(ranking.get(0).getReservationsCount()).isEqualTo(2L);
    }
}
