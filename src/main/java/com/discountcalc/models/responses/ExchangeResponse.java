package com.discountcalc.models.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record ExchangeResponse(@JsonProperty("conversion_rates") Map<String, Double> rates) {
}
