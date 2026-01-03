package com.truebalance.truebalance.domain.usecase;

import com.truebalance.truebalance.application.dto.output.ExportDataDTO;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExportData Use Case Tests")
class ExportDataTest {

    @Mock
    private BillRepositoryPort billRepository;

    @Mock
    private CreditCardRepositoryPort creditCardRepository;

    @Mock
    private InvoiceRepositoryPort invoiceRepository;

    @InjectMocks
    private ExportData useCase;

    @Test
    @DisplayName("Should export all data successfully")
    void shouldExportAllDataSuccessfully() {
        // Given: Data exists in repositories
        Bill bill1 = TestDataBuilder.createBill(1L, "Bill 1", new BigDecimal("1000.00"), 10);
        Bill bill2 = TestDataBuilder.createBill(2L, "Bill 2", new BigDecimal("500.00"), 5);
        List<Bill> bills = List.of(bill1, bill2);

        CreditCard card1 = TestDataBuilder.createCreditCard(1L, "Card 1", new BigDecimal("5000.00"), 10, 17);
        CreditCard card2 = TestDataBuilder.createCreditCard(2L, "Card 2", new BigDecimal("3000.00"), 15, 20);
        List<CreditCard> creditCards = List.of(card1, card2);

        Invoice invoice1 = TestDataBuilder.createInvoice(1L, 1L, LocalDate.of(2025, 1, 1), new BigDecimal("500.00"));
        Invoice invoice2 = TestDataBuilder.createInvoice(2L, 1L, LocalDate.of(2025, 2, 1), new BigDecimal("600.00"));
        Invoice invoice3 = TestDataBuilder.createInvoice(3L, 2L, LocalDate.of(2025, 1, 1), new BigDecimal("400.00"));
        List<Invoice> invoicesCard1 = List.of(invoice1, invoice2);
        List<Invoice> invoicesCard2 = List.of(invoice3);

        when(billRepository.findAll()).thenReturn(bills);
        when(creditCardRepository.findAll()).thenReturn(creditCards);
        when(invoiceRepository.findByCreditCardId(1L)).thenReturn(invoicesCard1);
        when(invoiceRepository.findByCreditCardId(2L)).thenReturn(invoicesCard2);

        // When
        ExportDataDTO result = useCase.execute();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getBills()).hasSize(2);
        assertThat(result.getCreditCards()).hasSize(2);
        assertThat(result.getInvoices()).hasSize(3);

        assertThat(result.getBills().get(0).getId()).isEqualTo(1L);
        assertThat(result.getBills().get(0).getName()).isEqualTo("Bill 1");
        assertThat(result.getBills().get(1).getId()).isEqualTo(2L);
        assertThat(result.getBills().get(1).getName()).isEqualTo("Bill 2");

        assertThat(result.getCreditCards().get(0).getId()).isEqualTo(1L);
        assertThat(result.getCreditCards().get(0).getName()).isEqualTo("Card 1"); // Name from TestDataBuilder.createCreditCard()
        assertThat(result.getCreditCards().get(1).getId()).isEqualTo(2L);

        assertThat(result.getInvoices()).hasSize(3);
    }

    @Test
    @DisplayName("Should export empty data when no records exist")
    void shouldExportEmptyDataWhenNoRecordsExist() {
        // Given: No data exists
        when(billRepository.findAll()).thenReturn(List.of());
        when(creditCardRepository.findAll()).thenReturn(List.of());

        // When
        ExportDataDTO result = useCase.execute();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getBills()).isEmpty();
        assertThat(result.getCreditCards()).isEmpty();
        assertThat(result.getInvoices()).isEmpty();
    }

    @Test
    @DisplayName("Should export bills only when no credit cards exist")
    void shouldExportBillsOnlyWhenNoCreditCardsExist() {
        // Given: Only bills exist
        Bill bill1 = TestDataBuilder.createBill(1L, "Bill 1", new BigDecimal("1000.00"), 10);
        List<Bill> bills = List.of(bill1);

        when(billRepository.findAll()).thenReturn(bills);
        when(creditCardRepository.findAll()).thenReturn(List.of());

        // When
        ExportDataDTO result = useCase.execute();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getBills()).hasSize(1);
        assertThat(result.getCreditCards()).isEmpty();
        assertThat(result.getInvoices()).isEmpty();
    }

    @Test
    @DisplayName("Should export credit cards and invoices when no bills exist")
    void shouldExportCreditCardsAndInvoicesWhenNoBillsExist() {
        // Given: Only credit cards and invoices exist
        CreditCard card1 = TestDataBuilder.createCreditCard(1L, "Card 1", new BigDecimal("5000.00"), 10, 17);
        List<CreditCard> creditCards = List.of(card1);

        Invoice invoice1 = TestDataBuilder.createInvoice(1L, 1L, LocalDate.of(2025, 1, 1), new BigDecimal("500.00"));
        List<Invoice> invoices = List.of(invoice1);

        when(billRepository.findAll()).thenReturn(List.of());
        when(creditCardRepository.findAll()).thenReturn(creditCards);
        when(invoiceRepository.findByCreditCardId(1L)).thenReturn(invoices);

        // When
        ExportDataDTO result = useCase.execute();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getBills()).isEmpty();
        assertThat(result.getCreditCards()).hasSize(1);
        assertThat(result.getInvoices()).hasSize(1);
    }

    @Test
    @DisplayName("Should export invoices for all credit cards")
    void shouldExportInvoicesForAllCreditCards() {
        // Given: Multiple credit cards with invoices
        CreditCard card1 = TestDataBuilder.createCreditCard(1L, "Card 1", new BigDecimal("5000.00"), 10, 17);
        CreditCard card2 = TestDataBuilder.createCreditCard(2L, "Card 2", new BigDecimal("3000.00"), 15, 20);
        CreditCard card3 = TestDataBuilder.createCreditCard(3L, "Card 3", new BigDecimal("2000.00"), 5, 10);
        List<CreditCard> creditCards = List.of(card1, card2, card3);

        Invoice invoice1 = TestDataBuilder.createInvoice(1L, 1L, LocalDate.of(2025, 1, 1), new BigDecimal("500.00"));
        Invoice invoice2 = TestDataBuilder.createInvoice(2L, 2L, LocalDate.of(2025, 1, 1), new BigDecimal("300.00"));
        Invoice invoice3 = TestDataBuilder.createInvoice(3L, 3L, LocalDate.of(2025, 1, 1), new BigDecimal("200.00"));

        when(billRepository.findAll()).thenReturn(List.of());
        when(creditCardRepository.findAll()).thenReturn(creditCards);
        when(invoiceRepository.findByCreditCardId(1L)).thenReturn(List.of(invoice1));
        when(invoiceRepository.findByCreditCardId(2L)).thenReturn(List.of(invoice2));
        when(invoiceRepository.findByCreditCardId(3L)).thenReturn(List.of(invoice3));

        // When
        ExportDataDTO result = useCase.execute();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCreditCards()).hasSize(3);
        assertThat(result.getInvoices()).hasSize(3);
    }

    @Test
    @DisplayName("Should handle credit cards with no invoices")
    void shouldHandleCreditCardsWithNoInvoices() {
        // Given: Credit cards without invoices
        CreditCard card1 = TestDataBuilder.createCreditCard(1L, "Card 1", new BigDecimal("5000.00"), 10, 17);
        CreditCard card2 = TestDataBuilder.createCreditCard(2L, "Card 2", new BigDecimal("3000.00"), 15, 20);
        List<CreditCard> creditCards = List.of(card1, card2);

        when(billRepository.findAll()).thenReturn(List.of());
        when(creditCardRepository.findAll()).thenReturn(creditCards);
        when(invoiceRepository.findByCreditCardId(1L)).thenReturn(List.of());
        when(invoiceRepository.findByCreditCardId(2L)).thenReturn(List.of());

        // When
        ExportDataDTO result = useCase.execute();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCreditCards()).hasSize(2);
        assertThat(result.getInvoices()).isEmpty();
    }

    @Test
    @DisplayName("Should export all bill properties correctly")
    void shouldExportAllBillPropertiesCorrectly() {
        // Given: Bill with all properties
        Bill bill = TestDataBuilder.createBill(1L, "Test Bill", new BigDecimal("1500.00"), 12);
        bill.setDescription("Test description");
        bill.setIsRecurring(true);
        List<Bill> bills = List.of(bill);

        when(billRepository.findAll()).thenReturn(bills);
        when(creditCardRepository.findAll()).thenReturn(List.of());

        // When
        ExportDataDTO result = useCase.execute();

        // Then
        assertThat(result.getBills()).hasSize(1);
        var billDTO = result.getBills().get(0);
        assertThat(billDTO.getId()).isEqualTo(1L);
        assertThat(billDTO.getName()).isEqualTo("Test Bill");
        assertThat(billDTO.getTotalAmount()).isEqualByComparingTo("1500.00");
        assertThat(billDTO.getNumberOfInstallments()).isEqualTo(12);
        assertThat(billDTO.getDescription()).isEqualTo("Test description");
        assertThat(billDTO.getIsRecurring()).isTrue();
    }

    @Test
    @DisplayName("Should export all credit card properties correctly")
    void shouldExportAllCreditCardPropertiesCorrectly() {
        // Given: Credit card with all properties
        CreditCard card = TestDataBuilder.createCreditCard(1L, "Test Card", new BigDecimal("10000.00"), 15, 20);
        card.setAllowsPartialPayment(true);
        List<CreditCard> creditCards = List.of(card);

        when(billRepository.findAll()).thenReturn(List.of());
        when(creditCardRepository.findAll()).thenReturn(creditCards);
        when(invoiceRepository.findByCreditCardId(1L)).thenReturn(List.of());

        // When
        ExportDataDTO result = useCase.execute();

        // Then
        assertThat(result.getCreditCards()).hasSize(1);
        var cardDTO = result.getCreditCards().get(0);
        assertThat(cardDTO.getId()).isEqualTo(1L);
        assertThat(cardDTO.getCreditLimit()).isEqualByComparingTo("10000.00");
        assertThat(cardDTO.getClosingDay()).isEqualTo(15);
        assertThat(cardDTO.getDueDay()).isEqualTo(20);
        assertThat(cardDTO.isAllowsPartialPayment()).isTrue();
    }

    @Test
    @DisplayName("Should export all invoice properties correctly")
    void shouldExportAllInvoicePropertiesCorrectly() {
        // Given: Invoice with all properties
        CreditCard card = TestDataBuilder.createCreditCard(1L, "Card 1", new BigDecimal("5000.00"), 10, 17);
        List<CreditCard> creditCards = List.of(card);

        Invoice invoice = TestDataBuilder.createInvoice(1L, 1L, LocalDate.of(2025, 2, 1), new BigDecimal("750.00"));
        invoice.setPreviousBalance(new BigDecimal("100.00"));
        invoice.setClosed(true);
        invoice.setPaid(true);
        List<Invoice> invoices = List.of(invoice);

        when(billRepository.findAll()).thenReturn(List.of());
        when(creditCardRepository.findAll()).thenReturn(creditCards);
        when(invoiceRepository.findByCreditCardId(1L)).thenReturn(invoices);

        // When
        ExportDataDTO result = useCase.execute();

        // Then
        assertThat(result.getInvoices()).hasSize(1);
        var invoiceDTO = result.getInvoices().get(0);
        assertThat(invoiceDTO.getId()).isEqualTo(1L);
        assertThat(invoiceDTO.getCreditCardId()).isEqualTo(1L);
        assertThat(invoiceDTO.getReferenceMonth()).isEqualTo(LocalDate.of(2025, 2, 1));
        assertThat(invoiceDTO.getTotalAmount()).isEqualByComparingTo("750.00");
        assertThat(invoiceDTO.getPreviousBalance()).isEqualByComparingTo("100.00");
        assertThat(invoiceDTO.isClosed()).isTrue();
        assertThat(invoiceDTO.isPaid()).isTrue();
    }
}
