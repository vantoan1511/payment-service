package com.shopbee.paymentservice.shared.sort;

import lombok.Getter;

@Getter
public enum SortField {
    CREATED_AT("createdAt");

    private final String column;

    SortField(String column) {
        this.column = column;
    }
}
