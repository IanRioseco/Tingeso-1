package com.example.TravelAgency.Controller;

import com.example.TravelAgency.Entity.PackageEntity;
import com.example.TravelAgency.Service.AccessControlService;
import com.example.TravelAgency.Service.PackageService;
import com.example.TravelAgency.enums.PackageStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PackageController.class)
@AutoConfigureMockMvc(addFilters = false)
class PackageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PackageService packageService;
    @MockBean
    private AccessControlService accessControlService;

    @Test
    void findAvailable_returns200() throws Exception {
        PackageEntity pkg = PackageEntity.builder()
                .id(1L)
                .name("P")
                .destination("D")
                .description("Desc")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .price(new BigDecimal("10.00"))
                .totalSlots(10)
                .availableSlots(10)
                .status(PackageStatus.AVAILABLE)
                .servicesIncluded("S")
                .conditions("C")
                .build();
        when(packageService.findAvailable()).thenReturn(List.of(pkg));

        mockMvc.perform(get("/api/packages/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("P"));
    }

    @Test
    void findById_returns200_whenPubliclyReservable() throws Exception {
        PackageEntity pkg = PackageEntity.builder()
                .id(1L)
                .name("P")
                .destination("D")
                .description("Desc")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .price(new BigDecimal("10.00"))
                .totalSlots(10)
                .availableSlots(10)
                .status(PackageStatus.AVAILABLE)
                .servicesIncluded("S")
                .conditions("C")
                .build();
        when(packageService.findById(anyLong())).thenReturn(pkg);
        when(packageService.isPubliclyReservable(pkg)).thenReturn(true);

        mockMvc.perform(get("/api/packages/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.destination").value("D"));
    }
}

