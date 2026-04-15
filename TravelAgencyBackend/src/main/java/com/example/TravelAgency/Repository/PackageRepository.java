package com.example.TravelAgency.Repository;

import com.example.TravelAgency.Entity.PackageEntity;
import com.example.TravelAgency.enums.PackageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PackageRepository extends JpaRepository<PackageEntity, Long> {

    List<PackageEntity> findByStatus(PackageStatus status);

    @Query("""
        SELECT p FROM PackageEntity p
        WHERE p.status = 'AVAILABLE'
        AND (:destination IS NULL OR LOWER(p.destination) LIKE LOWER(CONCAT('%', :destination, '%')))
        AND (:minPrice IS NULL OR p.price >= :minPrice)
        AND (:maxPrice IS NULL OR p.price <= :maxPrice)
        AND (:startDate IS NULL OR p.startDate >= :startDate)
        AND (:travelType IS NULL OR p.travelType = :travelType)
        AND p.endDate >= CURRENT_DATE
    """)
    List<PackageEntity> searchPackages(
            @Param("destination") String destination,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("startDate") LocalDate startDate,
            @Param("travelType") String travelType
    );
}
