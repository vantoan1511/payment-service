package com.shopbee.paymentservice.shared.exception.handler;

import com.shopbee.paymentservice.shared.exception.ErrorResponse;
import com.shopbee.paymentservice.shared.exception.ExternalServiceException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ExternalServiceExceptionHandler implements ExceptionMapper<ExternalServiceException> {

    @Override
    public Response toResponse(ExternalServiceException e) {
        return Response.status(e.getStatus())
                .entity(new ErrorResponse(e.getErrorDetails(), e.getMessage()))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
