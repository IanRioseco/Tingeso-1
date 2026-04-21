package com.example.TravelAgency.Service;

import com.example.TravelAgency.Entity.UserEntity;
import com.example.TravelAgency.Exceptions.BusinessException;
import com.example.TravelAgency.Exceptions.ResourceNotFoundException;
import com.example.TravelAgency.Repository.UserRepository;
import com.example.TravelAgency.enums.UserRole;
import com.example.TravelAgency.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserEntity findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    public UserEntity findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    // Sincroniza el usuario local con el sujeto autenticado en Keycloak.
    public UserEntity getOrCreateFromJwt(Jwt jwt) {
        String keycloakUserId = claimAsString(jwt, "sub");
        if (keycloakUserId == null) {
            throw new BusinessException("Token JWT invalido: no contiene claim sub");
        }

        return userRepository.findByKeycloakUserId(keycloakUserId)
                .orElseGet(() -> createOrLinkLocalUser(jwt, keycloakUserId));
    }

    public List<UserEntity> findAll() {
        return userRepository.findAll();
    }

    public UserEntity updateProfile(Long id, String fullName, String phone,
                                    String documentId, String nationality) {
        UserEntity user = findById(id);

        if (fullName != null && !fullName.isBlank()) {
            user.setFullName(fullName);
        }
        if (phone != null) {
            user.setPhone(phone);
        }
        if (documentId != null && !documentId.isBlank()) {
            user.setDocumentId(documentId);
        }
        if (nationality != null && !nationality.isBlank()) {
            user.setNationality(nationality);
        }

        return userRepository.save(user);
    }

    public void deactivate(Long id) {
        UserEntity user = findById(id);
        user.setStatus(UserStatus.INACTIVE);
        user.setActive(false);
        userRepository.save(user);
    }

    private UserEntity createOrLinkLocalUser(Jwt jwt, String keycloakUserId) {
        String preferredUsername = claimAsString(jwt, "preferred_username");
        String fullName = claimAsString(jwt, "name");
        String email = claimAsString(jwt, "email");

        if (fullName == null) {
            fullName = preferredUsername != null ? preferredUsername : "Usuario";
        }

        if (email == null) {
            String localPart = preferredUsername != null ? preferredUsername : keycloakUserId;
            email = (localPart + "@local.keycloak").toLowerCase(Locale.ROOT);
        } else {
            email = email.toLowerCase(Locale.ROOT);
        }

        UserEntity existingByEmail = userRepository.findByEmail(email).orElse(null);
        if (existingByEmail != null) {
            if (existingByEmail.getKeycloakUserId() != null
                    && !existingByEmail.getKeycloakUserId().equals(keycloakUserId)) {
                throw new BusinessException("El correo ya esta vinculado a otro usuario de Keycloak");
            }

            existingByEmail.setKeycloakUserId(keycloakUserId);
            if (existingByEmail.getFullName() == null || existingByEmail.getFullName().isBlank()) {
                existingByEmail.setFullName(fullName);
            }
            return userRepository.save(existingByEmail);
        }

        UserEntity user = UserEntity.builder()
                .keycloakUserId(keycloakUserId)
                .fullName(fullName)
                .email(email)
                .phone(null)
                .documentId(buildPendingDocumentId(keycloakUserId))
                .nationality("PENDING")
                .role(UserRole.CLIENT)
                .status(UserStatus.ACTIVE)
                .active(true)
                .build();

        return userRepository.save(user);
    }

    private String buildPendingDocumentId(String keycloakUserId) {
        String normalized = keycloakUserId.replaceAll("[^a-zA-Z0-9]", "");
        if (normalized.isBlank()) {
            normalized = "USER";
        }

        String base = "PENDING-" + normalized.substring(0, Math.min(normalized.length(), 18));
        String candidate = base;
        int suffix = 1;

        while (userRepository.existsByDocumentId(candidate)) {
            candidate = base + "-" + suffix;
            suffix++;
        }

        return candidate;
    }

    private String claimAsString(Jwt jwt, String claim) {
        Object value = jwt.getClaims().get(claim);
        if (value instanceof String text && !text.isBlank()) {
            return text.trim();
        }
        return null;
    }
}
