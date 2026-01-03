package com.truebalance.truebalance.domain.usecase.invoice;

import com.truebalance.truebalance.domain.entity.Invoice;
import com.truebalance.truebalance.domain.port.InvoiceRepositoryPort;
import com.truebalance.truebalance.domain.usecase.GenerateOrGetInvoiceForMonth;
import com.truebalance.truebalance.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GenerateOrGetInvoiceForMonth use case.
 *
 * Business Rules Tested:
 * - BR-I-001: Invoice creation with default values
 * - BR-I-002: One invoice per credit card per month
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GenerateOrGetInvoiceForMonth Use Case Tests")
class GenerateOrGetInvoiceForMonthTest {

    @Mock
    private InvoiceRepositoryPort invoiceRepository;

    @InjectMocks
    private GenerateOrGetInvoiceForMonth useCase;

    @Captor
    private ArgumentCaptor<Invoice> invoiceCaptor;

    private Long creditCardId;
    private LocalDate referenceMonth;

    @BeforeEach
    void setUp() {
        creditCardId = 1L;
        referenceMonth = LocalDate.of(2025, 1, 1);
    }

    // ==================== Happy Path Tests ====================

    @Test
    @DisplayName("Should return existing invoice when it already exists")
    void shouldReturnExistingInvoiceWhenItExists() {
        // Given: Invoice already exists for this card and month
        Invoice existingInvoice = TestDataBuilder.createInvoice(1L, creditCardId, referenceMonth, new BigDecimal("500.00"));

        when(invoiceRepository.findByCreditCardIdAndReferenceMonth(creditCardId, referenceMonth))
                .thenReturn(Optional.of(existingInvoice));

        // When
        Invoice result = useCase.execute(creditCardId, referenceMonth);

        // Then: Should return existing invoice without creating new one
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCreditCardId()).isEqualTo(creditCardId);
        assertThat(result.getReferenceMonth()).isEqualTo(referenceMonth);
        assertThat(result.getTotalAmount()).isEqualByComparingTo("500.00");

        // Verify save was NOT called (no new invoice created)
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    @DisplayName("BR-I-001: Should create new invoice with default values when it doesn't exist")
    void shouldCreateNewInvoiceWhenItDoesntExist() {
        // Given: No existing invoice
        when(invoiceRepository.findByCreditCardIdAndReferenceMonth(creditCardId, referenceMonth))
                .thenReturn(Optional.empty());

        Invoice savedInvoice = TestDataBuilder.createInvoice(1L, creditCardId, referenceMonth, BigDecimal.ZERO);
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(savedInvoice);

        // When
        Invoice result = useCase.execute(creditCardId, referenceMonth);

        // Then: Should create new invoice
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCreditCardId()).isEqualTo(creditCardId);
        assertThat(result.getReferenceMonth()).isEqualTo(referenceMonth);

        // Verify save was called
        verify(invoiceRepository).save(invoiceCaptor.capture());
        Invoice capturedInvoice = invoiceCaptor.getValue();

        assertThat(capturedInvoice.getCreditCardId()).isEqualTo(creditCardId);
        assertThat(capturedInvoice.getReferenceMonth()).isEqualTo(referenceMonth);
        assertThat(capturedInvoice.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(capturedInvoice.getPreviousBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(capturedInvoice.isClosed()).isFalse();
        assertThat(capturedInvoice.isPaid()).isFalse();
    }

    @Test
    @DisplayName("BR-I-001: Should initialize new invoice with all required default values")
    void shouldInitializeNewInvoiceWithCorrectDefaults() {
        // Given: No existing invoice
        when(invoiceRepository.findByCreditCardIdAndReferenceMonth(creditCardId, referenceMonth))
                .thenReturn(Optional.empty());
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        useCase.execute(creditCardId, referenceMonth);

        // Then: Verify all default values
        verify(invoiceRepository).save(invoiceCaptor.capture());
        Invoice newInvoice = invoiceCaptor.getValue();

        assertThat(newInvoice.getTotalAmount()).isEqualByComparingTo("0.00");
        assertThat(newInvoice.getPreviousBalance()).isEqualByComparingTo("0.00");
        assertThat(newInvoice.isClosed()).isFalse();
        assertThat(newInvoice.isPaid()).isFalse();
        assertThat(newInvoice.getCreditCardId()).isEqualTo(creditCardId);
        assertThat(newInvoice.getReferenceMonth()).isEqualTo(referenceMonth);
    }

    // ==================== BR-I-002: One Invoice Per Month Tests ====================

    @Test
    @DisplayName("BR-I-002: Should enforce one invoice per card per month")
    void shouldEnforceOneInvoicePerCardPerMonth() {
        // Given: Invoice exists
        Invoice existingInvoice = TestDataBuilder.createInvoice(1L, creditCardId, referenceMonth, new BigDecimal("1000.00"));
        when(invoiceRepository.findByCreditCardIdAndReferenceMonth(creditCardId, referenceMonth))
                .thenReturn(Optional.of(existingInvoice));

        // When: Request multiple times
        Invoice result1 = useCase.execute(creditCardId, referenceMonth);
        Invoice result2 = useCase.execute(creditCardId, referenceMonth);

        // Then: Should return same existing invoice both times
        assertThat(result1.getId()).isEqualTo(existingInvoice.getId());
        assertThat(result2.getId()).isEqualTo(existingInvoice.getId());

        // Verify save was never called
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should create different invoices for different months of same card")
    void shouldCreateDifferentInvoicesForDifferentMonths() {
        // Given: Different reference months
        LocalDate month1 = LocalDate.of(2025, 1, 1);
        LocalDate month2 = LocalDate.of(2025, 2, 1);

        when(invoiceRepository.findByCreditCardIdAndReferenceMonth(creditCardId, month1))
                .thenReturn(Optional.empty());
        when(invoiceRepository.findByCreditCardIdAndReferenceMonth(creditCardId, month2))
                .thenReturn(Optional.empty());

        Invoice savedInvoice1 = TestDataBuilder.createInvoice(1L, creditCardId, month1, BigDecimal.ZERO);
        Invoice savedInvoice2 = TestDataBuilder.createInvoice(2L, creditCardId, month2, BigDecimal.ZERO);

        when(invoiceRepository.save(any(Invoice.class)))
                .thenReturn(savedInvoice1)
                .thenReturn(savedInvoice2);

        // When
        Invoice result1 = useCase.execute(creditCardId, month1);
        Invoice result2 = useCase.execute(creditCardId, month2);

        // Then: Should create two different invoices
        assertThat(result1.getReferenceMonth()).isEqualTo(month1);
        assertThat(result2.getReferenceMonth()).isEqualTo(month2);

        verify(invoiceRepository, times(2)).save(any());
    }

    @Test
    @DisplayName("Should create different invoices for different credit cards in same month")
    void shouldCreateDifferentInvoicesForDifferentCards() {
        // Given: Different credit cards, same month
        Long card1Id = 1L;
        Long card2Id = 2L;

        when(invoiceRepository.findByCreditCardIdAndReferenceMonth(card1Id, referenceMonth))
                .thenReturn(Optional.empty());
        when(invoiceRepository.findByCreditCardIdAndReferenceMonth(card2Id, referenceMonth))
                .thenReturn(Optional.empty());

        Invoice savedInvoice1 = TestDataBuilder.createInvoice(1L, card1Id, referenceMonth, BigDecimal.ZERO);
        Invoice savedInvoice2 = TestDataBuilder.createInvoice(2L, card2Id, referenceMonth, BigDecimal.ZERO);

        when(invoiceRepository.save(any(Invoice.class)))
                .thenReturn(savedInvoice1)
                .thenReturn(savedInvoice2);

        // When
        Invoice result1 = useCase.execute(card1Id, referenceMonth);
        Invoice result2 = useCase.execute(card2Id, referenceMonth);

        // Then: Should create two different invoices
        assertThat(result1.getCreditCardId()).isEqualTo(card1Id);
        assertThat(result2.getCreditCardId()).isEqualTo(card2Id);

        verify(invoiceRepository, times(2)).save(any());
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("Should handle year transition correctly")
    void shouldHandleYearTransitionCorrectly() {
        // Given: December and January invoices
        LocalDate december = LocalDate.of(2024, 12, 1);
        LocalDate january = LocalDate.of(2025, 1, 1);

        when(invoiceRepository.findByCreditCardIdAndReferenceMonth(creditCardId, december))
                .thenReturn(Optional.empty());
        when(invoiceRepository.findByCreditCardIdAndReferenceMonth(creditCardId, january))
                .thenReturn(Optional.empty());

        Invoice decemberInvoice = TestDataBuilder.createInvoice(1L, creditCardId, december, BigDecimal.ZERO);
        Invoice januaryInvoice = TestDataBuilder.createInvoice(2L, creditCardId, january, BigDecimal.ZERO);

        when(invoiceRepository.save(any(Invoice.class)))
                .thenReturn(decemberInvoice)
                .thenReturn(januaryInvoice);

        // When
        Invoice result1 = useCase.execute(creditCardId, december);
        Invoice result2 = useCase.execute(creditCardId, january);

        // Then: Should create different invoices
        assertThat(result1.getReferenceMonth()).isEqualTo(december);
        assertThat(result2.getReferenceMonth()).isEqualTo(january);
        assertThat(result1.getId()).isNotEqualTo(result2.getId());
    }

    @Test
    @DisplayName("Should handle same month but different years correctly")
    void shouldHandleSameMonthDifferentYears() {
        // Given: January 2025 and January 2026
        LocalDate jan2025 = LocalDate.of(2025, 1, 1);
        LocalDate jan2026 = LocalDate.of(2026, 1, 1);

        when(invoiceRepository.findByCreditCardIdAndReferenceMonth(creditCardId, jan2025))
                .thenReturn(Optional.empty());
        when(invoiceRepository.findByCreditCardIdAndReferenceMonth(creditCardId, jan2026))
                .thenReturn(Optional.empty());

        Invoice invoice2025 = TestDataBuilder.createInvoice(1L, creditCardId, jan2025, BigDecimal.ZERO);
        Invoice invoice2026 = TestDataBuilder.createInvoice(2L, creditCardId, jan2026, BigDecimal.ZERO);

        when(invoiceRepository.save(any(Invoice.class)))
                .thenReturn(invoice2025)
                .thenReturn(invoice2026);

        // When
        Invoice result1 = useCase.execute(creditCardId, jan2025);
        Invoice result2 = useCase.execute(creditCardId, jan2026);

        // Then: Should create different invoices for different years
        assertThat(result1.getReferenceMonth().getYear()).isEqualTo(2025);
        assertThat(result2.getReferenceMonth().getYear()).isEqualTo(2026);
        assertThat(result1.getId()).isNotEqualTo(result2.getId());
    }

    @Test
    @DisplayName("Should not modify existing invoice when returning it")
    void shouldNotModifyExistingInvoice() {
        // Given: Existing invoice with specific values
        Invoice existingInvoice = TestDataBuilder.createInvoice(1L, creditCardId, referenceMonth, new BigDecimal("1500.00"));
        existingInvoice.setPreviousBalance(new BigDecimal("200.00"));
        existingInvoice.setClosed(true);
        existingInvoice.setPaid(true);

        when(invoiceRepository.findByCreditCardIdAndReferenceMonth(creditCardId, referenceMonth))
                .thenReturn(Optional.of(existingInvoice));

        // When
        Invoice result = useCase.execute(creditCardId, referenceMonth);

        // Then: Should return invoice unchanged
        assertThat(result.getTotalAmount()).isEqualByComparingTo("1500.00");
        assertThat(result.getPreviousBalance()).isEqualByComparingTo("200.00");
        assertThat(result.isClosed()).isTrue();
        assertThat(result.isPaid()).isTrue();

        verify(invoiceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should use first day of month for reference month")
    void shouldUseFirstDayOfMonthForReferenceMonth() {
        // Given: Reference month with day = 1
        LocalDate referenceMonth = LocalDate.of(2025, 3, 1);

        when(invoiceRepository.findByCreditCardIdAndReferenceMonth(creditCardId, referenceMonth))
                .thenReturn(Optional.empty());
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        useCase.execute(creditCardId, referenceMonth);

        // Then: Should use exact date provided
        verify(invoiceRepository).save(invoiceCaptor.capture());
        Invoice newInvoice = invoiceCaptor.getValue();

        assertThat(newInvoice.getReferenceMonth()).isEqualTo(referenceMonth);
        assertThat(newInvoice.getReferenceMonth().getDayOfMonth()).isEqualTo(1);
    }
}
