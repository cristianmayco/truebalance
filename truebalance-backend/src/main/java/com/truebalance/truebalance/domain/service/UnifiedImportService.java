package com.truebalance.truebalance.domain.service;

import com.truebalance.truebalance.application.dto.input.BillImportItemDTO;
import com.truebalance.truebalance.application.dto.input.CreditCardImportItemDTO;
import com.truebalance.truebalance.application.dto.input.InvoiceImportItemDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UnifiedImportService {

    private static final Logger logger = LoggerFactory.getLogger(UnifiedImportService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static class UnifiedImportResult {
        private List<BillImportItemDTO> bills = new ArrayList<>();
        private List<CreditCardImportItemDTO> creditCards = new ArrayList<>();
        private List<InvoiceImportItemDTO> invoices = new ArrayList<>();

        public List<BillImportItemDTO> getBills() { return bills; }
        public void setBills(List<BillImportItemDTO> bills) { this.bills = bills; }
        public List<CreditCardImportItemDTO> getCreditCards() { return creditCards; }
        public void setCreditCards(List<CreditCardImportItemDTO> creditCards) { this.creditCards = creditCards; }
        public List<InvoiceImportItemDTO> getInvoices() { return invoices; }
        public void setInvoices(List<InvoiceImportItemDTO> invoices) { this.invoices = invoices; }
    }

    /**
     * Importa todas as entidades de um único arquivo Excel com múltiplas abas
     */
    public UnifiedImportResult parseUnifiedFile(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("Nome do arquivo não pode ser nulo");
        }

        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        
        if (!extension.equals("xlsx") && !extension.equals("xls")) {
            throw new IllegalArgumentException("Formato de arquivo não suportado. Use XLS ou XLSX");
        }

        UnifiedImportResult result = new UnifiedImportResult();
        
        Workbook workbook = extension.equals("xlsx") 
                ? new XSSFWorkbook(file.getInputStream()) 
                : new HSSFWorkbook(file.getInputStream());

        try {
            // Processar aba de Contas
            Sheet billsSheet = workbook.getSheet("Contas");
            if (billsSheet != null) {
                result.setBills(parseBillsSheet(billsSheet));
                logger.info("Processadas {} contas da aba 'Contas'", result.getBills().size());
            }

            // Processar aba de Cartões de Crédito
            Sheet creditCardsSheet = workbook.getSheet("Cartões de Crédito");
            if (creditCardsSheet != null) {
                result.setCreditCards(parseCreditCardsSheet(creditCardsSheet));
                logger.info("Processados {} cartões da aba 'Cartões de Crédito'", result.getCreditCards().size());
            }

            // Processar aba de Faturas
            Sheet invoicesSheet = workbook.getSheet("Faturas");
            if (invoicesSheet != null) {
                result.setInvoices(parseInvoicesSheet(invoicesSheet));
                logger.info("Processadas {} faturas da aba 'Faturas'", result.getInvoices().size());
            }

        } finally {
            workbook.close();
        }

        return result;
    }

    private List<BillImportItemDTO> parseBillsSheet(Sheet sheet) {
        List<BillImportItemDTO> items = new ArrayList<>();
        
        if (sheet.getPhysicalNumberOfRows() < 2) {
            return items; // Apenas cabeçalho, sem dados
        }

        // Ler cabeçalho
        Row headerRow = sheet.getRow(0);
        Map<String, Integer> headerMap = new HashMap<>();
        for (Cell cell : headerRow) {
            String headerName = getCellValueAsString(cell);
            headerMap.put(headerName, cell.getColumnIndex());
        }

        // Processar linhas de dados
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            try {
                int lineNumber = i + 1;
                BillImportItemDTO item = parseBillRow(row, headerMap, lineNumber);
                items.add(item);
            } catch (Exception e) {
                logger.warn("Erro ao processar linha {} da aba Contas: {}", i + 1, e.getMessage());
                throw new RuntimeException("Erro na linha " + (i + 1) + " da aba Contas: " + e.getMessage(), e);
            }
        }

        return items;
    }

    private List<CreditCardImportItemDTO> parseCreditCardsSheet(Sheet sheet) {
        List<CreditCardImportItemDTO> items = new ArrayList<>();
        
        if (sheet.getPhysicalNumberOfRows() < 2) {
            return items;
        }

        Row headerRow = sheet.getRow(0);
        Map<String, Integer> headerMap = new HashMap<>();
        for (Cell cell : headerRow) {
            String headerName = getCellValueAsString(cell);
            headerMap.put(headerName, cell.getColumnIndex());
        }

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            try {
                int lineNumber = i + 1;
                CreditCardImportItemDTO item = parseCreditCardRow(row, headerMap, lineNumber);
                items.add(item);
            } catch (Exception e) {
                logger.warn("Erro ao processar linha {} da aba Cartões de Crédito: {}", i + 1, e.getMessage());
                throw new RuntimeException("Erro na linha " + (i + 1) + " da aba Cartões de Crédito: " + e.getMessage(), e);
            }
        }

        return items;
    }

    private List<InvoiceImportItemDTO> parseInvoicesSheet(Sheet sheet) {
        List<InvoiceImportItemDTO> items = new ArrayList<>();
        
        if (sheet.getPhysicalNumberOfRows() < 2) {
            return items;
        }

        Row headerRow = sheet.getRow(0);
        Map<String, Integer> headerMap = new HashMap<>();
        for (Cell cell : headerRow) {
            String headerName = getCellValueAsString(cell);
            headerMap.put(headerName, cell.getColumnIndex());
        }

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            try {
                int lineNumber = i + 1;
                InvoiceImportItemDTO item = parseInvoiceRow(row, headerMap, lineNumber);
                items.add(item);
            } catch (Exception e) {
                logger.warn("Erro ao processar linha {} da aba Faturas: {}", i + 1, e.getMessage());
                throw new RuntimeException("Erro na linha " + (i + 1) + " da aba Faturas: " + e.getMessage(), e);
            }
        }

        return items;
    }

    private BillImportItemDTO parseBillRow(Row row, Map<String, Integer> headerMap, int lineNumber) {
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

    private CreditCardImportItemDTO parseCreditCardRow(Row row, Map<String, Integer> headerMap, int lineNumber) {
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

    private InvoiceImportItemDTO parseInvoiceRow(Row row, Map<String, Integer> headerMap, int lineNumber) {
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

    private String getCellValue(Row row, Map<String, Integer> headerMap, String... keys) {
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

    private String getCellValue(Row row, Map<String, Integer> headerMap, String key1, String key2, String defaultValue) {
        String value = getCellValue(row, headerMap, key1, key2);
        return value != null ? value : defaultValue;
    }

    private String getCellValue(Row row, Map<String, Integer> headerMap, String key1, String key2, String key3, String defaultValue) {
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
            return LocalDate.parse(dateStr.trim(), DATE_FORMATTER).atStartOfDay();
        } catch (Exception e) {
            try {
                return LocalDate.parse(dateStr.trim()).atStartOfDay();
            } catch (Exception e2) {
                throw new IllegalArgumentException("Formato de data inválido: " + dateStr + ". Use dd/MM/yyyy");
            }
        }
    }

    private LocalDate parseReferenceMonth(String monthStr) {
        if (monthStr == null || monthStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Mês de referência não pode ser vazio");
        }
        
        try {
            String[] parts = monthStr.trim().split("/");
            if (parts.length == 2) {
                int month = Integer.parseInt(parts[0]);
                int year = Integer.parseInt(parts[1]);
                return LocalDate.of(year, month, 1);
            }
        } catch (Exception e) {
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
