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

    @Test
    void update_whenReservedSlotsAndReduceTotalBelowReserved_throws() {
        PackageEntity existing = PackageEntity.builder()
                .id(1L)
                .name("P1")
                .destination("D")
                .description("Desc")
                .startDate(LocalDate.now().plusDays(10))
                .endDate(LocalDate.now().plusDays(12))
                .price(new BigDecimal("100"))
                .totalSlots(10)
                .availableSlots(6) // reserved = 4
                .servicesIncluded("Hotel")
                .conditions("Cond")
                .status(PackageStatus.AVAILABLE)
                .build();
        when(packageRepository.findById(1L)).thenReturn(Optional.of(existing));

        PackageEntity updated = PackageEntity.builder().totalSlots(3).build(); // < reserved

        assertThatThrownBy(() -> packageService.update(1L, updated))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("reservas existentes");
        verify(packageRepository, never()).save(any());
    }

    @Test
    void update_whenReservedSlotsAndChangeStartDate_throws() {
        PackageEntity existing = PackageEntity.builder()
                .id(1L)
                .name("P1")
                .destination("D")
                .description("Desc")
                .startDate(LocalDate.now().plusDays(10))
                .endDate(LocalDate.now().plusDays(12))
                .price(new BigDecimal("100"))
                .totalSlots(10)
                .availableSlots(9) // reserved = 1
                .servicesIncluded("Hotel")
                .conditions("Cond")
                .status(PackageStatus.AVAILABLE)
                .build();
        when(packageRepository.findById(1L)).thenReturn(Optional.of(existing));

        PackageEntity updated = PackageEntity.builder()
                .startDate(existing.getStartDate().plusDays(1))
                .build();

        assertThatThrownBy(() -> packageService.update(1L, updated))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("fecha de inicio");
        verify(packageRepository, never()).save(any());
    }

    @Test
    void update_updatesFields_andAdjustsAvailableSlots_whenTotalSlotsChanges() {
        PackageEntity existing = PackageEntity.builder()
                .id(1L)
                .name("P1")
                .destination("D")
                .description("Desc")
                .startDate(LocalDate.now().plusDays(10))
                .endDate(LocalDate.now().plusDays(12))
                .price(new BigDecimal("100"))
                .totalSlots(10)
                .availableSlots(7) // reserved = 3
                .servicesIncluded("Hotel")
                .conditions("Cond")
                .status(PackageStatus.AVAILABLE)
                .build();
        when(packageRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(packageRepository.save(any(PackageEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        PackageEntity updated = PackageEntity.builder()
                .name("P2")
                .totalSlots(12) // diff +2 => available 9
                .build();

        PackageEntity saved = packageService.update(1L, updated);

        assertThat(saved.getName()).isEqualTo("P2");
        assertThat(saved.getTotalSlots()).isEqualTo(12);
        assertThat(saved.getAvailableSlots()).isEqualTo(9);
        verify(packageRepository).save(existing);
    }

    @Test
    void changeStatus_whenPublishingWithoutSlots_throws() {
        PackageEntity existing = PackageEntity.builder()
                .id(1L)
                .name("P1")
                .destination("D")
                .description("Desc")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .price(new BigDecimal("100"))
                .totalSlots(10)
                .availableSlots(0)
                .servicesIncluded("Hotel")
                .conditions("Cond")
                .status(PackageStatus.SOLD_OUT)
                .build();
        when(packageRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> packageService.changeStatus(1L, PackageStatus.AVAILABLE))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("sin cupos");
        verify(packageRepository, never()).save(any());
    }

    @Test
    void delete_whenHasReservations_throws() {
        PackageEntity existing = PackageEntity.builder()
                .id(1L)
                .name("P1")
                .destination("D")
                .description("Desc")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .price(new BigDecimal("100"))
                .totalSlots(10)
                .availableSlots(9) // reserved 1
                .servicesIncluded("Hotel")
                .conditions("Cond")
                .status(PackageStatus.AVAILABLE)
                .build();
        when(packageRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> packageService.delete(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("No se puede eliminar");
        verify(packageRepository, never()).delete(any());
    }

    @Test
    void syncStatus_whenComputedDifferent_savesAndReturnsUpdated() {
        PackageEntity existing = PackageEntity.builder()
                .id(1L)
                .status(PackageStatus.AVAILABLE)
                .availableSlots(0)
                .totalSlots(10)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .build();
        when(packageRepository.save(any(PackageEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        PackageEntity result = packageService.syncStatus(existing);

        assertThat(result.getStatus()).isEqualTo(PackageStatus.SOLD_OUT);
        verify(packageRepository).save(existing);
    }
}
