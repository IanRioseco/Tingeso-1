package com.example.TravelAgency.Service;

import com.example.TravelAgency.Entity.UserEntity;
import com.example.TravelAgency.Exceptions.BusinessException;
import com.example.TravelAgency.Exceptions.ResourceNotFoundException;
import com.example.TravelAgency.Repository.UserRepository;
import com.example.TravelAgency.enums.UserRole;
import com.example.TravelAgency.enums.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void findById_whenMissing_throws() {
        when(userRepository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.findById(10L));
    }

    @Test
    void getOrCreateFromJwt_whenSubMissing_throws() {
        Jwt jwt = Jwt.withTokenValue("t")
                .header("alg", "none")
                .claims(c -> c.putAll(Map.of()))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        assertThrows(BusinessException.class, () -> userService.getOrCreateFromJwt(jwt));
    }

    @Test
    void getOrCreateFromJwt_whenUserExistsByKeycloakId_returnsExisting() {
        UserEntity existing = UserEntity.builder()
                .id(1L)
                .keycloakUserId("KC1")
                .fullName("U")
                .email("u@u.com")
                .documentId("D")
                .nationality("CL")
                .role(UserRole.CLIENT)
                .status(UserStatus.ACTIVE)
                .active(true)
                .build();
        when(userRepository.findByKeycloakUserId("KC1")).thenReturn(Optional.of(existing));

        Jwt jwt = Jwt.withTokenValue("t")
                .header("alg", "none")
                .claim("sub", "KC1")
                .claim("email", "U@U.COM")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        UserEntity result = userService.getOrCreateFromJwt(jwt);
        assertThat(result).isSameAs(existing);
        verify(userRepository, never()).save(any());
    }

    @Test
    void getOrCreateFromJwt_whenEmailAlreadyLinkedToOtherKeycloakUser_throws() {
        UserEntity existingByEmail = UserEntity.builder()
                .id(1L)
                .keycloakUserId("OTHER")
                .fullName("U")
                .email("u@u.com")
                .documentId("D")
                .nationality("CL")
                .build();

        when(userRepository.findByKeycloakUserId("KC1")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("u@u.com")).thenReturn(Optional.of(existingByEmail));

        Jwt jwt = Jwt.withTokenValue("t")
                .header("alg", "none")
                .claim("sub", "KC1")
                .claim("email", "u@u.com")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        assertThrows(BusinessException.class, () -> userService.getOrCreateFromJwt(jwt));
    }

    @Test
    void getOrCreateFromJwt_whenNewUser_createsWithLowercasedEmail_andUniqueDocumentId() {
        when(userRepository.findByKeycloakUserId("KC1")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("u@u.com")).thenReturn(Optional.empty());
        when(userRepository.existsByDocumentId(anyString())).thenReturn(false);
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        Jwt jwt = Jwt.withTokenValue("t")
                .header("alg", "none")
                .claim("sub", "KC1")
                .claim("email", "U@U.COM")
                .claim("name", "Full Name")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        UserEntity created = userService.getOrCreateFromJwt(jwt);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("u@u.com");
        assertThat(captor.getValue().getKeycloakUserId()).isEqualTo("KC1");
        assertThat(created.getDocumentId()).startsWith("PENDING-");
    }

    @Test
    void updateProfile_updatesOnlyNonBlankFields() {
        UserEntity user = UserEntity.builder()
                .id(1L)
                .fullName("Old")
                .email("u@u.com")
                .phone("1")
                .documentId("D")
                .nationality("CL")
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        UserEntity updated = userService.updateProfile(1L, "New", null, "  ", "AR");

        assertThat(updated.getFullName()).isEqualTo("New");
        assertThat(updated.getPhone()).isEqualTo("1");
        assertThat(updated.getDocumentId()).isEqualTo("D");
        assertThat(updated.getNationality()).isEqualTo("AR");
    }

    @Test
    void findAll_delegatesToRepository() {
        when(userRepository.findAll()).thenReturn(List.of());
        userService.findAll();
        verify(userRepository).findAll();
    }
}

