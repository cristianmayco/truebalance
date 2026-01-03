package com.truebalance.truebalance.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.truebalance.truebalance.application.dto.input.BillRequestDTO;
import com.truebalance.truebalance.application.exception.GlobalExceptionHandler;
import com.truebalance.truebalance.config.TestWebMvcConfig;
import com.truebalance.truebalance.domain.entity.Bill;
import com.truebalance.truebalance.domain.entity.Installment;
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

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for BillController using MockMvc.
 * Tests HTTP endpoints, JSON serialization, and validation.
 */
@WebMvcTest(BillController.class)
@Import({GlobalExceptionHandler.class, TestWebMvcConfig.class})
@DisplayName("BillController API Tests")
class BillControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateBill createBill;

    @MockBean
    private CreateBillWithCreditCard createBillWithCreditCard;

    @MockBean
    private UpdateBill updateBill;

    @MockBean
    private GetAllBills getAllBills;

    @MockBean
    private GetBillById getBillById;

    @MockBean
    private DeleteBill deleteBill;

    @MockBean
    private GetBillInstallments getBillInstallments;

    // ==================== GET /bills ====================

    @Test
    @DisplayName("GET /bills - Should return all bills with 200 OK")
    void shouldReturnAllBills() throws Exception {
        // Given: Multiple bills exist
        Bill bill1 = TestDataBuilder.createBill(1L, "Bill 1", new BigDecimal("1000.00"), 10);
        Bill bill2 = TestDataBuilder.createBill(2L, "Bill 2", new BigDecimal("500.00"), 5);

        when(getAllBills.execute()).thenReturn(List.of(bill1, bill2));

        // When & Then
        mockMvc.perform(get("/bills")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Bill 1"))
                .andExpect(jsonPath("$[0].totalAmount").value(1000.00))
                .andExpect(jsonPath("$[0].numberOfInstallments").value(10))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Bill 2"));
    }

    @Test
    @DisplayName("GET /bills - Should return empty list when no bills exist")
    void shouldReturnEmptyListWhenNoBillsExist() throws Exception {
        // Given: No bills
        when(getAllBills.execute()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/bills")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ==================== GET /bills/{id} ====================

    @Test
    @DisplayName("GET /bills/{id} - Should return bill with 200 OK when found")
    void shouldReturnBillByIdWhenFound() throws Exception {
        // Given: Bill exists
        Long billId = 1L;
        Bill bill = TestDataBuilder.createBill(billId, "Test Bill", new BigDecimal("1500.00"), 12);

        when(getBillById.execute(billId)).thenReturn(Optional.of(bill));

        // When & Then
        mockMvc.perform(get("/bills/{id}", billId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Bill"))
                .andExpect(jsonPath("$.totalAmount").value(1500.00))
                .andExpect(jsonPath("$.numberOfInstallments").value(12));
    }

    @Test
    @DisplayName("GET /bills/{id} - Should return 404 when bill not found")
    void shouldReturn404WhenBillNotFound() throws Exception {
        // Given: Bill does not exist
        Long billId = 999L;
        when(getBillById.execute(billId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/bills/{id}", billId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // ==================== POST /bills (without credit card) ====================

    @Test
    @DisplayName("POST /bills - Should create bill without credit card and return 201 Created")
    void shouldCreateBillWithoutCreditCard() throws Exception {
        // Given: Valid bill request without creditCardId
        BillRequestDTO request = new BillRequestDTO(
                "New Bill",
                LocalDateTime.of(2025, 1, 15, 10, 0),
                new BigDecimal("1000.00"),
                10,
                "Test description",
                null  // No credit card
        );

        Bill createdBill = TestDataBuilder.createBill(1L, "New Bill", new BigDecimal("1000.00"), 10);
        createdBill.setInstallmentAmount(new BigDecimal("100.00"));

        when(createBill.addBill(any(Bill.class))).thenReturn(createdBill);

        // When & Then
        mockMvc.perform(post("/bills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("New Bill"))
                .andExpect(jsonPath("$.totalAmount").value(1000.00))
                .andExpect(jsonPath("$.installmentAmount").value(100.00));
    }

    @Test
    @DisplayName("POST /bills - Should create bill with credit card and return 201 Created")
    void shouldCreateBillWithCreditCard() throws Exception {
        // Given: Valid bill request with creditCardId
        BillRequestDTO request = new BillRequestDTO(
                "Credit Card Bill",
                LocalDateTime.of(2025, 1, 15, 10, 0),
                new BigDecimal("3000.00"),
                12,
                "Smartphone purchase",
                1L  // With credit card
        );

        Bill createdBill = TestDataBuilder.createBill(1L, "Credit Card Bill", new BigDecimal("3000.00"), 12);
        createdBill.setInstallmentAmount(new BigDecimal("250.00"));

        when(createBillWithCreditCard.execute(any(Bill.class), eq(1L))).thenReturn(createdBill);

        // When & Then
        mockMvc.perform(post("/bills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Credit Card Bill"))
                .andExpect(jsonPath("$.totalAmount").value(3000.00));
    }

    @Test
    @DisplayName("POST /bills - Should create recurring bill and return 201 Created")
    void shouldCreateRecurringBill() throws Exception {
        // Given: Valid recurring bill request
        BillRequestDTO request = new BillRequestDTO(
                "Internet Bill",
                LocalDateTime.of(2025, 1, 15, 10, 0),
                new BigDecimal("100.00"),
                1,
                "Monthly internet subscription",
                null
        );
        request.setIsRecurring(true);

        Bill createdBill = TestDataBuilder.createBill(1L, "Internet Bill", new BigDecimal("100.00"), 1);
        createdBill.setIsRecurring(true);
        createdBill.setInstallmentAmount(new BigDecimal("100.00"));

        when(createBill.addBill(any(Bill.class))).thenReturn(createdBill);

        // When & Then
        mockMvc.perform(post("/bills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Internet Bill"))
                .andExpect(jsonPath("$.isRecurring").value(true))
                .andExpect(jsonPath("$.totalAmount").value(100.00));
    }

    @Test
    @DisplayName("POST /bills - Should create non-recurring bill by default")
    void shouldCreateNonRecurringBillByDefault() throws Exception {
        // Given: Bill request without isRecurring field
        BillRequestDTO request = new BillRequestDTO(
                "One-time Bill",
                LocalDateTime.of(2025, 1, 15, 10, 0),
                new BigDecimal("500.00"),
                1,
                null,
                null
        );

        Bill createdBill = TestDataBuilder.createBill(1L, "One-time Bill", new BigDecimal("500.00"), 1);
        createdBill.setIsRecurring(false);
        createdBill.setInstallmentAmount(new BigDecimal("500.00"));

        when(createBill.addBill(any(Bill.class))).thenReturn(createdBill);

        // When & Then
        mockMvc.perform(post("/bills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isRecurring").value(false));
    }

    @Test
    @DisplayName("POST /bills - Should return 400 when name is blank")
    void shouldReturn400WhenNameIsBlank() throws Exception {
        // Given: Request with blank name
        BillRequestDTO request = new BillRequestDTO(
                "",  // Blank name
                LocalDateTime.of(2025, 1, 15, 10, 0),
                new BigDecimal("1000.00"),
                10,
                null,
                null
        );

        // When & Then
        mockMvc.perform(post("/bills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /bills - Should return 400 when totalAmount is null")
    void shouldReturn400WhenTotalAmountIsNull() throws Exception {
        // Given: Request with null total amount
        String jsonRequest = """
                {
                    "name": "Test Bill",
                    "executionDate": "2025-01-15T10:00:00",
                    "totalAmount": null,
                    "numberOfInstallments": 10
                }
                """;

        // When & Then
        mockMvc.perform(post("/bills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /bills - Should return 400 when totalAmount is not positive")
    void shouldReturn400WhenTotalAmountIsNotPositive() throws Exception {
        // Given: Request with zero/negative amount
        BillRequestDTO request = new BillRequestDTO(
                "Invalid Bill",
                LocalDateTime.of(2025, 1, 15, 10, 0),
                BigDecimal.ZERO,  // Not positive
                10,
                null,
                null
        );

        // When & Then
        mockMvc.perform(post("/bills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /bills - Should return 400 when numberOfInstallments is less than 1")
    void shouldReturn400WhenNumberOfInstallmentsIsLessThan1() throws Exception {
        // Given: Request with 0 installments
        BillRequestDTO request = new BillRequestDTO(
                "Invalid Bill",
                LocalDateTime.of(2025, 1, 15, 10, 0),
                new BigDecimal("1000.00"),
                0,  // Less than 1
                null,
                null
        );

        // When & Then
        mockMvc.perform(post("/bills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /bills - Should return 400 when executionDate is null")
    void shouldReturn400WhenExecutionDateIsNull() throws Exception {
        // Given: Request with null execution date
        String jsonRequest = """
                {
                    "name": "Test Bill",
                    "executionDate": null,
                    "totalAmount": 1000.00,
                    "numberOfInstallments": 10
                }
                """;

        // When & Then
        mockMvc.perform(post("/bills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    // ==================== PUT /bills/{id} ====================

    @Test
    @DisplayName("PUT /bills/{id} - Should update bill and return 200 OK")
    void shouldUpdateBillSuccessfully() throws Exception {
        // Given: Valid update request
        Long billId = 1L;
        BillRequestDTO request = new BillRequestDTO(
                "Updated Bill",
                LocalDateTime.of(2025, 1, 20, 10, 0),
                new BigDecimal("2000.00"),
                20,
                "Updated description",
                null
        );

        Bill updatedBill = TestDataBuilder.createBill(billId, "Updated Bill", new BigDecimal("2000.00"), 20);
        updatedBill.setInstallmentAmount(new BigDecimal("100.00"));

        when(updateBill.updateBill(eq(billId), any(Bill.class))).thenReturn(Optional.of(updatedBill));

        // When & Then
        mockMvc.perform(put("/bills/{id}", billId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Bill"))
                .andExpect(jsonPath("$.totalAmount").value(2000.00));
    }

    @Test
    @DisplayName("PUT /bills/{id} - Should return 404 when bill not found")
    void shouldReturn404WhenUpdatingNonExistentBill() throws Exception {
        // Given: Bill does not exist
        Long billId = 999L;
        BillRequestDTO request = new BillRequestDTO(
                "Updated Bill",
                LocalDateTime.of(2025, 1, 20, 10, 0),
                new BigDecimal("2000.00"),
                20,
                null,
                null
        );

        when(updateBill.updateBill(eq(billId), any(Bill.class))).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/bills/{id}", billId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /bills/{id} - Should return 400 when validation fails")
    void shouldReturn400WhenUpdateValidationFails() throws Exception {
        // Given: Invalid update request
        Long billId = 1L;
        BillRequestDTO request = new BillRequestDTO(
                "",  // Blank name
                LocalDateTime.of(2025, 1, 20, 10, 0),
                new BigDecimal("2000.00"),
                20,
                null,
                null
        );

        // When & Then
        mockMvc.perform(put("/bills/{id}", billId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /bills/{id} - Should update bill to recurring")
    void shouldUpdateBillToRecurring() throws Exception {
        // Given: Update request to make bill recurring
        Long billId = 1L;
        BillRequestDTO request = new BillRequestDTO(
                "Internet Bill",
                LocalDateTime.of(2025, 1, 20, 10, 0),
                new BigDecimal("100.00"),
                1,
                "Monthly subscription",
                null
        );
        request.setIsRecurring(true);

        Bill updatedBill = TestDataBuilder.createBill(billId, "Internet Bill", new BigDecimal("100.00"), 1);
        updatedBill.setIsRecurring(true);
        updatedBill.setInstallmentAmount(new BigDecimal("100.00"));

        when(updateBill.updateBill(eq(billId), any(Bill.class))).thenReturn(Optional.of(updatedBill));

        // When & Then
        mockMvc.perform(put("/bills/{id}", billId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isRecurring").value(true));
    }

    @Test
    @DisplayName("GET /bills/{id} - Should return bill with isRecurring field")
    void shouldReturnBillWithIsRecurringField() throws Exception {
        // Given: Recurring bill exists
        Long billId = 1L;
        Bill bill = TestDataBuilder.createBill(billId, "Internet Bill", new BigDecimal("100.00"), 1);
        bill.setIsRecurring(true);

        when(getBillById.execute(billId)).thenReturn(Optional.of(bill));

        // When & Then
        mockMvc.perform(get("/bills/{id}", billId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Internet Bill"))
                .andExpect(jsonPath("$.isRecurring").value(true));
    }

    // ==================== DELETE /bills/{id} ====================

    @Test
    @DisplayName("DELETE /bills/{id} - Should delete bill and return 204 No Content")
    void shouldDeleteBillSuccessfully() throws Exception {
        // Given: Bill exists
        Long billId = 1L;
        when(deleteBill.execute(billId)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/bills/{id}", billId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /bills/{id} - Should return 404 when bill not found")
    void shouldReturn404WhenDeletingNonExistentBill() throws Exception {
        // Given: Bill does not exist
        Long billId = 999L;
        when(deleteBill.execute(billId)).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/bills/{id}", billId))
                .andExpect(status().isNotFound());
    }

    // ==================== GET /bills/{id}/installments ====================

    @Test
    @DisplayName("GET /bills/{id}/installments - Should return installments with 200 OK")
    void shouldReturnBillInstallments() throws Exception {
        // Given: Bill with installments
        Long billId = 1L;
        Bill bill = TestDataBuilder.createBill(billId, "Test Bill", new BigDecimal("1000.00"), 10);

        Installment inst1 = TestDataBuilder.createInstallment(1L, billId, 1L, 1, new BigDecimal("100.00"), LocalDate.of(2025, 1, 17));
        Installment inst2 = TestDataBuilder.createInstallment(2L, billId, 2L, 2, new BigDecimal("100.00"), LocalDate.of(2025, 2, 17));

        when(getBillById.execute(billId)).thenReturn(Optional.of(bill));
        when(getBillInstallments.execute(billId)).thenReturn(List.of(inst1, inst2));

        // When & Then
        mockMvc.perform(get("/bills/{id}/installments", billId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].installmentNumber").value(1))
                .andExpect(jsonPath("$[0].amount").value(100.00))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].installmentNumber").value(2));
    }

    @Test
    @DisplayName("GET /bills/{id}/installments - Should return 404 when bill not found")
    void shouldReturn404WhenGettingInstallmentsForNonExistentBill() throws Exception {
        // Given: Bill does not exist
        Long billId = 999L;
        when(getBillById.execute(billId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/bills/{id}/installments", billId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /bills/{id}/installments - Should return empty list when bill has no installments")
    void shouldReturnEmptyListWhenBillHasNoInstallments() throws Exception {
        // Given: Bill exists but has no installments
        Long billId = 1L;
        Bill bill = TestDataBuilder.createBill(billId, "Test Bill", new BigDecimal("1000.00"), 10);

        when(getBillById.execute(billId)).thenReturn(Optional.of(bill));
        when(getBillInstallments.execute(billId)).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/bills/{id}/installments", billId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("POST /bills - Should handle missing fields with 400 Bad Request")
    void shouldReturn400WhenRequiredFieldsMissing() throws Exception {
        // Given: Incomplete request
        String jsonRequest = """
                {
                    "name": "Test Bill"
                }
                """;

        // When & Then
        mockMvc.perform(post("/bills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /bills - Should handle malformed JSON with 400 Bad Request")
    void shouldReturn400ForMalformedJson() throws Exception {
        // Given: Malformed JSON
        String malformedJson = "{ invalid json }";

        // When & Then
        mockMvc.perform(post("/bills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /bills - Should set correct content type header")
    void shouldSetCorrectContentTypeHeader() throws Exception {
        // Given
        when(getAllBills.execute()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/bills"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/json"));
    }
}
