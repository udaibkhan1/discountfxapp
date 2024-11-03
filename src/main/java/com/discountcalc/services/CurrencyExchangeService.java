package com.discountcalc.services;

import com.discountcalc.exceptions.CurrencyNotFoundException;
import com.discountcalc.exceptions.ExchangeRateServiceException;
import com.discountcalc.models.responses.ExchangeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
@Component
@RequiredArgsConstructor
public class CurrencyExchangeService {

    private final RestTemplate restTemplate;

    @Value("${exchange.api.url}")
    private String exchangeBaseUrl ;

    @Value("${exchange.api.key}")
    private String apiKey;


    @Cacheable(value = "exchangeRates", key = "#baseCurrency + '_' + #targetCurrency")
    public double getExchangeRate(String baseCurrency, String targetCurrency) throws ExchangeRateServiceException {
        String url = String.format("%s/%s/latest/%s", exchangeBaseUrl, apiKey, baseCurrency);

        try {
            ResponseEntity<ExchangeResponse> response = restTemplate.getForEntity(url, ExchangeResponse.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new ExchangeRateServiceException("Failed to retrieve exchange rates from the API. Status: " + response.getStatusCode());
            }

            return Optional.ofNullable(response.getBody().rates().get(targetCurrency))
                    .orElseThrow(() -> new CurrencyNotFoundException("Target currency not found in exchange rates: " + targetCurrency));

        } catch (RestClientException e) {
            throw new ExchangeRateServiceException("Error occurred while fetching exchange rates: " + e.getMessage(), e);
        }
    }
}
