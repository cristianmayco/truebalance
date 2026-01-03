package com.truebalance.truebalance.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.truebalance.truebalance.application.dto.input.PartialPaymentRequestDTO;
import com.truebalance.truebalance.application.dto.output.InvoiceBalanceDTO;
import com.truebalance.truebalance.application.exception.GlobalExceptionHandler;
import com.truebalance.truebalance.config.TestWebMvcConfig;
import com.truebalance.truebalance.domain.entity.Installment;
import com.truebalance.truebalance.domain.entity.Invoice;
import com.truebalance.truebalance.domain.entity.PartialPayment;
import com.truebalance.truebalance.domain.usecase.*;
import com.truebalance.truebalance.util.TestDataBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for InvoiceController using MockMvc.
 * Tests HTTP endpoints, JSON serialization, and validation.
 */
@WebMvcTest(InvoiceController.class)
@Import({GlobalExceptionHandler.class, TestWebMvcConfig.class})
@DisplayName("InvoiceController API Tests")
class InvoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GetInvoiceById getInvoiceById;

    @MockBean
    private CloseInvoice closeInvoice;

    @MockBean
    private GetInvoiceBalance getInvoiceBalance;

    @MockBean
    private GetInvoiceInstallments getInvoiceInstallments;

    @MockBean
    private GetPartialPaymentsByInvoice getPartialPaymentsByInvoice;

    @MockBean
    private RegisterPartialPayment registerPartialPayment;

    @MockBean
    private DeletePartialPayment deletePartialPayment;

    // ==================== GET /invoices/{id} ====================

    @Test
    @DisplayName("GET /invoices/{id} - Should return invoice with 200 OK when found")
    void shouldReturnInvoiceByIdWhenFound() throws Exception {
        // Given: Invoice exists
        Long invoiceId = 1L;
        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, 1L, LocalDate.of(2025, 1, 1), new BigDecimal("1500.00"));
        invoice.setPreviousBalance(new BigDecimal("200.00"));

        when(getInvoiceById.execute(invoiceId)).thenReturn(Optional.of(invoice));

        // When & Then
        mockMvc.perform(get("/invoices/{id}", invoiceId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.creditCardId").value(1))
                .andExpect(jsonPath("$.totalAmount").value(1500.00))
                .andExpect(jsonPath("$.previousBalance").value(200.00))
                .andExpect(jsonPath("$.closed").value(false))
                .andExpect(jsonPath("$.paid").value(false));
    }

    @Test
    @DisplayName("GET /invoices/{id} - Should return 404 when invoice not found")
    void shouldReturn404WhenInvoiceNotFound() throws Exception {
        // Given: Invoice does not exist
        Long invoiceId = 999L;
        when(getInvoiceById.execute(invoiceId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/invoices/{id}", invoiceId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // ==================== POST /invoices/{id}/close ====================

    @Test
    @DisplayName("POST /invoices/{id}/close - Should close invoice and return 200 OK")
    void shouldCloseInvoiceSuccessfully() throws Exception {
        // Given: Open invoice
        Long invoiceId = 1L;
        Invoice closedInvoice = TestDataBuilder.createInvoice(invoiceId, 1L, LocalDate.of(2025, 1, 1), new BigDecimal("1500.00"));
        closedInvoice.setClosed(true);
        closedInvoice.setPaid(false);

        when(closeInvoice.execute(invoiceId)).thenReturn(Optional.of(closedInvoice));

        // When & Then
        mockMvc.perform(post("/invoices/{id}/close", invoiceId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.closed").value(true));
    }

    @Test
    @DisplayName("POST /invoices/{id}/close - Should return 404 when invoice not found")
    void shouldReturn404WhenClosingNonExistentInvoice() throws Exception {
        // Given: Invoice does not exist
        Long invoiceId = 999L;
        when(closeInvoice.execute(invoiceId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/invoices/{id}/close", invoiceId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /invoices/{id}/close - Should return 400 when invoice already closed")
    void shouldReturn400WhenInvoiceAlreadyClosed() throws Exception {
        // Given: Invoice is already closed (throws IllegalStateException)
        Long invoiceId = 1L;
        when(closeInvoice.execute(invoiceId)).thenThrow(new IllegalStateException("Invoice already closed"));

        // When & Then
        mockMvc.perform(post("/invoices/{id}/close", invoiceId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ==================== GET /invoices/{id}/balance ====================

    @Test
    @DisplayName("GET /invoices/{id}/balance - Should return balance with 200 OK")
    void shouldReturnInvoiceBalance() throws Exception {
        // Given: Invoice with balance
        Long invoiceId = 1L;
        InvoiceBalanceDTO balance = new InvoiceBalanceDTO(
                invoiceId,
                new BigDecimal("2000.00"),
                new BigDecimal("300.00"),
                new BigDecimal("500.00"),
                new BigDecimal("1800.00"),
                false,
                false,
                2
        );

        when(getInvoiceBalance.execute(invoiceId)).thenReturn(Optional.of(balance));

        // When & Then
        mockMvc.perform(get("/invoices/{id}/balance", invoiceId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.invoiceId").value(1))
                .andExpect(jsonPath("$.totalAmount").value(2000.00))
                .andExpect(jsonPath("$.previousBalance").value(300.00))
                .andExpect(jsonPath("$.partialPaymentsTotal").value(500.00))
                .andExpect(jsonPath("$.currentBalance").value(1800.00))
                .andExpect(jsonPath("$.partialPaymentsCount").value(2));
    }

    @Test
    @DisplayName("GET /invoices/{id}/balance - Should return 404 when invoice not found")
    void shouldReturn404WhenGettingBalanceForNonExistentInvoice() throws Exception {
        // Given: Invoice does not exist
        Long invoiceId = 999L;
        when(getInvoiceBalance.execute(invoiceId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/invoices/{id}/balance", invoiceId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // ==================== GET /invoices/{id}/installments ====================

    @Test
    @DisplayName("GET /invoices/{id}/installments - Should return installments with 200 OK")
    void shouldReturnInvoiceInstallments() throws Exception {
        // Given: Invoice with installments
        Long invoiceId = 1L;
        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, 1L, LocalDate.of(2025, 1, 1), new BigDecimal("1500.00"));

        Installment inst1 = TestDataBuilder.createInstallment(1L, 1L, invoiceId, 1, new BigDecimal("500.00"), LocalDate.of(2025, 1, 17));
        Installment inst2 = TestDataBuilder.createInstallment(2L, 1L, invoiceId, 2, new BigDecimal("500.00"), LocalDate.of(2025, 2, 17));

        when(getInvoiceById.execute(invoiceId)).thenReturn(Optional.of(invoice));
        when(getInvoiceInstallments.execute(invoiceId)).thenReturn(List.of(inst1, inst2));

        // When & Then
        mockMvc.perform(get("/invoices/{id}/installments", invoiceId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].installmentNumber").value(1))
                .andExpect(jsonPath("$[0].amount").value(500.00))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    @DisplayName("GET /invoices/{id}/installments - Should return 404 when invoice not found")
    void shouldReturn404WhenGettingInstallmentsForNonExistentInvoice() throws Exception {
        // Given: Invoice does not exist
        Long invoiceId = 999L;
        when(getInvoiceById.execute(invoiceId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/invoices/{id}/installments", invoiceId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /invoices/{id}/installments - Should return empty list when invoice has no installments")
    void shouldReturnEmptyListWhenInvoiceHasNoInstallments() throws Exception {
        // Given: Invoice exists but has no installments
        Long invoiceId = 1L;
        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, 1L, LocalDate.of(2025, 1, 1), BigDecimal.ZERO);

        when(getInvoiceById.execute(invoiceId)).thenReturn(Optional.of(invoice));
        when(getInvoiceInstallments.execute(invoiceId)).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/invoices/{id}/installments", invoiceId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ==================== POST /invoices/{id}/partial-payments ====================

    @Test
    @DisplayName("POST /invoices/{id}/partial-payments - Should register payment and return 201 Created")
    void shouldRegisterPartialPaymentSuccessfully() throws Exception {
        // Given: Valid partial payment request
        Long invoiceId = 1L;
        PartialPaymentRequestDTO request = new PartialPaymentRequestDTO(
                new BigDecimal("500.00"),
                "Early payment"
        );

        PartialPayment registeredPayment = TestDataBuilder.createPartialPayment(1L, invoiceId, new BigDecimal("500.00"), LocalDateTime.now());
        AvailableLimitResult limitResult = TestDataBuilder.createAvailableLimitResult(
                1L,
                new BigDecimal("5000.00"),
                new BigDecimal("1500.00"),
                new BigDecimal("500.00"),
                new BigDecimal("4000.00")
        );

        RegisterPartialPayment.RegisterPartialPaymentResult result =
                new RegisterPartialPayment.RegisterPartialPaymentResult(registeredPayment, limitResult.getAvailableLimit());

        when(registerPartialPayment.execute(eq(invoiceId), any(PartialPayment.class))).thenReturn(result);

        // When & Then
        mockMvc.perform(post("/invoices/{id}/partial-payments", invoiceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(500.00))
                .andExpect(jsonPath("$.creditCardAvailableLimit").value(4000.00));
    }

    @Test
    @DisplayName("POST /invoices/{id}/partial-payments - Should return 400 when amount is null")
    void shouldReturn400WhenPartialPaymentAmountIsNull() throws Exception {
        // Given: Request with null amount
        Long invoiceId = 1L;
        String jsonRequest = """
                {
                    "amount": null,
                    "description": "Test payment"
                }
                """;

        // When & Then
        mockMvc.perform(post("/invoices/{id}/partial-payments", invoiceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /invoices/{id}/partial-payments - Should return 400 when amount is not positive")
    void shouldReturn400WhenPartialPaymentAmountIsNotPositive() throws Exception {
        // Given: Request with zero amount
        Long invoiceId = 1L;
        PartialPaymentRequestDTO request = new PartialPaymentRequestDTO(
                BigDecimal.ZERO,  // Not positive
                "Invalid payment"
        );

        // When & Then
        mockMvc.perform(post("/invoices/{id}/partial-payments", invoiceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /invoices/{id}/partial-payments - Should return 400 when business rule fails")
    void shouldReturn400WhenPartialPaymentBusinessRuleFails() throws Exception {
        // Given: Business rule violation (e.g., invoice closed or card doesn't allow partial payments)
        Long invoiceId = 1L;
        PartialPaymentRequestDTO request = new PartialPaymentRequestDTO(
                new BigDecimal("500.00"),
                "Payment attempt"
        );

        when(registerPartialPayment.execute(eq(invoiceId), any(PartialPayment.class)))
                .thenThrow(new IllegalStateException("Credit card does not allow partial payments"));

        // When & Then
        mockMvc.perform(post("/invoices/{id}/partial-payments", invoiceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ==================== GET /invoices/{id}/partial-payments ====================

    @Test
    @DisplayName("GET /invoices/{id}/partial-payments - Should return payments with 200 OK")
    void shouldReturnPartialPayments() throws Exception {
        // Given: Invoice with partial payments
        Long invoiceId = 1L;
        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, 1L, LocalDate.of(2025, 1, 1), new BigDecimal("1500.00"));

        PartialPayment payment1 = TestDataBuilder.createPartialPayment(1L, invoiceId, new BigDecimal("500.00"), LocalDateTime.of(2025, 1, 10, 14, 30));
        PartialPayment payment2 = TestDataBuilder.createPartialPayment(2L, invoiceId, new BigDecimal("300.00"), LocalDateTime.of(2025, 1, 15, 10, 0));

        when(getInvoiceById.execute(invoiceId)).thenReturn(Optional.of(invoice));
        when(getPartialPaymentsByInvoice.execute(invoiceId)).thenReturn(List.of(payment1, payment2));

        // When & Then
        mockMvc.perform(get("/invoices/{id}/partial-payments", invoiceId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].amount").value(500.00))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].amount").value(300.00));
    }

    @Test
    @DisplayName("GET /invoices/{id}/partial-payments - Should return 404 when invoice not found")
    void shouldReturn404WhenGettingPaymentsForNonExistentInvoice() throws Exception {
        // Given: Invoice does not exist
        Long invoiceId = 999L;
        when(getInvoiceById.execute(invoiceId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/invoices/{id}/partial-payments", invoiceId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /invoices/{id}/partial-payments - Should return empty list when invoice has no payments")
    void shouldReturnEmptyListWhenInvoiceHasNoPartialPayments() throws Exception {
        // Given: Invoice exists but has no partial payments
        Long invoiceId = 1L;
        Invoice invoice = TestDataBuilder.createInvoice(invoiceId, 1L, LocalDate.of(2025, 1, 1), new BigDecimal("1500.00"));

        when(getInvoiceById.execute(invoiceId)).thenReturn(Optional.of(invoice));
        when(getPartialPaymentsByInvoice.execute(invoiceId)).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/invoices/{id}/partial-payments", invoiceId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ==================== DELETE /invoices/partial-payments/{id} ====================

    @Test
    @DisplayName("DELETE /invoices/partial-payments/{id} - Should delete payment and return 204 No Content")
    void shouldDeletePartialPaymentSuccessfully() throws Exception {
        // Given: Payment exists
        Long paymentId = 1L;
        when(deletePartialPayment.execute(paymentId)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/invoices/partial-payments/{id}", paymentId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /invoices/partial-payments/{id} - Should return 404 when payment not found")
    void shouldReturn404WhenDeletingNonExistentPartialPayment() throws Exception {
        // Given: Payment does not exist
        Long paymentId = 999L;
        when(deletePartialPayment.execute(paymentId)).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/invoices/partial-payments/{id}", paymentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /invoices/partial-payments/{id} - Should return 400 when invoice is closed")
    void shouldReturn400WhenDeletingPaymentFromClosedInvoice() throws Exception {
        // Given: Invoice is closed (throws IllegalStateException)
        Long paymentId = 1L;
        when(deletePartialPayment.execute(paymentId))
                .thenThrow(new IllegalStateException("Cannot delete payment from closed invoice"));

        // When & Then
        mockMvc.perform(delete("/invoices/partial-payments/{id}", paymentId))
                .andExpect(status().isBadRequest());
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("POST /invoices/{id}/partial-payments - Should handle missing description (optional field)")
    void shouldHandleMissingDescriptionInPartialPayment() throws Exception {
        // Given: Request without description
        Long invoiceId = 1L;
        PartialPaymentRequestDTO request = new PartialPaymentRequestDTO(
                new BigDecimal("500.00"),
                null  // Optional description
        );

        PartialPayment registeredPayment = TestDataBuilder.createPartialPayment(1L, invoiceId, new BigDecimal("500.00"), LocalDateTime.now());
        registeredPayment.setDescription(null);

        AvailableLimitResult limitResult = TestDataBuilder.createAvailableLimitResult();
        RegisterPartialPayment.RegisterPartialPaymentResult result =
                new RegisterPartialPayment.RegisterPartialPaymentResult(registeredPayment, limitResult.getAvailableLimit());

        when(registerPartialPayment.execute(eq(invoiceId), any(PartialPayment.class))).thenReturn(result);

        // When & Then: Should succeed without description
        mockMvc.perform(post("/invoices/{id}/partial-payments", invoiceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /invoices/{id}/partial-payments - Should handle malformed JSON with 400 Bad Request")
    void shouldReturn400ForMalformedJsonInPartialPayment() throws Exception {
        // Given: Malformed JSON
        Long invoiceId = 1L;
        String malformedJson = "{ invalid json }";

        // When & Then
        mockMvc.perform(post("/invoices/{id}/partial-payments", invoiceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /invoices/{id}/balance - Should handle zero balance correctly")
    void shouldHandleZeroBalanceCorrectly() throws Exception {
        // Given: Invoice with zero balance (fully paid)
        Long invoiceId = 1L;
        InvoiceBalanceDTO balance = new InvoiceBalanceDTO(
                invoiceId,
                new BigDecimal("1000.00"),
                BigDecimal.ZERO,
                new BigDecimal("1000.00"),
                BigDecimal.ZERO,
                true,
                true,
                1
        );

        when(getInvoiceBalance.execute(invoiceId)).thenReturn(Optional.of(balance));

        // When & Then
        mockMvc.perform(get("/invoices/{id}/balance", invoiceId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentBalance").value(0.00))
                .andExpect(jsonPath("$.paid").value(true))
                .andExpect(jsonPath("$.closed").value(true));
    }

    @Test
    @DisplayName("GET /invoices/{id}/balance - Should handle negative balance (credit)")
    void shouldHandleNegativeBalanceCorrectly() throws Exception {
        // Given: Invoice with negative balance (overpaid)
        Long invoiceId = 1L;
        InvoiceBalanceDTO balance = new InvoiceBalanceDTO(
                invoiceId,
                new BigDecimal("1000.00"),
                BigDecimal.ZERO,
                new BigDecimal("1500.00"),
                new BigDecimal("-500.00"),
                true,
                true,
                2
        );

        when(getInvoiceBalance.execute(invoiceId)).thenReturn(Optional.of(balance));

        // When & Then
        mockMvc.perform(get("/invoices/{id}/balance", invoiceId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentBalance").value(-500.00));
    }
}
