package com.shopbee.paymentservice.external.order.filter;

import com.shopbee.paymentservice.external.order.dto.OrderStatus;
import jakarta.ws.rs.QueryParam;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FilterCriteria {

    @QueryParam("keyword")
    private String keyword;

    @QueryParam("status")
    private OrderStatus status;

    @QueryParam("productSlug")
    private String productSlug;
}
