package com.isitempty.backend.realtimeinfo.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ParkingInfoRes {

    @JsonProperty("GetParkingInfo")
    private GetParkingInfo getParkingInfo;

    @Data
    public static class GetParkingInfo {
        private RealtimeRes[] row;
    }
}