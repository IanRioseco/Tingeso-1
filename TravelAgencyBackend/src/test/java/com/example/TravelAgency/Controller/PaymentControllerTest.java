package com.example.TravelAgency.Controller;

import com.example.TravelAgency.Entity.BookingEntity;
import com.example.TravelAgency.Entity.PaymentEntity;
import com.example.TravelAgency.Entity.UserEntity;
import com.example.TravelAgency.Service.AccessControlService;
import com.example.TravelAgency.Service.BookingService;
import com.example.TravelAgency.Service.PaymentService;
import com.example.TravelAgency.Service.UserService;
import com.example.TravelAgency.enums.PaymentsStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;
    @MockBean
    private BookingService bookingService;
    @MockBean
    private AccessControlService accessControlService;
    @MockBean
    private UserService userService;

    @Test
    void pay_returns201() throws Exception {
        UserEntity currentUser = UserEntity.builder().id(1L).fullName("U").email("u@u.com").documentId("D").nationality("CL").build();
        when(userService.getOrCreateFromJwt(any())).thenReturn(currentUser);
        BookingEntity booking = new BookingEntity();
        booking.setId(10L);
        booking.setUser(currentUser);
        when(bookingService.findById(10L)).thenReturn(booking);
        when(accessControlService.requireSameUserOrAdmin(eq(1L), eq(1L))).thenReturn(currentUser);
        PaymentEntity payment = PaymentEntity.builder()
                .id(99L)
                .booking(booking)
                .amount(new BigDecimal("100.00"))
                .paymentMethod("CREDIT_CARD")
                .cardLast4("1111")
                .cardExpiry("12/30")
                .transactionRef("TXN")
                .status(PaymentsStatus.APPROVED)
                .paidAt(LocalDateTime.now())
                .build();
        when(paymentService.processPayment(eq(10L), anyString(), anyString(), anyString())).thenReturn(payment);

        mockMvc.perform(post("/api/payments/10")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cardNumber\":\"4111111111111111\",\"cardExpiry\":\"12/30\",\"cvv\":\"123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.bookingId").value(10))
                .andExpect(jsonPath("$.cardLast4").value("1111"));
    }

    @Test
    void preview_returns200() throws Exception {
        UserEntity currentUser = UserEntity.builder().id(1L).fullName("U").email("u@u.com").documentId("D").nationality("CL").build();
        when(userService.getOrCreateFromJwt(any())).thenReturn(currentUser);
        BookingEntity booking = new BookingEntity();
        booking.setId(10L);
        booking.setUser(currentUser);
        when(bookingService.findById(10L)).thenReturn(booking);
        when(accessControlService.requireSameUserOrAdmin(eq(1L), eq(1L))).thenReturn(currentUser);
        when(paymentService.previewPayment(10L)).thenReturn(
                new PaymentService.PaymentPreview(10L, "P", 1, BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.TEN, "Sin descuentos")
        );

        mockMvc.perform(get("/api/payments/summary/10").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId").value(10))
                .andExpect(jsonPath("$.packageName").value("P"));
    }
}

