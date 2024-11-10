package com.shopbee.paymentservice.shared.exception;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

public class PaymentServiceException extends WebApplicationException {

    public PaymentServiceException(String message, int status) {
        super(message, status);
    }

    public PaymentServiceException(String message, Response.Status status) {
        super(message, status);
    }
}
