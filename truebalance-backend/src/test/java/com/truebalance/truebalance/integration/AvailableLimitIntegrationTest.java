package com.truebalance.truebalance.integration;

import com.truebalance.truebalance.application.dto.input.BillRequestDTO;
import com.truebalance.truebalance.application.dto.input.CreditCardRequestDTO;
import com.truebalance.truebalance.application.dto.input.PartialPaymentRequestDTO;
import com.truebalance.truebalance.application.dto.output.AvailableLimitDTO;
import com.truebalance.truebalance.application.dto.output.CreditCardResponseDTO;
import com.truebalance.truebalance.infra.db.entity.InvoiceEntity;
import com.truebalance.truebalance.infra.db.repository.BillRepository;
import com.truebalance.truebalance.infra.db.repository.InstallmentRepository;
import com.truebalance.truebalance.infra.db.repository.InvoiceRepository;
import com.truebalance.truebalance.infra.db.repository.PartialPaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end integration tests for Available Limit calculation.
 * Tests complete flow of limit calculation with bills, invoices, and partial payments.
 *
 * Uses @SpringBootTest with real database (H2) and full Spring context.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Available Limit Integration Tests")
class AvailableLimitIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private InstallmentRepository installmentRepository;

    @Autowired
    private PartialPaymentRepository partialPaymentRepository;

    @Autowired
    private com.truebalance.truebalance.infra.db.repository.CreditCardRepository creditCardRepository;

    @BeforeEach
    void setUp() {
        // Clean database before each test
        partialPaymentRepository.deleteAll();
        installmentRepository.deleteAll();
        billRepository.deleteAll();
        invoiceRepository.deleteAll();
        creditCardRepository.deleteAll();
    }

    // ==================== Happy Path Tests ====================

    @Test
    @DisplayName("E2E: Should calculate full available limit when no bills exist")
    void shouldCalculateFullAvailableLimitWhenNoBills() {
        // Given: Create credit card with 5000 limit
        Long creditCardId = createCreditCard("Test Card", new BigDecimal("5000.00"), 10, 17);

        // When: Get available limit
        ResponseEntity<AvailableLimitDTO> response = restTemplate.getForEntity(
                "/credit-cards/" + creditCardId + "/available-limit",
                AvailableLimitDTO.class
        );

        // Then: Full limit available (5000 - 0 = 5000)
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCreditLimit()).isEqualByComparingTo("5000.00");
        assertThat(response.getBody().getUsedLimit()).isEqualByComparingTo("0.00");
        assertThat(response.getBody().getAvailableLimit()).isEqualByComparingTo("5000.00");
    }

    @Test
    @DisplayName("E2E: Should reduce available limit after creating bill")
    void shouldReduceAvailableLimitAfterCreatingBill() {
        // Given: Create credit card with 5000 limit
        Long creditCardId = createCreditCard("Test Card", new BigDecimal("5000.00"), 10, 17);

        // When: Create bill with 1200 (3 installments of 400)
        createBillWithCard(creditCardId, "Purchase", new BigDecimal("1200.00"), 3);

        // Get available limit
        ResponseEntity<AvailableLimitDTO> response = restTemplate.getForEntity(
                "/credit-cards/" + creditCardId + "/available-limit",
                AvailableLimitDTO.class
        );

        // Then: Limit reduced by total bill amount (5000 - 1200 = 3800)
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getCreditLimit()).isEqualByComparingTo("5000.00");
        assertThat(response.getBody().getUsedLimit()).isEqualByComparingTo("1200.00");
        assertThat(response.getBody().getAvailableLimit()).isEqualByComparingTo("3800.00");
    }

    @Test
    @DisplayName("E2E: Should increase available limit after partial payment")
    void shouldIncreaseAvailableLimitAfterPartialPayment() {
        // Given: Create credit card and bill
        Long creditCardId = createCreditCard("Test Card", new BigDecimal("5000.00"), 10, 17);
        createBillWithCard(creditCardId, "Purchase", new BigDecimal("2000.00"), 1);

        // Get invoice ID
        List<InvoiceEntity> invoices = invoiceRepository.findByCreditCardIdOrderByReferenceMonthDesc(creditCardId);
        Long invoiceId = invoices.get(0).getId();

        // Register partial payment of 500
        PartialPaymentRequestDTO paymentRequest = new PartialPaymentRequestDTO(
                new BigDecimal("500.00"),
                "Partial payment"
        );

        restTemplate.postForEntity(
                "/invoices/" + invoiceId + "/partial-payments",
                paymentRequest,
                Object.class
        );

        // When: Get available limit
        ResponseEntity<AvailableLimitDTO> response = restTemplate.getForEntity(
                "/credit-cards/" + creditCardId + "/available-limit",
                AvailableLimitDTO.class
        );

        // Then: Limit increased by payment (5000 - 2000 + 500 = 3500)
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getUsedLimit()).isEqualByComparingTo("2000.00");  // Sum of installments
        assertThat(response.getBody().getPartialPaymentsTotal()).isEqualByComparingTo("500.00");  // Sum of partial payments
        assertThat(response.getBody().getAvailableLimit()).isEqualByComparingTo("3500.00");  // 5000 - 2000 + 500
    }

    @Test
    @DisplayName("E2E: Should handle multiple bills reducing available limit")
    void shouldHandleMultipleBillsReducingLimit() {
        // Given: Create credit card with 10000 limit
        Long creditCardId = createCreditCard("Test Card", new BigDecimal("10000.00"), 10, 17);

        // When: Create 3 bills
        createBillWithCard(creditCardId, "Bill 1", new BigDecimal("2000.00"), 1);
        createBillWithCard(creditCardId, "Bill 2", new BigDecimal("1500.00"), 1);
        createBillWithCard(creditCardId, "Bill 3", new BigDecimal("3000.00"), 1);

        // Get available limit
        ResponseEntity<AvailableLimitDTO> response = restTemplate.getForEntity(
                "/credit-cards/" + creditCardId + "/available-limit",
                AvailableLimitDTO.class
        );

        // Then: Limit reduced by all bills (10000 - 6500 = 3500)
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getUsedLimit()).isEqualByComparingTo("6500.00");
        assertThat(response.getBody().getAvailableLimit()).isEqualByComparingTo("3500.00");
    }

    // ==================== Validation Tests ====================

    @Test
    @DisplayName("E2E: Should prevent bill creation when insufficient limit")
    void shouldPreventBillCreationWhenInsufficientLimit() {
        // Given: Create credit card with low limit
        Long creditCardId = createCreditCard("Low Limit Card", new BigDecimal("1000.00"), 10, 17);

        // When: Try to create bill that exceeds limit
        BillRequestDTO billRequest = new BillRequestDTO(
                "Expensive Purchase",
                LocalDateTime.of(2025, 1, 15, 10, 0),
                new BigDecimal("5000.00"),  // Exceeds 1000 limit
                12,
                null,
                creditCardId
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/bills",
                billRequest,
                String.class
        );

        // Then: Should fail with bad request
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // Verify: No bill or installments created
        long billCount = billRepository.count();
        long installmentCount = installmentRepository.count();

        assertThat(billCount).isEqualTo(0);
        assertThat(installmentCount).isEqualTo(0);
    }

    @Test
    @DisplayName("E2E: Should return 404 when credit card does not exist")
    void shouldReturn404WhenCreditCardDoesNotExist() {
        // Given: Non-existent credit card ID
        Long fakeCreditCardId = 99999L;

        // When: Get available limit
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/credit-cards/" + fakeCreditCardId + "/available-limit",
                String.class
        );

        // Then: Not found
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ==================== Helper Methods ====================

    private Long createCreditCard(String name, BigDecimal creditLimit, int closingDay, int dueDay) {
        CreditCardRequestDTO request = new CreditCardRequestDTO(name, creditLimit, closingDay, dueDay, true);
        ResponseEntity<CreditCardResponseDTO> response = restTemplate.postForEntity(
                "/credit-cards",
                request,
                CreditCardResponseDTO.class
        );
        return response.getBody().getId();
    }

    private void createBillWithCard(Long creditCardId, String name, BigDecimal amount, int installments) {
        BillRequestDTO request = new BillRequestDTO(
                name,
                LocalDateTime.now(),
                amount,
                installments,
                null,
                creditCardId
        );
        restTemplate.postForEntity("/bills", request, Object.class);
    }
}
