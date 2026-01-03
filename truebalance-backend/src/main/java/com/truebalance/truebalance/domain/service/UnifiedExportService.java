package com.truebalance.truebalance.domain.service;

import com.truebalance.truebalance.application.dto.output.BillResponseDTO;
import com.truebalance.truebalance.application.dto.output.CreditCardResponseDTO;
import com.truebalance.truebalance.application.dto.output.InvoiceResponseDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class UnifiedExportService {

    private static final Logger logger = LoggerFactory.getLogger(UnifiedExportService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MM/yyyy");

    /**
     * Exporta todas as entidades para um único arquivo Excel com múltiplas abas
     */
    public byte[] exportToExcel(List<BillResponseDTO> bills,
                                List<CreditCardResponseDTO> creditCards,
                                List<InvoiceResponseDTO> invoices) throws IOException {
        logger.info("Exportando {} contas, {} cartões e {} faturas para Excel", 
                bills.size(), creditCards.size(), invoices.size());

        Workbook workbook = new XSSFWorkbook();
        
        // Criar aba de Contas
        createBillsSheet(workbook, bills);
        
        // Criar aba de Cartões de Crédito
        createCreditCardsSheet(workbook, creditCards);
        
        // Criar aba de Faturas
        createInvoicesSheet(workbook, invoices);

        // Escrever para ByteArrayOutputStream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        logger.info("Exportação concluída. Tamanho do arquivo: {} bytes", outputStream.size());
        return outputStream.toByteArray();
    }

    private void createBillsSheet(Workbook workbook, List<BillResponseDTO> bills) {
        Sheet sheet = workbook.createSheet("Contas");
        
        // Criar estilo para cabeçalho
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        // Criar linha de cabeçalho
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Nome", "Descrição", "Data", "Valor Total", 
                           "Número de Parcelas", "Valor da Parcela", "ID Cartão", 
                           "Criado em", "Atualizado em"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Criar estilo para células de data
        CellStyle dateStyle = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy"));

        // Criar estilo para células de moeda
        CellStyle currencyStyle = workbook.createCellStyle();
        currencyStyle.setDataFormat(createHelper.createDataFormat().getFormat("R$ #,##0.00"));

        // Preencher dados
        int rowNum = 1;
        for (BillResponseDTO bill : bills) {
            Row row = sheet.createRow(rowNum++);
            
            int colNum = 0;
            createCell(row, colNum++, bill.getId(), null);
            createCell(row, colNum++, bill.getName(), null);
            createCell(row, colNum++, bill.getDescription() != null ? bill.getDescription() : "", null);
            createCell(row, colNum++, bill.getExecutionDate(), dateStyle);
            createCell(row, colNum++, bill.getTotalAmount(), currencyStyle);
            createCell(row, colNum++, bill.getNumberOfInstallments(), null);
            
            BigDecimal installmentAmount = bill.getTotalAmount()
                    .divide(BigDecimal.valueOf(bill.getNumberOfInstallments()), 2, java.math.RoundingMode.HALF_UP);
            createCell(row, colNum++, installmentAmount, currencyStyle);
            
            createCell(row, colNum++, bill.getCreditCardId() != null ? bill.getCreditCardId() : "", null);
            createCell(row, colNum++, bill.getCreatedAt(), dateStyle);
            createCell(row, colNum++, bill.getUpdatedAt(), dateStyle);
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createCreditCardsSheet(Workbook workbook, List<CreditCardResponseDTO> creditCards) {
        Sheet sheet = workbook.createSheet("Cartões de Crédito");
        
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Nome", "Limite de Crédito", "Limite Disponível", 
                           "Dia de Fechamento", "Dia de Vencimento", "Permite Pagamento Parcial", 
                           "Criado em", "Atualizado em"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        CreationHelper createHelper = workbook.getCreationHelper();
        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy"));
        
        CellStyle currencyStyle = workbook.createCellStyle();
        currencyStyle.setDataFormat(createHelper.createDataFormat().getFormat("R$ #,##0.00"));

        int rowNum = 1;
        for (CreditCardResponseDTO card : creditCards) {
            Row row = sheet.createRow(rowNum++);
            
            int colNum = 0;
            createCell(row, colNum++, card.getId(), null);
            createCell(row, colNum++, card.getName(), null);
            createCell(row, colNum++, card.getCreditLimit(), currencyStyle);
            createCell(row, colNum++, BigDecimal.ZERO, currencyStyle); // Available limit não disponível no DTO
            createCell(row, colNum++, card.getClosingDay(), null);
            createCell(row, colNum++, card.getDueDay(), null);
            createCell(row, colNum++, card.isAllowsPartialPayment() ? "Sim" : "Não", null);
            createCell(row, colNum++, card.getCreatedAt(), dateStyle);
            createCell(row, colNum++, card.getUpdatedAt(), dateStyle);
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createInvoicesSheet(Workbook workbook, List<InvoiceResponseDTO> invoices) {
        Sheet sheet = workbook.createSheet("Faturas");
        
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "ID Cartão", "Mês de Referência", "Valor Total", 
                           "Saldo Anterior", "Fechada", "Paga", "Criado em", "Atualizado em"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        CreationHelper createHelper = workbook.getCreationHelper();
        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy"));
        
        CellStyle currencyStyle = workbook.createCellStyle();
        currencyStyle.setDataFormat(createHelper.createDataFormat().getFormat("R$ #,##0.00"));

        int rowNum = 1;
        for (InvoiceResponseDTO invoice : invoices) {
            Row row = sheet.createRow(rowNum++);
            
            int colNum = 0;
            createCell(row, colNum++, invoice.getId(), null);
            createCell(row, colNum++, invoice.getCreditCardId(), null);
            
            // Formatar mês de referência
            LocalDate refMonth = invoice.getReferenceMonth();
            String monthStr = String.format("%02d/%d", refMonth.getMonthValue(), refMonth.getYear());
            createCell(row, colNum++, monthStr, null);
            
            createCell(row, colNum++, invoice.getTotalAmount(), currencyStyle);
            createCell(row, colNum++, invoice.getPreviousBalance() != null ? invoice.getPreviousBalance() : BigDecimal.ZERO, currencyStyle);
            createCell(row, colNum++, invoice.isClosed() ? "Sim" : "Não", null);
            createCell(row, colNum++, invoice.isPaid() ? "Sim" : "Não", null);
            createCell(row, colNum++, invoice.getCreatedAt(), dateStyle);
            createCell(row, colNum++, invoice.getUpdatedAt(), dateStyle);
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createCell(Row row, int column, Object value, CellStyle style) {
        Cell cell = row.createCell(column);
        
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Number) {
            if (value instanceof BigDecimal) {
                cell.setCellValue(((BigDecimal) value).doubleValue());
            } else if (value instanceof Long) {
                cell.setCellValue(((Long) value).doubleValue());
            } else if (value instanceof Integer) {
                cell.setCellValue(((Integer) value).doubleValue());
            } else {
                cell.setCellValue(((Number) value).doubleValue());
            }
        } else if (value instanceof LocalDateTime) {
            cell.setCellValue((LocalDateTime) value);
        } else if (value instanceof LocalDate) {
            cell.setCellValue((LocalDate) value);
        } else {
            cell.setCellValue(value.toString());
        }
        
        if (style != null) {
            cell.setCellStyle(style);
        }
    }
}
