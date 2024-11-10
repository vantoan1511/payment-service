package com.shopbee.paymentservice.shared.filter;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.QueryParam;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FilterCriteria {

    @QueryParam("productSlug")
    private String productSlug;

    @QueryParam("rating")
    @Min(1)
    @Max(5)
    private Integer rating;
}
