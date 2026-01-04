package com.truebalance.truebalance.config;

import com.truebalance.truebalance.domain.port.BillRepositoryPort;
import com.truebalance.truebalance.domain.port.CreditCardRepositoryPort;
import com.truebalance.truebalance.domain.port.InstallmentRepositoryPort;
import com.truebalance.truebalance.domain.port.InvoiceRepositoryPort;
import com.truebalance.truebalance.domain.port.PartialPaymentRepositoryPort;
import com.truebalance.truebalance.domain.service.InstallmentDateCalculator;
import com.truebalance.truebalance.domain.usecase.*;
import com.truebalance.truebalance.infra.db.repository.BillRepository;
import com.truebalance.truebalance.infra.db.repository.CreditCardRepository;
import com.truebalance.truebalance.infra.db.repository.InvoiceRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public CreateBill createBill(BillRepositoryPort billRepositoryPort) {
        return new CreateBill(billRepositoryPort);
    }

    @Bean
    public UpdateBill updateBill(BillRepositoryPort billRepositoryPort) {
        return new UpdateBill(billRepositoryPort);
    }

    @Bean
    public GetAllBills getAllBills(BillRepositoryPort billRepositoryPort) {
        return new GetAllBills(billRepositoryPort);
    }

    @Bean
    public GetBillById getBillById(BillRepositoryPort billRepositoryPort) {
        return new GetBillById(billRepositoryPort);
    }

    @Bean
    public DeleteBill deleteBill(BillRepositoryPort billRepositoryPort) {
        return new DeleteBill(billRepositoryPort);
    }

    @Bean
    public CreateCreditCard createCreditCard(CreditCardRepositoryPort creditCardRepositoryPort) {
        return new CreateCreditCard(creditCardRepositoryPort);
    }

    @Bean
    public GetAllCreditCards getAllCreditCards(CreditCardRepositoryPort creditCardRepositoryPort) {
        return new GetAllCreditCards(creditCardRepositoryPort);
    }

    @Bean
    public GetCreditCardById getCreditCardById(CreditCardRepositoryPort creditCardRepositoryPort) {
        return new GetCreditCardById(creditCardRepositoryPort);
    }

    @Bean
    public UpdateCreditCard updateCreditCard(CreditCardRepositoryPort creditCardRepositoryPort) {
        return new UpdateCreditCard(creditCardRepositoryPort);
    }

    @Bean
    public DeleteCreditCard deleteCreditCard(CreditCardRepositoryPort creditCardRepositoryPort) {
        return new DeleteCreditCard(creditCardRepositoryPort);
    }

    @Bean
    public GetInvoicesByCreditCard getInvoicesByCreditCard(InvoiceRepositoryPort invoiceRepositoryPort) {
        return new GetInvoicesByCreditCard(invoiceRepositoryPort);
    }

    @Bean
    public GetInvoiceById getInvoiceById(InvoiceRepositoryPort invoiceRepositoryPort) {
        return new GetInvoiceById(invoiceRepositoryPort);
    }

    @Bean
    public GenerateOrGetInvoiceForMonth generateOrGetInvoiceForMonth(InvoiceRepositoryPort invoiceRepositoryPort) {
        return new GenerateOrGetInvoiceForMonth(invoiceRepositoryPort);
    }

    @Bean
    public GetInvoiceBalance getInvoiceBalance(InvoiceRepositoryPort invoiceRepositoryPort,
                                                PartialPaymentRepositoryPort partialPaymentRepositoryPort) {
        return new GetInvoiceBalance(invoiceRepositoryPort, partialPaymentRepositoryPort);
    }

    @Bean
    public CloseInvoice closeInvoice(InvoiceRepositoryPort invoiceRepositoryPort,
                                      PartialPaymentRepositoryPort partialPaymentRepositoryPort) {
        return new CloseInvoice(invoiceRepositoryPort, partialPaymentRepositoryPort);
    }

    @Bean
    public AutoCloseInvoicesIfNeeded autoCloseInvoicesIfNeeded(CreditCardRepositoryPort creditCardRepositoryPort,
                                                                 CloseInvoice closeInvoice) {
        return new AutoCloseInvoicesIfNeeded(creditCardRepositoryPort, closeInvoice);
    }

    @Bean
    public GetBillInstallments getBillInstallments(InstallmentRepositoryPort installmentRepositoryPort) {
        return new GetBillInstallments(installmentRepositoryPort);
    }

    @Bean
    public GetInvoiceInstallments getInvoiceInstallments(InstallmentRepositoryPort installmentRepositoryPort) {
        return new GetInvoiceInstallments(installmentRepositoryPort);
    }

    @Bean
    public GetPartialPaymentsByInvoice getPartialPaymentsByInvoice(PartialPaymentRepositoryPort partialPaymentRepositoryPort) {
        return new GetPartialPaymentsByInvoice(partialPaymentRepositoryPort);
    }

    @Bean
    public RegisterPartialPayment registerPartialPayment(PartialPaymentRepositoryPort partialPaymentRepositoryPort,
                                                          InvoiceRepositoryPort invoiceRepositoryPort,
                                                          CreditCardRepositoryPort creditCardRepositoryPort,
                                                          GetAvailableLimit getAvailableLimit) {
        return new RegisterPartialPayment(partialPaymentRepositoryPort, invoiceRepositoryPort, creditCardRepositoryPort, getAvailableLimit);
    }

    @Bean
    public DeletePartialPayment deletePartialPayment(PartialPaymentRepositoryPort partialPaymentRepositoryPort,
                                                      InvoiceRepositoryPort invoiceRepositoryPort) {
        return new DeletePartialPayment(partialPaymentRepositoryPort, invoiceRepositoryPort);
    }

    // Phase 4.5: Bill-CreditCard Integration

    @Bean
    public InstallmentDateCalculator installmentDateCalculator() {
        return new InstallmentDateCalculator();
    }

    @Bean
    public GetAvailableLimit getAvailableLimit(
            CreditCardRepositoryPort creditCardRepository,
            InvoiceRepositoryPort invoiceRepository,
            InstallmentRepositoryPort installmentRepository,
            PartialPaymentRepositoryPort partialPaymentRepository) {
        return new GetAvailableLimit(
                creditCardRepository,
                invoiceRepository,
                installmentRepository,
                partialPaymentRepository
        );
    }

    @Bean
    public CreateBillWithCreditCard createBillWithCreditCard(
            CreateBill createBill,
            CreditCardRepositoryPort creditCardRepository,
            InstallmentRepositoryPort installmentRepository,
            InvoiceRepositoryPort invoiceRepository,
            GenerateOrGetInvoiceForMonth generateOrGetInvoiceForMonth,
            GetAvailableLimit getAvailableLimit,
            InstallmentDateCalculator installmentDateCalculator) {
        return new CreateBillWithCreditCard(
                createBill,
                creditCardRepository,
                installmentRepository,
                invoiceRepository,
                generateOrGetInvoiceForMonth,
                getAvailableLimit,
                installmentDateCalculator
        );
    }

    @Bean
    public UpdateBillWithCreditCard updateBillWithCreditCard(
            UpdateBill updateBill,
            CreditCardRepositoryPort creditCardRepository,
            InstallmentRepositoryPort installmentRepository,
            InvoiceRepositoryPort invoiceRepository,
            GenerateOrGetInvoiceForMonth generateOrGetInvoiceForMonth,
            GetAvailableLimit getAvailableLimit,
            InstallmentDateCalculator installmentDateCalculator,
            GetBillInstallments getBillInstallments) {
        return new UpdateBillWithCreditCard(
                updateBill,
                creditCardRepository,
                installmentRepository,
                invoiceRepository,
                generateOrGetInvoiceForMonth,
                getAvailableLimit,
                installmentDateCalculator,
                getBillInstallments
        );
    }

    @Bean
    public MarkInvoiceAsPaid markInvoiceAsPaid(InvoiceRepositoryPort invoiceRepositoryPort) {
        return new MarkInvoiceAsPaid(invoiceRepositoryPort);
    }

    @Bean
    public MarkInvoiceAsUnpaid markInvoiceAsUnpaid(InvoiceRepositoryPort invoiceRepositoryPort) {
        return new MarkInvoiceAsUnpaid(invoiceRepositoryPort);
    }

    @Bean
    public UpdateInvoiceUseAbsoluteValue updateInvoiceUseAbsoluteValue(InvoiceRepositoryPort invoiceRepositoryPort) {
        return new UpdateInvoiceUseAbsoluteValue(invoiceRepositoryPort);
    }

    @Bean
    public UpdateInvoiceTotalAmount updateInvoiceTotalAmount(InvoiceRepositoryPort invoiceRepositoryPort) {
        return new UpdateInvoiceTotalAmount(invoiceRepositoryPort);
    }

    @Bean
    public UpdateInvoiceRegisteredLimit updateInvoiceRegisteredLimit(InvoiceRepositoryPort invoiceRepositoryPort) {
        return new UpdateInvoiceRegisteredLimit(invoiceRepositoryPort);
    }

    @Bean
    public ExportData exportData(BillRepositoryPort billRepositoryPort,
                                 CreditCardRepositoryPort creditCardRepositoryPort,
                                 InvoiceRepositoryPort invoiceRepositoryPort,
                                 InstallmentRepositoryPort installmentRepositoryPort) {
        return new ExportData(billRepositoryPort, creditCardRepositoryPort, invoiceRepositoryPort, installmentRepositoryPort);
    }

    @Bean
    public ImportData importData(BillRepositoryPort billRepositoryPort,
                                 CreditCardRepositoryPort creditCardRepositoryPort,
                                 InvoiceRepositoryPort invoiceRepositoryPort,
                                 CreateBill createBill,
                                 CreateCreditCard createCreditCard,
                                 CreateBillWithCreditCard createBillWithCreditCard,
                                 ProcessPostImport processPostImport) {
        return new ImportData(billRepositoryPort, creditCardRepositoryPort, invoiceRepositoryPort,
                createBill, createCreditCard, createBillWithCreditCard, processPostImport);
    }

    @Bean
    public ProcessPostImport processPostImport(
            BillRepositoryPort billRepositoryPort,
            CreditCardRepositoryPort creditCardRepositoryPort,
            InvoiceRepositoryPort invoiceRepositoryPort,
            InstallmentRepositoryPort installmentRepositoryPort,
            GenerateOrGetInvoiceForMonth generateOrGetInvoiceForMonth,
            InstallmentDateCalculator installmentDateCalculator,
            CreateBillWithCreditCard createBillWithCreditCard) {
        return new ProcessPostImport(
                billRepositoryPort,
                creditCardRepositoryPort,
                invoiceRepositoryPort,
                installmentRepositoryPort,
                generateOrGetInvoiceForMonth,
                installmentDateCalculator,
                createBillWithCreditCard
        );
    }
}