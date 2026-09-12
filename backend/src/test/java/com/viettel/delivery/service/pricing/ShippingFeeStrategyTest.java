package com.viettel.delivery.service.pricing;

import com.viettel.delivery.constant.enums.ServiceType;
import com.viettel.delivery.entity.PricingRule;
import com.viettel.delivery.model.FeeBreakdown;
import com.viettel.delivery.model.FeeCalculationContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ShippingFeeStrategyTest {

    private static final LocalDateTime MORNING = LocalDateTime.of(2026, 9, 11, 9, 0);
    private static final LocalDateTime AFTERNOON = LocalDateTime.of(2026, 9, 11, 16, 0);

    private PricingRule standardRule() {
        return PricingRule.builder()
                .serviceType(ServiceType.STANDARD)
                .baseFee(new BigDecimal("15000"))
                .baseDistanceKm(new BigDecimal("3"))
                .perKmFee(new BigDecimal("4000"))
                .baseWeightKg(new BigDecimal("3"))
                .perKgFee(new BigDecimal("5000"))
                .remoteAreaSurcharge(new BigDecimal("0"))
                .codFeePercent(new BigDecimal("1.00"))
                .minFee(new BigDecimal("15000"))
                .active(Boolean.TRUE)
                .build();
    }

    private PricingRule expressRule() {
        PricingRule rule = standardRule();
        rule.setServiceType(ServiceType.EXPRESS);
        rule.setBaseFee(new BigDecimal("25000"));
        rule.setPerKmFee(new BigDecimal("6000"));
        rule.setRemoteAreaSurcharge(new BigDecimal("10000"));
        rule.setMinFee(new BigDecimal("25000"));
        return rule;
    }

    private PricingRule sameDayRule() {
        PricingRule rule = standardRule();
        rule.setServiceType(ServiceType.SAME_DAY);
        rule.setBaseFee(new BigDecimal("40000"));
        rule.setPerKmFee(new BigDecimal("8000"));
        rule.setMinFee(new BigDecimal("40000"));
        return rule;
    }

    private FeeCalculationContext context(String distanceKm, String weightKg, String codAmount,
                                          boolean fragile, boolean remoteArea, LocalDateTime orderTime) {
        return new FeeCalculationContext(new BigDecimal(distanceKm), new BigDecimal(weightKg),
                new BigDecimal(codAmount), fragile, remoteArea, orderTime);
    }

    @Test
    @DisplayName("Trong pham vi co ban thi chi tinh phi co ban")
    void shouldChargeOnlyBaseFeeWhenWithinBaseRange() {
        FeeBreakdown result = new StandardShippingFeeStrategy()
                .calculate(context("2", "2", "0", false, false, MORNING), standardRule());

        assertThat(result.distanceFee()).isEqualByComparingTo("0");
        assertThat(result.weightFee()).isEqualByComparingTo("0");
        assertThat(result.shippingFee()).isEqualByComparingTo("15000");
    }

    @Test
    @DisplayName("Vuot quang duong va khoi luong thi cong phi theo don gia")
    void shouldChargeExceededDistanceAndWeight() {
        // Vuot 5km x 4000 = 20000, vuot 2kg x 5000 = 10000, phi co ban 15000
        FeeBreakdown result = new StandardShippingFeeStrategy()
                .calculate(context("8", "5", "0", false, false, MORNING), standardRule());

        assertThat(result.distanceFee()).isEqualByComparingTo("20000");
        assertThat(result.weightFee()).isEqualByComparingTo("10000");
        assertThat(result.shippingFee()).isEqualByComparingTo("45000");
    }

    @Test
    @DisplayName("Khoi luong vuot le duoc lam tron len tron kg")
    void shouldRoundUpExceededWeight() {
        // Vuot 1.2kg duoc tinh thanh 2kg x 5000 = 10000
        FeeBreakdown result = new StandardShippingFeeStrategy()
                .calculate(context("3", "4.2", "0", false, false, MORNING), standardRule());

        assertThat(result.weightFee()).isEqualByComparingTo("10000");
    }

    @Test
    @DisplayName("Co tien thu ho thi cong phi COD theo ti le cau hinh")
    void shouldChargeCodFee() {
        // 1% cua 500000 = 5000
        FeeBreakdown result = new StandardShippingFeeStrategy()
                .calculate(context("3", "3", "500000", false, false, MORNING), standardRule());

        assertThat(result.codFee()).isEqualByComparingTo("5000");
        assertThat(result.shippingFee()).isEqualByComparingTo("20000");
    }

    @Test
    @DisplayName("Hang de vo bi cong 10% phi co ban")
    void shouldChargeFragileSurcharge() {
        FeeBreakdown result = new StandardShippingFeeStrategy()
                .calculate(context("3", "3", "0", true, false, MORNING), standardRule());

        assertThat(result.surcharge()).isEqualByComparingTo("1500");
    }

    @Test
    @DisplayName("Tong phi khong duoc thap hon phi toi thieu")
    void shouldApplyMinimumFee() {
        PricingRule rule = standardRule();
        rule.setBaseFee(new BigDecimal("5000"));
        rule.setMinFee(new BigDecimal("15000"));

        FeeBreakdown result = new StandardShippingFeeStrategy()
                .calculate(context("1", "1", "0", false, false, MORNING), rule);

        assertThat(result.shippingFee()).isEqualByComparingTo("15000");
    }

    @Test
    @DisplayName("Giao nhanh cong them 15% phi quang duong va phu phi vung xa")
    void shouldAddExpressPrioritySurcharge() {
        // Vuot 5km x 6000 = 30000 -> phu phi uu tien 15% = 4500, vung xa 10000
        FeeBreakdown result = new ExpressShippingFeeStrategy()
                .calculate(context("8", "3", "0", false, true, MORNING), expressRule());

        assertThat(result.distanceFee()).isEqualByComparingTo("30000");
        assertThat(result.surcharge()).isEqualByComparingTo("14500");
        assertThat(result.shippingFee()).isEqualByComparingTo("69500");
    }

    @Test
    @DisplayName("Giao trong ngay dat truoc 15h khong co phu phi gio cao diem")
    void shouldNotAddPeakSurchargeBeforeThreshold() {
        FeeBreakdown result = new SameDayShippingFeeStrategy()
                .calculate(context("5", "2", "0", false, false, MORNING), sameDayRule());

        assertThat(result.surcharge()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("Giao trong ngay dat sau 15h bi cong 20% phu phi gio cao diem")
    void shouldAddPeakSurchargeAfterThreshold() {
        // Phi co ban 40000 + phi quang duong 2km x 8000 = 16000 -> 20% cua 56000 = 11200
        FeeBreakdown result = new SameDayShippingFeeStrategy()
                .calculate(context("5", "2", "0", false, false, AFTERNOON), sameDayRule());

        assertThat(result.surcharge()).isEqualByComparingTo("11200");
        assertThat(result.shippingFee()).isEqualByComparingTo("67200");
    }

    @Test
    @DisplayName("Moi chien luoc khai bao dung loai dich vu cua minh")
    void shouldExposeCorrectServiceType() {
        assertThat(new StandardShippingFeeStrategy().getServiceType()).isEqualTo(ServiceType.STANDARD);
        assertThat(new ExpressShippingFeeStrategy().getServiceType()).isEqualTo(ServiceType.EXPRESS);
        assertThat(new SameDayShippingFeeStrategy().getServiceType()).isEqualTo(ServiceType.SAME_DAY);
    }
}
