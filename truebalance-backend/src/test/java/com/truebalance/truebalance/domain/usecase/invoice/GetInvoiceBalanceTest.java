package com.truebalance.truebalance.domain.usecase.invoice;

import com.truebalance.truebalance.application.dto.output.InvoiceBalanceDTO;
import com.truebalance.truebalance.domain.entity.Invoice;
import com.truebalance.truebalance.domain.port.InvoiceRepositoryPort;
import com.truebalance.truebalance.domain.port.PartialPaymentRepositoryPort;
import com.truebalance.truebalance.domain.usecase.GetInvoiceBalance;
import com.truebalance.truebalance.util.TestDataBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for GetInvoiceBalance use case.
 *
 * Business Rules Tested:
 * - BR-I-011: Current balance = totalAmount + previousBalance - partialPaymentsTotal
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetInvoiceBalance Use Case Tests")
class GetInvoiceBalanceTest {

    @Mock
    private InvoiceRepositoryPort invoiceRepository;

    @Mock
    private PartialPaymentRepositoryPort partialPaymentRepository;

    @InjectMocks
    private GetInvoiceBalance useCase;

    // ==================== Happy Path Tests ====================

    @Test
    @DisplayName("Should return balance DTO for existing invoice")
    void shouldReturnBalanceDTOForExistingInvoice() {
        // Given: Invoice with partial payments
        Long invoiceId = 1L;
        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, 1L, LocalDate.of(2025, 1, 1), new BigDecimal("1000.00"));
        invoice.setPreviousBalance(BigDecimal.ZERO);

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(partialPaymentRepository.sumByInvoiceId(invoiceId)).thenReturn(new BigDecimal("300.00"));
        when(partialPaymentRepository.countByInvoiceId(invoiceId)).thenReturn(2);

        // When
        Optional<InvoiceBalanceDTO> result = useCase.execute(invoiceId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getInvoiceId()).isEqualTo(invoiceId);
        assertThat(result.get().getTotalAmount()).isEqualByComparingTo("1000.00");
        assertThat(result.get().getPreviousBalance()).isEqualByComparingTo("0.00");
        assertThat(result.get().getPartialPaymentsTotal()).isEqualByComparingTo("300.00");
        assertThat(result.get().getCurrentBalance()).isEqualByComparingTo("700.00");
        assertThat(result.get().getPartialPaymentsCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should return empty Optional when invoice does not exist")
    void shouldReturnEmptyWhenInvoiceDoesNotExist() {
        // Given: Non-existent invoice
        Long invoiceId = 999L;
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.empty());

        // When
        Optional<InvoiceBalanceDTO> result = useCase.execute(invoiceId);

        // Then
        assertThat(result).isEmpty();
    }

    // ==================== BR-I-011: Balance Formula Tests ====================

    @Test
    @DisplayName("BR-I-011: Should apply correct formula for current balance")
    void shouldApplyCorrectFormulaForCurrentBalance() {
        // Given: Specific values to test formula
        Long invoiceId = 1L;
        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, 1L, LocalDate.of(2025, 1, 1), new BigDecimal("2500.75"));
        invoice.setPreviousBalance(new BigDecimal("350.50"));

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(partialPaymentRepository.sumByInvoiceId(invoiceId)).thenReturn(new BigDecimal("1200.25"));
        when(partialPaymentRepository.countByInvoiceId(invoiceId)).thenReturn(3);

        // When
        Optional<InvoiceBalanceDTO> result = useCase.execute(invoiceId);

        // Then: 2500.75 + 350.50 - 1200.25 = 1651.00
        assertThat(result).isPresent();
        InvoiceBalanceDTO balance = result.get();

        assertThat(balance.getTotalAmount()).isEqualByComparingTo("2500.75");
        assertThat(balance.getPreviousBalance()).isEqualByComparingTo("350.50");
        assertThat(balance.getPartialPaymentsTotal()).isEqualByComparingTo("1200.25");
        assertThat(balance.getCurrentBalance()).isEqualByComparingTo("1651.00");

        // Verify formula manually
        BigDecimal expected = balance.getTotalAmount()
                .add(balance.getPreviousBalance())
                .subtract(balance.getPartialPaymentsTotal());
        assertThat(balance.getCurrentBalance()).isEqualByComparingTo(expected);
    }

    @Test
    @DisplayName("BR-I-011: Should calculate zero balance when fully paid")
    void shouldCalculateZeroBalanceWhenFullyPaid() {
        // Given: Invoice fully paid
        Long invoiceId = 1L;
        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, 1L, LocalDate.of(2025, 1, 1), new BigDecimal("1000.00"));
        invoice.setPreviousBalance(BigDecimal.ZERO);

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(partialPaymentRepository.sumByInvoiceId(invoiceId)).thenReturn(new BigDecimal("1000.00"));
        when(partialPaymentRepository.countByInvoiceId(invoiceId)).thenReturn(1);

        // When
        Optional<InvoiceBalanceDTO> result = useCase.execute(invoiceId);

        // Then: Balance should be 0
        assertThat(result).isPresent();
        assertThat(result.get().getCurrentBalance()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("BR-I-011: Should calculate negative balance when overpaid (credit)")
    void shouldCalculateNegativeBalanceWhenOverpaid() {
        // Given: Overpaid invoice
        Long invoiceId = 1L;
        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, 1L, LocalDate.of(2025, 1, 1), new BigDecimal("1000.00"));
        invoice.setPreviousBalance(BigDecimal.ZERO);

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(partialPaymentRepository.sumByInvoiceId(invoiceId)).thenReturn(new BigDecimal("1500.00"));
        when(partialPaymentRepository.countByInvoiceId(invoiceId)).thenReturn(2);

        // When
        Optional<InvoiceBalanceDTO> result = useCase.execute(invoiceId);

        // Then: Balance should be -500.00 (credit)
        assertThat(result).isPresent();
        assertThat(result.get().getCurrentBalance()).isEqualByComparingTo("-500.00");
    }

    @Test
    @DisplayName("BR-I-011: Should include previous balance in calculation")
    void shouldIncludePreviousBalanceInCalculation() {
        // Given: Invoice with previous balance
        Long invoiceId = 1L;
        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, 1L, LocalDate.of(2025, 2, 1), new BigDecimal("800.00"));
        invoice.setPreviousBalance(new BigDecimal("200.00")); // Debt from last month

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(partialPaymentRepository.sumByInvoiceId(invoiceId)).thenReturn(new BigDecimal("500.00"));
        when(partialPaymentRepository.countByInvoiceId(invoiceId)).thenReturn(1);

        // When
        Optional<InvoiceBalanceDTO> result = useCase.execute(invoiceId);

        // Then: 800 + 200 - 500 = 500
        assertThat(result).isPresent();
        assertThat(result.get().getCurrentBalance()).isEqualByComparingTo("500.00");
    }

    @Test
    @DisplayName("BR-I-011: Should handle negative previous balance (credit from last month)")
    void shouldHandleNegativePreviousBalance() {
        // Given: Invoice with credit from previous month
        Long invoiceId = 1L;
        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, 1L, LocalDate.of(2025, 2, 1), new BigDecimal("1000.00"));
        invoice.setPreviousBalance(new BigDecimal("-300.00")); // Credit from last month

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(partialPaymentRepository.sumByInvoiceId(invoiceId)).thenReturn(BigDecimal.ZERO);
        when(partialPaymentRepository.countByInvoiceId(invoiceId)).thenReturn(0);

        // When
        Optional<InvoiceBalanceDTO> result = useCase.execute(invoiceId);

        // Then: 1000 + (-300) - 0 = 700
        assertThat(result).isPresent();
        assertThat(result.get().getCurrentBalance()).isEqualByComparingTo("700.00");
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("Should handle invoice with no partial payments")
    void shouldHandleInvoiceWithNoPartialPayments() {
        // Given: Invoice with no payments
        Long invoiceId = 1L;
        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, 1L, LocalDate.of(2025, 1, 1), new BigDecimal("1500.00"));
        invoice.setPreviousBalance(BigDecimal.ZERO);

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(partialPaymentRepository.sumByInvoiceId(invoiceId)).thenReturn(BigDecimal.ZERO);
        when(partialPaymentRepository.countByInvoiceId(invoiceId)).thenReturn(0);

        // When
        Optional<InvoiceBalanceDTO> result = useCase.execute(invoiceId);

        // Then: Balance should equal total amount
        assertThat(result).isPresent();
        assertThat(result.get().getCurrentBalance()).isEqualByComparingTo("1500.00");
        assertThat(result.get().getPartialPaymentsCount()).isEqualTo(0);
        assertThat(result.get().getPartialPaymentsTotal()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Should handle invoice with zero total amount")
    void shouldHandleInvoiceWithZeroTotalAmount() {
        // Given: Invoice with no charges this month
        Long invoiceId = 1L;
        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, 1L, LocalDate.of(2025, 1, 1), BigDecimal.ZERO);
        invoice.setPreviousBalance(BigDecimal.ZERO);

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(partialPaymentRepository.sumByInvoiceId(invoiceId)).thenReturn(BigDecimal.ZERO);
        when(partialPaymentRepository.countByInvoiceId(invoiceId)).thenReturn(0);

        // When
        Optional<InvoiceBalanceDTO> result = useCase.execute(invoiceId);

        // Then: Balance should be zero
        assertThat(result).isPresent();
        assertThat(result.get().getCurrentBalance()).isEqualByComparingTo("0.00");
        assertThat(result.get().getTotalAmount()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Should handle very small amounts with precision")
    void shouldHandleVerySmallAmountsWithPrecision() {
        // Given: Invoice with small values
        Long invoiceId = 1L;
        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, 1L, LocalDate.of(2025, 1, 1), new BigDecimal("0.01"));
        invoice.setPreviousBalance(new BigDecimal("0.02"));

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(partialPaymentRepository.sumByInvoiceId(invoiceId)).thenReturn(new BigDecimal("0.01"));
        when(partialPaymentRepository.countByInvoiceId(invoiceId)).thenReturn(1);

        // When
        Optional<InvoiceBalanceDTO> result = useCase.execute(invoiceId);

        // Then: 0.01 + 0.02 - 0.01 = 0.02
        assertThat(result).isPresent();
        assertThat(result.get().getCurrentBalance()).isEqualByComparingTo("0.02");
    }

    @Test
    @DisplayName("Should handle very large amounts")
    void shouldHandleVeryLargeAmounts() {
        // Given: Invoice with large values
        Long invoiceId = 1L;
        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, 1L, LocalDate.of(2025, 1, 1), new BigDecimal("999999999.99"));
        invoice.setPreviousBalance(new BigDecimal("100000000.00"));

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(partialPaymentRepository.sumByInvoiceId(invoiceId)).thenReturn(new BigDecimal("500000000.00"));
        when(partialPaymentRepository.countByInvoiceId(invoiceId)).thenReturn(10);

        // When
        Optional<InvoiceBalanceDTO> result = useCase.execute(invoiceId);

        // Then: Should handle large numbers correctly
        assertThat(result).isPresent();
        BigDecimal expected = new BigDecimal("999999999.99")
                .add(new BigDecimal("100000000.00"))
                .subtract(new BigDecimal("500000000.00"));
        assertThat(result.get().getCurrentBalance()).isEqualByComparingTo(expected);
    }

    // ==================== State Transfer Tests ====================

    @Test
    @DisplayName("Should correctly transfer paid and closed status to DTO")
    void shouldTransferPaidAndClosedStatusToDTO() {
        // Given: Paid and closed invoice
        Long invoiceId = 1L;
        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, 1L, LocalDate.of(2025, 1, 1), new BigDecimal("1000.00"));
        invoice.setPaid(true);
        invoice.setClosed(true);

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(partialPaymentRepository.sumByInvoiceId(invoiceId)).thenReturn(new BigDecimal("1000.00"));
        when(partialPaymentRepository.countByInvoiceId(invoiceId)).thenReturn(1);

        // When
        Optional<InvoiceBalanceDTO> result = useCase.execute(invoiceId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().isPaid()).isTrue();
        assertThat(result.get().isClosed()).isTrue();
    }

    @Test
    @DisplayName("Should handle unpaid and open invoice")
    void shouldHandleUnpaidAndOpenInvoice() {
        // Given: Unpaid and open invoice
        Long invoiceId = 1L;
        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, 1L, LocalDate.of(2025, 1, 1), new BigDecimal("1000.00"));
        invoice.setPaid(false);
        invoice.setClosed(false);

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(partialPaymentRepository.sumByInvoiceId(invoiceId)).thenReturn(BigDecimal.ZERO);
        when(partialPaymentRepository.countByInvoiceId(invoiceId)).thenReturn(0);

        // When
        Optional<InvoiceBalanceDTO> result = useCase.execute(invoiceId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().isPaid()).isFalse();
        assertThat(result.get().isClosed()).isFalse();
    }

    @Test
    @DisplayName("Should handle multiple partial payments count correctly")
    void shouldHandleMultiplePartialPaymentsCountCorrectly() {
        // Given: Invoice with 5 partial payments
        Long invoiceId = 1L;
        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, 1L, LocalDate.of(2025, 1, 1), new BigDecimal("1000.00"));

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(partialPaymentRepository.sumByInvoiceId(invoiceId)).thenReturn(new BigDecimal("750.00"));
        when(partialPaymentRepository.countByInvoiceId(invoiceId)).thenReturn(5);

        // When
        Optional<InvoiceBalanceDTO> result = useCase.execute(invoiceId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getPartialPaymentsCount()).isEqualTo(5);
        assertThat(result.get().getPartialPaymentsTotal()).isEqualByComparingTo("750.00");
    }
}
