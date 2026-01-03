package com.truebalance.truebalance.domain.usecase.partialpayment;

import com.truebalance.truebalance.domain.entity.CreditCard;
import com.truebalance.truebalance.domain.entity.Invoice;
import com.truebalance.truebalance.domain.entity.PartialPayment;
import com.truebalance.truebalance.domain.port.CreditCardRepositoryPort;
import com.truebalance.truebalance.domain.port.InvoiceRepositoryPort;
import com.truebalance.truebalance.domain.port.PartialPaymentRepositoryPort;
import com.truebalance.truebalance.domain.usecase.AvailableLimitResult;
import com.truebalance.truebalance.domain.usecase.GetAvailableLimit;
import com.truebalance.truebalance.domain.usecase.RegisterPartialPayment;
import com.truebalance.truebalance.domain.usecase.RegisterPartialPayment.RegisterPartialPaymentResult;
import com.truebalance.truebalance.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for RegisterPartialPayment use case.
 *
 * This use case handles registration of partial payments on open invoices
 * with multiple validation rules.
 *
 * Business Rules Tested:
 * - BR-PP-001: Validates invoice is open and credit card allows partial payments
 * - BR-PP-002: Amount can exceed invoice balance (creates credit)
 * - BR-PP-006: Calculates and returns available limit in real-time
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterPartialPayment - Complex Validation Tests")
class RegisterPartialPaymentTest {

    @Mock
    private PartialPaymentRepositoryPort partialPaymentRepository;

    @Mock
    private InvoiceRepositoryPort invoiceRepository;

    @Mock
    private CreditCardRepositoryPort creditCardRepository;

    @Mock
    private GetAvailableLimit getAvailableLimit;

    @Captor
    private ArgumentCaptor<PartialPayment> partialPaymentCaptor;

    private RegisterPartialPayment useCase;

    @BeforeEach
    void setUp() {
        useCase = new RegisterPartialPayment(
                partialPaymentRepository,
                invoiceRepository,
                creditCardRepository,
                getAvailableLimit
        );

        // Default stub for getAvailableLimit - tests can override if needed
        // Use lenient() to avoid UnnecessaryStubbingException in tests that throw exceptions early
        lenient().when(getAvailableLimit.execute(anyLong())).thenAnswer(invocation -> {
            Long creditCardId = invocation.getArgument(0);
            return new AvailableLimitResult(
                    creditCardId,
                    new BigDecimal("5000.00"),
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    new BigDecimal("5000.00")
            );
        });
    }

    // ========== HAPPY PATH TESTS ==========

    @Test
    @DisplayName("Should register partial payment successfully")
    void shouldRegisterPartialPaymentSuccessfully() {
        // Given: Valid invoice and credit card, partial payment
        Long invoiceId = 1L;
        Long creditCardId = 1L;

        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, creditCardId, LocalDate.of(2025, 1, 1), new BigDecimal("1000.00"));
        invoice.setClosed(false);

        CreditCard creditCard = TestDataBuilder.createCreditCard(creditCardId, "Test Card", new BigDecimal("5000.00"), 10, 17);
        creditCard.setAllowsPartialPayment(true);

        PartialPayment payment = new PartialPayment();
        payment.setAmount(new BigDecimal("500.00"));
        payment.setDescription("Partial payment test");

        PartialPayment savedPayment = TestDataBuilder.createPartialPayment(1L, invoiceId, new BigDecimal("500.00"), LocalDateTime.now());

        AvailableLimitResult limitResult = new AvailableLimitResult(
                creditCardId,
                new BigDecimal("5000.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("5000.00")
        );

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(creditCardRepository.findById(creditCardId)).thenReturn(Optional.of(creditCard));
        when(partialPaymentRepository.save(any(PartialPayment.class))).thenReturn(savedPayment);
        when(getAvailableLimit.execute(creditCardId)).thenReturn(limitResult);

        // When
        RegisterPartialPaymentResult result = useCase.execute(invoiceId, payment);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPartialPayment()).isNotNull();
        assertThat(result.getPartialPayment().getAmount()).isEqualByComparingTo("500.00");
        assertThat(result.getAvailableLimit()).isEqualByComparingTo("5000.00");

        verify(partialPaymentRepository).save(partialPaymentCaptor.capture());
        PartialPayment capturedPayment = partialPaymentCaptor.getValue();
        assertThat(capturedPayment.getInvoiceId()).isEqualTo(invoiceId);
        assertThat(capturedPayment.getPaymentDate()).isNotNull();
    }

    @Test
    @DisplayName("Should allow multiple partial payments on same invoice")
    void shouldAllowMultiplePartialPaymentsOnSameInvoice() {
        // Given: Invoice with existing partial payments, registering another
        Long invoiceId = 1L;
        Long creditCardId = 1L;

        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, creditCardId, LocalDate.of(2025, 1, 1), new BigDecimal("1000.00"));
        invoice.setClosed(false);

        CreditCard creditCard = TestDataBuilder.createCreditCard(creditCardId, "Test Card", new BigDecimal("5000.00"), 10, 17);
        creditCard.setAllowsPartialPayment(true);

        PartialPayment newPayment = new PartialPayment();
        newPayment.setAmount(new BigDecimal("200.00"));
        newPayment.setDescription("Second partial payment");

        PartialPayment savedPayment = TestDataBuilder.createPartialPayment(2L, invoiceId, new BigDecimal("200.00"), LocalDateTime.now());

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(creditCardRepository.findById(creditCardId)).thenReturn(Optional.of(creditCard));
        when(partialPaymentRepository.save(any(PartialPayment.class))).thenReturn(savedPayment);

        // When
        RegisterPartialPaymentResult result = useCase.execute(invoiceId, newPayment);

        // Then: Should succeed
        assertThat(result.getPartialPayment().getAmount()).isEqualByComparingTo("200.00");
        verify(partialPaymentRepository).save(any(PartialPayment.class));
    }

    @Test
    @DisplayName("BR-PP-002: Should allow payment amount to exceed invoice balance")
    void shouldAllowPaymentAmountToExceedInvoiceBalance() {
        // Given: Invoice with 1000.00 balance, payment of 1500.00 (creates 500.00 credit)
        Long invoiceId = 1L;
        Long creditCardId = 1L;

        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, creditCardId, LocalDate.of(2025, 1, 1), new BigDecimal("1000.00"));
        invoice.setClosed(false);

        CreditCard creditCard = TestDataBuilder.createCreditCard(creditCardId, "Test Card", new BigDecimal("5000.00"), 10, 17);
        creditCard.setAllowsPartialPayment(true);

        PartialPayment payment = new PartialPayment();
        payment.setAmount(new BigDecimal("1500.00")); // Exceeds balance
        payment.setDescription("Overpayment");

        PartialPayment savedPayment = TestDataBuilder.createPartialPayment(1L, invoiceId, new BigDecimal("1500.00"), LocalDateTime.now());

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(creditCardRepository.findById(creditCardId)).thenReturn(Optional.of(creditCard));
        when(partialPaymentRepository.save(any(PartialPayment.class))).thenReturn(savedPayment);

        // When
        RegisterPartialPaymentResult result = useCase.execute(invoiceId, payment);

        // Then: Should succeed (no validation against balance)
        assertThat(result.getPartialPayment().getAmount()).isEqualByComparingTo("1500.00");
    }

    // ========== VALIDATION TESTS ==========

    @Test
    @DisplayName("Should throw exception when invoice not found")
    void shouldThrowExceptionWhenInvoiceNotFound() {
        // Given: Invoice does not exist
        Long nonExistentInvoiceId = 999L;
        PartialPayment payment = new PartialPayment();
        payment.setAmount(new BigDecimal("100.00"));

        when(invoiceRepository.findById(nonExistentInvoiceId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> useCase.execute(nonExistentInvoiceId, payment))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Invoice not found");

        verify(partialPaymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when credit card not found")
    void shouldThrowExceptionWhenCreditCardNotFound() {
        // Given: Invoice exists but credit card does not
        Long invoiceId = 1L;
        Long creditCardId = 1L;

        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, creditCardId, LocalDate.of(2025, 1, 1), new BigDecimal("1000.00"));

        PartialPayment payment = new PartialPayment();
        payment.setAmount(new BigDecimal("100.00"));

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(creditCardRepository.findById(creditCardId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> useCase.execute(invoiceId, payment))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Credit card not found");

        verify(partialPaymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("BR-PP-001: Should throw exception when credit card does not allow partial payments")
    void shouldThrowExceptionWhenCreditCardDoesNotAllowPartialPayments() {
        // Given: Credit card with allowsPartialPayment = false
        Long invoiceId = 1L;
        Long creditCardId = 1L;

        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, creditCardId, LocalDate.of(2025, 1, 1), new BigDecimal("1000.00"));
        invoice.setClosed(false);

        CreditCard creditCard = TestDataBuilder.createCreditCard(creditCardId, "Restricted Card", new BigDecimal("5000.00"), 10, 17);
        creditCard.setAllowsPartialPayment(false); // Does not allow partial payments

        PartialPayment payment = new PartialPayment();
        payment.setAmount(new BigDecimal("500.00"));

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(creditCardRepository.findById(creditCardId)).thenReturn(Optional.of(creditCard));

        // When & Then
        assertThatThrownBy(() -> useCase.execute(invoiceId, payment))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Credit card does not allow partial payments");

        verify(partialPaymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("BR-PP-001: Should throw exception when invoice is closed")
    void shouldThrowExceptionWhenInvoiceIsClosed() {
        // Given: Invoice is closed
        Long invoiceId = 1L;
        Long creditCardId = 1L;

        Invoice closedInvoice = TestDataBuilder.createInvoice(invoiceId, creditCardId, LocalDate.of(2025, 1, 1), new BigDecimal("1000.00"));
        closedInvoice.setClosed(true); // Closed

        CreditCard creditCard = TestDataBuilder.createCreditCard(creditCardId, "Test Card", new BigDecimal("5000.00"), 10, 17);
        creditCard.setAllowsPartialPayment(true);

        PartialPayment payment = new PartialPayment();
        payment.setAmount(new BigDecimal("500.00"));

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(closedInvoice));
        when(creditCardRepository.findById(creditCardId)).thenReturn(Optional.of(creditCard));

        // When & Then
        assertThatThrownBy(() -> useCase.execute(invoiceId, payment))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Invoice is closed. Cannot register partial payments on closed invoices");

        verify(partialPaymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when payment amount is zero")
    void shouldThrowExceptionWhenPaymentAmountIsZero() {
        // Given: Payment with zero amount
        Long invoiceId = 1L;
        Long creditCardId = 1L;

        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, creditCardId, LocalDate.of(2025, 1, 1), new BigDecimal("1000.00"));
        invoice.setClosed(false);

        CreditCard creditCard = TestDataBuilder.createCreditCard(creditCardId, "Test Card", new BigDecimal("5000.00"), 10, 17);
        creditCard.setAllowsPartialPayment(true);

        PartialPayment payment = new PartialPayment();
        payment.setAmount(BigDecimal.ZERO); // Zero amount

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(creditCardRepository.findById(creditCardId)).thenReturn(Optional.of(creditCard));

        // When & Then
        assertThatThrownBy(() -> useCase.execute(invoiceId, payment))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Payment amount must be greater than zero");

        verify(partialPaymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when payment amount is negative")
    void shouldThrowExceptionWhenPaymentAmountIsNegative() {
        // Given: Payment with negative amount
        Long invoiceId = 1L;
        Long creditCardId = 1L;

        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, creditCardId, LocalDate.of(2025, 1, 1), new BigDecimal("1000.00"));
        invoice.setClosed(false);

        CreditCard creditCard = TestDataBuilder.createCreditCard(creditCardId, "Test Card", new BigDecimal("5000.00"), 10, 17);
        creditCard.setAllowsPartialPayment(true);

        PartialPayment payment = new PartialPayment();
        payment.setAmount(new BigDecimal("-100.00")); // Negative amount

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(creditCardRepository.findById(creditCardId)).thenReturn(Optional.of(creditCard));

        // When & Then
        assertThatThrownBy(() -> useCase.execute(invoiceId, payment))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Payment amount must be greater than zero");

        verify(partialPaymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when payment amount is null")
    void shouldThrowExceptionWhenPaymentAmountIsNull() {
        // Given: Payment with null amount
        Long invoiceId = 1L;
        Long creditCardId = 1L;

        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, creditCardId, LocalDate.of(2025, 1, 1), new BigDecimal("1000.00"));
        invoice.setClosed(false);

        CreditCard creditCard = TestDataBuilder.createCreditCard(creditCardId, "Test Card", new BigDecimal("5000.00"), 10, 17);
        creditCard.setAllowsPartialPayment(true);

        PartialPayment payment = new PartialPayment();
        payment.setAmount(null); // Null amount

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(creditCardRepository.findById(creditCardId)).thenReturn(Optional.of(creditCard));

        // When & Then
        assertThatThrownBy(() -> useCase.execute(invoiceId, payment))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Payment amount must be greater than zero");

        verify(partialPaymentRepository, never()).save(any());
    }

    // ========== BUSINESS RULES TESTS ==========

    @Test
    @DisplayName("BR-PP-006: Should return available limit in result")
    void shouldReturnAvailableLimitInResult() {
        // Given
        Long invoiceId = 1L;
        Long creditCardId = 1L;

        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, creditCardId, LocalDate.of(2025, 1, 1), new BigDecimal("1000.00"));
        invoice.setClosed(false);

        CreditCard creditCard = TestDataBuilder.createCreditCard(creditCardId, "Test Card", new BigDecimal("7500.00"), 10, 17);
        creditCard.setAllowsPartialPayment(true);

        PartialPayment payment = new PartialPayment();
        payment.setAmount(new BigDecimal("300.00"));

        PartialPayment savedPayment = TestDataBuilder.createPartialPayment(1L, invoiceId, new BigDecimal("300.00"), LocalDateTime.now());

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(creditCardRepository.findById(creditCardId)).thenReturn(Optional.of(creditCard));
        when(partialPaymentRepository.save(any(PartialPayment.class))).thenReturn(savedPayment);
        when(getAvailableLimit.execute(creditCardId)).thenReturn(
                new AvailableLimitResult(creditCardId, new BigDecimal("7500.00"), BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("7500.00"))
        );

        // When
        RegisterPartialPaymentResult result = useCase.execute(invoiceId, payment);

        // Then: Available limit should be returned (simplified: creditLimit)
        assertThat(result.getAvailableLimit()).isNotNull();
        assertThat(result.getAvailableLimit()).isEqualByComparingTo("7500.00");
    }

    // ========== INTEGRATION TESTS ==========

    @Test
    @DisplayName("Should set invoiceId and paymentDate automatically")
    void shouldSetInvoiceIdAndPaymentDateAutomatically() {
        // Given
        Long invoiceId = 42L;
        Long creditCardId = 1L;

        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, creditCardId, LocalDate.of(2025, 1, 1), new BigDecimal("1000.00"));
        invoice.setClosed(false);

        CreditCard creditCard = TestDataBuilder.createCreditCard(creditCardId, "Test Card", new BigDecimal("5000.00"), 10, 17);
        creditCard.setAllowsPartialPayment(true);

        PartialPayment payment = new PartialPayment();
        payment.setAmount(new BigDecimal("250.00"));
        payment.setDescription("Test payment");
        // Note: invoiceId and paymentDate NOT set initially

        PartialPayment savedPayment = TestDataBuilder.createPartialPayment(1L, invoiceId, new BigDecimal("250.00"), LocalDateTime.now());

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(creditCardRepository.findById(creditCardId)).thenReturn(Optional.of(creditCard));
        when(partialPaymentRepository.save(any(PartialPayment.class))).thenReturn(savedPayment);

        // When
        useCase.execute(invoiceId, payment);

        // Then: Verify invoiceId and paymentDate were set before saving
        verify(partialPaymentRepository).save(partialPaymentCaptor.capture());
        PartialPayment capturedPayment = partialPaymentCaptor.getValue();

        assertThat(capturedPayment.getInvoiceId()).isEqualTo(42L);
        assertThat(capturedPayment.getPaymentDate()).isNotNull();
        assertThat(capturedPayment.getPaymentDate()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should preserve payment description and amount")
    void shouldPreservePaymentDescriptionAndAmount() {
        // Given
        Long invoiceId = 1L;
        Long creditCardId = 1L;

        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, creditCardId, LocalDate.of(2025, 1, 1), new BigDecimal("1000.00"));
        invoice.setClosed(false);

        CreditCard creditCard = TestDataBuilder.createCreditCard(creditCardId, "Test Card", new BigDecimal("5000.00"), 10, 17);
        creditCard.setAllowsPartialPayment(true);

        PartialPayment payment = new PartialPayment();
        payment.setAmount(new BigDecimal("678.90"));
        payment.setDescription("Custom payment description");

        PartialPayment savedPayment = TestDataBuilder.createPartialPayment(1L, invoiceId, new BigDecimal("678.90"), LocalDateTime.now());
        savedPayment.setDescription("Custom payment description");

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(creditCardRepository.findById(creditCardId)).thenReturn(Optional.of(creditCard));
        when(partialPaymentRepository.save(any(PartialPayment.class))).thenReturn(savedPayment);

        // When
        RegisterPartialPaymentResult result = useCase.execute(invoiceId, payment);

        // Then
        verify(partialPaymentRepository).save(partialPaymentCaptor.capture());
        PartialPayment capturedPayment = partialPaymentCaptor.getValue();

        assertThat(capturedPayment.getAmount()).isEqualByComparingTo("678.90");
        assertThat(capturedPayment.getDescription()).isEqualTo("Custom payment description");
    }

    @Test
    @DisplayName("Should call repositories in correct order")
    void shouldCallRepositoriesInCorrectOrder() {
        // Given
        Long invoiceId = 1L;
        Long creditCardId = 1L;

        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, creditCardId, LocalDate.of(2025, 1, 1), new BigDecimal("1000.00"));
        invoice.setClosed(false);

        CreditCard creditCard = TestDataBuilder.createCreditCard(creditCardId, "Test Card", new BigDecimal("5000.00"), 10, 17);
        creditCard.setAllowsPartialPayment(true);

        PartialPayment payment = new PartialPayment();
        payment.setAmount(new BigDecimal("100.00"));

        PartialPayment savedPayment = TestDataBuilder.createPartialPayment();

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(creditCardRepository.findById(creditCardId)).thenReturn(Optional.of(creditCard));
        when(partialPaymentRepository.save(any(PartialPayment.class))).thenReturn(savedPayment);

        // When
        useCase.execute(invoiceId, payment);

        // Then: Verify correct order
        var inOrder = inOrder(invoiceRepository, creditCardRepository, partialPaymentRepository);
        inOrder.verify(invoiceRepository).findById(invoiceId);
        inOrder.verify(creditCardRepository).findById(creditCardId);
        inOrder.verify(partialPaymentRepository).save(any(PartialPayment.class));
    }

    @Test
    @DisplayName("Should return result with both payment and available limit")
    void shouldReturnResultWithBothPaymentAndAvailableLimit() {
        // Given
        Long invoiceId = 1L;
        Long creditCardId = 1L;

        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, creditCardId, LocalDate.of(2025, 1, 1), new BigDecimal("1000.00"));
        invoice.setClosed(false);

        CreditCard creditCard = TestDataBuilder.createCreditCard(creditCardId, "Test Card", new BigDecimal("8000.00"), 10, 17);
        creditCard.setAllowsPartialPayment(true);

        PartialPayment payment = new PartialPayment();
        payment.setAmount(new BigDecimal("450.00"));

        PartialPayment savedPayment = TestDataBuilder.createPartialPayment(99L, invoiceId, new BigDecimal("450.00"), LocalDateTime.now());

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(creditCardRepository.findById(creditCardId)).thenReturn(Optional.of(creditCard));
        when(partialPaymentRepository.save(any(PartialPayment.class))).thenReturn(savedPayment);
        when(getAvailableLimit.execute(creditCardId)).thenReturn(
                new AvailableLimitResult(creditCardId, new BigDecimal("8000.00"), BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("8000.00"))
        );

        // When
        RegisterPartialPaymentResult result = useCase.execute(invoiceId, payment);

        // Then: Result should contain both values
        assertThat(result).isNotNull();
        assertThat(result.getPartialPayment()).isNotNull();
        assertThat(result.getPartialPayment().getId()).isEqualTo(99L);
        assertThat(result.getPartialPayment().getAmount()).isEqualByComparingTo("450.00");
        assertThat(result.getAvailableLimit()).isNotNull();
        assertThat(result.getAvailableLimit()).isEqualByComparingTo("8000.00");
    }

    @Test
    @DisplayName("Should handle very small payment amounts (0.01)")
    void shouldHandleVerySmallPaymentAmounts() {
        // Given: Very small payment
        Long invoiceId = 1L;
        Long creditCardId = 1L;

        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, creditCardId, LocalDate.of(2025, 1, 1), new BigDecimal("1000.00"));
        invoice.setClosed(false);

        CreditCard creditCard = TestDataBuilder.createCreditCard(creditCardId, "Test Card", new BigDecimal("5000.00"), 10, 17);
        creditCard.setAllowsPartialPayment(true);

        PartialPayment payment = new PartialPayment();
        payment.setAmount(new BigDecimal("0.01")); // Tiny amount, but > 0

        PartialPayment savedPayment = TestDataBuilder.createPartialPayment(1L, invoiceId, new BigDecimal("0.01"), LocalDateTime.now());

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(creditCardRepository.findById(creditCardId)).thenReturn(Optional.of(creditCard));
        when(partialPaymentRepository.save(any(PartialPayment.class))).thenReturn(savedPayment);

        // When
        RegisterPartialPaymentResult result = useCase.execute(invoiceId, payment);

        // Then: Should succeed
        assertThat(result.getPartialPayment().getAmount()).isEqualByComparingTo("0.01");
    }
}
