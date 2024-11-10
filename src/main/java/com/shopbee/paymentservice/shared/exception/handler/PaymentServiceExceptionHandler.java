package com.shopbee.paymentservice.shared.exception.handler;

import com.shopbee.paymentservice.shared.exception.ErrorResponse;
import com.shopbee.paymentservice.shared.exception.PaymentServiceException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class PaymentServiceExceptionHandler implements ExceptionMapper<PaymentServiceException> {

    @Override
    public Response toResponse(PaymentServiceException e) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage(e.getMessage());
        return Response.status(e.getResponse().getStatus()).entity(errorResponse).type(MediaType.APPLICATION_JSON).build();
    }
}
