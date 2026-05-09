package com.example.TravelAgency.Service;

import com.example.TravelAgency.Entity.PackageEntity;
import com.example.TravelAgency.Exceptions.BusinessException;
import com.example.TravelAgency.Exceptions.ResourceNotFoundException;
import com.example.TravelAgency.Repository.PackageRepository;
import com.example.TravelAgency.enums.PackageStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PackageServiceTest {

    @Mock
    private PackageRepository packageRepository;

    @InjectMocks
    private PackageService packageService;

    @Test
    void create_setsAvailableSlotsToTotalSlots_andSaves() {
        PackageEntity pkg = PackageEntity.builder()
                .name("P1")
                .destination("D")
                .description("Desc")
                .startDate(LocalDate.now().plusDays(10))
                .endDate(LocalDate.now().plusDays(12))
                .price(new BigDecimal("100"))
                .totalSlots(10)
                .servicesIncluded("Hotel")
                .conditions("Cond")
                .build();

        when(packageRepository.save(any(PackageEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        PackageEntity saved = packageService.create(pkg);

        assertThat(saved.getAvailableSlots()).isEqualTo(10);
        verify(packageRepository).save(pkg);
    }

    @Test
    void create_whenMissingName_throws() {
        PackageEntity pkg = PackageEntity.builder()
                .name(" ")
                .destination("D")
                .description("Desc")
                .startDate(LocalDate.now().plusDays(10))
                .endDate(LocalDate.now().plusDays(12))
                .price(new BigDecimal("100"))
                .totalSlots(10)
                .servicesIncluded("Hotel")
                .conditions("Cond")
                .build();

        assertThrows(BusinessException.class, () -> packageService.create(pkg));
        verifyNoInteractions(packageRepository);
    }

    @Test
    void findById_whenNotFound_throws() {
        when(packageRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> packageService.findById(99L));
    }

    @Test
    void decreaseSlots_whenReachesZero_setsSoldOut() {
        PackageEntity pkg = PackageEntity.builder()
                .id(1L)
                .availableSlots(2)
                .totalSlots(2)
                .status(PackageStatus.AVAILABLE)
                .build();
        when(packageRepository.save(any(PackageEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        packageService.decreaseSlots(pkg, 2);

        ArgumentCaptor<PackageEntity> captor = ArgumentCaptor.forClass(PackageEntity.class);
        verify(packageRepository).save(captor.capture());
        assertThat(captor.getValue().getAvailableSlots()).isZero();
        assertThat(captor.getValue().getStatus()).isEqualTo(PackageStatus.SOLD_OUT);
    }

    @Test
    void releaseSlots_whenSoldOut_setsAvailable() {
        PackageEntity pkg = PackageEntity.builder()
                .id(1L)
                .availableSlots(0)
                .totalSlots(2)
                .status(PackageStatus.SOLD_OUT)
                .build();
        when(packageRepository.save(any(PackageEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        packageService.releaseSlots(pkg, 1);

        verify(packageRepository).save(pkg);
        assertThat(pkg.getAvailableSlots()).isEqualTo(1);
        assertThat(pkg.getStatus()).isEqualTo(PackageStatus.AVAILABLE);
    }

    @Test
    void findAvailable_filtersOnlyPubliclyReservable() {
        LocalDate today = LocalDate.now();
        PackageEntity ok = PackageEntity.builder()
                .id(1L)
                .name("Ok")
                .startDate(today.plusDays(1))
                .endDate(today.plusDays(2))
                .availableSlots(1)
                .status(PackageStatus.AVAILABLE)
                .build();
        PackageEntity notOk = PackageEntity.builder()
                .id(2L)
                .name("NotOk")
                .startDate(today.minusDays(2))
                .endDate(today.minusDays(1))
                .availableSlots(3)
                .status(PackageStatus.AVAILABLE)
                .build();

        when(packageRepository.findPublicPackages()).thenReturn(List.of(ok, notOk));
        when(packageRepository.save(any(PackageEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        List<PackageEntity> result = packageService.findAvailable();

        assertThat(result).extracting(PackageEntity::getId).containsExactly(1L);
    }
}
