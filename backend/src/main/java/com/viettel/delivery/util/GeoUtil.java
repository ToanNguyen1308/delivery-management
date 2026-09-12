package com.viettel.delivery.util;

import com.viettel.delivery.constant.AppConstants;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Tinh khoang cach giua hai toa do bang cong thuc Haversine.
 */
public final class GeoUtil {

    /**
     * He so quy doi tu duong chim bay sang quang duong duong bo thuc te.
     */
    private static final BigDecimal ROAD_FACTOR = new BigDecimal("1.30");

    private GeoUtil() {
    }

    public static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return AppConstants.EARTH_RADIUS_KM * c;
    }

    public static BigDecimal distanceKm(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return BigDecimal.ZERO;
        }
        double km = haversineKm(lat1.doubleValue(), lon1.doubleValue(), lat2.doubleValue(), lon2.doubleValue());
        return BigDecimal.valueOf(km).setScale(AppConstants.DISTANCE_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Quang duong uoc tinh khi di chuyen thuc te, dung de tinh cuoc van chuyen.
     */
    public static BigDecimal roadDistanceKm(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        return distanceKm(lat1, lon1, lat2, lon2)
                .multiply(ROAD_FACTOR)
                .setScale(AppConstants.DISTANCE_SCALE, RoundingMode.HALF_UP);
    }
}
