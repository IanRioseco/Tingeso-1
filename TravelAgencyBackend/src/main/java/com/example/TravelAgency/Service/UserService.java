package com.example.TravelAgency.Service;

import com.example.TravelAgency.Entity.UserEntity;
import com.example.TravelAgency.Exceptions.BusinessException;
import com.example.TravelAgency.Exceptions.ResourceNotFoundException;
import com.example.TravelAgency.Repository.UserRepository;
import com.example.TravelAgency.enums.UserRole;
import com.example.TravelAgency.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_MINUTES = 30;

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserEntity register(String fullName, String email, String password, String phone,
                               String documentId, String nationality) {
        if (fullName == null || fullName.isBlank()) {
            throw new BusinessException("El nombre completo es obligatorio");
        }
        if (email == null || !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            throw new BusinessException("Formato de correo invalido");
        }
        if (password == null || password.length() < 8) {
            throw new BusinessException("La contrasena debe tener al menos 8 caracteres");
        }
        if (documentId == null || documentId.isBlank()) {
            throw new BusinessException("El documento de identidad es obligatorio");
        }
        if (nationality == null || nationality.isBlank()) {
            throw new BusinessException("La nacionalidad es obligatoria");
        }
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("El correo ya esta registrado");
        }

        UserEntity user = UserEntity.builder()
                .fullName(fullName)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .phone(phone)
                .documentId(documentId)
                .nationality(nationality)
                .role(UserRole.CLIENT)
                .status(UserStatus.ACTIVE)
                .active(true)
                .build();

        return userRepository.save(user);
    }

    public UserEntity findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    public UserEntity findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
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

    public void registerFailedAttempt(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setFailedAttempts(user.getFailedAttempts() + 1);
            if (user.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
                user.setStatus(UserStatus.LOCKED);
                user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_MINUTES));
            }
            userRepository.save(user);
        });
    }

    public void resetFailedAttempts(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setFailedAttempts(0);
            user.setLockedUntil(null);
            if (user.getStatus() == UserStatus.LOCKED) {
                user.setStatus(UserStatus.ACTIVE);
                user.setActive(true);
            }
            userRepository.save(user);
        });
    }

    public boolean isAccountActive(UserEntity user) {
        if (user.getStatus() == UserStatus.LOCKED) {
            if (user.getLockedUntil() != null && LocalDateTime.now().isAfter(user.getLockedUntil())) {
                resetFailedAttempts(user.getEmail());
                return true;
            }
            return false;
        }
        return user.getStatus() == UserStatus.ACTIVE;
    }
}
