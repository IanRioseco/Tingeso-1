package com.example.TravelAgency.Repository;

import com.example.TravelAgency.Entity.UserEntity;
import com.example.TravelAgency.enums.UserRole;
import com.example.TravelAgency.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    List<UserEntity> findByStatus(UserStatus status);

    List<UserEntity> findByRole(UserRole role);

    List<UserEntity> findByRoleAndStatus(UserRole role, UserStatus status);

    @Query("""
        SELECT u FROM UserEntity u
        WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :name, '%'))
    """)
    List<UserEntity> findByFullNameContaining(@Param("name") String name);

    @Query("""
        SELECT u FROM UserEntity u
        WHERE u.status = 'LOCKED'
        AND u.lockedUntil IS NOT NULL
        AND u.lockedUntil <= :now
    """)
    List<UserEntity> findExpiredLocks(@Param("now") LocalDateTime now);

    // Depende de una entidad Booking que todavia no existe en el proyecto.
    default List<UserEntity> findFrequentClients(int minBookings) {
        return List.of();
    }

    // Depende de una entidad Booking que todavia no existe en el proyecto.
    default boolean isFrequentClient(Long userId, int minBookings) {
        return false;
    }

    @Query("""
        SELECT u FROM UserEntity u
        WHERE u.createdAt BETWEEN :from AND :to
        ORDER BY u.createdAt DESC
    """)
    List<UserEntity> findRegisteredBetween(@Param("from") LocalDateTime from,
                                           @Param("to") LocalDateTime to);

    long countByRole(UserRole role);

    long countByStatus(UserStatus status);
}
