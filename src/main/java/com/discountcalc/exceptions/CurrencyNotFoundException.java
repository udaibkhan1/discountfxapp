package com.discountcalc.exceptions;

public class CurrencyNotFoundException extends ExchangeRateServiceException{
    public CurrencyNotFoundException(String message) {
        super(message);
    }
}
