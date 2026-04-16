package com.example.TravelAgency.Repository;

import com.example.TravelAgency.Entity.PromotionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PromotionRepository extends JpaRepository<PromotionEntity, Long> {

    // Trae todas las promociones vigentes hoy
    @Query("""
        SELECT p FROM PromotionEntity p
        WHERE p.active = true
        AND :today BETWEEN p.validFrom AND p.validTo
    """)
    List<PromotionEntity> findActivePromotions(@Param("today") LocalDate today);
}
