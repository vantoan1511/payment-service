package com.shopbee.paymentservice.impl;

import com.shopbee.paymentservice.configuration.ProductConfiguration;
import com.shopbee.paymentservice.dto.PaymentRequest;
import com.shopbee.paymentservice.dto.VNPayReturn;
import com.shopbee.paymentservice.entity.Transaction;
import com.shopbee.paymentservice.enums.TransactionStatus;
import com.shopbee.paymentservice.external.order.OrderServiceClient;
import com.shopbee.paymentservice.external.order.dto.Order;
import com.shopbee.paymentservice.external.order.dto.PaymentMethod;
import com.shopbee.paymentservice.repository.TransactionRepository;
import com.shopbee.paymentservice.security.SecureKeyUtil;
import com.shopbee.paymentservice.shared.exception.PaymentServiceException;
import com.shopbee.paymentservice.shared.page.PageRequest;
import com.shopbee.paymentservice.shared.page.PagedResponse;
import com.shopbee.paymentservice.shared.sort.SortCriteria;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@ApplicationScoped
public class PaymentService {

    private final ProductConfiguration configuration;
    private final OrderServiceClient orderServiceClient;
    private final TransactionRepository transactionRepository;

    @Inject
    public PaymentService(ProductConfiguration configuration,
                          @RestClient OrderServiceClient orderServiceClient,
                          TransactionRepository transactionRepository) {
        this.configuration = configuration;
        this.orderServiceClient = orderServiceClient;
        this.transactionRepository = transactionRepository;
    }

    public PagedResponse<Transaction> getByCriteria(PageRequest pageRequest, SortCriteria sortCriteria) {
        List<Transaction> transactions = transactionRepository.findByCriteria(pageRequest, sortCriteria);
        long totalItems = transactionRepository.count();
        return PagedResponse.of(totalItems, pageRequest, transactions);
    }

    public Transaction getById(Long id) {
        return transactionRepository.findByIdOptional(id)
                .orElseThrow(() -> new PaymentServiceException("Transaction not found", Response.Status.NOT_FOUND));
    }

    public String createPaymentUrl(PaymentRequest paymentRequest, String requestIP) {
        Order order = getOrderById(paymentRequest.getOrderId());
        validateOrderForPayment(order);

        Map<String, String> vnPayParameters = buildVNPayParameters(order, requestIP);

        String secureHash = generateSecureHash(vnPayParameters);
        String queryUrl = buildQueryUrl(vnPayParameters) + "&vnp_SecureHash=" + secureHash;

        return configuration.getVnp_PayUrl() + "?" + queryUrl;
    }

    @Transactional
    public Transaction handleVnpayReturn(VNPayReturn vnPayReturn) {
        String referenceId = vnPayReturn.getVnp_TransactionNo();
        if (!referenceId.equals("0") && transactionRepository.findByReferenceId(referenceId).isPresent()) {
            throw new PaymentServiceException("Transaction has already existed", Response.Status.CONFLICT);
        }

        Long orderId = Long.valueOf(vnPayReturn.getVnp_TxnRef());
        if (transactionRepository.findByOrderId(orderId).isPresent()) {
            throw new PaymentServiceException("Order has already been checked out", Response.Status.CONFLICT);
        }

        TransactionStatus status = TransactionStatus.fromCode(vnPayReturn.getVnp_ResponseCode());

        Transaction transaction = new Transaction();
        transaction.setOrderId(orderId);
        transaction.setStatus(status);
        transaction.setReferenceId(referenceId);
        transactionRepository.persist(transaction);

        String secureHash = SecureKeyUtil.generateHMAC();
        if (status.equals(TransactionStatus.SUCCESS)) {
            orderServiceClient.invokeSuccessCheckout(orderId, secureHash);
        } else {
            orderServiceClient.invokeFailureCheckout(orderId, secureHash);
        }

        return transaction;
    }

    private Order getOrderById(Long orderId) {
        Order order = orderServiceClient.getOrderById(orderId);
        return Optional.ofNullable(order)
                .orElseThrow(() -> new PaymentServiceException("Order not found", Response.Status.NOT_FOUND));
    }

    private void validateOrderForPayment(Order order) {
        if (order.getPaymentMethod().equals(PaymentMethod.CASH)) {
            throw new PaymentServiceException("This order has already been checked out with CASH", Response.Status.METHOD_NOT_ALLOWED);
        }

        boolean transactionExists = transactionRepository.findByOrderId(order.getId()).stream()
                .anyMatch(transaction -> !transaction.getReferenceId().equals("0"));

        if (transactionExists) {
            throw new PaymentServiceException("Transaction already exists", Response.Status.CONFLICT);
        }
    }

    private Map<String, String> buildVNPayParameters(Order order, String requestIP) {
        long amount = order.getTotalAmount().longValue() * 100L;
        ZonedDateTime createdDate = ZonedDateTime.now(ZoneId.of("GMT+7"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", configuration.getVnp_TmnCode());
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_BankCode", "VNBANK");
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", order.getId().toString());
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + order.getId());
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", configuration.getVnp_ReturnUrl());
        vnp_Params.put("vnp_IpAddr", requestIP);
        vnp_Params.put("vnp_CreateDate", formatter.format(createdDate));
        vnp_Params.put("vnp_ExpireDate", formatter.format(createdDate.plusMinutes(15)));

        return vnp_Params;
    }


    private String buildQueryUrl(Map<String, String> parameters) {
        Map<String, String> sortedParameters = new TreeMap<>(parameters);
        StringBuilder query = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedParameters.entrySet()) {
            if (!query.isEmpty()) {
                query.append("&");
            }
            query.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                    .append("=")
                    .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        return query.toString();
    }

    private String generateSecureHash(Map<String, String> parameters) {
        Map<String, String> sortedParameters = new TreeMap<>(parameters);
        StringJoiner hashData = new StringJoiner("&");

        sortedParameters.forEach((key, value) -> {
            if (value != null && !value.isEmpty()) {
                String encodedValue = URLEncoder.encode(value, StandardCharsets.US_ASCII);
                hashData.add(key + "=" + encodedValue);
            }
        });

        return ProductConfiguration.hmacSHA512(configuration.getSecretKey(), hashData.toString());
    }

}
