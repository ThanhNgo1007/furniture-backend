package com.furniture.modal;

import com.fasterxml.jackson.annotation.JsonProperty; // Import thư viện này
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @JsonProperty("name") // Thêm dòng này
    private String name;

    @JsonProperty("locality") // Thêm dòng này
    private String locality;

    @JsonProperty("address") // Thêm dòng này
    private String address;

    @JsonProperty("city") // Thêm dòng này
    private String city;

    @JsonProperty("ward") // Frontend gửi "state", Backend đang để "ward"? Hãy sửa cho khớp
    private String ward; // Đổi tên biến này thành state cho khớp Frontend, hoặc map "state" vào "ward"

    @JsonProperty("pinCode") // Thêm dòng này
    private String pinCode;

    @JsonProperty("mobile") // Thêm dòng này
    private String mobile;
}