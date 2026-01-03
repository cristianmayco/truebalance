package com.truebalance.truebalance.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.truebalance.truebalance.application.dto.input.CreditCardRequestDTO;
import com.truebalance.truebalance.application.exception.GlobalExceptionHandler;
import com.truebalance.truebalance.config.TestWebMvcConfig;
import com.truebalance.truebalance.domain.entity.CreditCard;
import com.truebalance.truebalance.domain.entity.Invoice;
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
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for CreditCardController using MockMvc.
 * Tests HTTP endpoints, JSON serialization, and validation.
 */
@WebMvcTest(CreditCardController.class)
@Import({GlobalExceptionHandler.class, TestWebMvcConfig.class})
@DisplayName("CreditCardController API Tests")
class CreditCardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateCreditCard createCreditCard;

    @MockBean
    private GetAllCreditCards getAllCreditCards;

    @MockBean
    private GetCreditCardById getCreditCardById;

    @MockBean
    private UpdateCreditCard updateCreditCard;

    @MockBean
    private DeleteCreditCard deleteCreditCard;

    @MockBean
    private GetInvoicesByCreditCard getInvoicesByCreditCard;

    @MockBean
    private GetAvailableLimit getAvailableLimit;

    // ==================== POST /credit-cards ====================

    @Test
    @DisplayName("POST /credit-cards - Should create credit card and return 201 Created")
    void shouldCreateCreditCardSuccessfully() throws Exception {
        // Given: Valid credit card request
        CreditCardRequestDTO request = new CreditCardRequestDTO(
                "Visa Gold",
                new BigDecimal("5000.00"),
                10,
                17,
                true
        );

        CreditCard createdCard = TestDataBuilder.createCreditCard(1L, "Visa Gold", new BigDecimal("5000.00"), 10, 17);

        when(createCreditCard.execute(any(CreditCard.class))).thenReturn(createdCard);

        // When & Then
        mockMvc.perform(post("/credit-cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Visa Gold"))
                .andExpect(jsonPath("$.creditLimit").value(5000.00))
                .andExpect(jsonPath("$.closingDay").value(10))
                .andExpect(jsonPath("$.dueDay").value(17))
                .andExpect(jsonPath("$.allowsPartialPayment").value(true));
    }

    @Test
    @DisplayName("POST /credit-cards - Should return 400 when name is blank")
    void shouldReturn400WhenNameIsBlank() throws Exception {
        // Given: Request with blank name
        CreditCardRequestDTO request = new CreditCardRequestDTO(
                "",  // Blank name
                new BigDecimal("5000.00"),
                10,
                17,
                true
        );

        // When & Then
        mockMvc.perform(post("/credit-cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /credit-cards - Should return 400 when creditLimit is not positive")
    void shouldReturn400WhenCreditLimitIsNotPositive() throws Exception {
        // Given: Request with zero credit limit
        CreditCardRequestDTO request = new CreditCardRequestDTO(
                "Test Card",
                BigDecimal.ZERO,  // Not positive
                10,
                17,
                true
        );

        // When & Then
        mockMvc.perform(post("/credit-cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /credit-cards - Should return 400 when closingDay is less than 1")
    void shouldReturn400WhenClosingDayIsLessThan1() throws Exception {
        // Given: Request with invalid closing day
        CreditCardRequestDTO request = new CreditCardRequestDTO(
                "Test Card",
                new BigDecimal("5000.00"),
                0,  // Less than 1
                17,
                true
        );

        // When & Then
        mockMvc.perform(post("/credit-cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /credit-cards - Should return 400 when closingDay is greater than 31")
    void shouldReturn400WhenClosingDayIsGreaterThan31() throws Exception {
        // Given: Request with invalid closing day
        CreditCardRequestDTO request = new CreditCardRequestDTO(
                "Test Card",
                new BigDecimal("5000.00"),
                32,  // Greater than 31
                17,
                true
        );

        // When & Then
        mockMvc.perform(post("/credit-cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /credit-cards - Should return 400 when dueDay is less than 1")
    void shouldReturn400WhenDueDayIsLessThan1() throws Exception {
        // Given: Request with invalid due day
        CreditCardRequestDTO request = new CreditCardRequestDTO(
                "Test Card",
                new BigDecimal("5000.00"),
                10,
                0,  // Less than 1
                true
        );

        // When & Then
        mockMvc.perform(post("/credit-cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /credit-cards - Should return 400 when dueDay is greater than 31")
    void shouldReturn400WhenDueDayIsGreaterThan31() throws Exception {
        // Given: Request with invalid due day
        CreditCardRequestDTO request = new CreditCardRequestDTO(
                "Test Card",
                new BigDecimal("5000.00"),
                10,
                32,  // Greater than 31
                true
        );

        // When & Then
        mockMvc.perform(post("/credit-cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /credit-cards - Should return 400 when allowsPartialPayment is null")
    void shouldReturn400WhenAllowsPartialPaymentIsNull() throws Exception {
        // Given: Request with null allowsPartialPayment
        String jsonRequest = """
                {
                    "name": "Test Card",
                    "creditLimit": 5000.00,
                    "closingDay": 10,
                    "dueDay": 17,
                    "allowsPartialPayment": null
                }
                """;

        // When & Then
        mockMvc.perform(post("/credit-cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    // ==================== GET /credit-cards ====================

    @Test
    @DisplayName("GET /credit-cards - Should return all credit cards with 200 OK")
    void shouldReturnAllCreditCards() throws Exception {
        // Given: Multiple credit cards exist
        CreditCard card1 = TestDataBuilder.createCreditCard(1L, "Visa", new BigDecimal("5000.00"), 10, 17);
        CreditCard card2 = TestDataBuilder.createCreditCard(2L, "Mastercard", new BigDecimal("8000.00"), 5, 12);

        when(getAllCreditCards.execute()).thenReturn(List.of(card1, card2));

        // When & Then
        mockMvc.perform(get("/credit-cards")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Visa"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Mastercard"));
    }

    @Test
    @DisplayName("GET /credit-cards - Should return empty list when no cards exist")
    void shouldReturnEmptyListWhenNoCardsExist() throws Exception {
        // Given: No credit cards
        when(getAllCreditCards.execute()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/credit-cards")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ==================== GET /credit-cards/{id} ====================

    @Test
    @DisplayName("GET /credit-cards/{id} - Should return credit card with 200 OK when found")
    void shouldReturnCreditCardByIdWhenFound() throws Exception {
        // Given: Credit card exists
        Long cardId = 1L;
        CreditCard card = TestDataBuilder.createCreditCard(cardId, "Visa Gold", new BigDecimal("10000.00"), 15, 22);

        when(getCreditCardById.execute(cardId)).thenReturn(Optional.of(card));

        // When & Then
        mockMvc.perform(get("/credit-cards/{id}", cardId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Visa Gold"))
                .andExpect(jsonPath("$.creditLimit").value(10000.00));
    }

    @Test
    @DisplayName("GET /credit-cards/{id} - Should return 404 when card not found")
    void shouldReturn404WhenCardNotFound() throws Exception {
        // Given: Card does not exist
        Long cardId = 999L;
        when(getCreditCardById.execute(cardId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/credit-cards/{id}", cardId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // ==================== PUT /credit-cards/{id} ====================

    @Test
    @DisplayName("PUT /credit-cards/{id} - Should update credit card and return 200 OK")
    void shouldUpdateCreditCardSuccessfully() throws Exception {
        // Given: Valid update request
        Long cardId = 1L;
        CreditCardRequestDTO request = new CreditCardRequestDTO(
                "Updated Card",
                new BigDecimal("12000.00"),
                20,
                27,
                false
        );

        CreditCard updatedCard = TestDataBuilder.createCreditCard(cardId, "Updated Card", new BigDecimal("12000.00"), 20, 27);
        updatedCard.setAllowsPartialPayment(false);

        when(updateCreditCard.execute(eq(cardId), any(CreditCard.class))).thenReturn(Optional.of(updatedCard));

        // When & Then
        mockMvc.perform(put("/credit-cards/{id}", cardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Card"))
                .andExpect(jsonPath("$.creditLimit").value(12000.00))
                .andExpect(jsonPath("$.allowsPartialPayment").value(false));
    }

    @Test
    @DisplayName("PUT /credit-cards/{id} - Should return 404 when card not found")
    void shouldReturn404WhenUpdatingNonExistentCard() throws Exception {
        // Given: Card does not exist
        Long cardId = 999L;
        CreditCardRequestDTO request = new CreditCardRequestDTO(
                "Updated Card",
                new BigDecimal("12000.00"),
                20,
                27,
                true
        );

        when(updateCreditCard.execute(eq(cardId), any(CreditCard.class))).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/credit-cards/{id}", cardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ==================== DELETE /credit-cards/{id} ====================

    @Test
    @DisplayName("DELETE /credit-cards/{id} - Should delete card and return 204 No Content")
    void shouldDeleteCreditCardSuccessfully() throws Exception {
        // Given: Card exists
        Long cardId = 1L;
        when(deleteCreditCard.execute(cardId)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/credit-cards/{id}", cardId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /credit-cards/{id} - Should return 404 when card not found")
    void shouldReturn404WhenDeletingNonExistentCard() throws Exception {
        // Given: Card does not exist
        Long cardId = 999L;
        when(deleteCreditCard.execute(cardId)).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/credit-cards/{id}", cardId))
                .andExpect(status().isNotFound());
    }

    // ==================== GET /credit-cards/{id}/invoices ====================

    @Test
    @DisplayName("GET /credit-cards/{id}/invoices - Should return invoices with 200 OK")
    void shouldReturnCreditCardInvoices() throws Exception {
        // Given: Card with invoices
        Long cardId = 1L;
        CreditCard card = TestDataBuilder.createCreditCard(cardId, "Test Card", new BigDecimal("5000.00"), 10, 17);

        Invoice invoice1 = TestDataBuilder.createInvoice(1L, cardId, LocalDate.of(2025, 1, 1), new BigDecimal("500.00"));
        Invoice invoice2 = TestDataBuilder.createInvoice(2L, cardId, LocalDate.of(2025, 2, 1), new BigDecimal("750.00"));

        when(getCreditCardById.execute(cardId)).thenReturn(Optional.of(card));
        when(getInvoicesByCreditCard.execute(cardId)).thenReturn(List.of(invoice1, invoice2));

        // When & Then
        mockMvc.perform(get("/credit-cards/{id}/invoices", cardId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].totalAmount").value(500.00))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].totalAmount").value(750.00));
    }

    @Test
    @DisplayName("GET /credit-cards/{id}/invoices - Should return 404 when card not found")
    void shouldReturn404WhenGettingInvoicesForNonExistentCard() throws Exception {
        // Given: Card does not exist
        Long cardId = 999L;
        when(getCreditCardById.execute(cardId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/credit-cards/{id}/invoices", cardId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // ==================== GET /credit-cards/{id}/available-limit ====================

    @Test
    @DisplayName("GET /credit-cards/{id}/available-limit - Should return available limit with 200 OK")
    void shouldReturnAvailableLimit() throws Exception {
        // Given: Card with available limit
        Long cardId = 1L;
        AvailableLimitResult result = TestDataBuilder.createAvailableLimitResult(
                cardId,
                new BigDecimal("5000.00"),
                new BigDecimal("2000.00"),
                new BigDecimal("500.00"),
                new BigDecimal("3500.00")
        );

        when(getAvailableLimit.execute(cardId)).thenReturn(result);

        // When & Then
        mockMvc.perform(get("/credit-cards/{id}/available-limit", cardId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.creditCardId").value(1))
                .andExpect(jsonPath("$.creditLimit").value(5000.00))
                .andExpect(jsonPath("$.usedLimit").value(2000.00))
                .andExpect(jsonPath("$.partialPaymentsTotal").value(500.00))
                .andExpect(jsonPath("$.availableLimit").value(3500.00));
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("POST /credit-cards - Should handle missing fields with 400 Bad Request")
    void shouldReturn400WhenRequiredFieldsMissing() throws Exception {
        // Given: Incomplete request
        String jsonRequest = """
                {
                    "name": "Test Card"
                }
                """;

        // When & Then
        mockMvc.perform(post("/credit-cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /credit-cards - Should accept boundary values (closingDay = 1, dueDay = 31)")
    void shouldAcceptBoundaryValues() throws Exception {
        // Given: Request with boundary values
        CreditCardRequestDTO request = new CreditCardRequestDTO(
                "Boundary Card",
                new BigDecimal("5000.00"),
                1,   // Min value
                31,  // Max value
                true
        );

        CreditCard createdCard = TestDataBuilder.createCreditCard(1L, "Boundary Card", new BigDecimal("5000.00"), 1, 31);
        when(createCreditCard.execute(any(CreditCard.class))).thenReturn(createdCard);

        // When & Then
        mockMvc.perform(post("/credit-cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.closingDay").value(1))
                .andExpect(jsonPath("$.dueDay").value(31));
    }

    @Test
    @DisplayName("GET /credit-cards/{id}/invoices - Should return empty list when card has no invoices")
    void shouldReturnEmptyListWhenCardHasNoInvoices() throws Exception {
        // Given: Card exists but has no invoices
        Long cardId = 1L;
        CreditCard card = TestDataBuilder.createCreditCard(cardId, "Test Card", new BigDecimal("5000.00"), 10, 17);

        when(getCreditCardById.execute(cardId)).thenReturn(Optional.of(card));
        when(getInvoicesByCreditCard.execute(cardId)).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/credit-cards/{id}/invoices", cardId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
