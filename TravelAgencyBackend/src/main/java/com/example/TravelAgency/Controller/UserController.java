package com.example.TravelAgency.Controller;

import com.example.TravelAgency.Entity.UserEntity;
import com.example.TravelAgency.Exceptions.BusinessException;
import com.example.TravelAgency.Service.UserService;
import com.example.TravelAgency.dto.request.LoginRequest;
import com.example.TravelAgency.dto.request.RegisterRequest;
import com.example.TravelAgency.dto.response.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest req) {
        UserEntity user = userService.register(
                req.getFullName(),
                req.getEmail(),
                req.getPassword(),
                req.getPhone(),
                req.getDocumentId(),
                req.getNationality()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest req) {
        UserEntity user = userService.findByEmail(req.getEmail());

        if (!userService.isAccountActive(user)) {
            throw new BusinessException("Cuenta bloqueada o inactiva");
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            userService.registerFailedAttempt(req.getEmail());
            throw new BusinessException("Credenciales invalidas");
        }

        userService.resetFailedAttempts(req.getEmail());
        return ResponseEntity.ok(Map.of(
                "message", "Login exitoso",
                "user", UserResponse.from(user)
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(UserResponse.from(userService.findById(id)));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> findAll() {
        return ResponseEntity.ok(userService.findAll().stream().map(UserResponse::from).toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable Long id,
                                               @RequestBody RegisterRequest req) {
        UserEntity updated = userService.updateProfile(
                id,
                req.getFullName(),
                req.getPhone(),
                req.getDocumentId(),
                req.getNationality()
        );
        return ResponseEntity.ok(UserResponse.from(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deactivate(@PathVariable Long id) {
        userService.deactivate(id);
        return ResponseEntity.ok(Map.of("message", "Usuario desactivado correctamente"));
    }
}
