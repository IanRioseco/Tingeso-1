package com.example.TravelAgency.Controller;

import com.example.TravelAgency.Entity.BookingEntity;
import com.example.TravelAgency.Entity.PackageEntity;
import com.example.TravelAgency.Entity.PaymentEntity;
import com.example.TravelAgency.Entity.UserEntity;
import com.example.TravelAgency.Service.AccessControlService;
import com.example.TravelAgency.Service.BookingService;
import com.example.TravelAgency.Service.PaymentService;
import com.example.TravelAgency.Service.UserService;
import com.example.TravelAgency.enums.BookingStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;
    @MockBean
    private PaymentService paymentService;
    @MockBean
    private AccessControlService accessControlService;
    @MockBean
    private UserService userService;

    @Test
    void create_returns201() throws Exception {
        UserEntity currentUser = UserEntity.builder().id(1L).fullName("U").email("u@u.com").documentId("D").nationality("CL").build();
        when(userService.getOrCreateFromJwt(any())).thenReturn(currentUser);
        when(accessControlService.requireActiveUser(1L)).thenReturn(currentUser);

        PackageEntity pkg = PackageEntity.builder().id(10L).name("PKG").destination("D").startDate(LocalDate.now()).endDate(LocalDate.now()).build();
        BookingEntity booking = new BookingEntity();
        booking.setId(100L);
        booking.setUser(currentUser);
        booking.setPackageEntity(pkg);
        booking.setPassengersCount(2);
        booking.setBaseAmount(new BigDecimal("200.00"));
        booking.setDiscountAmount(BigDecimal.ZERO);
        booking.setFinalAmount(new BigDecimal("200.00"));
        booking.setDiscountDetail("Sin descuentos");
        booking.setBookingStatus(BookingStatus.PENDING);
        booking.setSessionId("S1");
        booking.setCreatedAt(LocalDateTime.now());
        when(bookingService.create(eq(currentUser), eq(10L), eq(2), eq("S1"))).thenReturn(booking);

        mockMvc.perform(post("/api/bookings")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"packageId\":10,\"passengers\":2,\"sessionId\":\"S1\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.packageId").value(10))
                .andExpect(jsonPath("$.passengers").value(2));
    }

    @Test
    void receipt_whenConfirmed_returns200() throws Exception {
        UserEntity currentUser = UserEntity.builder().id(1L).fullName("U").email("u@u.com").documentId("D").nationality("CL").build();
        when(userService.getOrCreateFromJwt(any())).thenReturn(currentUser);

        BookingEntity booking = new BookingEntity();
        booking.setId(100L);
        booking.setUser(UserEntity.builder().id(1L).fullName("U").email("u@u.com").documentId("D").nationality("CL").build());
        booking.setPackageEntity(PackageEntity.builder().id(10L).name("PKG").destination("D").startDate(LocalDate.now()).endDate(LocalDate.now()).build());
        booking.setPassengersCount(1);
        booking.setBaseAmount(new BigDecimal("10.00"));
        booking.setDiscountAmount(BigDecimal.ZERO);
        booking.setFinalAmount(new BigDecimal("10.00"));
        booking.setDiscountDetail("Sin descuentos");
        booking.setBookingStatus(BookingStatus.CONFIRMED);

        when(bookingService.findById(100L)).thenReturn(booking);
        when(accessControlService.requireSameUserOrAdmin(eq(1L), eq(1L))).thenReturn(currentUser);
        PaymentEntity payment = PaymentEntity.builder().id(200L).status(com.example.TravelAgency.enums.PaymentsStatus.APPROVED).amount(new BigDecimal("10.00")).transactionRef("TXN").paidAt(LocalDateTime.now()).booking(booking).build();
        when(paymentService.findByBooking(100L)).thenReturn(payment);

        mockMvc.perform(get("/api/bookings/100/receipt").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId").value(100))
                .andExpect(jsonPath("$.paymentId").value(200));
    }

    @Test
    void cancel_returns200_message() throws Exception {
        UserEntity currentUser = UserEntity.builder().id(1L).fullName("U").email("u@u.com").documentId("D").nationality("CL").build();
        when(userService.getOrCreateFromJwt(any())).thenReturn(currentUser);
        BookingEntity booking = new BookingEntity();
        booking.setId(100L);
        booking.setUser(currentUser);
        when(bookingService.findById(100L)).thenReturn(booking);
        when(accessControlService.requireSameUserOrAdmin(eq(1L), eq(1L))).thenReturn(currentUser);

        mockMvc.perform(patch("/api/bookings/100/cancel").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Reserva cancelada correctamente"));
    }
}

