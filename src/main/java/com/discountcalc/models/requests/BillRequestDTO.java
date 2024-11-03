package com.discountcalc.models.requests;

import lombok.Builder;

import java.util.List;

@Builder
public record BillRequestDTO(
        List<Item> items,
        double totalAmount,
        String originalCurrency,
        String targetCurrency,
        UserDTO user
) {
    public record Item(String name,String category, double price) {}
}