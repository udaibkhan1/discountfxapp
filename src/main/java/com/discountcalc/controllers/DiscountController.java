package com.discountcalc.controllers;

import com.discountcalc.exceptions.ExchangeRateServiceException;
import com.discountcalc.models.requests.BillRequestDTO;
import com.discountcalc.models.responses.BillResponseDTO;
import com.discountcalc.services.DiscountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/discount")
public class DiscountController {

    private final DiscountService discountService;
    public DiscountController(DiscountService discountService) {
        this.discountService = discountService;
    }

    @PostMapping("/calculate")
    public ResponseEntity<BillResponseDTO> calculateDiscount(@RequestBody BillRequestDTO billRequest) throws ExchangeRateServiceException {
        BillResponseDTO response = discountService.calculateDiscount(billRequest);
        return ResponseEntity.ok(response);
    }
}