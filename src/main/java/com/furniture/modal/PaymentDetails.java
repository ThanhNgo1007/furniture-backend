package com.furniture.modal;

import com.furniture.domain.PaymentStatus;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDetails {
    private String paymentId;
    private String paymentLinkId;
    private String paymentLinkRefernceId;
    private String paymentLinkStatus;
    private String paymentIdZWSP;
    private PaymentStatus status;
}
