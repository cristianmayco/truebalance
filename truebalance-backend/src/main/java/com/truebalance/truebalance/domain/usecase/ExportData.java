package com.truebalance.truebalance.domain.usecase;

import com.truebalance.truebalance.application.dto.output.BillResponseDTO;
import com.truebalance.truebalance.application.dto.output.CategoryResponseDTO;
import com.truebalance.truebalance.application.dto.output.CreditCardResponseDTO;
import com.truebalance.truebalance.application.dto.output.ExportDataDTO;
import com.truebalance.truebalance.application.dto.output.InvoiceResponseDTO;
import com.truebalance.truebalance.domain.entity.Bill;
import com.truebalance.truebalance.domain.entity.Category;
import com.truebalance.truebalance.domain.entity.CreditCard;
import com.truebalance.truebalance.domain.entity.Invoice;
import com.truebalance.truebalance.domain.entity.Installment;
import com.truebalance.truebalance.domain.port.BillRepositoryPort;
import com.truebalance.truebalance.domain.port.CategoryRepositoryPort;
import com.truebalance.truebalance.domain.port.CreditCardRepositoryPort;
import com.truebalance.truebalance.domain.port.InvoiceRepositoryPort;
import com.truebalance.truebalance.domain.port.InstallmentRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class ExportData {

    private static final Logger logger = LoggerFactory.getLogger(ExportData.class);
    private final BillRepositoryPort billRepository;
    private final CreditCardRepositoryPort creditCardRepository;
    private final InvoiceRepositoryPort invoiceRepository;
    private final InstallmentRepositoryPort installmentRepository;
    private final CategoryRepositoryPort categoryRepository;

    public ExportData(BillRepositoryPort billRepository,
                      CreditCardRepositoryPort creditCardRepository,
                      InvoiceRepositoryPort invoiceRepository,
                      InstallmentRepositoryPort installmentRepository,
                      CategoryRepositoryPort categoryRepository) {
        this.billRepository = billRepository;
        this.creditCardRepository = creditCardRepository;
        this.invoiceRepository = invoiceRepository;
        this.installmentRepository = installmentRepository;
        this.categoryRepository = categoryRepository;
    }

    public ExportDataDTO execute() {
        logger.info("Exportando todos os dados do sistema");

        // Coletar todos os IDs de cartões referenciados por contas (através de parcelas)
        Set<Long> referencedCreditCardIds = new HashSet<>();
        
        // Exportar contas e coletar creditCardIds das parcelas
        List<Bill> bills = billRepository.findAll();
        List<BillResponseDTO> billsDTO = new ArrayList<>();
        
        for (Bill bill : bills) {
            Long creditCardId = null;
            List<Installment> installments = installmentRepository.findByBillId(bill.getId());
            if (!installments.isEmpty()) {
                // Pegar o creditCardId da primeira parcela (todas devem ter o mesmo)
                creditCardId = installments.stream()
                        .map(Installment::getCreditCardId)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(null);
                
                if (creditCardId != null) {
                    referencedCreditCardIds.add(creditCardId);
                }
            }
            billsDTO.add(BillResponseDTO.fromBill(bill, creditCardId));
        }
        logger.info("Exportadas {} contas", billsDTO.size());

        // Coletar IDs de cartões referenciados por faturas
        List<CreditCard> allCreditCards = creditCardRepository.findAll();
        Set<Long> invoiceCreditCardIds = new HashSet<>();
        for (CreditCard cc : allCreditCards) {
            List<Invoice> invoices = invoiceRepository.findByCreditCardId(cc.getId());
            if (!invoices.isEmpty()) {
                invoiceCreditCardIds.add(cc.getId());
            }
        }

        // Unir todos os IDs de cartões referenciados
        referencedCreditCardIds.addAll(invoiceCreditCardIds);

        // Exportar apenas os cartões que são referenciados por contas ou faturas
        // Se não houver referências, exportar todos os cartões
        List<CreditCard> creditCardsToExport;
        if (referencedCreditCardIds.isEmpty()) {
            creditCardsToExport = allCreditCards;
            logger.info("Nenhum cartão referenciado encontrado, exportando todos os {} cartões", allCreditCards.size());
        } else {
            creditCardsToExport = allCreditCards.stream()
                    .filter(cc -> referencedCreditCardIds.contains(cc.getId()))
                    .collect(Collectors.toList());
            logger.info("Exportando {} cartões referenciados (de {} total)", 
                    creditCardsToExport.size(), allCreditCards.size());
        }

        List<CreditCardResponseDTO> creditCardsDTO = creditCardsToExport.stream()
                .map(CreditCardResponseDTO::fromCreditCard)
                .collect(Collectors.toList());
        logger.info("Exportados {} cartões de crédito", creditCardsDTO.size());

        // Exportar faturas apenas dos cartões exportados
        List<InvoiceResponseDTO> invoicesDTO = creditCardsToExport.stream()
                .flatMap(cc -> {
                    List<Invoice> invoices = invoiceRepository.findByCreditCardId(cc.getId());
                    return invoices.stream()
                            .map(InvoiceResponseDTO::fromInvoice);
                })
                .collect(Collectors.toList());
        logger.info("Exportadas {} faturas", invoicesDTO.size());

        // Buscar categorias referenciadas pelas bills através do nome da categoria
        Set<String> referencedCategoryNames = new HashSet<>();
        for (Bill bill : bills) {
            if (bill.getCategory() != null && !bill.getCategory().trim().isEmpty()) {
                referencedCategoryNames.add(bill.getCategory());
            }
        }
        
        // Exportar categorias referenciadas ou todas se não houver referências
        List<Category> allCategories = categoryRepository.findAll();
        List<CategoryResponseDTO> categoriesDTO;
        
        if (referencedCategoryNames.isEmpty()) {
            // Exportar todas as categorias se não houver referências
            categoriesDTO = allCategories.stream()
                    .map(CategoryResponseDTO::fromCategory)
                    .collect(Collectors.toList());
            logger.info("Nenhuma categoria referenciada encontrada, exportando todas as {} categorias", allCategories.size());
        } else {
            // Exportar apenas categorias referenciadas
            categoriesDTO = allCategories.stream()
                    .filter(cat -> referencedCategoryNames.contains(cat.getName()))
                    .map(CategoryResponseDTO::fromCategory)
                    .collect(Collectors.toList());
            logger.info("Exportando {} categorias referenciadas (de {} total)", 
                    categoriesDTO.size(), allCategories.size());
        }
        logger.info("Exportadas {} categorias", categoriesDTO.size());

        ExportDataDTO exportData = new ExportDataDTO(billsDTO, creditCardsDTO, invoicesDTO, categoriesDTO);
        logger.info("Exportação concluída: {} contas, {} cartões, {} faturas, {} categorias",
                billsDTO.size(), creditCardsDTO.size(), invoicesDTO.size(), categoriesDTO.size());

        return exportData;
    }
}
