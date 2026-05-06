package com.example.TravelAgency.Controller;

import com.example.TravelAgency.Entity.UserEntity;
import com.example.TravelAgency.Service.AccessControlService;
import com.example.TravelAgency.Service.ReportService;
import com.example.TravelAgency.Service.UserService;
import com.example.TravelAgency.dto.response.PackageRankingItemResponse;
import com.example.TravelAgency.dto.response.SalesReportItemResponse;
import com.example.TravelAgency.enums.BookingStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;
    @MockBean
    private AccessControlService accessControlService;
    @MockBean
    private UserService userService;

    @Test
    void sales_returns200() throws Exception {
        UserEntity admin = UserEntity.builder().id(1L).fullName("A").email("a@a.com").documentId("D").nationality("CL").build();
        when(userService.getOrCreateFromJwt(any())).thenReturn(admin);
        when(accessControlService.requireAdmin(1L)).thenReturn(admin);
        when(reportService.salesByPeriod(any(LocalDate.class), any(LocalDate.class))).thenReturn(
                List.of(SalesReportItemResponse.builder()
                        .operationDate(LocalDateTime.of(2026, 5, 1, 10, 0))
                        .bookingId(10L)
                        .paymentId(20L)
                        .clientName("C")
                        .clientEmail("c@c.com")
                        .packageName("P")
                        .passengers(1)
                        .bookingTotal(new BigDecimal("10.00"))
                        .amountPaid(new BigDecimal("10.00"))
                        .bookingStatus(BookingStatus.CONFIRMED)
                        .build())
        );

        mockMvc.perform(get("/api/reports/sales?from=2026-05-01&to=2026-05-02").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookingId").value(10));
    }

    @Test
    void packagesRanking_returns200() throws Exception {
        UserEntity admin = UserEntity.builder().id(1L).fullName("A").email("a@a.com").documentId("D").nationality("CL").build();
        when(userService.getOrCreateFromJwt(any())).thenReturn(admin);
        when(accessControlService.requireAdmin(1L)).thenReturn(admin);
        when(reportService.rankingPackagesByPeriod(any(LocalDate.class), any(LocalDate.class))).thenReturn(
                List.of(PackageRankingItemResponse.builder()
                        .packageId(1L)
                        .packageName("P")
                        .reservationsCount(2)
                        .passengersCount(3)
                        .totalRevenue(new BigDecimal("100.00"))
                        .build())
        );

        mockMvc.perform(get("/api/reports/packages-ranking?from=2026-05-01&to=2026-05-02").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].packageId").value(1));
    }
}

