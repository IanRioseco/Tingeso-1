package com.example.TravelAgency.Service;

import com.example.TravelAgency.Entity.UserEntity;
import com.example.TravelAgency.Exceptions.BusinessException;
import com.example.TravelAgency.enums.UserRole;
import com.example.TravelAgency.enums.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccessControlServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AccessControlService accessControlService;

    @Test
    void requireActiveUser_whenUserIsActive_returnsUser() {
        UserEntity user = UserEntity.builder()
                .id(10L)
                .active(true)
                .status(UserStatus.ACTIVE)
                .role(UserRole.CLIENT)
                .email("a@a.com")
                .fullName("A")
                .documentId("DOC-1")
                .nationality("CL")
                .build();
        when(userService.findById(10L)).thenReturn(user);

        UserEntity result = accessControlService.requireActiveUser(10L);

        assertSame(user, result);
    }

    @Test
    void requireActiveUser_whenInactive_throws() {
        UserEntity user = UserEntity.builder()
                .id(10L)
                .active(false)
                .status(UserStatus.INACTIVE)
                .role(UserRole.CLIENT)
                .email("a@a.com")
                .fullName("A")
                .documentId("DOC-1")
                .nationality("CL")
                .build();
        when(userService.findById(10L)).thenReturn(user);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> accessControlService.requireActiveUser(10L));
        assertTrue(ex.getMessage().toLowerCase().contains("no esta activa"));
    }

    @Test
    void requireAdmin_whenNotAdmin_throws() {
        UserEntity user = UserEntity.builder()
                .id(10L)
                .active(true)
                .status(UserStatus.ACTIVE)
                .role(UserRole.CLIENT)
                .email("a@a.com")
                .fullName("A")
                .documentId("DOC-1")
                .nationality("CL")
                .build();
        when(userService.findById(10L)).thenReturn(user);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> accessControlService.requireAdmin(10L));
        assertTrue(ex.getMessage().toLowerCase().contains("administrador"));
    }

    @Test
    void requireAdmin_whenAdmin_returnsUser() {
        UserEntity user = UserEntity.builder()
                .id(10L)
                .active(true)
                .status(UserStatus.ACTIVE)
                .role(UserRole.ADMIN)
                .email("a@a.com")
                .fullName("A")
                .documentId("DOC-1")
                .nationality("CL")
                .build();
        when(userService.findById(10L)).thenReturn(user);

        UserEntity result = accessControlService.requireAdmin(10L);

        assertSame(user, result);
    }

    @Test
    void requireSameUserOrAdmin_whenSameUser_returnsActor() {
        UserEntity user = UserEntity.builder()
                .id(10L)
                .active(true)
                .status(UserStatus.ACTIVE)
                .role(UserRole.CLIENT)
                .email("a@a.com")
                .fullName("A")
                .documentId("DOC-1")
                .nationality("CL")
                .build();
        when(userService.findById(10L)).thenReturn(user);

        UserEntity actor = accessControlService.requireSameUserOrAdmin(10L, 10L);

        assertSame(user, actor);
    }

    @Test
    void requireSameUserOrAdmin_whenAdmin_returnsActor() {
        UserEntity admin = UserEntity.builder()
                .id(10L)
                .active(true)
                .status(UserStatus.ACTIVE)
                .role(UserRole.ADMIN)
                .email("a@a.com")
                .fullName("A")
                .documentId("DOC-1")
                .nationality("CL")
                .build();
        when(userService.findById(10L)).thenReturn(admin);

        UserEntity actor = accessControlService.requireSameUserOrAdmin(10L, 99L);

        assertSame(admin, actor);
    }

    @Test
    void requireSameUserOrAdmin_whenDifferentAndNotAdmin_throws() {
        UserEntity user = UserEntity.builder()
                .id(10L)
                .active(true)
                .status(UserStatus.ACTIVE)
                .role(UserRole.CLIENT)
                .email("a@a.com")
                .fullName("A")
                .documentId("DOC-1")
                .nationality("CL")
                .build();
        when(userService.findById(10L)).thenReturn(user);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> accessControlService.requireSameUserOrAdmin(10L, 99L));
        assertTrue(ex.getMessage().toLowerCase().contains("permisos"));
    }
}

