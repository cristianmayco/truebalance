package com.truebalance.truebalance.domain.service;

import com.truebalance.truebalance.application.dto.input.BillImportItemDTO;
import com.truebalance.truebalance.application.dto.input.CreditCardImportItemDTO;
import com.truebalance.truebalance.application.dto.input.InvoiceImportItemDTO;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileImportService {

    private static final Logger logger = LoggerFactory.getLogger(FileImportService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MM/yyyy");
    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Parse CSV file and convert to BillImportItemDTO list
     */
    public List<BillImportItemDTO> parseBillsFromFile(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("Nome do arquivo não pode ser nulo");
        }

        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        
        if (extension.equals("csv")) {
            return parseBillsFromCSV(file.getInputStream());
        } else if (extension.equals("xlsx") || extension.equals("xls")) {
            return parseBillsFromExcel(file.getInputStream(), extension.equals("xlsx"));
        } else {
            throw new IllegalArgumentException("Formato de arquivo não suportado: " + extension);
        }
    }

    /**
     * Parse CSV file and convert to InvoiceImportItemDTO list
     */
    public List<InvoiceImportItemDTO> parseInvoicesFromFile(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("Nome do arquivo não pode ser nulo");
        }

        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        
        if (extension.equals("csv")) {
            return parseInvoicesFromCSV(file.getInputStream());
        } else if (extension.equals("xlsx") || extension.equals("xls")) {
            return parseInvoicesFromExcel(file.getInputStream(), extension.equals("xlsx"));
        } else {
            throw new IllegalArgumentException("Formato de arquivo não suportado: " + extension);
        }
    }

    /**
     * Parse CSV file and convert to CreditCardImportItemDTO list
     */
    public List<CreditCardImportItemDTO> parseCreditCardsFromFile(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("Nome do arquivo não pode ser nulo");
        }

        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        
        if (extension.equals("csv")) {
            return parseCreditCardsFromCSV(file.getInputStream());
        } else if (extension.equals("xlsx") || extension.equals("xls")) {
            return parseCreditCardsFromExcel(file.getInputStream(), extension.equals("xlsx"));
        } else {
            throw new IllegalArgumentException("Formato de arquivo não suportado: " + extension);
        }
    }

    // ========== CSV Parsers ==========

    private List<BillImportItemDTO> parseBillsFromCSV(InputStream inputStream) throws Exception {
        List<BillImportItemDTO> items = new ArrayList<>();
        
        try (InputStreamReader reader = new InputStreamReader(inputStream, "UTF-8");
             CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader)) {
            
            int lineNumber = 1; // Header is line 1
            for (CSVRecord record : parser) {
                lineNumber++;
                try {
                    BillImportItemDTO item = parseBillRecord(record, lineNumber);
                    items.add(item);
                } catch (Exception e) {
                    logger.warn("Erro ao processar linha {} do CSV: {}", lineNumber, e.getMessage());
                    throw new RuntimeException("Erro na linha " + lineNumber + ": " + e.getMessage(), e);
                }
            }
        }
        
        return items;
    }

    private List<InvoiceImportItemDTO> parseInvoicesFromCSV(InputStream inputStream) throws Exception {
        List<InvoiceImportItemDTO> items = new ArrayList<>();
        
        try (InputStreamReader reader = new InputStreamReader(inputStream, "UTF-8");
             CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader)) {
            
            int lineNumber = 1;
            for (CSVRecord record : parser) {
                lineNumber++;
                try {
                    InvoiceImportItemDTO item = parseInvoiceRecord(record, lineNumber);
                    items.add(item);
                } catch (Exception e) {
                    logger.warn("Erro ao processar linha {} do CSV: {}", lineNumber, e.getMessage());
                    throw new RuntimeException("Erro na linha " + lineNumber + ": " + e.getMessage(), e);
                }
            }
        }
        
        return items;
    }

    private List<CreditCardImportItemDTO> parseCreditCardsFromCSV(InputStream inputStream) throws Exception {
        List<CreditCardImportItemDTO> items = new ArrayList<>();
        
        try (InputStreamReader reader = new InputStreamReader(inputStream, "UTF-8");
             CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader)) {
            
            int lineNumber = 1;
            for (CSVRecord record : parser) {
                lineNumber++;
                try {
                    CreditCardImportItemDTO item = parseCreditCardRecord(record, lineNumber);
                    items.add(item);
                } catch (Exception e) {
                    logger.warn("Erro ao processar linha {} do CSV: {}", lineNumber, e.getMessage());
                    throw new RuntimeException("Erro na linha " + lineNumber + ": " + e.getMessage(), e);
                }
            }
        }
        
        return items;
    }

    // ========== Excel Parsers ==========

    private List<BillImportItemDTO> parseBillsFromExcel(InputStream inputStream, boolean isXLSX) throws Exception {
        List<BillImportItemDTO> items = new ArrayList<>();
        
        Workbook workbook = isXLSX ? new XSSFWorkbook(inputStream) : new HSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        
        // Get header row
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            workbook.close();
            throw new IllegalArgumentException("Arquivo Excel vazio ou sem cabeçalho");
        }
        
        // Create header map
        java.util.Map<String, Integer> headerMap = new java.util.HashMap<>();
        for (Cell cell : headerRow) {
            String headerName = getCellValueAsString(cell);
            headerMap.put(headerName, cell.getColumnIndex());
        }
        
        // Process data rows
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            
            int lineNumber = i + 1; // Excel rows are 1-indexed, header is row 1
            try {
                BillImportItemDTO item = parseBillRow(row, headerMap, lineNumber);
                items.add(item);
            } catch (Exception e) {
                logger.warn("Erro ao processar linha {} do Excel: {}", lineNumber, e.getMessage());
                throw new RuntimeException("Erro na linha " + lineNumber + ": " + e.getMessage(), e);
            }
        }
        
        workbook.close();
        return items;
    }

    private List<InvoiceImportItemDTO> parseInvoicesFromExcel(InputStream inputStream, boolean isXLSX) throws Exception {
        List<InvoiceImportItemDTO> items = new ArrayList<>();
        
        Workbook workbook = isXLSX ? new XSSFWorkbook(inputStream) : new HSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            workbook.close();
            throw new IllegalArgumentException("Arquivo Excel vazio ou sem cabeçalho");
        }
        
        java.util.Map<String, Integer> headerMap = new java.util.HashMap<>();
        for (Cell cell : headerRow) {
            String headerName = getCellValueAsString(cell);
            headerMap.put(headerName, cell.getColumnIndex());
        }
        
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            
            int lineNumber = i + 1;
            try {
                InvoiceImportItemDTO item = parseInvoiceRow(row, headerMap, lineNumber);
                items.add(item);
            } catch (Exception e) {
                logger.warn("Erro ao processar linha {} do Excel: {}", lineNumber, e.getMessage());
                throw new RuntimeException("Erro na linha " + lineNumber + ": " + e.getMessage(), e);
            }
        }
        
        workbook.close();
        return items;
    }

    private List<CreditCardImportItemDTO> parseCreditCardsFromExcel(InputStream inputStream, boolean isXLSX) throws Exception {
        List<CreditCardImportItemDTO> items = new ArrayList<>();
        
        Workbook workbook = isXLSX ? new XSSFWorkbook(inputStream) : new HSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            workbook.close();
            throw new IllegalArgumentException("Arquivo Excel vazio ou sem cabeçalho");
        }
        
        java.util.Map<String, Integer> headerMap = new java.util.HashMap<>();
        for (Cell cell : headerRow) {
            String headerName = getCellValueAsString(cell);
            headerMap.put(headerName, cell.getColumnIndex());
        }
        
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            
            int lineNumber = i + 1;
            try {
                CreditCardImportItemDTO item = parseCreditCardRow(row, headerMap, lineNumber);
                items.add(item);
            } catch (Exception e) {
                logger.warn("Erro ao processar linha {} do Excel: {}", lineNumber, e.getMessage());
                throw new RuntimeException("Erro na linha " + lineNumber + ": " + e.getMessage(), e);
            }
        }
        
        workbook.close();
        return items;
    }

    // ========== Record Parsers ==========

    private BillImportItemDTO parseBillRecord(CSVRecord record, int lineNumber) {
        String name = getValue(record, "Nome", "name");
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        
        String description = getValue(record, "Descrição", "description");
        String dateStr = getValue(record, "Data", "date", "executionDate");
        if (dateStr == null || dateStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Data é obrigatória");
        }
        
        String totalAmountStr = getValue(record, "Valor Total", "totalAmount");
        if (totalAmountStr == null || totalAmountStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Valor Total é obrigatório");
        }
        
        String numberOfInstallmentsStr = getValue(record, "Número de Parcelas", "numberOfInstallments", "1");
        String creditCardIdStr = getValue(record, "ID Cartão", "creditCardId");

        LocalDateTime executionDate = parseDateTime(dateStr);
        BigDecimal totalAmount = parseCurrency(totalAmountStr);
        Integer numberOfInstallments = Integer.parseInt(numberOfInstallmentsStr);
        Long creditCardId = creditCardIdStr != null && !creditCardIdStr.isEmpty() ? Long.parseLong(creditCardIdStr) : null;

        return new BillImportItemDTO(
                name, description, executionDate, totalAmount,
                numberOfInstallments, false, creditCardId, lineNumber
        );
    }

    private InvoiceImportItemDTO parseInvoiceRecord(CSVRecord record, int lineNumber) {
        String creditCardIdStr = getValue(record, "ID Cartão", "Cartão de Crédito", "creditCardId");
        if (creditCardIdStr == null || creditCardIdStr.trim().isEmpty()) {
            throw new IllegalArgumentException("ID Cartão é obrigatório");
        }
        
        String referenceMonthStr = getValue(record, "Mês de Referência", "referenceMonth");
        if (referenceMonthStr == null || referenceMonthStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Mês de Referência é obrigatório");
        }
        
        String totalAmountStr = getValue(record, "Valor Total", "totalAmount");
        if (totalAmountStr == null || totalAmountStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Valor Total é obrigatório");
        }
        
        String previousBalanceStr = getValue(record, "Saldo Anterior", "previousBalance", "0");
        String closedStr = getValue(record, "Fechada", "closed", "false");
        String paidStr = getValue(record, "Paga", "paid", "false");

        Long creditCardId = Long.parseLong(creditCardIdStr);
        LocalDate referenceMonth = parseReferenceMonth(referenceMonthStr);
        BigDecimal totalAmount = parseCurrency(totalAmountStr);
        BigDecimal previousBalance = parseCurrency(previousBalanceStr);
        Boolean closed = parseBoolean(closedStr);
        Boolean paid = parseBoolean(paidStr);

        return new InvoiceImportItemDTO(
                creditCardId, referenceMonth, totalAmount,
                previousBalance, closed, paid, lineNumber
        );
    }

    private CreditCardImportItemDTO parseCreditCardRecord(CSVRecord record, int lineNumber) {
        String name = getValue(record, "Nome", "name");
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        
        String creditLimitStr = getValue(record, "Limite de Crédito", "Limite", "creditLimit");
        if (creditLimitStr == null || creditLimitStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Limite de Crédito é obrigatório");
        }
        
        String closingDayStr = getValue(record, "Dia de Fechamento", "Dia Fechamento", "closingDay");
        if (closingDayStr == null || closingDayStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Dia de Fechamento é obrigatório");
        }
        
        String dueDayStr = getValue(record, "Dia de Vencimento", "Dia Vencimento", "dueDay");
        if (dueDayStr == null || dueDayStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Dia de Vencimento é obrigatório");
        }
        
        String allowsPartialPaymentStr = getValue(record, "Permite Pagamento Parcial", "Pagamento Parcial", "allowsPartialPayment", "true");

        BigDecimal creditLimit = parseCurrency(creditLimitStr);
        Integer closingDay = Integer.parseInt(closingDayStr);
        Integer dueDay = Integer.parseInt(dueDayStr);
        Boolean allowsPartialPayment = parseBoolean(allowsPartialPaymentStr);

        return new CreditCardImportItemDTO(
                name, creditLimit, closingDay, dueDay, allowsPartialPayment, lineNumber
        );
    }

    // ========== Row Parsers (Excel) ==========

    private BillImportItemDTO parseBillRow(Row row, java.util.Map<String, Integer> headerMap, int lineNumber) {
        String name = getCellValue(row, headerMap, "Nome", "name");
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        
        String description = getCellValue(row, headerMap, "Descrição", "description");
        String dateStr = getCellValue(row, headerMap, "Data", "date", "executionDate");
        if (dateStr == null || dateStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Data é obrigatória");
        }
        
        String totalAmountStr = getCellValue(row, headerMap, "Valor Total", "totalAmount");
        if (totalAmountStr == null || totalAmountStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Valor Total é obrigatório");
        }
        
        String numberOfInstallmentsStr = getCellValue(row, headerMap, "Número de Parcelas", "numberOfInstallments", "1");
        String creditCardIdStr = getCellValue(row, headerMap, "ID Cartão", "creditCardId");

        LocalDateTime executionDate = parseDateTime(dateStr);
        BigDecimal totalAmount = parseCurrency(totalAmountStr);
        Integer numberOfInstallments = Integer.parseInt(numberOfInstallmentsStr);
        Long creditCardId = creditCardIdStr != null && !creditCardIdStr.isEmpty() ? Long.parseLong(creditCardIdStr) : null;

        return new BillImportItemDTO(
                name, description, executionDate, totalAmount,
                numberOfInstallments, false, creditCardId, lineNumber
        );
    }

    private InvoiceImportItemDTO parseInvoiceRow(Row row, java.util.Map<String, Integer> headerMap, int lineNumber) {
        String creditCardIdStr = getCellValue(row, headerMap, "ID Cartão", "Cartão de Crédito", "creditCardId");
        if (creditCardIdStr == null || creditCardIdStr.trim().isEmpty()) {
            throw new IllegalArgumentException("ID Cartão é obrigatório");
        }
        
        String referenceMonthStr = getCellValue(row, headerMap, "Mês de Referência", "referenceMonth");
        if (referenceMonthStr == null || referenceMonthStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Mês de Referência é obrigatório");
        }
        
        String totalAmountStr = getCellValue(row, headerMap, "Valor Total", "totalAmount");
        if (totalAmountStr == null || totalAmountStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Valor Total é obrigatório");
        }
        
        String previousBalanceStr = getCellValue(row, headerMap, "Saldo Anterior", "previousBalance", "0");
        String closedStr = getCellValue(row, headerMap, "Fechada", "closed", "false");
        String paidStr = getCellValue(row, headerMap, "Paga", "paid", "false");

        Long creditCardId = Long.parseLong(creditCardIdStr);
        LocalDate referenceMonth = parseReferenceMonth(referenceMonthStr);
        BigDecimal totalAmount = parseCurrency(totalAmountStr);
        BigDecimal previousBalance = parseCurrency(previousBalanceStr);
        Boolean closed = parseBoolean(closedStr);
        Boolean paid = parseBoolean(paidStr);

        return new InvoiceImportItemDTO(
                creditCardId, referenceMonth, totalAmount,
                previousBalance, closed, paid, lineNumber
        );
    }

    private CreditCardImportItemDTO parseCreditCardRow(Row row, java.util.Map<String, Integer> headerMap, int lineNumber) {
        String name = getCellValue(row, headerMap, "Nome", "name");
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        
        String creditLimitStr = getCellValue(row, headerMap, "Limite de Crédito", "Limite", "creditLimit");
        if (creditLimitStr == null || creditLimitStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Limite de Crédito é obrigatório");
        }
        
        String closingDayStr = getCellValue(row, headerMap, "Dia de Fechamento", "Dia Fechamento", "closingDay");
        if (closingDayStr == null || closingDayStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Dia de Fechamento é obrigatório");
        }
        
        String dueDayStr = getCellValue(row, headerMap, "Dia de Vencimento", "Dia Vencimento", "dueDay");
        if (dueDayStr == null || dueDayStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Dia de Vencimento é obrigatório");
        }
        
        String allowsPartialPaymentStr = getCellValue(row, headerMap, "Permite Pagamento Parcial", "Pagamento Parcial", "allowsPartialPayment", "true");

        BigDecimal creditLimit = parseCurrency(creditLimitStr);
        Integer closingDay = Integer.parseInt(closingDayStr);
        Integer dueDay = Integer.parseInt(dueDayStr);
        Boolean allowsPartialPayment = parseBoolean(allowsPartialPaymentStr);

        return new CreditCardImportItemDTO(
                name, creditLimit, closingDay, dueDay, allowsPartialPayment, lineNumber
        );
    }

    // ========== Helper Methods ==========

    private String getValue(CSVRecord record, String... keys) {
        for (String key : keys) {
            try {
                String value = record.get(key);
                if (value != null && !value.trim().isEmpty()) {
                    return value.trim();
                }
            } catch (IllegalArgumentException e) {
                // Try next key
            }
        }
        return null;
    }

    private String getValue(CSVRecord record, String key1, String key2, String defaultValue) {
        String value = getValue(record, key1, key2);
        return value != null ? value : defaultValue;
    }

    private String getValue(CSVRecord record, String key1, String key2, String key3, String defaultValue) {
        String value = getValue(record, key1, key2, key3);
        return value != null ? value : defaultValue;
    }

    private String getCellValue(Row row, java.util.Map<String, Integer> headerMap, String... keys) {
        for (String key : keys) {
            Integer colIndex = headerMap.get(key);
            if (colIndex != null) {
                Cell cell = row.getCell(colIndex);
                if (cell != null) {
                    String value = getCellValueAsString(cell);
                    if (value != null && !value.trim().isEmpty()) {
                        return value.trim();
                    }
                }
            }
        }
        return null;
    }

    private String getCellValue(Row row, java.util.Map<String, Integer> headerMap, String key1, String key2, String defaultValue) {
        String value = getCellValue(row, headerMap, key1, key2);
        return value != null ? value : defaultValue;
    }

    private String getCellValue(Row row, java.util.Map<String, Integer> headerMap, String key1, String key2, String key3, String defaultValue) {
        String value = getCellValue(row, headerMap, key1, key2, key3);
        return value != null ? value : defaultValue;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double numValue = cell.getNumericCellValue();
                    // Remove .0 if it's an integer
                    if (numValue == (long) numValue) {
                        return String.valueOf((long) numValue);
                    }
                    return String.valueOf(numValue);
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    private LocalDateTime parseDateTime(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Data não pode ser vazia");
        }
        
        try {
            // Try dd/MM/yyyy format
            return LocalDate.parse(dateStr.trim(), DATE_FORMATTER).atStartOfDay();
        } catch (DateTimeParseException e) {
            try {
                // Try ISO format
                return LocalDate.parse(dateStr.trim(), ISO_DATE_FORMATTER).atStartOfDay();
            } catch (DateTimeParseException e2) {
                throw new IllegalArgumentException("Formato de data inválido: " + dateStr + ". Use dd/MM/yyyy");
            }
        }
    }

    private LocalDate parseReferenceMonth(String monthStr) {
        if (monthStr == null || monthStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Mês de referência não pode ser vazio");
        }
        
        try {
            // Try MM/yyyy format
            String[] parts = monthStr.trim().split("/");
            if (parts.length == 2) {
                int month = Integer.parseInt(parts[0]);
                int year = Integer.parseInt(parts[1]);
                return LocalDate.of(year, month, 1);
            }
        } catch (Exception e) {
            // Try yyyy-MM format
            try {
                String[] parts = monthStr.trim().split("-");
                if (parts.length == 2) {
                    int year = Integer.parseInt(parts[0]);
                    int month = Integer.parseInt(parts[1]);
                    return LocalDate.of(year, month, 1);
                }
            } catch (Exception e2) {
                // Ignore
            }
        }
        
        throw new IllegalArgumentException("Formato de mês inválido: " + monthStr + ". Use MM/yyyy ou yyyy-MM");
    }

    private BigDecimal parseCurrency(String valueStr) {
        if (valueStr == null || valueStr.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // Remove R$, spaces, and replace comma with dot
        String cleanValue = valueStr.trim()
                .replace("R$", "")
                .replace("$", "")
                .replace(" ", "")
                .replace(",", ".");
        
        try {
            return new BigDecimal(cleanValue);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valor monetário inválido: " + valueStr);
        }
    }

    private Boolean parseBoolean(String valueStr) {
        if (valueStr == null || valueStr.trim().isEmpty()) {
            return false;
        }
        
        String lower = valueStr.trim().toLowerCase();
        return lower.equals("true") || lower.equals("sim") || lower.equals("s") 
                || lower.equals("1") || lower.equals("yes") || lower.equals("y");
    }
}
