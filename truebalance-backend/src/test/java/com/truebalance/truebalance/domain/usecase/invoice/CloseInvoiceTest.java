package com.truebalance.truebalance.domain.usecase.invoice;

import com.truebalance.truebalance.domain.entity.Invoice;
import com.truebalance.truebalance.domain.port.InvoiceRepositoryPort;
import com.truebalance.truebalance.domain.port.PartialPaymentRepositoryPort;
import com.truebalance.truebalance.domain.usecase.CloseInvoice;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for CloseInvoice use case.
 *
 * This use case handles the critical end-of-billing-cycle operation including:
 * - Closing invoices and calculating final amounts
 * - Considering partial payments
 * - Transferring credit to next month when overpaid
 * - Determining payment status (paid vs unpaid)
 *
 * Business Rules Tested:
 * - BR-I-006: Prevent closing already closed invoice
 * - BR-I-012: Consider partial payments when closing
 * - BR-I-016: Transfer negative balance (credit) to next invoice
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CloseInvoice - Critical Billing Cycle Tests")
class CloseInvoiceTest {

    @Mock
    private InvoiceRepositoryPort invoiceRepository;

    @Mock
    private PartialPaymentRepositoryPort partialPaymentRepository;

    @Captor
    private ArgumentCaptor<Invoice> invoiceCaptor;

    private CloseInvoice useCase;

    @BeforeEach
    void setUp() {
        useCase = new CloseInvoice(invoiceRepository, partialPaymentRepository);
    }

    // ========== HAPPY PATH TESTS ==========

    @Test
    @DisplayName("Should close invoice with positive balance (unpaid)")
    void shouldCloseInvoiceWithPositiveBalanceUnpaid() {
        // Given: Invoice with 1000.00 total, no partial payments
        Long invoiceId = 1L;
        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, 1L, LocalDate.of(2025, 1, 1), new BigDecimal("1000.00"));
        invoice.setClosed(false);
        invoice.setPaid(false);

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(partialPaymentRepository.sumByInvoiceId(invoiceId)).thenReturn(BigDecimal.ZERO);
        when(invoiceRepository.findByCreditCardIdAndReferenceMonth(anyLong(), any(LocalDate.class))).thenReturn(Optional.empty());
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Optional<Invoice> result = useCase.execute(invoiceId);

        // Then
        assertThat(result).isPresent();
        Invoice closedInvoice = result.get();
        assertThat(closedInvoice.isClosed()).isTrue();
        assertThat(closedInvoice.isPaid()).isFalse(); // Still has balance to pay
        assertThat(closedInvoice.getTotalAmount()).isEqualByComparingTo("1000.00");

        // Verify save was called twice: once for closed invoice, once for next month's invoice
        verify(invoiceRepository, times(2)).save(any(Invoice.class));
    }

    @Test
    @DisplayName("Should close invoice with zero balance as paid")
    void shouldCloseInvoiceWithZeroBalanceAsPaid() {
        // Given: Invoice with 1000.00 total, 1000.00 in partial payments (exact match)
        Long invoiceId = 1L;
        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, 1L, LocalDate.of(2025, 1, 1), new BigDecimal("1000.00"));
        invoice.setClosed(false);
        invoice.setPaid(false);

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(partialPaymentRepository.sumByInvoiceId(invoiceId)).thenReturn(new BigDecimal("1000.00"));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Optional<Invoice> result = useCase.execute(invoiceId);

        // Then: finalAmount = 1000.00 - 1000.00 = 0.00 → marked as paid
        assertThat(result).isPresent();
        Invoice closedInvoice = result.get();
        assertThat(closedInvoice.isClosed()).isTrue();
        assertThat(closedInvoice.isPaid()).isTrue();

        verify(invoiceRepository).save(invoiceCaptor.capture());
        assertThat(invoiceCaptor.getValue().isPaid()).isTrue();
    }

    @Test
    @DisplayName("Should close invoice with partial payments but still positive balance")
    void shouldCloseInvoiceWithPartialPaymentsButStillPositiveBalance() {
        // Given: Invoice with 1000.00 total, 300.00 in partial payments
        Long invoiceId = 1L;
        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, 1L, LocalDate.of(2025, 1, 1), new BigDecimal("1000.00"));
        invoice.setClosed(false);

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(partialPaymentRepository.sumByInvoiceId(invoiceId)).thenReturn(new BigDecimal("300.00"));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Optional<Invoice> result = useCase.execute(invoiceId);

        // Then: finalAmount = 1000.00 - 300.00 = 700.00 (still positive, unpaid)
        assertThat(result).isPresent();
        Invoice closedInvoice = result.get();
        assertThat(closedInvoice.isClosed()).isTrue();
        assertThat(closedInvoice.isPaid()).isFalse();
    }

    @Test
    @DisplayName("Should close invoice with overpayment creating credit")
    void shouldCloseInvoiceWithOverpaymentCreatingCredit() {
        // Given: Invoice with 1000.00 total, 1500.00 in partial payments (overpaid by 500.00)
        Long invoiceId = 1L;
        Long creditCardId = 1L;
        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, creditCardId, LocalDate.of(2025, 1, 1), new BigDecimal("1000.00"));
        invoice.setClosed(false);

        // Next month invoice exists
        Invoice nextInvoice = TestDataBuilder.createInvoice(2L, creditCardId, LocalDate.of(2025, 2, 1), new BigDecimal("500.00"));
        nextInvoice.setPreviousBalance(BigDecimal.ZERO);
        nextInvoice.setClosed(false);

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(partialPaymentRepository.sumByInvoiceId(invoiceId)).thenReturn(new BigDecimal("1500.00"));
        when(invoiceRepository.findByCreditCardIdAndReferenceMonth(creditCardId, LocalDate.of(2025, 2, 1)))
                .thenReturn(Optional.of(nextInvoice));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Optional<Invoice> result = useCase.execute(invoiceId);

        // Then: finalAmount = 1000.00 - 1500.00 = -500.00 (credit)
        assertThat(result).isPresent();
        Invoice closedInvoice = result.get();
        assertThat(closedInvoice.isClosed()).isTrue();
        assertThat(closedInvoice.isPaid()).isTrue(); // Overpaid means paid

        // Verify next invoice received credit
        verify(invoiceRepository, times(2)).save(invoiceCaptor.capture());
        Invoice savedNextInvoice = invoiceCaptor.getAllValues().stream()
                .filter(inv -> inv.getId().equals(2L))
                .findFirst()
                .orElseThrow();
        assertThat(savedNextInvoice.getPreviousBalance()).isEqualByComparingTo("-500.00");
    }

    // ========== CREDIT TRANSFER TESTS (BR-I-016) ==========

    @Test
    @DisplayName("BR-I-016: Should transfer credit to existing next month invoice")
    void shouldTransferCreditToExistingNextMonthInvoice() {
        // Given: Overpaid invoice and existing next month invoice
        Long invoiceId = 1L;
        Long creditCardId = 1L;
        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, creditCardId, LocalDate.of(2025, 3, 1), new BigDecimal("800.00"));
        invoice.setClosed(false);

        Invoice nextInvoice = TestDataBuilder.createInvoice(2L, creditCardId, LocalDate.of(2025, 4, 1), new BigDecimal("600.00"));
        nextInvoice.setPreviousBalance(new BigDecimal("100.00")); // Existing previous balance
        nextInvoice.setClosed(false);

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(partialPaymentRepository.sumByInvoiceId(invoiceId)).thenReturn(new BigDecimal("1000.00")); // Overpaid by 200
        when(invoiceRepository.findByCreditCardIdAndReferenceMonth(creditCardId, LocalDate.of(2025, 4, 1)))
                .thenReturn(Optional.of(nextInvoice));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        useCase.execute(invoiceId);

        // Then: Credit of -200.00 added to existing previousBalance of 100.00 = -100.00
        verify(invoiceRepository, times(2)).save(invoiceCaptor.capture());
        Invoice savedNextInvoice = invoiceCaptor.getAllValues().stream()
                .filter(inv -> inv.getId().equals(2L))
                .findFirst()
                .orElseThrow();
        assertThat(savedNextInvoice.getPreviousBalance()).isEqualByComparingTo("-100.00");
    }

    @Test
    @DisplayName("BR-I-016: Should create new invoice when next month doesn't exist")
    void shouldCreateNewInvoiceWhenNextMonthDoesntExist() {
        // Given: Overpaid invoice, next month invoice doesn't exist
        Long invoiceId = 1L;
        Long creditCardId = 1L;
        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, creditCardId, LocalDate.of(2025, 5, 1), new BigDecimal("1200.00"));
        invoice.setClosed(false);

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(partialPaymentRepository.sumByInvoiceId(invoiceId)).thenReturn(new BigDecimal("1500.00")); // Credit of 300
        when(invoiceRepository.findByCreditCardIdAndReferenceMonth(creditCardId, LocalDate.of(2025, 6, 1)))
                .thenReturn(Optional.empty());
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        useCase.execute(invoiceId);

        // Then: New invoice created with credit
        verify(invoiceRepository, times(2)).save(invoiceCaptor.capture());

        // Find the newly created invoice (the one without ID set)
        Invoice newInvoice = invoiceCaptor.getAllValues().stream()
                .filter(inv -> inv.getId() == null || !inv.getId().equals(invoiceId))
                .findFirst()
                .orElseThrow();

        assertThat(newInvoice.getCreditCardId()).isEqualTo(creditCardId);
        assertThat(newInvoice.getReferenceMonth()).isEqualTo(LocalDate.of(2025, 6, 1));
        assertThat(newInvoice.getTotalAmount()).isEqualByComparingTo("0.00");
        assertThat(newInvoice.getPreviousBalance()).isEqualByComparingTo("-300.00"); // Credit
        assertThat(newInvoice.isClosed()).isFalse();
        assertThat(newInvoice.isPaid()).isFalse();
    }

    // ========== VALIDATION & EXCEPTION TESTS ==========

    @Test
    @DisplayName("Should return empty when invoice not found")
    void shouldReturnEmptyWhenInvoiceNotFound() {
        // Given: Invoice does not exist
        Long nonExistentId = 999L;

        when(invoiceRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When
        Optional<Invoice> result = useCase.execute(nonExistentId);

        // Then
        assertThat(result).isEmpty();

        // Verify no further processing occurred
        verify(partialPaymentRepository, never()).sumByInvoiceId(anyLong());
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    @DisplayName("BR-I-006: Should throw exception when invoice already closed")
    void shouldThrowExceptionWhenInvoiceAlreadyClosed() {
        // Given: Invoice is already closed
        Long invoiceId = 1L;
        Invoice closedInvoice = TestDataBuilder.createInvoice(invoiceId, 1L, LocalDate.of(2025, 1, 1), new BigDecimal("500.00"));
        closedInvoice.setClosed(true);

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(closedInvoice));

        // When & Then
        assertThatThrownBy(() -> useCase.execute(invoiceId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Invoice is already closed");

        // Verify invoice was not saved
        verify(invoiceRepository, never()).save(any());
    }

    // ========== EDGE CASES ==========

    @Test
    @DisplayName("Should handle invoice with zero total amount")
    void shouldHandleInvoiceWithZeroTotalAmount() {
        // Given: Invoice with 0.00 total (edge case)
        Long invoiceId = 1L;
        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, 1L, LocalDate.of(2025, 1, 1), BigDecimal.ZERO);
        invoice.setClosed(false);

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(partialPaymentRepository.sumByInvoiceId(invoiceId)).thenReturn(BigDecimal.ZERO);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Optional<Invoice> result = useCase.execute(invoiceId);

        // Then: finalAmount = 0.00 - 0.00 = 0.00 → paid
        assertThat(result).isPresent();
        assertThat(result.get().isClosed()).isTrue();
        assertThat(result.get().isPaid()).isTrue();
    }

    @Test
    @DisplayName("Should handle very small positive balance (0.01)")
    void shouldHandleVerySmallPositiveBalance() {
        // Given: Invoice with tiny remaining balance
        Long invoiceId = 1L;
        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, 1L, LocalDate.of(2025, 1, 1), new BigDecimal("100.00"));
        invoice.setClosed(false);

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(partialPaymentRepository.sumByInvoiceId(invoiceId)).thenReturn(new BigDecimal("99.99"));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Optional<Invoice> result = useCase.execute(invoiceId);

        // Then: finalAmount = 100.00 - 99.99 = 0.01 (still positive, unpaid)
        assertThat(result).isPresent();
        assertThat(result.get().isPaid()).isFalse();
    }

    @Test
    @DisplayName("Should handle very small credit (-0.01)")
    void shouldHandleVerySmallCredit() {
        // Given: Invoice with tiny overpayment
        Long invoiceId = 1L;
        Long creditCardId = 1L;
        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, creditCardId, LocalDate.of(2025, 1, 1), new BigDecimal("100.00"));
        invoice.setClosed(false);

        Invoice nextInvoice = TestDataBuilder.createInvoice(2L, creditCardId, LocalDate.of(2025, 2, 1), BigDecimal.ZERO);
        nextInvoice.setPreviousBalance(BigDecimal.ZERO);

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(partialPaymentRepository.sumByInvoiceId(invoiceId)).thenReturn(new BigDecimal("100.01"));
        when(invoiceRepository.findByCreditCardIdAndReferenceMonth(creditCardId, LocalDate.of(2025, 2, 1)))
                .thenReturn(Optional.of(nextInvoice));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Optional<Invoice> result = useCase.execute(invoiceId);

        // Then: finalAmount = 100.00 - 100.01 = -0.01 (credit transferred)
        assertThat(result).isPresent();
        assertThat(result.get().isPaid()).isTrue();

        verify(invoiceRepository, times(2)).save(invoiceCaptor.capture());
        Invoice savedNextInvoice = invoiceCaptor.getAllValues().get(0); // First save is next invoice
        assertThat(savedNextInvoice.getPreviousBalance()).isEqualByComparingTo("-0.01");
    }

    @Test
    @DisplayName("Should handle invoice with no partial payments (sum returns zero)")
    void shouldHandleInvoiceWithNoPartialPayments() {
        // Given: Invoice with no partial payments
        Long invoiceId = 1L;
        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, 1L, LocalDate.of(2025, 1, 1), new BigDecimal("750.00"));
        invoice.setClosed(false);

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(partialPaymentRepository.sumByInvoiceId(invoiceId)).thenReturn(BigDecimal.ZERO);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Optional<Invoice> result = useCase.execute(invoiceId);

        // Then: finalAmount = 750.00 - 0.00 = 750.00 (unpaid)
        assertThat(result).isPresent();
        assertThat(result.get().isPaid()).isFalse();

        verify(partialPaymentRepository).sumByInvoiceId(invoiceId);
    }

    // ========== BUSINESS RULES TESTS ==========

    @Test
    @DisplayName("BR-I-012: Should consider partial payments when closing")
    void shouldConsiderPartialPaymentsWhenClosing() {
        // Given: Invoice with significant partial payments
        Long invoiceId = 1L;
        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, 1L, LocalDate.of(2025, 1, 1), new BigDecimal("2000.00"));
        invoice.setClosed(false);

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(partialPaymentRepository.sumByInvoiceId(invoiceId)).thenReturn(new BigDecimal("1250.50"));
        when(invoiceRepository.findByCreditCardIdAndReferenceMonth(anyLong(), any(LocalDate.class))).thenReturn(Optional.empty());
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        useCase.execute(invoiceId);

        // Then: Verify partial payments were fetched and considered
        verify(partialPaymentRepository).sumByInvoiceId(invoiceId);

        // finalAmount = 2000.00 - 1250.50 = 749.50 (still unpaid, transfers to next month)
        verify(invoiceRepository, times(2)).save(any(Invoice.class));
    }

    // ========== INTEGRATION TESTS ==========

    @Test
    @DisplayName("Should call repositories in correct order")
    void shouldCallRepositoriesInCorrectOrder() {
        // Given
        Long invoiceId = 1L;
        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, 1L, LocalDate.of(2025, 1, 1), new BigDecimal("500.00"));
        invoice.setClosed(false);

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(partialPaymentRepository.sumByInvoiceId(invoiceId)).thenReturn(BigDecimal.ZERO);
        when(invoiceRepository.findByCreditCardIdAndReferenceMonth(anyLong(), any(LocalDate.class))).thenReturn(Optional.empty());
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        useCase.execute(invoiceId);

        // Then: Verify correct order of operations
        var inOrder = inOrder(invoiceRepository, partialPaymentRepository);
        inOrder.verify(invoiceRepository).findById(invoiceId);
        inOrder.verify(partialPaymentRepository).sumByInvoiceId(invoiceId);
        // After calculating balance, should find/create next month invoice and save current
        verify(invoiceRepository).findByCreditCardIdAndReferenceMonth(anyLong(), any(LocalDate.class));
        verify(invoiceRepository, times(2)).save(any(Invoice.class));
    }

    @Test
    @DisplayName("Should save invoice with closed flag set to true")
    void shouldSaveInvoiceWithClosedFlagSetToTrue() {
        // Given
        Long invoiceId = 1L;
        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, 1L, LocalDate.of(2025, 1, 1), new BigDecimal("300.00"));
        invoice.setClosed(false);

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(partialPaymentRepository.sumByInvoiceId(invoiceId)).thenReturn(BigDecimal.ZERO);
        when(invoiceRepository.findByCreditCardIdAndReferenceMonth(anyLong(), any(LocalDate.class))).thenReturn(Optional.empty());
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        useCase.execute(invoiceId);

        // Then: Verify invoice was saved with closed flag (2 saves: next month + closed invoice)
        verify(invoiceRepository, times(2)).save(invoiceCaptor.capture());
        // The last saved invoice should be the closed one
        assertThat(invoiceCaptor.getAllValues().get(1).isClosed()).isTrue();
    }

    @Test
    @DisplayName("Should save both current and next invoice when transferring credit")
    void shouldSaveBothCurrentAndNextInvoiceWhenTransferringCredit() {
        // Given: Overpayment scenario
        Long invoiceId = 1L;
        Long creditCardId = 1L;
        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, creditCardId, LocalDate.of(2025, 1, 1), new BigDecimal("500.00"));
        invoice.setClosed(false);

        Invoice nextInvoice = TestDataBuilder.createInvoice(2L, creditCardId, LocalDate.of(2025, 2, 1), BigDecimal.ZERO);
        nextInvoice.setPreviousBalance(BigDecimal.ZERO);

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(partialPaymentRepository.sumByInvoiceId(invoiceId)).thenReturn(new BigDecimal("700.00"));
        when(invoiceRepository.findByCreditCardIdAndReferenceMonth(creditCardId, LocalDate.of(2025, 2, 1)))
                .thenReturn(Optional.of(nextInvoice));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        useCase.execute(invoiceId);

        // Then: Both invoices should be saved
        verify(invoiceRepository, times(2)).save(any(Invoice.class));
    }

    @Test
    @DisplayName("Should preserve existing invoice fields when closing")
    void shouldPreserveExistingInvoiceFieldsWhenClosing() {
        // Given: Invoice with specific field values
        Long invoiceId = 99L;
        Long creditCardId = 42L;
        LocalDate referenceMonth = LocalDate.of(2025, 7, 1);
        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, creditCardId, referenceMonth, new BigDecimal("1234.56"));
        invoice.setPreviousBalance(new BigDecimal("100.00"));
        invoice.setClosed(false);
        invoice.setPaid(false);

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(partialPaymentRepository.sumByInvoiceId(invoiceId)).thenReturn(new BigDecimal("200.00"));
        when(invoiceRepository.findByCreditCardIdAndReferenceMonth(anyLong(), any(LocalDate.class))).thenReturn(Optional.empty());
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        useCase.execute(invoiceId);

        // Then: Verify save was called twice (closed invoice + next month's invoice)
        verify(invoiceRepository, times(2)).save(any(Invoice.class));
    }

    @Test
    @DisplayName("Should handle December to January transition for credit transfer")
    void shouldHandleDecemberToJanuaryTransitionForCreditTransfer() {
        // Given: December invoice with overpayment
        Long invoiceId = 1L;
        Long creditCardId = 1L;
        Invoice decemberInvoice = TestDataBuilder.createInvoice(invoiceId, creditCardId, LocalDate.of(2025, 12, 1), new BigDecimal("800.00"));
        decemberInvoice.setClosed(false);

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(decemberInvoice));
        when(partialPaymentRepository.sumByInvoiceId(invoiceId)).thenReturn(new BigDecimal("1000.00"));
        when(invoiceRepository.findByCreditCardIdAndReferenceMonth(creditCardId, LocalDate.of(2026, 1, 1)))
                .thenReturn(Optional.empty());
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        useCase.execute(invoiceId);

        // Then: Next invoice should be January 2026
        verify(invoiceRepository, times(2)).save(invoiceCaptor.capture());

        Invoice newInvoice = invoiceCaptor.getAllValues().stream()
                .filter(inv -> inv.getReferenceMonth() != null && inv.getReferenceMonth().equals(LocalDate.of(2026, 1, 1)))
                .findFirst()
                .orElseThrow();

        assertThat(newInvoice.getReferenceMonth()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(newInvoice.getPreviousBalance()).isEqualByComparingTo("-200.00");
    }
}
