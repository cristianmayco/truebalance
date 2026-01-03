package com.truebalance.truebalance.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.truebalance.truebalance.application.dto.input.BillRequestDTO;
import com.truebalance.truebalance.application.dto.input.CreditCardRequestDTO;
import com.truebalance.truebalance.application.dto.input.ImportDataDTO;
import com.truebalance.truebalance.application.dto.input.InvoiceImportItemDTO;
import com.truebalance.truebalance.application.dto.output.ExportDataDTO;
import com.truebalance.truebalance.application.dto.output.ImportResultDTO;
import com.truebalance.truebalance.application.exception.GlobalExceptionHandler;
import com.truebalance.truebalance.config.TestWebMvcConfig;
import com.truebalance.truebalance.domain.usecase.ExportData;
import com.truebalance.truebalance.domain.usecase.ImportData;
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

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ImportExportController.class)
@Import({GlobalExceptionHandler.class, TestWebMvcConfig.class})
@DisplayName("ImportExportController API Tests")
class ImportExportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ExportData exportData;

    @MockBean
    private ImportData importData;

    // ==================== GET /import-export/export ====================

    @Test
    @DisplayName("GET /import-export/export - Should export all data successfully")
    void shouldExportAllDataSuccessfully() throws Exception {
        // Given: Export data exists
        ExportDataDTO exportDataDTO = new ExportDataDTO();
        exportDataDTO.setBills(List.of());
        exportDataDTO.setCreditCards(List.of());
        exportDataDTO.setInvoices(List.of());

        when(exportData.execute()).thenReturn(exportDataDTO);

        // When & Then
        mockMvc.perform(get("/import-export/export")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.bills").isArray())
                .andExpect(jsonPath("$.creditCards").isArray())
                .andExpect(jsonPath("$.invoices").isArray());
    }

    @Test
    @DisplayName("GET /import-export/export - Should return exported data with all entities")
    void shouldReturnExportedDataWithAllEntities() throws Exception {
        // Given: Export data with entities
        ExportDataDTO exportDataDTO = new ExportDataDTO();
        exportDataDTO.setBills(List.of());
        exportDataDTO.setCreditCards(List.of());
        exportDataDTO.setInvoices(List.of());

        when(exportData.execute()).thenReturn(exportDataDTO);

        // When & Then
        mockMvc.perform(get("/import-export/export")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bills").exists())
                .andExpect(jsonPath("$.creditCards").exists())
                .andExpect(jsonPath("$.invoices").exists());
    }

    // ==================== POST /import-export/import ====================

    @Test
    @DisplayName("POST /import-export/import - Should import data successfully")
    void shouldImportDataSuccessfully() throws Exception {
        // Given: Valid import data
        ImportDataDTO importDataDTO = new ImportDataDTO();
        importDataDTO.setBills(List.of());
        importDataDTO.setCreditCards(List.of());
        importDataDTO.setInvoices(List.of());

        ImportResultDTO importResult = new ImportResultDTO();
        importResult.setTotalProcessed(0);
        importResult.setTotalCreated(0);
        importResult.setTotalSkipped(0);
        importResult.setTotalErrors(0);
        importResult.setErrors(List.of());

        when(importData.execute(any(ImportDataDTO.class))).thenReturn(importResult);

        // When & Then
        mockMvc.perform(post("/import-export/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(importDataDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalProcessed").value(0))
                .andExpect(jsonPath("$.totalCreated").value(0))
                .andExpect(jsonPath("$.totalSkipped").value(0))
                .andExpect(jsonPath("$.totalErrors").value(0));
    }

    @Test
    @DisplayName("POST /import-export/import - Should import bills successfully")
    void shouldImportBillsSuccessfully() throws Exception {
        // Given: Import data with bills
        ImportDataDTO importDataDTO = new ImportDataDTO();
        BillRequestDTO billDTO = new BillRequestDTO(
                "Test Bill",
                LocalDateTime.of(2025, 1, 15, 10, 0),
                new BigDecimal("1000.00"),
                10,
                "Test description",
                null
        );
        importDataDTO.setBills(List.of(billDTO));

        ImportResultDTO importResult = new ImportResultDTO();
        importResult.setTotalProcessed(1);
        importResult.setTotalCreated(1);
        importResult.setTotalSkipped(0);
        importResult.setTotalErrors(0);
        importResult.setErrors(List.of());

        when(importData.execute(any(ImportDataDTO.class))).thenReturn(importResult);

        // When & Then
        mockMvc.perform(post("/import-export/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(importDataDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProcessed").value(1))
                .andExpect(jsonPath("$.totalCreated").value(1))
                .andExpect(jsonPath("$.totalSkipped").value(0))
                .andExpect(jsonPath("$.totalErrors").value(0));
    }

    @Test
    @DisplayName("POST /import-export/import - Should import credit cards successfully")
    void shouldImportCreditCardsSuccessfully() throws Exception {
        // Given: Import data with credit cards
        ImportDataDTO importDataDTO = new ImportDataDTO();
        CreditCardRequestDTO cardDTO = new CreditCardRequestDTO(
                "Test Card",
                new BigDecimal("5000.00"),
                10,
                17,
                true
        );
        importDataDTO.setCreditCards(List.of(cardDTO));

        ImportResultDTO importResult = new ImportResultDTO();
        importResult.setTotalProcessed(1);
        importResult.setTotalCreated(1);
        importResult.setTotalSkipped(0);
        importResult.setTotalErrors(0);
        importResult.setErrors(List.of());

        when(importData.execute(any(ImportDataDTO.class))).thenReturn(importResult);

        // When & Then
        mockMvc.perform(post("/import-export/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(importDataDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProcessed").value(1))
                .andExpect(jsonPath("$.totalCreated").value(1));
    }

    @Test
    @DisplayName("POST /import-export/import - Should import invoices successfully")
    void shouldImportInvoicesSuccessfully() throws Exception {
        // Given: Import data with invoices
        ImportDataDTO importDataDTO = new ImportDataDTO();
        InvoiceImportItemDTO invoiceDTO = new InvoiceImportItemDTO();
        invoiceDTO.setCreditCardId(1L);
        invoiceDTO.setReferenceMonth(LocalDate.of(2025, 1, 1));
        invoiceDTO.setTotalAmount(new BigDecimal("500.00"));
        invoiceDTO.setPreviousBalance(BigDecimal.ZERO);
        invoiceDTO.setClosed(false);
        invoiceDTO.setPaid(false);
        importDataDTO.setInvoices(List.of(invoiceDTO));

        ImportResultDTO importResult = new ImportResultDTO();
        importResult.setTotalProcessed(1);
        importResult.setTotalCreated(1);
        importResult.setTotalSkipped(0);
        importResult.setTotalErrors(0);
        importResult.setErrors(List.of());

        when(importData.execute(any(ImportDataDTO.class))).thenReturn(importResult);

        // When & Then
        mockMvc.perform(post("/import-export/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(importDataDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProcessed").value(1))
                .andExpect(jsonPath("$.totalCreated").value(1));
    }

    @Test
    @DisplayName("POST /import-export/import - Should return import result with errors")
    void shouldReturnImportResultWithErrors() throws Exception {
        // Given: Import data that produces errors
        ImportDataDTO importDataDTO = new ImportDataDTO();
        importDataDTO.setBills(List.of());

        ImportResultDTO importResult = new ImportResultDTO();
        importResult.setTotalProcessed(1);
        importResult.setTotalCreated(0);
        importResult.setTotalSkipped(1);
        importResult.setTotalErrors(1);
        importResult.setErrors(List.of("Erro ao importar conta 'Test': Dados inválidos"));

        when(importData.execute(any(ImportDataDTO.class))).thenReturn(importResult);

        // When & Then
        mockMvc.perform(post("/import-export/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(importDataDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalErrors").value(1))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0]").value("Erro ao importar conta 'Test': Dados inválidos"));
    }

    @Test
    @DisplayName("POST /import-export/import - Should return 400 when validation fails")
    void shouldReturn400WhenValidationFails() throws Exception {
        // Given: Invalid import data (invoice without required fields)
        String invalidJson = """
                {
                    "invoices": [
                        {
                            "creditCardId": null,
                            "referenceMonth": "2025-01-01",
                            "totalAmount": 500.00
                        }
                    ]
                }
                """;

        // When & Then
        mockMvc.perform(post("/import-export/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /import-export/import - Should handle empty import data")
    void shouldHandleEmptyImportData() throws Exception {
        // Given: Empty import data
        ImportDataDTO importDataDTO = new ImportDataDTO();

        ImportResultDTO importResult = new ImportResultDTO();
        importResult.setTotalProcessed(0);
        importResult.setTotalCreated(0);
        importResult.setTotalSkipped(0);
        importResult.setTotalErrors(0);
        importResult.setErrors(List.of());

        when(importData.execute(any(ImportDataDTO.class))).thenReturn(importResult);

        // When & Then
        mockMvc.perform(post("/import-export/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(importDataDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProcessed").value(0));
    }

    @Test
    @DisplayName("POST /import-export/import - Should handle malformed JSON")
    void shouldHandleMalformedJson() throws Exception {
        // Given: Malformed JSON
        String malformedJson = "{ invalid json }";

        // When & Then
        mockMvc.perform(post("/import-export/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /import-export/export - Should set correct content type header")
    void shouldSetCorrectContentTypeHeader() throws Exception {
        // Given
        ExportDataDTO exportDataDTO = new ExportDataDTO();
        exportDataDTO.setBills(List.of());
        exportDataDTO.setCreditCards(List.of());
        exportDataDTO.setInvoices(List.of());

        when(exportData.execute()).thenReturn(exportDataDTO);

        // When & Then
        mockMvc.perform(get("/import-export/export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/json"));
    }
}
