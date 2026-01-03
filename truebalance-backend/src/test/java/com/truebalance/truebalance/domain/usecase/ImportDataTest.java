package com.truebalance.truebalance.domain.usecase;

import com.truebalance.truebalance.application.dto.input.BillRequestDTO;
import com.truebalance.truebalance.application.dto.input.CreditCardRequestDTO;
import com.truebalance.truebalance.application.dto.input.ImportDataDTO;
import com.truebalance.truebalance.application.dto.input.InvoiceImportItemDTO;
import com.truebalance.truebalance.application.dto.output.ImportResultDTO;
import com.truebalance.truebalance.domain.entity.Bill;
import com.truebalance.truebalance.domain.entity.CreditCard;
import com.truebalance.truebalance.domain.entity.Invoice;
import com.truebalance.truebalance.domain.port.BillRepositoryPort;
import com.truebalance.truebalance.domain.port.CreditCardRepositoryPort;
import com.truebalance.truebalance.domain.port.InvoiceRepositoryPort;
import com.truebalance.truebalance.util.TestDataBuilder;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ImportData Use Case Tests")
class ImportDataTest {

    @Mock
    private BillRepositoryPort billRepository;

    @Mock
    private CreditCardRepositoryPort creditCardRepository;

    @Mock
    private InvoiceRepositoryPort invoiceRepository;

    @Mock
    private CreateBill createBill;

    @Mock
    private CreateCreditCard createCreditCard;

    @InjectMocks
    private ImportData useCase;

    @Captor
    private ArgumentCaptor<Bill> billCaptor;

    @Captor
    private ArgumentCaptor<CreditCard> creditCardCaptor;

    @Captor
    private ArgumentCaptor<Invoice> invoiceCaptor;

    // ==================== Credit Cards Import Tests ====================

    @Test
    @DisplayName("Should import credit cards successfully")
    void shouldImportCreditCardsSuccessfully() {
        // Given: Import data with credit cards
        ImportDataDTO importData = new ImportDataDTO();
        CreditCardRequestDTO cardDTO1 = new CreditCardRequestDTO("Card 1", new BigDecimal("5000.00"), 10, 17, true);
        CreditCardRequestDTO cardDTO2 = new CreditCardRequestDTO("Card 2", new BigDecimal("3000.00"), 15, 20, false);
        importData.setCreditCards(List.of(cardDTO1, cardDTO2));

        when(creditCardRepository.findAll()).thenReturn(List.of());
        when(createCreditCard.execute(any(CreditCard.class))).thenAnswer(invocation -> {
            CreditCard card = invocation.getArgument(0);
            card.setId(1L);
            return card;
        });

        // When
        ImportResultDTO result = useCase.execute(importData);

        // Then
        assertThat(result.getTotalProcessed()).isEqualTo(2);
        assertThat(result.getTotalCreated()).isEqualTo(2);
        assertThat(result.getTotalSkipped()).isEqualTo(0);
        assertThat(result.getTotalErrors()).isEqualTo(0);
        verify(createCreditCard, times(2)).execute(any(CreditCard.class));
    }

    @Test
    @DisplayName("Should skip duplicate credit cards")
    void shouldSkipDuplicateCreditCards() {
        // Given: Import data with duplicate credit card
        ImportDataDTO importData = new ImportDataDTO();
        CreditCardRequestDTO cardDTO = new CreditCardRequestDTO("Existing Card", new BigDecimal("5000.00"), 10, 17, true);
        importData.setCreditCards(List.of(cardDTO));

        CreditCard existingCard = TestDataBuilder.createCreditCard(1L, "existing card", new BigDecimal("5000.00"), 10, 17);
        when(creditCardRepository.findAll()).thenReturn(List.of(existingCard));

        // When
        ImportResultDTO result = useCase.execute(importData);

        // Then
        assertThat(result.getTotalProcessed()).isEqualTo(1);
        assertThat(result.getTotalCreated()).isEqualTo(0);
        assertThat(result.getTotalSkipped()).isEqualTo(1);
        assertThat(result.getTotalErrors()).isEqualTo(0);
        verify(createCreditCard, never()).execute(any(CreditCard.class));
    }

    @Test
    @DisplayName("Should handle errors when importing credit cards")
    void shouldHandleErrorsWhenImportingCreditCards() {
        // Given: Import data with invalid credit card
        ImportDataDTO importData = new ImportDataDTO();
        CreditCardRequestDTO cardDTO = new CreditCardRequestDTO("Invalid Card", new BigDecimal("5000.00"), 10, 17, true);
        importData.setCreditCards(List.of(cardDTO));

        when(creditCardRepository.findAll()).thenReturn(List.of());
        when(createCreditCard.execute(any(CreditCard.class))).thenThrow(new IllegalArgumentException("Invalid data"));

        // When
        ImportResultDTO result = useCase.execute(importData);

        // Then
        assertThat(result.getTotalProcessed()).isEqualTo(1);
        assertThat(result.getTotalCreated()).isEqualTo(0);
        assertThat(result.getTotalSkipped()).isEqualTo(1);
        assertThat(result.getTotalErrors()).isEqualTo(1);
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0)).contains("Erro ao importar cartão");
    }

    // ==================== Bills Import Tests ====================

    @Test
    @DisplayName("Should import bills successfully")
    void shouldImportBillsSuccessfully() {
        // Given: Import data with bills
        ImportDataDTO importData = new ImportDataDTO();
        BillRequestDTO billDTO1 = new BillRequestDTO("Bill 1", LocalDateTime.now(), new BigDecimal("1000.00"), 10, "Description 1", null);
        BillRequestDTO billDTO2 = new BillRequestDTO("Bill 2", LocalDateTime.now(), new BigDecimal("500.00"), 5, "Description 2", null);
        importData.setBills(List.of(billDTO1, billDTO2));

        Bill createdBill1 = TestDataBuilder.createBill(1L, "Bill 1", new BigDecimal("1000.00"), 10);
        Bill createdBill2 = TestDataBuilder.createBill(2L, "Bill 2", new BigDecimal("500.00"), 5);

        when(createBill.addBill(any(Bill.class))).thenReturn(createdBill1, createdBill2);

        // When
        ImportResultDTO result = useCase.execute(importData);

        // Then
        assertThat(result.getTotalProcessed()).isEqualTo(2);
        assertThat(result.getTotalCreated()).isEqualTo(2);
        assertThat(result.getTotalSkipped()).isEqualTo(0);
        assertThat(result.getTotalErrors()).isEqualTo(0);
        verify(createBill, times(2)).addBill(any(Bill.class));
    }

    @Test
    @DisplayName("Should handle errors when importing bills")
    void shouldHandleErrorsWhenImportingBills() {
        // Given: Import data with invalid bill
        ImportDataDTO importData = new ImportDataDTO();
        BillRequestDTO billDTO = new BillRequestDTO("Invalid Bill", LocalDateTime.now(), new BigDecimal("1000.00"), 10, null, null);
        importData.setBills(List.of(billDTO));

        when(createBill.addBill(any(Bill.class))).thenThrow(new IllegalArgumentException("Invalid bill"));

        // When
        ImportResultDTO result = useCase.execute(importData);

        // Then
        assertThat(result.getTotalProcessed()).isEqualTo(1);
        assertThat(result.getTotalCreated()).isEqualTo(0);
        assertThat(result.getTotalSkipped()).isEqualTo(1);
        assertThat(result.getTotalErrors()).isEqualTo(1);
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0)).contains("Erro ao importar conta");
    }

    // ==================== Invoices Import Tests ====================

    @Test
    @DisplayName("Should import invoices successfully")
    void shouldImportInvoicesSuccessfully() {
        // Given: Import data with invoices
        ImportDataDTO importData = new ImportDataDTO();
        InvoiceImportItemDTO invoiceDTO1 = new InvoiceImportItemDTO();
        invoiceDTO1.setCreditCardId(1L);
        invoiceDTO1.setReferenceMonth(LocalDate.of(2025, 1, 1));
        invoiceDTO1.setTotalAmount(new BigDecimal("500.00"));
        invoiceDTO1.setPreviousBalance(BigDecimal.ZERO);
        invoiceDTO1.setClosed(false);
        invoiceDTO1.setPaid(false);

        InvoiceImportItemDTO invoiceDTO2 = new InvoiceImportItemDTO();
        invoiceDTO2.setCreditCardId(1L);
        invoiceDTO2.setReferenceMonth(LocalDate.of(2025, 2, 1));
        invoiceDTO2.setTotalAmount(new BigDecimal("600.00"));
        invoiceDTO2.setPreviousBalance(new BigDecimal("50.00"));
        invoiceDTO2.setClosed(true);
        invoiceDTO2.setPaid(false);

        importData.setInvoices(List.of(invoiceDTO1, invoiceDTO2));

        CreditCard creditCard = TestDataBuilder.createCreditCard(1L, "Card 1", new BigDecimal("5000.00"), 10, 17);
        when(creditCardRepository.findById(1L)).thenReturn(Optional.of(creditCard));
        when(invoiceRepository.findByCreditCardIdAndReferenceMonth(anyLong(), any(LocalDate.class))).thenReturn(Optional.empty());
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> {
            Invoice invoice = invocation.getArgument(0);
            invoice.setId(1L);
            return invoice;
        });

        // When
        ImportResultDTO result = useCase.execute(importData);

        // Then
        assertThat(result.getTotalProcessed()).isEqualTo(2);
        assertThat(result.getTotalCreated()).isEqualTo(2);
        assertThat(result.getTotalSkipped()).isEqualTo(0);
        assertThat(result.getTotalErrors()).isEqualTo(0);
        verify(invoiceRepository, times(2)).save(any(Invoice.class));
    }

    @Test
    @DisplayName("Should skip duplicate invoices")
    void shouldSkipDuplicateInvoices() {
        // Given: Import data with duplicate invoice
        ImportDataDTO importData = new ImportDataDTO();
        InvoiceImportItemDTO invoiceDTO = new InvoiceImportItemDTO();
        invoiceDTO.setCreditCardId(1L);
        invoiceDTO.setReferenceMonth(LocalDate.of(2025, 1, 1));
        invoiceDTO.setTotalAmount(new BigDecimal("500.00"));
        importData.setInvoices(List.of(invoiceDTO));

        CreditCard creditCard = TestDataBuilder.createCreditCard(1L, "Card 1", new BigDecimal("5000.00"), 10, 17);
        Invoice existingInvoice = TestDataBuilder.createInvoice(1L, 1L, LocalDate.of(2025, 1, 1), new BigDecimal("500.00"));

        when(creditCardRepository.findById(1L)).thenReturn(Optional.of(creditCard));
        when(invoiceRepository.findByCreditCardIdAndReferenceMonth(1L, LocalDate.of(2025, 1, 1)))
                .thenReturn(Optional.of(existingInvoice));

        // When
        ImportResultDTO result = useCase.execute(importData);

        // Then
        assertThat(result.getTotalProcessed()).isEqualTo(1);
        assertThat(result.getTotalCreated()).isEqualTo(0);
        assertThat(result.getTotalSkipped()).isEqualTo(1);
        assertThat(result.getTotalErrors()).isEqualTo(0);
        verify(invoiceRepository, never()).save(any(Invoice.class));
    }

    @Test
    @DisplayName("Should skip invoice when credit card not found")
    void shouldSkipInvoiceWhenCreditCardNotFound() {
        // Given: Import data with invoice for non-existent credit card
        ImportDataDTO importData = new ImportDataDTO();
        InvoiceImportItemDTO invoiceDTO = new InvoiceImportItemDTO();
        invoiceDTO.setCreditCardId(999L);
        invoiceDTO.setReferenceMonth(LocalDate.of(2025, 1, 1));
        invoiceDTO.setTotalAmount(new BigDecimal("500.00"));
        importData.setInvoices(List.of(invoiceDTO));

        when(creditCardRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        ImportResultDTO result = useCase.execute(importData);

        // Then
        assertThat(result.getTotalProcessed()).isEqualTo(1);
        assertThat(result.getTotalCreated()).isEqualTo(0);
        assertThat(result.getTotalSkipped()).isEqualTo(1);
        assertThat(result.getTotalErrors()).isEqualTo(1);
        assertThat(result.getErrors().get(0)).contains("Cartão de crédito ID=999 não encontrado");
        verify(invoiceRepository, never()).save(any(Invoice.class));
    }

    @Test
    @DisplayName("Should handle errors when importing invoices")
    void shouldHandleErrorsWhenImportingInvoices() {
        // Given: Import data with invoice that causes error
        ImportDataDTO importData = new ImportDataDTO();
        InvoiceImportItemDTO invoiceDTO = new InvoiceImportItemDTO();
        invoiceDTO.setCreditCardId(1L);
        invoiceDTO.setReferenceMonth(LocalDate.of(2025, 1, 1));
        invoiceDTO.setTotalAmount(new BigDecimal("500.00"));
        importData.setInvoices(List.of(invoiceDTO));

        CreditCard creditCard = TestDataBuilder.createCreditCard(1L, "Card 1", new BigDecimal("5000.00"), 10, 17);
        when(creditCardRepository.findById(1L)).thenReturn(Optional.of(creditCard));
        when(invoiceRepository.findByCreditCardIdAndReferenceMonth(anyLong(), any(LocalDate.class))).thenReturn(Optional.empty());
        when(invoiceRepository.save(any(Invoice.class))).thenThrow(new RuntimeException("Database error"));

        // When
        ImportResultDTO result = useCase.execute(importData);

        // Then
        assertThat(result.getTotalProcessed()).isEqualTo(1);
        assertThat(result.getTotalCreated()).isEqualTo(0);
        assertThat(result.getTotalSkipped()).isEqualTo(1);
        assertThat(result.getTotalErrors()).isEqualTo(1);
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0)).contains("Erro ao importar fatura");
    }

    // ==================== Mixed Import Tests ====================

    @Test
    @DisplayName("Should import all data types successfully")
    void shouldImportAllDataTypesSuccessfully() {
        // Given: Import data with all types
        ImportDataDTO importData = new ImportDataDTO();

        // Credit cards
        CreditCardRequestDTO cardDTO = new CreditCardRequestDTO("Card 1", new BigDecimal("5000.00"), 10, 17, true);
        importData.setCreditCards(List.of(cardDTO));

        // Bills
        BillRequestDTO billDTO = new BillRequestDTO("Bill 1", LocalDateTime.now(), new BigDecimal("1000.00"), 10, null, null);
        importData.setBills(List.of(billDTO));

        // Invoices
        InvoiceImportItemDTO invoiceDTO = new InvoiceImportItemDTO();
        invoiceDTO.setCreditCardId(1L);
        invoiceDTO.setReferenceMonth(LocalDate.of(2025, 1, 1));
        invoiceDTO.setTotalAmount(new BigDecimal("500.00"));
        importData.setInvoices(List.of(invoiceDTO));

        // Mock responses
        when(creditCardRepository.findAll()).thenReturn(List.of());
        when(createCreditCard.execute(any(CreditCard.class))).thenAnswer(invocation -> {
            CreditCard card = invocation.getArgument(0);
            card.setId(1L);
            return card;
        });

        Bill createdBill = TestDataBuilder.createBill(1L, "Bill 1", new BigDecimal("1000.00"), 10);
        when(createBill.addBill(any(Bill.class))).thenReturn(createdBill);

        CreditCard creditCard = TestDataBuilder.createCreditCard(1L, "Card 1", new BigDecimal("5000.00"), 10, 17);
        when(creditCardRepository.findById(1L)).thenReturn(Optional.of(creditCard));
        when(invoiceRepository.findByCreditCardIdAndReferenceMonth(anyLong(), any(LocalDate.class))).thenReturn(Optional.empty());
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> {
            Invoice invoice = invocation.getArgument(0);
            invoice.setId(1L);
            return invoice;
        });

        // When
        ImportResultDTO result = useCase.execute(importData);

        // Then
        assertThat(result.getTotalProcessed()).isEqualTo(3);
        assertThat(result.getTotalCreated()).isEqualTo(3);
        assertThat(result.getTotalSkipped()).isEqualTo(0);
        assertThat(result.getTotalErrors()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should handle empty import data")
    void shouldHandleEmptyImportData() {
        // Given: Empty import data
        ImportDataDTO importData = new ImportDataDTO();

        // When
        ImportResultDTO result = useCase.execute(importData);

        // Then
        assertThat(result.getTotalProcessed()).isEqualTo(0);
        assertThat(result.getTotalCreated()).isEqualTo(0);
        assertThat(result.getTotalSkipped()).isEqualTo(0);
        assertThat(result.getTotalErrors()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should set default values for invoice optional fields")
    void shouldSetDefaultValuesForInvoiceOptionalFields() {
        // Given: Invoice with null optional fields
        ImportDataDTO importData = new ImportDataDTO();
        InvoiceImportItemDTO invoiceDTO = new InvoiceImportItemDTO();
        invoiceDTO.setCreditCardId(1L);
        invoiceDTO.setReferenceMonth(LocalDate.of(2025, 1, 1));
        invoiceDTO.setTotalAmount(new BigDecimal("500.00"));
        invoiceDTO.setPreviousBalance(null);
        invoiceDTO.setClosed(null);
        invoiceDTO.setPaid(null);
        importData.setInvoices(List.of(invoiceDTO));

        CreditCard creditCard = TestDataBuilder.createCreditCard(1L, "Card 1", new BigDecimal("5000.00"), 10, 17);
        when(creditCardRepository.findById(1L)).thenReturn(Optional.of(creditCard));
        when(invoiceRepository.findByCreditCardIdAndReferenceMonth(anyLong(), any(LocalDate.class))).thenReturn(Optional.empty());
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> {
            Invoice invoice = invocation.getArgument(0);
            invoice.setId(1L);
            return invoice;
        });

        // When
        useCase.execute(importData);

        // Then
        verify(invoiceRepository).save(invoiceCaptor.capture());
        Invoice savedInvoice = invoiceCaptor.getValue();
        assertThat(savedInvoice.getPreviousBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(savedInvoice.isClosed()).isFalse();
        assertThat(savedInvoice.isPaid()).isFalse();
    }
}
