package com.example.TravelAgency.Controller;

import com.example.TravelAgency.Entity.UserEntity;
import com.example.TravelAgency.Service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void me_returns200() throws Exception {
        UserEntity me = UserEntity.builder().id(1L).fullName("U").email("u@u.com").documentId("D").nationality("CL").build();
        when(userService.getOrCreateFromJwt(any())).thenReturn(me);

        mockMvc.perform(get("/api/users/me").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("u@u.com"));
    }

    @Test
    void updateMe_returns200() throws Exception {
        UserEntity me = UserEntity.builder().id(1L).fullName("U").email("u@u.com").documentId("D").nationality("CL").build();
        when(userService.getOrCreateFromJwt(any())).thenReturn(me);
        UserEntity updated = UserEntity.builder().id(1L).fullName("New").email("u@u.com").documentId("D").nationality("AR").build();
        when(userService.updateProfile(eq(1L), eq("New"), eq("123"), eq("DOC2"), eq("AR"))).thenReturn(updated);

        mockMvc.perform(put("/api/users/me")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"New\",\"phone\":\"123\",\"documentId\":\"DOC2\",\"nationality\":\"AR\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("New"))
                .andExpect(jsonPath("$.nationality").value("AR"));
    }
}

