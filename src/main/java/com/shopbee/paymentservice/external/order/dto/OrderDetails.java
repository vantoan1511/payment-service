package com.shopbee.paymentservice.external.order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetails {
    private Long id;
    private Integer quantity;
    private BigDecimal price;
    private String productSlug;
}
