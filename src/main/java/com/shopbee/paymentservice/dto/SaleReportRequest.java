package com.shopbee.paymentservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.QueryParam;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SaleReportRequest {

    @QueryParam("period")
    @NotNull(message = "Period is required")
    private String period;

    @QueryParam("year")
    private Integer year;
}
