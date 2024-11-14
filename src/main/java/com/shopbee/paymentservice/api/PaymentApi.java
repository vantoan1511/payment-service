package com.shopbee.paymentservice.api;

import com.shopbee.paymentservice.dto.PaymentRequest;
import com.shopbee.paymentservice.dto.PaymentResponse;
import com.shopbee.paymentservice.dto.SaleReportRequest;
import com.shopbee.paymentservice.dto.VNPayReturn;
import com.shopbee.paymentservice.impl.PaymentService;
import com.shopbee.paymentservice.impl.ReportService;
import com.shopbee.paymentservice.shared.page.PageRequest;
import com.shopbee.paymentservice.shared.sort.SortCriteria;
import io.quarkus.security.Authenticated;
import io.vertx.ext.web.RoutingContext;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("payments")
@Produces(MediaType.APPLICATION_JSON)
public class PaymentApi {

    private final PaymentService paymentService;
    private final RoutingContext routingContext;
    private final ReportService reportService;

    @Inject
    public PaymentApi(PaymentService paymentService,
                      RoutingContext routingContext,
                      ReportService reportService) {
        this.paymentService = paymentService;
        this.routingContext = routingContext;
        this.reportService = reportService;
    }

    @POST
    @Path("process")
    @Authenticated
    public Response createPaymentUrl(@Valid PaymentRequest paymentRequest) {
        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setProcessUrl(paymentService.createPaymentUrl(paymentRequest, routingContext.request().host()));
        return Response.ok(paymentResponse).build();
    }

    @POST
    @Path("/vnpay-return")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response handleVnpayReturn(VNPayReturn vnPayReturn) {
        return Response.ok(paymentService.handleVnpayReturn(vnPayReturn)).build();
    }

    @GET
    @Path("/transactions")
    @Authenticated
    public Response getTransactions(@BeanParam PageRequest pageRequest,
                                    @BeanParam SortCriteria sortCriteria) {
        return Response.ok(paymentService.getByCriteria(pageRequest, sortCriteria)).build();
    }

    @GET
    @Path("/transactions/{id}")
    @Authenticated
    public Response getTransactionById(@PathParam("id") Long id) {
        return Response.ok(paymentService.getById(id)).build();
    }

    @GET
    @Path("report")
    @Authenticated
    public Response getSaleReport(@BeanParam @Valid SaleReportRequest saleReportRequest) {
        return Response.ok(reportService.getSaleReport(saleReportRequest)).build();
    }
}
