package com.example.TravelAgency.Service;

import com.example.TravelAgency.Entity.PackageEntity;
import com.example.TravelAgency.Exceptions.BusinessException;
import com.example.TravelAgency.Exceptions.ResourceNotFoundException;
import com.example.TravelAgency.Repository.PackageRepository;
import com.example.TravelAgency.enums.PackageStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PackageService {

    private final PackageRepository packageRepository;

    public PackageEntity create(PackageEntity pkg) {
        validatePackage(pkg);
        pkg.setAvailableSlots(pkg.getTotalSlots());
        return packageRepository.save(pkg);
    }

    public PackageEntity findById(Long id) {
        PackageEntity pkg = packageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paquete no encontrado"));
        return syncStatus(pkg);
    }

    public List<PackageEntity> findAll() {
        return packageRepository.findAll().stream()
                .map(this::syncStatus)
                .toList();
    }

    public List<PackageEntity> findAvailable() {
        return packageRepository.findPublicPackages().stream()
                .map(this::syncStatus)
                .filter(this::isPubliclyReservable)
                .toList();
    }

    public List<PackageEntity> search(String destination, BigDecimal minPrice,
                                      BigDecimal maxPrice, LocalDate startDate,
                                      LocalDate endDate, String travelType,
                                      String season, Integer minDurationDays,
                                      Integer maxDurationDays) {
        return packageRepository.searchPackages(
                destination, minPrice, maxPrice, startDate, endDate,
                travelType, season, minDurationDays, maxDurationDays
        ).stream().map(this::syncStatus).filter(this::isPubliclyReservable).toList();
    }

    public PackageEntity update(Long id, PackageEntity updated) {
        PackageEntity existing = findById(id);

        int reservedSlots = existing.getTotalSlots() - existing.getAvailableSlots();
        if (reservedSlots > 0) {
            if (updated.getTotalSlots() > 0 && updated.getTotalSlots() < reservedSlots) {
                throw new BusinessException(
                        "No se pueden reducir los cupos por debajo de las reservas existentes (" + reservedSlots + ")"
                );
            }
            if (updated.getStartDate() != null && !updated.getStartDate().equals(existing.getStartDate())) {
                throw new BusinessException("No se puede modificar la fecha de inicio con reservas activas");
            }
            if (updated.getEndDate() != null && !updated.getEndDate().equals(existing.getEndDate())) {
                throw new BusinessException("No se puede modificar la fecha de termino con reservas activas");
            }
        }

        if (updated.getName() != null) {
            existing.setName(updated.getName());
        }
        if (updated.getDestination() != null) {
            existing.setDestination(updated.getDestination());
        }
        if (updated.getDescription() != null) {
            existing.setDescription(updated.getDescription());
        }
        if (updated.getStartDate() != null) {
            existing.setStartDate(updated.getStartDate());
        }
        if (updated.getEndDate() != null) {
            existing.setEndDate(updated.getEndDate());
        }
        if (updated.getPrice() != null) {
            existing.setPrice(updated.getPrice());
        }
        if (updated.getTotalSlots() > 0) {
            int diff = updated.getTotalSlots() - existing.getTotalSlots();
            existing.setTotalSlots(updated.getTotalSlots());
            existing.setAvailableSlots(existing.getAvailableSlots() + diff);
        }
        if (updated.getTravelType() != null) {
            existing.setTravelType(updated.getTravelType());
        }
        if (updated.getSeason() != null) {
            existing.setSeason(updated.getSeason());
        }
        if (updated.getServicesIncluded() != null) {
            existing.setServicesIncluded(updated.getServicesIncluded());
        }
        if (updated.getRestrictions() != null) {
            existing.setRestrictions(updated.getRestrictions());
        }
        if (updated.getConditions() != null) {
            existing.setConditions(updated.getConditions());
        }
        if (updated.getStatus() != null) {
            existing.setStatus(updated.getStatus());
        }

        validatePackage(existing);
        return syncStatus(packageRepository.save(existing));
    }

    public void changeStatus(Long id, PackageStatus newStatus) {
        PackageEntity pkg = findById(id);

        if (newStatus == PackageStatus.AVAILABLE && pkg.getAvailableSlots() == 0) {
            throw new BusinessException("No se puede publicar un paquete sin cupos disponibles");
        }

        pkg.setStatus(newStatus);
        packageRepository.save(pkg);
    }

    public boolean isPubliclyReservable(PackageEntity pkg) {
        LocalDate today = LocalDate.now();
        return pkg.getStatus() == PackageStatus.AVAILABLE
                && pkg.getAvailableSlots() > 0
                && pkg.getStartDate() != null
                && !pkg.getStartDate().isBefore(today)
                && pkg.getEndDate() != null
                && !pkg.getEndDate().isBefore(today);
    }

    public void delete(Long id) {
        PackageEntity pkg = findById(id);
        int reservedSlots = pkg.getTotalSlots() - pkg.getAvailableSlots();
        if (reservedSlots > 0) {
            throw new BusinessException(
                    "No se puede eliminar un paquete con reservas. Cambie su estado en su lugar."
            );
        }
        packageRepository.delete(pkg);
    }

    public void decreaseSlots(PackageEntity pkg, int quantity) {
        if (pkg.getAvailableSlots() < quantity) {
            throw new BusinessException("Cupos insuficientes");
        }
        pkg.setAvailableSlots(pkg.getAvailableSlots() - quantity);
        if (pkg.getAvailableSlots() == 0) {
            pkg.setStatus(PackageStatus.SOLD_OUT);
        }
        packageRepository.save(pkg);
    }

    public void releaseSlots(PackageEntity pkg, int quantity) {
        pkg.setAvailableSlots(pkg.getAvailableSlots() + quantity);
        if (pkg.getStatus() == PackageStatus.SOLD_OUT) {
            pkg.setStatus(PackageStatus.AVAILABLE);
        }
        packageRepository.save(pkg);
    }

    @Transactional
    public PackageEntity syncStatus(PackageEntity pkg) {
        PackageStatus computedStatus = calculateOperationalStatus(pkg);
        if (pkg.getStatus() != computedStatus) {
            pkg.setStatus(computedStatus);
            return packageRepository.save(pkg);
        }
        return pkg;
    }

    private PackageStatus calculateOperationalStatus(PackageEntity pkg) {
        if (pkg.getStatus() == PackageStatus.CANCELED) {
            return PackageStatus.CANCELED;
        }
        if (pkg.getEndDate() != null && !pkg.getEndDate().isAfter(LocalDate.now())) {
            return PackageStatus.EXPIRED;
        }
        if (pkg.getAvailableSlots() <= 0) {
            return PackageStatus.SOLD_OUT;
        }
        return PackageStatus.AVAILABLE;
    }

    private void validatePackage(PackageEntity pkg) {
        if (pkg.getName() == null || pkg.getName().isBlank()) {
            throw new BusinessException("El nombre del paquete es obligatorio");
        }
        if (pkg.getDestination() == null || pkg.getDestination().isBlank()) {
            throw new BusinessException("El destino es obligatorio");
        }
        if (pkg.getDescription() == null || pkg.getDescription().isBlank()) {
            throw new BusinessException("La descripcion del paquete es obligatoria");
        }
        if (pkg.getPrice() == null || pkg.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El precio debe ser mayor que cero");
        }
        if (pkg.getTotalSlots() <= 0) {
            throw new BusinessException("Los cupos deben ser mayores que cero");
        }
        if (pkg.getStartDate() == null || pkg.getEndDate() == null) {
            throw new BusinessException("Las fechas son obligatorias");
        }
        if (!pkg.getEndDate().isAfter(pkg.getStartDate())) {
            throw new BusinessException("La fecha de termino debe ser posterior a la de inicio");
        }
        if (pkg.getServicesIncluded() == null || pkg.getServicesIncluded().isBlank()) {
            throw new BusinessException("Los servicios incluidos son obligatorios");
        }
        if (pkg.getConditions() == null || pkg.getConditions().isBlank()) {
            throw new BusinessException("Las condiciones del paquete son obligatorias");
        }
    }
}
