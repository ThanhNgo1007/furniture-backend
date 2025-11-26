package com.furniture.modal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.furniture.domain.AccountStatus;
import com.furniture.domain.USER_ROLE;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Seller {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String sellerName;

    private String mobile;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    @Embedded
    @JsonProperty("businessDetails") // Map key "businessDetails" từ FE vào biến này
    private BussinessDetails bussinessDetails = new BussinessDetails();

    @Embedded
    @JsonProperty("bankDetails") // Map key "bankDetails"
    private BankDetails bankDetails = new BankDetails();

    @OneToOne(cascade = CascadeType.ALL)
    @JsonProperty("pickupAddress") // Map key "pickupAddress"
    private Address pickupAddress = new Address();

    @JsonProperty("MST") // Map key "MST"
    private String MST;

    private USER_ROLE role = USER_ROLE.ROLE_SELLER;

    private boolean isEmailVerified = false;

    private AccountStatus accountStatus = AccountStatus.PENDING_VERIFICATION;




}
