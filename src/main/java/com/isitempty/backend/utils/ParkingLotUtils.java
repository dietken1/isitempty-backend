package com.isitempty.backend.utils;

/**
 * 주차장 관련 공통 유틸리티 기능
 */
public class ParkingLotUtils {
    /**
     * 주소 정규화
     */
    public static String normalizeAddress(String address) {
        if (address == null) return "";
        address = address.toLowerCase().trim()
                .replaceAll("^(서울특별시|경기도|부산광역시|대구광역시|광주광역시|대전광역시|울산광역시|세종특별자치시|제주특별자치도)", "")
                .replaceAll("-0\\b", "")
                .replaceAll("\\s\\(.*?\\)", "");
        return address.trim();
    }

    /**
     * 전화번호 정규화
     */
    public static String normalizePhoneNumber(String phone) {
        if (phone == null) return "";
        return phone.replaceAll("[^0-9]", "");
    }
} 