package com.example.TravelAgency.Controller;

import com.example.TravelAgency.Entity.UserEntity;
import com.example.TravelAgency.Service.UserService;
import com.example.TravelAgency.dto.request.UpdateUserProfileRequest;
import com.example.TravelAgency.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal Jwt jwt) {
        UserEntity currentUser = userService.getOrCreateFromJwt(jwt);
        return ResponseEntity.ok(UserResponse.from(currentUser));
    }

    @PutMapping("/me")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<UserResponse> updateMe(@AuthenticationPrincipal Jwt jwt,
                                                 @RequestBody UpdateUserProfileRequest req) {
        UserEntity currentUser = userService.getOrCreateFromJwt(jwt);
        UserEntity updated = userService.updateProfile(
                currentUser.getId(),
                req.getFullName(),
                req.getPhone(),
                req.getDocumentId(),
                req.getNationality()
        );
        return ResponseEntity.ok(UserResponse.from(updated));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(UserResponse.from(userService.findById(id)));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> findAll() {
        return ResponseEntity.ok(userService.findAll().stream().map(UserResponse::from).toList());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> update(@PathVariable Long id,
                               @RequestBody UpdateUserProfileRequest req) {
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deactivate(@PathVariable Long id) {
        userService.deactivate(id);
        return ResponseEntity.ok(Map.of("message", "Usuario desactivado correctamente"));
    }
}
