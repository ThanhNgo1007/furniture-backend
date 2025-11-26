package com.furniture.modal;

import com.fasterxml.jackson.annotation.JsonProperty; // Import thư viện này
import lombok.Data;

@Data
public class BussinessDetails {

    @JsonProperty("businessName") // Frontend gửi "businessName" -> map vào biến này
    private String bussinessName;

    @JsonProperty("businessEmail")
    private String bussinessEmail;

    @JsonProperty("businessMobile")
    private String bussinessMobile;

    @JsonProperty("businessAddress")
    private String bussinessAddress;

    @JsonProperty("logo")
    private String logo;

    @JsonProperty("banner")
    private String banner;
}