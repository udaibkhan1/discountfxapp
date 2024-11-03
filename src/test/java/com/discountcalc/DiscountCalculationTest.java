package com.discountcalc;

import com.discountcalc.config.SecurityConfig;
import com.discountcalc.enums.UserType;
import com.discountcalc.exceptions.ExchangeRateServiceException;
import com.discountcalc.models.requests.BillRequestDTO;
import com.discountcalc.models.requests.UserDTO;
import com.discountcalc.models.responses.BillResponseDTO;
import com.discountcalc.services.CurrencyExchangeService;
import com.discountcalc.services.DiscountService;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@MockBean(name = "currencyExchangeService", value = CurrencyExchangeService.class)
@RequiredArgsConstructor(onConstructor_ =@Autowired)
@ActiveProfiles("test")
class DiscountCalculationTest {

	private final CurrencyExchangeService currencyExchangeService;

	private final TestRestTemplate restTemplate;

	private final DiscountService discountService;

	private final double EXCHANGE_RATE = 0.92;


	@Test
	void testCalculateDiscount_ReturnsExpectedValue() throws ExchangeRateServiceException {
		BillRequestDTO request = createBillRequest(UserType.EMPLOYEE, 3,
				List.of(new BillRequestDTO.Item("Laptop", "electronics", 100.0),
						new BillRequestDTO.Item("Apples", "groceries", 50.0)));

		when(currencyExchangeService.getExchangeRate(anyString(), anyString())).thenReturn(EXCHANGE_RATE);
		ResponseEntity<BillResponseDTO> response = restTemplate.postForEntity(
				apiBaseUrl(),
				new HttpEntity<>(request),
				BillResponseDTO.class
		);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());


		double expectedPayableAmountAmount = calculateExpectedPayableAmount(request, 0.30, 5);
		assertEquals(expectedPayableAmountAmount, response.getBody().payableAmount(), 0.01);
	}

	@Test
	void testCalculateDiscount_OnlyGroceryItems_ReturnsFullAmount() throws ExchangeRateServiceException {
		BillRequestDTO request = createBillRequest(UserType.EMPLOYEE, 3,
				List.of(new BillRequestDTO.Item("Bananas", "groceries", 40.0),
						new BillRequestDTO.Item("Bread", "groceries", 20.0)));

		when(currencyExchangeService.getExchangeRate(anyString(), anyString())).thenReturn(EXCHANGE_RATE);

		ResponseEntity<BillResponseDTO> response = restTemplate.postForEntity(
				apiBaseUrl(),
				request,
				BillResponseDTO.class
		);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		double expectedPayableAmountAmount = request.totalAmount() * EXCHANGE_RATE;
		assertEquals(expectedPayableAmountAmount, response.getBody().payableAmount(), 0.01);
	}

	@Test
	void testCalculateDiscount_AffiliateUser_ReturnsCorrectDiscount() throws ExchangeRateServiceException {
		BillRequestDTO request = createBillRequest(UserType.AFFILIATE, 1,
				List.of(new BillRequestDTO.Item("Laptop", "electronics", 200.0)));

		when(currencyExchangeService.getExchangeRate(anyString(), anyString())).thenReturn(EXCHANGE_RATE);

		ResponseEntity<BillResponseDTO> response = restTemplate.postForEntity(
				apiBaseUrl(),
				new HttpEntity<>(request),
				BillResponseDTO.class
		);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		double totalNonGroceryAmount = request.items().stream()
				.filter(item -> !"groceries".equalsIgnoreCase(item.category()))
				.mapToDouble(BillRequestDTO.Item::price)
				.sum();

		double discount = totalNonGroceryAmount * 0.10; // Affiliate discount

		int additionalDiscount = ((int) request.totalAmount() / 100) * 5;

		double expectedAmount = (request.totalAmount() - discount - additionalDiscount) * EXCHANGE_RATE;

		assertEquals(expectedAmount, response.getBody().payableAmount(), 0.01);
	}

	@Test
	void testCalculateDiscount_CustomerUser_ReturnsCorrectDiscount() throws ExchangeRateServiceException {
		BillRequestDTO request = createBillRequest(UserType.CUSTOMER, 3,
				List.of(new BillRequestDTO.Item("Laptop", "electronics", 200.0)));

		when(currencyExchangeService.getExchangeRate(anyString(), anyString())).thenReturn(EXCHANGE_RATE);

		ResponseEntity<BillResponseDTO> response = restTemplate.postForEntity(
				apiBaseUrl(),
				new HttpEntity<>(request),
				BillResponseDTO.class
		);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		double totalNonGroceryAmount = request.items().stream()
				.filter(item -> !"groceries".equalsIgnoreCase(item.category()))
				.mapToDouble(BillRequestDTO.Item::price)
				.sum();

		double discount = (request.user().tenure() > 2) ? totalNonGroceryAmount * 0.05 : 0;

		int additionalDiscount = ((int) request.totalAmount() / 100) * 5;

		double expectedAmount = (request.totalAmount() - discount - additionalDiscount) * EXCHANGE_RATE;

		assertEquals(expectedAmount, response.getBody().payableAmount(), 0.01);
	}


	@Test
	void testGetExchangeRate_Non2xxResponse() throws ExchangeRateServiceException {
		String baseCurrency = "USD";
		String targetCurrency = "EUR";


		when(currencyExchangeService.getExchangeRate(baseCurrency, targetCurrency)).thenThrow(new ExchangeRateServiceException("Failed to retrieve exchange rates from the API. Status: 400 BAD REQUEST"));

		ExchangeRateServiceException thrown = assertThrows(ExchangeRateServiceException.class, () -> {
			currencyExchangeService.getExchangeRate(baseCurrency, targetCurrency);
		});

		assertEquals("Failed to retrieve exchange rates from the API. Status: 400 BAD REQUEST", thrown.getMessage());
	}
	@Test
	void testCalculatePayableAmount_Non2xxResponse() throws ExchangeRateServiceException {

		var items = List.of(new BillRequestDTO.Item("Laptop", "electronics", 200.0));
		var user = new UserDTO(UserType.EMPLOYEE, 3);
		var billDTO = new BillRequestDTO(items, 200.0, "USD", "EUR", user);

		when(currencyExchangeService.getExchangeRate(anyString(), anyString())).thenThrow(new ExchangeRateServiceException("Exchange rate API failed"));

		ExchangeRateServiceException thrown = assertThrows(ExchangeRateServiceException.class, () -> {
			discountService.calculateDiscount(billDTO);
		});

		assertEquals("Exchange rate API failed", thrown.getMessage());
	}

	private BillRequestDTO createBillRequest(UserType userType, int tenure, List<BillRequestDTO.Item> items) {
		return BillRequestDTO.builder()
				.items(items)
				.totalAmount(items.stream().mapToDouble(BillRequestDTO.Item::price).sum())
				.originalCurrency("USD")
				.targetCurrency("EUR")
				.user(new UserDTO(userType, tenure))
				.build();
	}
	private double calculateExpectedPayableAmount(BillRequestDTO request, double employeeDiscountRate, double additionalDiscountRate) {
		double totalNonGroceryAmount = request.items().stream()
				.filter(item -> !"groceries".equalsIgnoreCase(item.category()))
				.mapToDouble(BillRequestDTO.Item::price)
				.sum();

		double discount = employeeDiscountRate * totalNonGroceryAmount;
		int additionalDiscount = (request.totalAmount() > 100)
				? ((int) request.totalAmount() / 100) * (int)additionalDiscountRate
				: 0;

		double discountedAmount = request.totalAmount() - discount - additionalDiscount;
		return discountedAmount * EXCHANGE_RATE;
	}


	private String apiBaseUrl(){
		return "/api/discount/calculate";
	}
}
