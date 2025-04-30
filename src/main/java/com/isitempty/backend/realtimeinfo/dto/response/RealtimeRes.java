package com.isitempty.backend.realtimeinfo.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RealtimeRes {
    @JsonProperty("PKLT_CD")
    private String id;

    @JsonProperty("PKLT_NM")
    private String name;

    @JsonProperty("ADDR")
    private String address;

    @JsonProperty("TELNO")
    private String phoneNumber;

    @JsonProperty("TPKCT")
    private Integer totalSpaces;

    @JsonProperty("NOW_PRK_VHCL_CNT")
    private Integer currentParked;
}