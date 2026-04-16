package com.example.TravelAgency.Service;

import com.example.TravelAgency.Entity.PromotionEntity;
import com.example.TravelAgency.Entity.UserEntity;
import com.example.TravelAgency.Exceptions.BusinessException;
import com.example.TravelAgency.Repository.BookingRepository;
import com.example.TravelAgency.Repository.PromotionRepository;
import com.example.TravelAgency.config.TravelProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private final BookingRepository bookingRepository;
    private final PromotionRepository promotionRepository;
    private final TravelProperties travelProperties;

    public record DiscountResult(
            BigDecimal discountAmount,
            BigDecimal finalAmount,
            String discountDetail
    ) {}

    public List<PromotionEntity> findActivePromotions(LocalDate today) {
        return promotionRepository.findActivePromotions(today);
    }

    public List<PromotionEntity> findAll() {
        return promotionRepository.findAll().stream()
                .sorted((left, right) -> right.getValidFrom().compareTo(left.getValidFrom()))
                .toList();
    }

    public PromotionEntity create(PromotionEntity promotion) {
        validatePromotion(promotion);
        return promotionRepository.save(promotion);
    }

    public PromotionEntity update(Long id, PromotionEntity updated) {
        PromotionEntity existing = promotionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Promocion no encontrada"));

        existing.setName(updated.getName());
        existing.setDiscountPct(updated.getDiscountPct());
        existing.setValidFrom(updated.getValidFrom());
        existing.setValidTo(updated.getValidTo());
        existing.setActive(updated.isActive());

        validatePromotion(existing);
        return promotionRepository.save(existing);
    }

    public PromotionEntity changeStatus(Long id, boolean active) {
        PromotionEntity promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Promocion no encontrada"));
        promotion.setActive(active);
        return promotionRepository.save(promotion);
    }

    public DiscountResult calculate(BigDecimal baseAmount, int passengers,
                                    UserEntity user, String sessionId) {
        TravelProperties.Discount d = travelProperties.getDiscount();
        List<String> appliedDiscounts = new ArrayList<>();
        BigDecimal totalDiscountPct = BigDecimal.ZERO;

        if (passengers >= d.getGroupMinPassengers()) {
            totalDiscountPct = totalDiscountPct.add(d.getGroupPercent());
            appliedDiscounts.add("Descuento por grupo (" + d.getGroupPercent() + "%)");
        }

        long confirmedBookings = bookingRepository.countConfirmedByUser(user);
        if (confirmedBookings >= d.getFrequentMinPaidBookings()) {
            totalDiscountPct = totalDiscountPct.add(d.getFrequentPercent());
            appliedDiscounts.add("Cliente frecuente (" + d.getFrequentPercent() + "%)");
        }

        boolean multiBySession = sessionId != null && !sessionId.isBlank()
                && bookingRepository.findBySessionId(sessionId).size() >= 1;
        LocalDateTime since = LocalDateTime.now().minusDays(d.getMultiPackageLookbackDays());
        boolean multiByPeriod = bookingRepository.countActiveBookingsSince(user, since) >= 1;
        if (multiBySession || multiByPeriod) {
            totalDiscountPct = totalDiscountPct.add(d.getMultiPackagePercent());
            String origin = multiBySession && multiByPeriod
                    ? "misma sesion y periodo reciente"
                    : multiBySession ? "misma sesion de compra" : "varios paquetes en periodo reciente";
            appliedDiscounts.add("Compra multiple (" + d.getMultiPackagePercent() + "%) — " + origin);
        }

        List<PromotionEntity> activePromotions = promotionRepository.findActivePromotions(LocalDate.now());
        for (PromotionEntity promotion : activePromotions) {
            totalDiscountPct = totalDiscountPct.add(promotion.getDiscountPct());
            appliedDiscounts.add("Promocion: " + promotion.getName() + " (" + promotion.getDiscountPct() + "%)");
        }

        BigDecimal rawPct = totalDiscountPct;
        BigDecimal maxPct = d.getMaxTotalPercent();
        if (totalDiscountPct.compareTo(maxPct) > 0) {
            totalDiscountPct = maxPct;
        }

        BigDecimal discountAmount = baseAmount
                .multiply(totalDiscountPct)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        BigDecimal finalAmount = baseAmount.subtract(discountAmount);
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
        }

        String detail = appliedDiscounts.isEmpty()
                ? "Sin descuentos aplicados"
                : String.join(", ", appliedDiscounts);
        if (rawPct.compareTo(maxPct) > 0) {
            detail = detail + " [tope acumulado " + maxPct + "% aplicado]";
        }

        return new DiscountResult(discountAmount, finalAmount, detail);
    }

    private void validatePromotion(PromotionEntity promotion) {
        if (promotion.getName() == null || promotion.getName().isBlank()) {
            throw new BusinessException("El nombre de la promocion es obligatorio");
        }
        if (promotion.getDiscountPct() == null
                || promotion.getDiscountPct().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El descuento debe ser mayor que cero");
        }
        if (promotion.getValidFrom() == null || promotion.getValidTo() == null) {
            throw new BusinessException("La vigencia de la promocion es obligatoria");
        }
        if (promotion.getValidTo().isBefore(promotion.getValidFrom())) {
            throw new BusinessException("La fecha de termino de la promocion debe ser posterior o igual a la de inicio");
        }
    }
}
