package com.example.TravelAgency.Service;

import com.example.TravelAgency.Entity.UserEntity;
import com.example.TravelAgency.Exceptions.BusinessException;
import com.example.TravelAgency.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccessControlService {

    private final UserService userService;

    public UserEntity requireActiveUser(Long userId) {
        UserEntity user = userService.findById(userId);
        if (!userService.isAccountActive(user)) {
            throw new BusinessException("La cuenta del usuario no esta activa");
        }
        return user;
    }

    public UserEntity requireAdmin(Long userId) {
        UserEntity user = requireActiveUser(userId);
        if (user.getRole() != UserRole.ADMIN) {
            throw new BusinessException("Se requiere rol administrador para esta operacion");
        }
        return user;
    }

    public UserEntity requireSameUserOrAdmin(Long actorUserId, Long ownerUserId) {
        UserEntity actor = requireActiveUser(actorUserId);
        if (actor.getRole() == UserRole.ADMIN || actor.getId().equals(ownerUserId)) {
            return actor;
        }
        throw new BusinessException("No tiene permisos para acceder a este recurso");
    }
}
