import { format } from 'date-fns';
import { ptBR } from 'date-fns/locale';

// Format currency for export
export const formatCurrencyForExport = (value: number): string => {
  return new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL',
  }).format(value);
};

// Format date for export
export const formatDateForExport = (date: string | Date): string => {
  if (!date) return '';
  const dateObj = typeof date === 'string' ? new Date(date) : date;
  return format(dateObj, 'dd/MM/yyyy', { locale: ptBR });
};

// Format bills data for export
export const formatBillsForExport = (bills: any[]) => {
  return bills.map((bill) => ({
    'ID': bill.id,
    'Nome': bill.name,
    'Descrição': bill.description || '',
    'Data': formatDateForExport(bill.date),
    'Valor Total': formatCurrencyForExport(bill.totalAmount),
    'Número de Parcelas': bill.numberOfInstallments,
    'Valor da Parcela': formatCurrencyForExport(
      bill.totalAmount / bill.numberOfInstallments
    ),
    'Criado em': formatDateForExport(bill.createdAt),
    'Atualizado em': formatDateForExport(bill.updatedAt),
  }));
};

// Format credit cards data for export
export const formatCreditCardsForExport = (cards: any[]) => {
  return cards.map((card) => ({
    'ID': card.id,
    'Nome': card.name,
    'Limite Total': formatCurrencyForExport(card.totalLimit),
    'Limite Disponível': formatCurrencyForExport(card.availableLimit || 0),
    'Limite Usado': formatCurrencyForExport(
      card.totalLimit - (card.availableLimit || 0)
    ),
    'Uso (%)': (
      ((card.totalLimit - (card.availableLimit || 0)) / card.totalLimit) *
      100
    ).toFixed(2) + '%',
    'Dia de Fechamento': card.closingDay,
    'Dia de Vencimento': card.dueDay,
    'Permite Pagamento Parcial': card.allowsPartialPayment ? 'Sim' : 'Não',
    'Criado em': formatDateForExport(card.createdAt),
  }));
};

// Format invoices data for export
export const formatInvoicesForExport = (invoices: any[]) => {
  return invoices.map((invoice) => ({
    'ID': invoice.id,
    'Mês de Referência': `${String(invoice.referenceMonth).padStart(2, '0')}/${invoice.referenceYear}`,
    'Cartão': invoice.creditCard?.name || '',
    'Valor Total': formatCurrencyForExport(invoice.totalAmount),
    'Saldo Anterior': formatCurrencyForExport(invoice.previousBalance || 0),
    'Valor Pago': formatCurrencyForExport(invoice.paidAmount || 0),
    'Saldo Restante': formatCurrencyForExport(
      invoice.totalAmount + (invoice.previousBalance || 0) - (invoice.paidAmount || 0)
    ),
    'Status': invoice.status === 'PAID' ? 'Paga' : invoice.status === 'CLOSED' ? 'Fechada' : 'Aberta',
    'Data de Fechamento': formatDateForExport(invoice.closingDate),
    'Data de Vencimento': formatDateForExport(invoice.dueDate),
  }));
};

// Format installments data for export
export const formatInstallmentsForExport = (installments: any[]) => {
  return installments.map((installment) => ({
    'ID': installment.id,
    'Descrição': installment.bill?.name || '',
    'Número da Parcela': installment.installmentNumber,
    'Total de Parcelas': installment.totalInstallments,
    'Parcela': `${installment.installmentNumber}/${installment.totalInstallments}`,
    'Valor': formatCurrencyForExport(installment.amount),
    'Data de Vencimento': formatDateForExport(installment.dueDate),
    'Status': installment.status === 'PAID' ? 'Paga' : 'Pendente',
    'Fatura': installment.invoice ? `${installment.invoice.referenceMonth}/${installment.invoice.referenceYear}` : '',
  }));
};

// Format partial payments data for export
export const formatPartialPaymentsForExport = (payments: any[]) => {
  return payments.map((payment) => ({
    'ID': payment.id,
    'Valor': formatCurrencyForExport(payment.amount),
    'Descrição': payment.description || '',
    'Data do Pagamento': formatDateForExport(payment.paymentDate),
    'Fatura': payment.invoice ? `${payment.invoice.referenceMonth}/${payment.invoice.referenceYear}` : '',
  }));
};

// Format monthly expenses data for export (reports)
export const formatMonthlyExpensesForExport = (expenses: any[]) => {
  return expenses.map((expense) => ({
    'Mês': expense.month,
    'Contas': formatCurrencyForExport(expense.bills || 0),
    'Cartões de Crédito': formatCurrencyForExport(expense.creditCards || 0),
    'Total': formatCurrencyForExport((expense.bills || 0) + (expense.creditCards || 0)),
  }));
};

// Format category breakdown for export (reports)
export const formatCategoryBreakdownForExport = (categories: any[]) => {
  return categories.map((category) => ({
    'Categoria': category.name,
    'Valor': formatCurrencyForExport(category.value),
    'Porcentagem': category.percentage.toFixed(2) + '%',
    'Quantidade': category.count || 0,
  }));
};
