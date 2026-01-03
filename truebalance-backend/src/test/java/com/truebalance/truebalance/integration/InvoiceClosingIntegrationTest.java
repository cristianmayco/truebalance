package com.truebalance.truebalance.integration;

import com.truebalance.truebalance.application.dto.input.BillRequestDTO;
import com.truebalance.truebalance.application.dto.input.CreditCardRequestDTO;
import com.truebalance.truebalance.application.dto.input.PartialPaymentRequestDTO;
import com.truebalance.truebalance.application.dto.output.CreditCardResponseDTO;
import com.truebalance.truebalance.application.dto.output.InvoiceBalanceDTO;
import com.truebalance.truebalance.application.dto.output.InvoiceResponseDTO;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end integration tests for Invoice Closing flow.
 * Tests invoice closure, partial payments, and credit transfers.
 *
 * Uses @SpringBootTest with real database (H2) and full Spring context.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Invoice Closing Integration Tests")
class InvoiceClosingIntegrationTest {

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
    @DisplayName("E2E: Should close invoice and mark as unpaid when balance > 0")
    void shouldCloseInvoiceAndMarkAsUnpaidWhenBalancePositive() {
        // Given: Create card and bill to generate invoice
        Long creditCardId = createCreditCard("Test Card", new BigDecimal("5000.00"), 10, 17);
        createBillWithCard(creditCardId, "Purchase", new BigDecimal("1500.00"), 1);

        // Get the created invoice
        List<InvoiceEntity> invoices = invoiceRepository.findByCreditCardIdOrderByReferenceMonthDesc(creditCardId);
        assertThat(invoices).isNotEmpty();
        Long invoiceId = invoices.get(0).getId();

        // When: Close invoice
        ResponseEntity<InvoiceResponseDTO> response = restTemplate.postForEntity(
                "/invoices/" + invoiceId + "/close",
                null,
                InvoiceResponseDTO.class
        );

        // Then: Invoice closed but not paid (balance > 0)
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isClosed()).isTrue();
        assertThat(response.getBody().isPaid()).isFalse();

        // Verify in database
        InvoiceEntity closedInvoice = invoiceRepository.findById(invoiceId).orElseThrow();
        assertThat(closedInvoice.isClosed()).isTrue();
        assertThat(closedInvoice.isPaid()).isFalse();
    }

    @Test
    @DisplayName("E2E: Should close invoice and mark as paid when fully paid")
    void shouldCloseInvoiceAndMarkAsPaidWhenFullyPaid() {
        // Given: Create card and bill
        Long creditCardId = createCreditCard("Test Card", new BigDecimal("5000.00"), 10, 17);
        createBillWithCard(creditCardId, "Purchase", new BigDecimal("1000.00"), 1);

        // Get invoice
        List<InvoiceEntity> invoices = invoiceRepository.findByCreditCardIdOrderByReferenceMonthDesc(creditCardId);
        Long invoiceId = invoices.get(0).getId();

        // Register partial payment that covers full amount
        PartialPaymentRequestDTO paymentRequest = new PartialPaymentRequestDTO(
                new BigDecimal("1000.00"),
                "Full payment"
        );

        restTemplate.postForEntity(
                "/invoices/" + invoiceId + "/partial-payments",
                paymentRequest,
                Object.class
        );

        // When: Close invoice
        ResponseEntity<InvoiceResponseDTO> response = restTemplate.postForEntity(
                "/invoices/" + invoiceId + "/close",
                null,
                InvoiceResponseDTO.class
        );

        // Then: Invoice closed and paid
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isClosed()).isTrue();
        assertThat(response.getBody().isPaid()).isTrue();
    }

    @Test
    @DisplayName("E2E: Should transfer credit to next month when overpaid")
    void shouldTransferCreditToNextMonthWhenOverpaid() {
        // Given: Create card and bill
        Long creditCardId = createCreditCard("Test Card", new BigDecimal("5000.00"), 10, 17);
        createBillWithCard(creditCardId, "Purchase", new BigDecimal("1000.00"), 1);

        // Get invoice
        List<InvoiceEntity> invoices = invoiceRepository.findByCreditCardIdOrderByReferenceMonthDesc(creditCardId);
        Long invoiceId = invoices.get(0).getId();
        LocalDate referenceMonth = invoices.get(0).getReferenceMonth();

        // Register overpayment
        PartialPaymentRequestDTO paymentRequest = new PartialPaymentRequestDTO(
                new BigDecimal("1500.00"),  // $500 overpayment
                "Overpayment"
        );

        restTemplate.postForEntity(
                "/invoices/" + invoiceId + "/partial-payments",
                paymentRequest,
                Object.class
        );

        // When: Close invoice
        ResponseEntity<InvoiceResponseDTO> response = restTemplate.postForEntity(
                "/invoices/" + invoiceId + "/close",
                null,
                InvoiceResponseDTO.class
        );

        // Then: Invoice closed and paid
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isPaid()).isTrue();

        // Verify: Next month invoice created with negative balance (credit)
        LocalDate nextMonth = referenceMonth.plusMonths(1);
        List<InvoiceEntity> nextMonthInvoices = invoiceRepository.findByCreditCardIdAndReferenceMonth(creditCardId, nextMonth)
                .stream().toList();

        assertThat(nextMonthInvoices).isNotEmpty();
        InvoiceEntity nextMonthInvoice = nextMonthInvoices.get(0);
        assertThat(nextMonthInvoice.getPreviousBalance()).isEqualByComparingTo("-500.00");  // Credit transferred
    }

    // ==================== Balance Calculation Tests ====================

    @Test
    @DisplayName("E2E: Should calculate invoice balance correctly with partial payments")
    void shouldCalculateInvoiceBalanceCorrectlyWithPartialPayments() {
        // Given: Create card and bill
        Long creditCardId = createCreditCard("Test Card", new BigDecimal("5000.00"), 10, 17);
        createBillWithCard(creditCardId, "Purchase", new BigDecimal("2000.00"), 1);

        // Get invoice
        List<InvoiceEntity> invoices = invoiceRepository.findByCreditCardIdOrderByReferenceMonthDesc(creditCardId);
        Long invoiceId = invoices.get(0).getId();

        // Register partial payment
        PartialPaymentRequestDTO paymentRequest = new PartialPaymentRequestDTO(
                new BigDecimal("500.00"),
                "Partial payment"
        );

        restTemplate.postForEntity(
                "/invoices/" + invoiceId + "/partial-payments",
                paymentRequest,
                Object.class
        );

        // When: Get invoice balance
        ResponseEntity<InvoiceBalanceDTO> response = restTemplate.getForEntity(
                "/invoices/" + invoiceId + "/balance",
                InvoiceBalanceDTO.class
        );

        // Then: Balance calculated correctly (2000 - 500 = 1500)
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTotalAmount()).isEqualByComparingTo("2000.00");
        assertThat(response.getBody().getPartialPaymentsTotal()).isEqualByComparingTo("500.00");
        assertThat(response.getBody().getCurrentBalance()).isEqualByComparingTo("1500.00");
        assertThat(response.getBody().getPartialPaymentsCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("E2E: Should handle multiple partial payments")
    void shouldHandleMultiplePartialPayments() {
        // Given: Create card and bill
        Long creditCardId = createCreditCard("Test Card", new BigDecimal("5000.00"), 10, 17);
        createBillWithCard(creditCardId, "Purchase", new BigDecimal("1500.00"), 1);

        // Get invoice
        List<InvoiceEntity> invoices = invoiceRepository.findByCreditCardIdOrderByReferenceMonthDesc(creditCardId);
        Long invoiceId = invoices.get(0).getId();

        // Register multiple payments
        restTemplate.postForEntity(
                "/invoices/" + invoiceId + "/partial-payments",
                new PartialPaymentRequestDTO(new BigDecimal("300.00"), "Payment 1"),
                Object.class
        );

        restTemplate.postForEntity(
                "/invoices/" + invoiceId + "/partial-payments",
                new PartialPaymentRequestDTO(new BigDecimal("200.00"), "Payment 2"),
                Object.class
        );

        restTemplate.postForEntity(
                "/invoices/" + invoiceId + "/partial-payments",
                new PartialPaymentRequestDTO(new BigDecimal("500.00"), "Payment 3"),
                Object.class
        );

        // When: Get invoice balance
        ResponseEntity<InvoiceBalanceDTO> response = restTemplate.getForEntity(
                "/invoices/" + invoiceId + "/balance",
                InvoiceBalanceDTO.class
        );

        // Then: All payments summed (1500 - 1000 = 500)
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getPartialPaymentsTotal()).isEqualByComparingTo("1000.00");
        assertThat(response.getBody().getCurrentBalance()).isEqualByComparingTo("500.00");
        assertThat(response.getBody().getPartialPaymentsCount()).isEqualTo(3);
    }

    // ==================== Validation Tests ====================

    @Test
    @DisplayName("E2E: Should return 400 when trying to close already closed invoice")
    void shouldReturn400WhenTryingToCloseAlreadyClosedInvoice() {
        // Given: Create card, bill, and close invoice
        Long creditCardId = createCreditCard("Test Card", new BigDecimal("5000.00"), 10, 17);
        createBillWithCard(creditCardId, "Purchase", new BigDecimal("1000.00"), 1);

        List<InvoiceEntity> invoices = invoiceRepository.findByCreditCardIdOrderByReferenceMonthDesc(creditCardId);
        Long invoiceId = invoices.get(0).getId();

        // Close invoice first time
        restTemplate.postForEntity("/invoices/" + invoiceId + "/close", null, InvoiceResponseDTO.class);

        // When: Try to close again
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/invoices/" + invoiceId + "/close",
                null,
                String.class
        );

        // Then: Bad request
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("E2E: Should return 404 when closing non-existent invoice")
    void shouldReturn404WhenClosingNonExistentInvoice() {
        // Given: Non-existent invoice ID
        Long fakeInvoiceId = 99999L;

        // When: Try to close
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/invoices/" + fakeInvoiceId + "/close",
                null,
                String.class
        );

        // Then: Not found
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ==================== Previous Balance Tests ====================

    @Test
    @DisplayName("E2E: Should include previous balance in current balance calculation")
    void shouldIncludePreviousBalanceInCalculation() {
        // Given: Create card with closing=10, due=17
        // NOTE: Since dueDay(17) > closingDay(10), bills go to NEXT month's invoice
        Long creditCardId = createCreditCard("Test Card", new BigDecimal("5000.00"), 10, 17);

        // Create first bill in DECEMBER (executionDate = Dec 5)
        // dueDate = Dec 17, and 17 > 10, so referenceMonth = Jan 2025
        BillRequestDTO bill1Request = new BillRequestDTO(
                "Month 1 Purchase",
                LocalDateTime.of(2024, 12, 5, 10, 0),  // December
                new BigDecimal("1000.00"),
                1,
                null,
                creditCardId
        );
        restTemplate.postForEntity("/bills", bill1Request, Object.class);

        // Get month 1 invoice (Jan 2025)
        List<InvoiceEntity> invoices = invoiceRepository.findByCreditCardIdOrderByReferenceMonthDesc(creditCardId);
        Long month1InvoiceId = invoices.get(0).getId();

        // Close month 1 invoice without payment (balance carries over to Feb 2025)
        restTemplate.postForEntity("/invoices/" + month1InvoiceId + "/close", null, InvoiceResponseDTO.class);

        // Create second bill in JANUARY (executionDate = Jan 5)
        // dueDate = Jan 17, and 17 > 10, so referenceMonth = Feb 2025
        BillRequestDTO bill2Request = new BillRequestDTO(
                "Month 2 Purchase",
                LocalDateTime.of(2025, 1, 5, 10, 0),  // January
                new BigDecimal("500.00"),
                1,
                null,
                creditCardId
        );
        restTemplate.postForEntity("/bills", bill2Request, Object.class);

        // Get month 2 invoice (Feb 2025)
        invoices = invoiceRepository.findByCreditCardIdOrderByReferenceMonthDesc(creditCardId);
        Long month2InvoiceId = invoices.get(0).getId();

        // When: Get month 2 balance
        ResponseEntity<InvoiceBalanceDTO> response = restTemplate.getForEntity(
                "/invoices/" + month2InvoiceId + "/balance",
                InvoiceBalanceDTO.class
        );

        // Then: Balance includes previous month (500 + 1000 = 1500)
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getTotalAmount()).isEqualByComparingTo("500.00");
        assertThat(response.getBody().getPreviousBalance()).isEqualByComparingTo("1000.00");
        assertThat(response.getBody().getCurrentBalance()).isEqualByComparingTo("1500.00");
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
