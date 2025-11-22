package com.furniture.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class VNPayConfig {

    @Value("${vnpay.api.url}")
    private String vnpUrl;

    @Value("${vnpay.tmn.code}")
    private String tmnCode;

    @Value("${vnpay.secret.key}")
    private String secretKey;

    @Value("${vnpay.return.url}")
    private String returnUrl;

    @Value("${vnpay.api.version}")
    private String version;

    @Value("${vnpay.command}")
    private String command;

    @Value("${vnpay.order.type}")
    private String orderType;

    @Value("${app.currency.default:VND}")
    private String defaultCurrency;
}