package com.discountcalc.services;

import com.discountcalc.exceptions.ExchangeRateServiceException;
import com.discountcalc.models.requests.BillRequestDTO;
import com.discountcalc.models.responses.BillResponseDTO;
import org.springframework.stereotype.Service;
@Service
public class DiscountService {

    private  CurrencyExchangeService currencyExchangeService;
    public DiscountService(CurrencyExchangeService currencyExchangeService) {
        this.currencyExchangeService = currencyExchangeService;
    }

    public BillResponseDTO calculateDiscount(BillRequestDTO billRequest) throws ExchangeRateServiceException {
        double exchangeRate = currencyExchangeService.getExchangeRate(billRequest.originalCurrency(), billRequest.targetCurrency());
        double payableAmount = calculatePayableAmount(billRequest, exchangeRate);
        return new BillResponseDTO(payableAmount, billRequest.targetCurrency());
    }

    private double calculatePayableAmount(BillRequestDTO billDTO, double exchangeRate) {
        double discountedAmount = getDiscountAmount(billDTO);
        return discountedAmount * exchangeRate;
    }
    private double getDiscountAmount(BillRequestDTO billRequest) {
        double totalNonGroceryAmount = billRequest.items().stream()
                .filter(item -> !"groceries".equalsIgnoreCase(item.category()))
                .mapToDouble(BillRequestDTO.Item::price)
                .sum();

        double discountAmount = 0.0;
        switch (billRequest.user().userType()) {
            case EMPLOYEE:
                discountAmount = totalNonGroceryAmount * 0.30;
                break;
            case AFFILIATE:
                discountAmount = totalNonGroceryAmount * 0.10;
                break;
            case CUSTOMER:
                if (billRequest.user().tenure() > 2) {
                    discountAmount = totalNonGroceryAmount * 0.05;
                }
                break;
        }

        int additionalDiscount = ((int) billRequest.totalAmount() / 100) * 5;
        return billRequest.totalAmount() - discountAmount - additionalDiscount;
    }
}