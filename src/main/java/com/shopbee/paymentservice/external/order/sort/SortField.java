package com.shopbee.paymentservice.external.order.sort;

import lombok.Getter;

@Getter
public enum SortField {
    CREATED_AT("createdAt"),
    STATUS("orderStatus"),
    USERNAME("username");

    private final String column;

    SortField(String column) {
        this.column = column;
    }
}
