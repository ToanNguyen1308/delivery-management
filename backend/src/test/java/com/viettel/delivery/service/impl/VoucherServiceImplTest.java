package com.viettel.delivery.service.impl;

import com.viettel.delivery.constant.ErrorCode;
import com.viettel.delivery.constant.enums.DiscountType;
import com.viettel.delivery.constant.enums.VoucherStatus;
import com.viettel.delivery.entity.Voucher;
import com.viettel.delivery.mapper.VoucherMapper;
import com.viettel.delivery.model.VoucherDiscountResult;
import com.viettel.delivery.repository.UserRepository;
import com.viettel.delivery.repository.VoucherRepository;
import com.viettel.delivery.repository.VoucherUsageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VoucherServiceImplTest {

    private static final Long USER_ID = 10L;
    private static final BigDecimal SHIPPING_FEE = new BigDecimal("50000");

    @Mock
    private VoucherRepository voucherRepository;

    @Mock
    private VoucherUsageRepository voucherUsageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VoucherMapper voucherMapper;

    @InjectMocks
    private VoucherServiceImpl voucherService;

    private Voucher buildVoucher(DiscountType type, String value, String maxDiscount) {
        return Voucher.builder()
                .id(1L)
                .code("TEST")
                .name("Voucher kiem thu")
                .discountType(type)
                .discountValue(new BigDecimal(value))
                .minOrderAmount(BigDecimal.ZERO)
                .maxDiscountAmount(maxDiscount == null ? null : new BigDecimal(maxDiscount))
                .quantity(100)
                .usedCount(0)
                .usageLimitPerUser(1)
                .validFrom(LocalDateTime.now().minusDays(1))
                .validTo(LocalDateTime.now().plusDays(1))
                .status(VoucherStatus.ACTIVE)
                .build();
    }

    private void stubVoucher(Voucher voucher) {
        when(voucherRepository.findByCodeAndIsDeletedFalse("TEST")).thenReturn(Optional.of(voucher));
        when(voucherUsageRepository.countByVoucherAndUser(anyLong(), anyLong())).thenReturn(0L);
    }

    @Test
    @DisplayName("Khong nhap ma giam gia thi khong ap dung giam")
    void shouldReturnNoneWhenCodeIsBlank() {
        VoucherDiscountResult result = voucherService.evaluate("  ", SHIPPING_FEE, USER_ID);

        assertThat(result.applied()).isFalse();
        assertThat(result.discountAmount()).isEqualByComparingTo("0");
        assertThat(result.messageCode()).isNull();
    }

    @Test
    @DisplayName("Ma giam gia khong ton tai thi tra ve ly do tuong ung")
    void shouldRejectUnknownCode() {
        when(voucherRepository.findByCodeAndIsDeletedFalse("KHONGCO")).thenReturn(Optional.empty());

        VoucherDiscountResult result = voucherService.evaluate("khongco", SHIPPING_FEE, USER_ID);

        assertThat(result.applied()).isFalse();
        assertThat(result.messageCode()).isEqualTo(ErrorCode.VOUCHER_NOT_FOUND);
    }

    @Test
    @DisplayName("Giam theo phan tram bi gioi han boi muc giam toi da")
    void shouldCapPercentDiscountByMaxAmount() {
        // 30% cua 50000 = 15000 nhung muc giam toi da chi 10000
        stubVoucher(buildVoucher(DiscountType.PERCENT, "30", "10000"));

        VoucherDiscountResult result = voucherService.evaluate("TEST", SHIPPING_FEE, USER_ID);

        assertThat(result.applied()).isTrue();
        assertThat(result.discountAmount()).isEqualByComparingTo("10000");
    }

    @Test
    @DisplayName("Giam theo phan tram khi chua vuot muc toi da")
    void shouldCalculatePercentDiscount() {
        stubVoucher(buildVoucher(DiscountType.PERCENT, "20", "30000"));

        VoucherDiscountResult result = voucherService.evaluate("TEST", SHIPPING_FEE, USER_ID);

        assertThat(result.discountAmount()).isEqualByComparingTo("10000");
    }

    @Test
    @DisplayName("Mien phi van chuyen thi giam dung bang cuoc")
    void shouldDiscountFullShippingFeeForFreeShip() {
        stubVoucher(buildVoucher(DiscountType.FREE_SHIP, "0", null));

        VoucherDiscountResult result = voucherService.evaluate("TEST", SHIPPING_FEE, USER_ID);

        assertThat(result.discountAmount()).isEqualByComparingTo(SHIPPING_FEE);
    }

    @Test
    @DisplayName("So tien giam khong duoc vuot qua cuoc van chuyen")
    void shouldNotDiscountMoreThanShippingFee() {
        stubVoucher(buildVoucher(DiscountType.FIXED, "80000", null));

        VoucherDiscountResult result = voucherService.evaluate("TEST", SHIPPING_FEE, USER_ID);

        assertThat(result.discountAmount()).isEqualByComparingTo(SHIPPING_FEE);
    }

    @Test
    @DisplayName("Voucher het han thi bi tu choi")
    void shouldRejectExpiredVoucher() {
        Voucher voucher = buildVoucher(DiscountType.FIXED, "10000", null);
        voucher.setValidTo(LocalDateTime.now().minusHours(1));
        stubVoucher(voucher);

        VoucherDiscountResult result = voucherService.evaluate("TEST", SHIPPING_FEE, USER_ID);

        assertThat(result.applied()).isFalse();
        assertThat(result.messageCode()).isEqualTo(ErrorCode.VOUCHER_EXPIRED);
    }

    @Test
    @DisplayName("Voucher chua den thoi gian su dung thi bi tu choi")
    void shouldRejectNotStartedVoucher() {
        Voucher voucher = buildVoucher(DiscountType.FIXED, "10000", null);
        voucher.setValidFrom(LocalDateTime.now().plusDays(1));
        stubVoucher(voucher);

        VoucherDiscountResult result = voucherService.evaluate("TEST", SHIPPING_FEE, USER_ID);

        assertThat(result.messageCode()).isEqualTo(ErrorCode.VOUCHER_NOT_STARTED);
    }

    @Test
    @DisplayName("Voucher het luot su dung thi bi tu choi")
    void shouldRejectWhenQuantityExhausted() {
        Voucher voucher = buildVoucher(DiscountType.FIXED, "10000", null);
        voucher.setUsedCount(100);
        stubVoucher(voucher);

        VoucherDiscountResult result = voucherService.evaluate("TEST", SHIPPING_FEE, USER_ID);

        assertThat(result.messageCode()).isEqualTo(ErrorCode.VOUCHER_OUT_OF_QUANTITY);
    }

    @Test
    @DisplayName("Don chua dat gia tri toi thieu thi bi tu choi")
    void shouldRejectWhenMinOrderNotMet() {
        Voucher voucher = buildVoucher(DiscountType.FIXED, "10000", null);
        voucher.setMinOrderAmount(new BigDecimal("100000"));
        stubVoucher(voucher);

        VoucherDiscountResult result = voucherService.evaluate("TEST", SHIPPING_FEE, USER_ID);

        assertThat(result.messageCode()).isEqualTo(ErrorCode.VOUCHER_MIN_ORDER_NOT_MET);
    }

    @Test
    @DisplayName("Nguoi dung da dung het gioi han cua minh thi bi tu choi")
    void shouldRejectWhenUserReachedUsageLimit() {
        Voucher voucher = buildVoucher(DiscountType.FIXED, "10000", null);
        when(voucherRepository.findByCodeAndIsDeletedFalse("TEST")).thenReturn(Optional.of(voucher));
        when(voucherUsageRepository.countByVoucherAndUser(anyLong(), anyLong())).thenReturn(1L);

        VoucherDiscountResult result = voucherService.evaluate("TEST", SHIPPING_FEE, USER_ID);

        assertThat(result.messageCode()).isEqualTo(ErrorCode.VOUCHER_ALREADY_USED);
    }

    @Test
    @DisplayName("Voucher dang tam ngung thi bi tu choi")
    void shouldRejectInactiveVoucher() {
        Voucher voucher = buildVoucher(DiscountType.FIXED, "10000", null);
        voucher.setStatus(VoucherStatus.INACTIVE);
        stubVoucher(voucher);

        VoucherDiscountResult result = voucherService.evaluate("TEST", SHIPPING_FEE, USER_ID);

        assertThat(result.messageCode()).isEqualTo(ErrorCode.VOUCHER_INACTIVE);
    }

    @Test
    @DisplayName("Ma giam gia duoc chuan hoa ve chu in hoa truoc khi tra cuu")
    void shouldNormalizeCodeBeforeLookup() {
        stubVoucher(buildVoucher(DiscountType.FIXED, "10000", null));

        VoucherDiscountResult result = voucherService.evaluate(" test ", SHIPPING_FEE, USER_ID);

        assertThat(result.applied()).isTrue();
    }
}
