package com.discountcalc.exceptions;

public class ExchangeRateServiceException extends Exception{
    public ExchangeRateServiceException(String message) {
        super(message);
    }
    public ExchangeRateServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
