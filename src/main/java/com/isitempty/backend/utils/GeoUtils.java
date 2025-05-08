package com.isitempty.backend.utils;

/**
 * 지리 관련 계산 유틸리티 클래스
 */
public class GeoUtils {
    
    private static final double EARTH_RADIUS_KM = 6371.0; // 지구 반지름 (킬로미터)
    
    /**
     * 두 좌표 사이의 거리를 계산합니다 (Haversine 공식 사용).
     * @param lat1 첫 번째 좌표의 위도
     * @param lon1 첫 번째 좌표의 경도
     * @param lat2 두 번째 좌표의 위도
     * @param lon2 두 번째 좌표의 경도
     * @return 두 좌표 사이의 거리 (킬로미터)
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // 위도/경도를 라디안으로 변환
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        // Haversine 공식
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        // 거리 계산 (킬로미터)
        return EARTH_RADIUS_KM * c;
    }
} 