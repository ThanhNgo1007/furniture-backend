package com.furniture.request;

import com.furniture.domain.PaymentMethod;
import com.furniture.modal.Address;
import lombok.Data;

@Data
public class PaymentRequest {
    private Address shippingAddress;
    private PaymentMethod paymentMethod;
}