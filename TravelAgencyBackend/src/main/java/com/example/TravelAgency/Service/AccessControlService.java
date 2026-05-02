package com.example.TravelAgency.Service;

import com.example.TravelAgency.Entity.UserEntity;
import com.example.TravelAgency.Exceptions.BusinessException;
import com.example.TravelAgency.enums.UserRole;
import com.example.TravelAgency.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccessControlService {

    private final UserService userService;

    public UserEntity requireActiveUser(Long userId) {
        // La identidad del actor se obtiene desde JWT en los controladores y
        // aqui se centraliza la validacion del estado activo del usuario local.
        UserEntity user = userService.findById(userId);
        if (!user.isActive() || user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException("La cuenta del usuario no esta activa");
        }
        return user;
    }

    public UserEntity requireAdmin(Long userId) {
        // Centraliza la regla de “admin-only” para que controladores/servicios no repitan lógica.
        UserEntity user = requireActiveUser(userId);
        if (user.getRole() != UserRole.ADMIN) {
            throw new BusinessException("Se requiere rol administrador para esta operacion");
        }
        return user;
    }

    public UserEntity requireSameUserOrAdmin(Long actorUserId, Long ownerUserId) {
        // Se usa cuando existe un “dueño” del recurso (ownerUserId) y un “actor” que solicita la acción.
        // Permite acceso si el actor es ADMIN o si actor == dueño.
        UserEntity actor = requireActiveUser(actorUserId);
        if (actor.getRole() == UserRole.ADMIN || actor.getId().equals(ownerUserId)) {
            return actor;
        }
        throw new BusinessException("No tiene permisos para acceder a este recurso");
    }
}
