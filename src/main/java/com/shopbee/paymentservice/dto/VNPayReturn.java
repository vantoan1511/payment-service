package com.shopbee.paymentservice.dto;

import jakarta.ws.rs.QueryParam;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VNPayReturn {

    @QueryParam("vnp_Amount")
    private String vnp_Amount;

    @QueryParam("vnp_ResponseCode")
    private String vnp_ResponseCode;

    @QueryParam("vnp_TransactionNo")
    private String vnp_TransactionNo;

    @QueryParam("vnp_TransactionStatus")
    private String vnp_TransactionStatus;

    @QueryParam("vnp_TxnRef")
    private String vnp_TxnRef;

    @QueryParam("vnp_SecureHash")
    private String vnp_SecureHash;
}
