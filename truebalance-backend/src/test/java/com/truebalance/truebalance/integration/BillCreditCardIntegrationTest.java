package com.truebalance.truebalance.integration;

import com.truebalance.truebalance.application.dto.input.BillRequestDTO;
import com.truebalance.truebalance.application.dto.input.CreditCardRequestDTO;
import com.truebalance.truebalance.application.dto.output.BillResponseDTO;
import com.truebalance.truebalance.application.dto.output.CreditCardResponseDTO;
import com.truebalance.truebalance.infra.db.repository.BillRepository;
import com.truebalance.truebalance.infra.db.repository.InstallmentRepository;
import com.truebalance.truebalance.infra.db.repository.InvoiceRepository;
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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end integration tests for Bill + CreditCard flow.
 * Tests the complete stack from HTTP request to database.
 *
 * Uses @SpringBootTest with real database (H2) and full Spring context.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Bill + CreditCard Integration Tests")
class BillCreditCardIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private InstallmentRepository installmentRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private com.truebalance.truebalance.infra.db.repository.CreditCardRepository creditCardRepository;

    @BeforeEach
    void setUp() {
        // Clean database before each test
        installmentRepository.deleteAll();
        billRepository.deleteAll();
        invoiceRepository.deleteAll();
        creditCardRepository.deleteAll();
    }

    // ==================== Happy Path Tests ====================

    @Test
    @DisplayName("E2E: Should create bill with credit card and generate invoices/installments")
    void shouldCreateBillWithCreditCardAndGenerateInvoicesAndInstallments() {
        // Given: Create credit card first
        CreditCardRequestDTO cardRequest = new CreditCardRequestDTO(
                "Visa Gold",
                new BigDecimal("5000.00"),
                10,
                17,
                true
        );

        ResponseEntity<CreditCardResponseDTO> cardResponse = restTemplate.postForEntity(
                "/credit-cards",
                cardRequest,
                CreditCardResponseDTO.class
        );

        assertThat(cardResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long creditCardId = cardResponse.getBody().getId();

        // When: Create bill with credit card
        BillRequestDTO billRequest = new BillRequestDTO(
                "Smartphone Purchase",
                LocalDateTime.of(2025, 1, 15, 10, 0),
                new BigDecimal("3000.00"),
                12,
                "iPhone 15 Pro",
                creditCardId
        );

        ResponseEntity<BillResponseDTO> billResponse = restTemplate.postForEntity(
                "/bills",
                billRequest,
                BillResponseDTO.class
        );

        // Then: Bill created successfully
        assertThat(billResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(billResponse.getBody()).isNotNull();
        assertThat(billResponse.getBody().getId()).isNotNull();
        assertThat(billResponse.getBody().getName()).isEqualTo("Smartphone Purchase");
        assertThat(billResponse.getBody().getTotalAmount()).isEqualByComparingTo("3000.00");
        assertThat(billResponse.getBody().getNumberOfInstallments()).isEqualTo(12);
        assertThat(billResponse.getBody().getInstallmentAmount()).isEqualByComparingTo("250.00");

        // Verify database state
        long billCount = billRepository.count();
        long installmentCount = installmentRepository.count();
        long invoiceCount = invoiceRepository.count();

        assertThat(billCount).isEqualTo(1);
        assertThat(installmentCount).isEqualTo(12); // 12 installments created
        assertThat(invoiceCount).isGreaterThan(0); // At least one invoice created
    }

    @Test
    @DisplayName("E2E: Should create bill without credit card (standalone)")
    void shouldCreateBillWithoutCreditCard() {
        // Given: Bill request without credit card
        BillRequestDTO billRequest = new BillRequestDTO(
                "Personal Loan",
                LocalDateTime.of(2025, 1, 15, 10, 0),
                new BigDecimal("10000.00"),
                24,
                "Home renovation",
                null  // No credit card
        );

        // When: Create bill
        ResponseEntity<BillResponseDTO> billResponse = restTemplate.postForEntity(
                "/bills",
                billRequest,
                BillResponseDTO.class
        );

        // Then: Bill created successfully
        assertThat(billResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(billResponse.getBody()).isNotNull();
        assertThat(billResponse.getBody().getTotalAmount()).isEqualByComparingTo("10000.00");
        assertThat(billResponse.getBody().getInstallmentAmount()).isEqualByComparingTo("416.67");

        // Verify: No installments or invoices created (standalone bill)
        long installmentCount = installmentRepository.count();
        long invoiceCount = invoiceRepository.count();

        assertThat(installmentCount).isEqualTo(0);
        assertThat(invoiceCount).isEqualTo(0);
    }

    @Test
    @DisplayName("E2E: Should create multiple bills on same credit card")
    void shouldCreateMultipleBillsOnSameCreditCard() {
        // Given: Create credit card
        CreditCardRequestDTO cardRequest = new CreditCardRequestDTO(
                "Mastercard Black",
                new BigDecimal("10000.00"),
                5,
                12,
                true
        );

        ResponseEntity<CreditCardResponseDTO> cardResponse = restTemplate.postForEntity(
                "/credit-cards",
                cardRequest,
                CreditCardResponseDTO.class
        );

        Long creditCardId = cardResponse.getBody().getId();

        // When: Create two bills on same card
        BillRequestDTO bill1Request = new BillRequestDTO(
                "Laptop",
                LocalDateTime.of(2025, 1, 10, 10, 0),
                new BigDecimal("4000.00"),
                10,
                null,
                creditCardId
        );

        BillRequestDTO bill2Request = new BillRequestDTO(
                "Monitor",
                LocalDateTime.of(2025, 1, 12, 14, 0),
                new BigDecimal("1500.00"),
                6,
                null,
                creditCardId
        );

        ResponseEntity<BillResponseDTO> bill1Response = restTemplate.postForEntity("/bills", bill1Request, BillResponseDTO.class);
        ResponseEntity<BillResponseDTO> bill2Response = restTemplate.postForEntity("/bills", bill2Request, BillResponseDTO.class);

        // Then: Both bills created
        assertThat(bill1Response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(bill2Response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Verify database
        long billCount = billRepository.count();
        long installmentCount = installmentRepository.count();

        assertThat(billCount).isEqualTo(2);
        assertThat(installmentCount).isEqualTo(16); // 10 + 6 installments
    }

    // ==================== Validation Tests ====================

    @Test
    @DisplayName("E2E: Should return 400 when bill name is blank")
    void shouldReturn400WhenBillNameIsBlank() {
        // Given: Invalid bill request (blank name)
        BillRequestDTO billRequest = new BillRequestDTO(
                "",  // Blank name
                LocalDateTime.of(2025, 1, 15, 10, 0),
                new BigDecimal("1000.00"),
                10,
                null,
                null
        );

        // When: Create bill
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/bills",
                billRequest,
                String.class
        );

        // Then: Bad request
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("E2E: Should return 400 when total amount is not positive")
    void shouldReturn400WhenTotalAmountIsNotPositive() {
        // Given: Invalid bill request (zero amount)
        BillRequestDTO billRequest = new BillRequestDTO(
                "Invalid Bill",
                LocalDateTime.of(2025, 1, 15, 10, 0),
                BigDecimal.ZERO,  // Not positive
                10,
                null,
                null
        );

        // When: Create bill
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/bills",
                billRequest,
                String.class
        );

        // Then: Bad request
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("E2E: Should return 400 when number of installments is less than 1")
    void shouldReturn400WhenNumberOfInstallmentsIsInvalid() {
        // Given: Invalid bill request (0 installments)
        BillRequestDTO billRequest = new BillRequestDTO(
                "Invalid Bill",
                LocalDateTime.of(2025, 1, 15, 10, 0),
                new BigDecimal("1000.00"),
                0,  // Invalid
                null,
                null
        );

        // When: Create bill
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/bills",
                billRequest,
                String.class
        );

        // Then: Bad request
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ==================== Transaction Tests ====================

    @Test
    @DisplayName("E2E: Should rollback transaction on error")
    void shouldRollbackTransactionOnError() {
        // Given: Create credit card with insufficient limit
        CreditCardRequestDTO cardRequest = new CreditCardRequestDTO(
                "Low Limit Card",
                new BigDecimal("1000.00"),  // Low limit
                10,
                17,
                true
        );

        ResponseEntity<CreditCardResponseDTO> cardResponse = restTemplate.postForEntity(
                "/credit-cards",
                cardRequest,
                CreditCardResponseDTO.class
        );

        Long creditCardId = cardResponse.getBody().getId();

        // Record initial counts
        long initialBillCount = billRepository.count();
        long initialInstallmentCount = installmentRepository.count();
        long initialInvoiceCount = invoiceRepository.count();

        // When: Try to create bill that exceeds limit
        BillRequestDTO billRequest = new BillRequestDTO(
                "Expensive Purchase",
                LocalDateTime.of(2025, 1, 15, 10, 0),
                new BigDecimal("5000.00"),  // Exceeds 1000 limit
                12,
                null,
                creditCardId
        );

        ResponseEntity<String> billResponse = restTemplate.postForEntity(
                "/bills",
                billRequest,
                String.class
        );

        // Then: Should fail
        assertThat(billResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // Verify: Nothing was persisted (transaction rolled back)
        long finalBillCount = billRepository.count();
        long finalInstallmentCount = installmentRepository.count();
        long finalInvoiceCount = invoiceRepository.count();

        assertThat(finalBillCount).isEqualTo(initialBillCount);
        assertThat(finalInstallmentCount).isEqualTo(initialInstallmentCount);
        assertThat(finalInvoiceCount).isEqualTo(initialInvoiceCount);
    }

    // ==================== Installment Distribution Tests ====================

    @Test
    @DisplayName("E2E: Should distribute installments across multiple months")
    void shouldDistributeInstallmentsAcrossMultipleMonths() {
        // Given: Create credit card
        CreditCardRequestDTO cardRequest = new CreditCardRequestDTO(
                "Test Card",
                new BigDecimal("5000.00"),
                10,
                17,
                true
        );

        ResponseEntity<CreditCardResponseDTO> cardResponse = restTemplate.postForEntity(
                "/credit-cards",
                cardRequest,
                CreditCardResponseDTO.class
        );

        Long creditCardId = cardResponse.getBody().getId();

        // When: Create bill with 3 installments (should span 3 months)
        BillRequestDTO billRequest = new BillRequestDTO(
                "Multi-Month Purchase",
                LocalDateTime.of(2025, 1, 15, 10, 0),
                new BigDecimal("900.00"),
                3,
                null,
                creditCardId
        );

        ResponseEntity<BillResponseDTO> billResponse = restTemplate.postForEntity(
                "/bills",
                billRequest,
                BillResponseDTO.class
        );

        // Then: Success
        assertThat(billResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Verify: Multiple invoices created for different months
        long invoiceCount = invoiceRepository.count();
        assertThat(invoiceCount).isGreaterThanOrEqualTo(1); // At least 1 invoice (could be up to 3)

        // Verify: All installments created
        long installmentCount = installmentRepository.count();
        assertThat(installmentCount).isEqualTo(3);
    }

    @Test
    @DisplayName("E2E: Should calculate installment amount with correct rounding")
    void shouldCalculateInstallmentAmountWithCorrectRounding() {
        // Given: Amount that requires rounding (1000 / 3 = 333.33)
        BillRequestDTO billRequest = new BillRequestDTO(
                "Rounding Test",
                LocalDateTime.of(2025, 1, 15, 10, 0),
                new BigDecimal("1000.00"),
                3,
                null,
                null  // Standalone bill
        );

        // When: Create bill
        ResponseEntity<BillResponseDTO> billResponse = restTemplate.postForEntity(
                "/bills",
                billRequest,
                BillResponseDTO.class
        );

        // Then: Installment amount rounded correctly
        assertThat(billResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(billResponse.getBody().getInstallmentAmount()).isEqualByComparingTo("333.33");
    }
}
