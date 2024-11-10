package com.shopbee.paymentservice.external.order;

import com.shopbee.paymentservice.external.order.dto.Order;
import com.shopbee.paymentservice.external.order.dto.UpdateStatusRequest;
import com.shopbee.paymentservice.shared.exception.mapper.ExternalServiceExceptionMapper;
import com.shopbee.paymentservice.shared.filter.FilterCriteria;
import com.shopbee.paymentservice.shared.page.PageRequest;
import com.shopbee.paymentservice.shared.page.PagedResponse;
import com.shopbee.paymentservice.shared.sort.SortCriteria;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("orders")
@RegisterRestClient(configKey = "orderservice")
@RegisterClientHeaders
@RegisterProvider(ExternalServiceExceptionMapper.class)
public interface OrderServiceClient {

    @GET
    PagedResponse<Order> getOrders(@BeanParam @Valid FilterCriteria filterCriteria,
                                   @BeanParam @Valid PageRequest pageRequest,
                                   @BeanParam @Valid SortCriteria sortCriteria);

    @GET
    @Path("{id}")
    Order getOrderById(@PathParam("id") Long id);

    @PATCH
    @Path("{id}")
    void updateStatus(@PathParam("id") Long id, UpdateStatusRequest updateStatusRequest);

    @POST
    @Path("{id}")
    void invokeSuccessCheckout(@PathParam("id") Long id, @QueryParam("secureKey") String secureKey);
}
