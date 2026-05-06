package com.example.TravelAgency.Controller;

import com.example.TravelAgency.Entity.PromotionEntity;
import com.example.TravelAgency.Entity.UserEntity;
import com.example.TravelAgency.Service.AccessControlService;
import com.example.TravelAgency.Service.PromotionService;
import com.example.TravelAgency.Service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PromotionController.class)
@AutoConfigureMockMvc(addFilters = false)
class PromotionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PromotionService promotionService;
    @MockBean
    private UserService userService;
    @MockBean
    private AccessControlService accessControlService;

    @Test
    void findActive_returns200() throws Exception {
        when(promotionService.findActivePromotions(any(LocalDate.class))).thenReturn(
                List.of(PromotionEntity.builder()
                        .id(1L).name("Promo").discountPct(new BigDecimal("10"))
                        .validFrom(LocalDate.now().minusDays(1)).validTo(LocalDate.now().plusDays(1))
                        .active(true).build())
        );

        mockMvc.perform(get("/api/promotions/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Promo"));
    }

    @Test
    void calculate_returns200() throws Exception {
        UserEntity currentUser = UserEntity.builder().id(1L).fullName("U").email("u@u.com").documentId("D").nationality("CL").build();
        when(userService.getOrCreateFromJwt(any())).thenReturn(currentUser);
        when(accessControlService.requireActiveUser(1L)).thenReturn(currentUser);
        when(promotionService.calculate(any(), eq(2), eq(currentUser), eq("S1")))
                .thenReturn(new PromotionService.DiscountResult(new BigDecimal("5.00"), new BigDecimal("95.00"), "X"));

        mockMvc.perform(post("/api/promotions/calculate")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"baseAmount\":100.00,\"passengers\":2,\"sessionId\":\"S1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.discountAmount").value(5.00))
                .andExpect(jsonPath("$.finalAmount").value(95.00));
    }
}

