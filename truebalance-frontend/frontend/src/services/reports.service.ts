import { axiosInstance } from '@/lib/axios';
import type { BillResponseDTO } from '@/types/dtos/bill.dto';
import type { PaginatedResponse } from '@/types/dtos/common.dto';
import { creditCardsService } from './creditCards.service';
import { invoicesService } from './invoices.service';

export interface MonthlyExpense {
  month: string;
  year: number;
  bills: number;
  creditCards: number;
  total: number;
}

export interface CategoryExpense {
  category: string;
  amount: number;
  percentage: number;
  count: number;
}

export interface ExpenseMetrics {
  totalExpenses: number;
  averageMonthly: number;
  highestMonth: {
    month: string;
    amount: number;
  };
  lowestMonth: {
    month: string;
    amount: number;
  };
  periodComparison: {
    current: number;
    previous: number;
    percentageChange: number;
  };
}

/**
 * Service para relatórios e analytics
 * Como a API pode não ter endpoints específicos de relatórios,
 * implementamos agregações client-side
 */
class ReportsService {
  /**
   * Formata data no formato ISO_DATE_TIME (yyyy-MM-ddTHH:mm:ss) para compatibilidade com backend Java
   */
  private formatDateTime(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = String(date.getSeconds()).padStart(2, '0');
    return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;
  }
  /**
   * Busca gastos mensais por período (últimos N meses)
   */
  async getMonthlyExpensesByPeriod(months: number): Promise<MonthlyExpense[]> {
    const endDate = new Date();
    endDate.setHours(23, 59, 59, 999);
    
    const startDate = new Date();
    startDate.setMonth(startDate.getMonth() - months);
    startDate.setDate(1); // Primeiro dia do mês
    startDate.setHours(0, 0, 0, 0);
    
    // Use the same logic as getMonthlyExpenses but with custom date range
    try {
      // Fetch bills with date filters
      const billsResponse = await axiosInstance.get<PaginatedResponse<BillResponseDTO>>('/bills', {
        params: {
          startDate: this.formatDateTime(startDate),
          endDate: this.formatDateTime(endDate),
          size: 1000,
          page: 0,
        },
      });

      const bills = billsResponse.data.content || [];

      // Fetch all credit cards first, then get invoices for each
      // NOTE: Invoices API doesn't support date filters, so we fetch all and filter client-side
      const creditCards = await creditCardsService.getAll();
      
      // Fetch invoices for all credit cards in parallel
      const invoicePromises = creditCards.map(card => 
        invoicesService.getByCreditCard(card.id).catch(() => []) // Return empty array on error
      );
      
      const invoiceArrays = await Promise.all(invoicePromises);
      const allInvoices = invoiceArrays.flat();

      // Filter invoices by date range (based on referenceMonth)
      const invoices = allInvoices.filter((invoice) => {
        if (!invoice.referenceMonth) return false;
        
        // Parse referenceMonth (format: YYYY-MM-DD or YYYY-MM)
        const dateParts = invoice.referenceMonth.split('-');
        if (dateParts.length < 2) return false;
        
        const year = parseInt(dateParts[0]);
        const month = parseInt(dateParts[1]);
        if (isNaN(year) || isNaN(month) || month < 1 || month > 12) return false;
        
        // Create date for the first day of the invoice month
        const invoiceDate = new Date(year, month - 1, 1);
        
        // Check if invoice date is within range
        return invoiceDate >= startDate && invoiceDate <= endDate;
      });

      // Group by month
      const monthlyData = new Map<string, MonthlyExpense>();

      // Process bills
      bills.forEach((bill) => {
        const billDate = bill.billDate || bill.date || bill.executionDate;
        if (!billDate) return;
        
        const date = new Date(billDate);
        // Valida se a data é válida
        if (isNaN(date.getTime())) return;
        
        const monthKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;

        if (!monthlyData.has(monthKey)) {
          monthlyData.set(monthKey, {
            month: date.toLocaleDateString('pt-BR', { month: 'long' }),
            year: date.getFullYear(),
            bills: 0,
            creditCards: 0,
            total: 0,
          });
        }

        const data = monthlyData.get(monthKey)!;
        const installments = bill.numberOfInstallments || 1;
        const installmentValue = (bill.totalAmount || 0) / installments;
        if (!isNaN(installmentValue) && isFinite(installmentValue)) {
          data.bills += installmentValue;
          data.total += installmentValue;
        }
      });

      // Process invoices
      invoices.forEach((invoice) => {
        const invoiceDate = invoice.referenceMonth;
        if (!invoiceDate) return;
        
        // Parse date string as local date to avoid timezone issues
        const dateParts = invoiceDate.split('-');
        if (dateParts.length < 2) return;
        
        const year = parseInt(dateParts[0]);
        const month = parseInt(dateParts[1]);
        // Valida se ano e mês são válidos
        if (isNaN(year) || isNaN(month) || month < 1 || month > 12) return;
        
        const date = new Date(year, month - 1, 1); // month is 0-indexed
        // Valida se a data é válida
        if (isNaN(date.getTime())) return;
        
        const monthKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;

        if (!monthlyData.has(monthKey)) {
          monthlyData.set(monthKey, {
            month: date.toLocaleDateString('pt-BR', { month: 'long' }),
            year: date.getFullYear(),
            bills: 0,
            creditCards: 0,
            total: 0,
          });
        }

        const data = monthlyData.get(monthKey)!;
        const amount = invoice.totalAmount || 0;
        if (!isNaN(amount) && isFinite(amount)) {
          data.creditCards += amount;
          data.total += amount;
        }
      });

      // Convert to array and sort by month (most recent first)
      // Helper para obter índice do mês
      const getMonthIndex = (month: string): number => {
        const normalized = month
          .toLowerCase()
          .normalize('NFD')
          .replace(/[\u0300-\u036f]/g, '');
        const monthOrder = ['janeiro', 'fevereiro', 'marco', 'abril', 'maio', 'junho', 
                           'julho', 'agosto', 'setembro', 'outubro', 'novembro', 'dezembro'];
        const index = monthOrder.indexOf(normalized);
        return index !== -1 ? index : 0; // Fallback para 0 se não encontrar
      };
      
      return Array.from(monthlyData.values()).sort((a, b) => {
        // Ordena por ano (mais recente primeiro) e depois por mês (mais recente primeiro)
        if (a.year !== b.year) {
          return b.year - a.year;
        }
        const aMonthIndex = getMonthIndex(a.month);
        const bMonthIndex = getMonthIndex(b.month);
        return bMonthIndex - aMonthIndex;
      });
    } catch (error) {
      console.error('Error fetching monthly expenses by period:', error);
      throw error;
    }
  }

  /**
   * Calcula gastos mensais com base em contas e faturas
   */
  async getMonthlyExpenses(year: number, startDate?: Date, endDate?: Date): Promise<MonthlyExpense[]> {
    try {
      // Calculate date range if not provided
      const start = startDate || new Date(year, 0, 1);
      const end = endDate || new Date(year, 11, 31, 23, 59, 59);

      // Fetch bills with date filters
      const billsResponse = await axiosInstance.get<PaginatedResponse<BillResponseDTO>>('/bills', {
        params: {
          startDate: this.formatDateTime(start),
          endDate: this.formatDateTime(end),
          size: 1000, // Get all for the period
          page: 0,
        },
      });

      const bills = billsResponse.data.content || [];

      // Fetch all credit cards first, then get invoices for each
      const creditCards = await creditCardsService.getAll();
      
      // Fetch invoices for all credit cards in parallel
      const invoicePromises = creditCards.map(card => 
        invoicesService.getByCreditCard(card.id).catch(() => []) // Return empty array on error
      );
      
      const invoiceArrays = await Promise.all(invoicePromises);
      const allInvoices = invoiceArrays.flat();

      // Filter invoices by date range (based on referenceMonth)
      const invoices = allInvoices.filter((invoice) => {
        if (!invoice.referenceMonth) return false;
        
        // Parse referenceMonth (format: YYYY-MM-DD or YYYY-MM)
        const dateParts = invoice.referenceMonth.split('-');
        if (dateParts.length < 2) return false;
        
        const year = parseInt(dateParts[0]);
        const month = parseInt(dateParts[1]);
        if (isNaN(year) || isNaN(month) || month < 1 || month > 12) return false;
        
        // Create date for the first day of the invoice month
        const invoiceDate = new Date(year, month - 1, 1);
        
        // Check if invoice date is within range
        return invoiceDate >= start && invoiceDate <= end;
      });

      // Group by month
      const monthlyData = new Map<string, MonthlyExpense>();

      // Process bills
      bills.forEach((bill) => {
        const billDate = bill.billDate || bill.date || bill.executionDate;
        if (!billDate) return;
        
        const date = new Date(billDate);
        // Valida se a data é válida
        if (isNaN(date.getTime())) return;
        
        const monthKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;

        if (!monthlyData.has(monthKey)) {
          monthlyData.set(monthKey, {
            month: date.toLocaleDateString('pt-BR', { month: 'long' }),
            year: date.getFullYear(),
            bills: 0,
            creditCards: 0,
            total: 0,
          });
        }

        const data = monthlyData.get(monthKey)!;
        const installments = bill.numberOfInstallments || 1;
        const installmentValue = (bill.totalAmount || 0) / installments;
        if (!isNaN(installmentValue) && isFinite(installmentValue)) {
          data.bills += installmentValue;
          data.total += installmentValue;
        }
      });

      // Process invoices
      invoices.forEach((invoice) => {
        const invoiceDate = invoice.referenceMonth;
        if (!invoiceDate) return;
        
        // Parse date string as local date to avoid timezone issues
        const dateParts = invoiceDate.split('-');
        if (dateParts.length < 2) return;
        
        const year = parseInt(dateParts[0]);
        const month = parseInt(dateParts[1]);
        // Valida se ano e mês são válidos
        if (isNaN(year) || isNaN(month) || month < 1 || month > 12) return;
        
        const date = new Date(year, month - 1, 1); // month is 0-indexed
        // Valida se a data é válida
        if (isNaN(date.getTime())) return;
        
        const monthKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;

        if (!monthlyData.has(monthKey)) {
          monthlyData.set(monthKey, {
            month: date.toLocaleDateString('pt-BR', { month: 'long' }),
            year: date.getFullYear(),
            bills: 0,
            creditCards: 0,
            total: 0,
          });
        }

        const data = monthlyData.get(monthKey)!;
        const amount = invoice.totalAmount || 0;
        if (!isNaN(amount) && isFinite(amount)) {
          data.creditCards += amount;
          data.total += amount;
        }
      });

      // Convert to array and sort by month
      return Array.from(monthlyData.values()).sort((a, b) => {
        return a.year !== b.year ? a.year - b.year : a.month.localeCompare(b.month);
      });
    } catch (error) {
      console.error('Error fetching monthly expenses:', error);
      throw error;
    }
  }

  /**
   * Calcula distribuição de gastos por categoria
   * Nota: Isso assume que as contas/faturas têm um campo de categoria
   */
  async getCategoryBreakdown(startDate?: Date, endDate?: Date): Promise<CategoryExpense[]> {
    try {
      const params: any = {};
      if (startDate) params.startDate = this.formatDateTime(startDate);
      if (endDate) params.endDate = this.formatDateTime(endDate);

      // Fetch bills with date filters
      const billsResponse = await axiosInstance.get<PaginatedResponse<BillResponseDTO>>('/bills', { 
        params: { 
          ...params, 
          size: 1000,
          page: 0,
        } 
      });

      const bills = billsResponse.data.content || [];

      // Fetch all credit cards first, then get invoices for each
      const creditCards = await creditCardsService.getAll();
      
      // Fetch invoices for all credit cards in parallel
      const invoicePromises = creditCards.map(card => 
        invoicesService.getByCreditCard(card.id).catch(() => []) // Return empty array on error
      );
      
      const invoiceArrays = await Promise.all(invoicePromises);
      const allInvoices = invoiceArrays.flat();

      // Filter invoices by date range if dates provided
      let invoices = allInvoices;
      if (startDate || endDate) {
        const start = startDate || new Date(0); // Beginning of time if not provided
        const end = endDate || new Date(); // Now if not provided
        
        invoices = allInvoices.filter((invoice) => {
          if (!invoice.referenceMonth) return false;
          
          // Parse referenceMonth (format: YYYY-MM-DD or YYYY-MM)
          const dateParts = invoice.referenceMonth.split('-');
          if (dateParts.length < 2) return false;
          
          const year = parseInt(dateParts[0]);
          const month = parseInt(dateParts[1]);
          if (isNaN(year) || isNaN(month) || month < 1 || month > 12) return false;
          
          // Create date for the first day of the invoice month
          const invoiceDate = new Date(year, month - 1, 1);
          
          // Check if invoice date is within range
          return invoiceDate >= start && invoiceDate <= end;
        });
      }

      const categoryMap = new Map<string, { amount: number; count: number }>();
      let totalAmount = 0;

      // Process bills (assuming they have a category or description we can use)
      bills.forEach((bill) => {
        const category = (bill as any).category || 'Contas';
        const installments = bill.numberOfInstallments || 1;
        const installmentValue = bill.totalAmount / installments;

        if (!categoryMap.has(category)) {
          categoryMap.set(category, { amount: 0, count: 0 });
        }

        const data = categoryMap.get(category)!;
        data.amount += installmentValue;
        data.count += 1;
        totalAmount += installmentValue;
      });

      // Process invoices
      invoices.forEach((invoice) => {
        const category = 'Cartão de Crédito';

        if (!categoryMap.has(category)) {
          categoryMap.set(category, { amount: 0, count: 0 });
        }

        const data = categoryMap.get(category)!;
        data.amount += invoice.totalAmount;
        data.count += 1;
        totalAmount += invoice.totalAmount;
      });

      // Convert to array with percentages
      return Array.from(categoryMap.entries())
        .map(([category, data]) => ({
          category,
          amount: data.amount,
          percentage: (data.amount / totalAmount) * 100,
          count: data.count,
        }))
        .sort((a, b) => b.amount - a.amount);
    } catch (error) {
      console.error('Error fetching category breakdown:', error);
      throw error;
    }
  }

  /**
   * Calcula métricas gerais de gastos
   */
  async getExpenseMetrics(year: number): Promise<ExpenseMetrics> {
    try {
      const monthlyExpenses = await this.getMonthlyExpenses(year);

      if (monthlyExpenses.length === 0) {
        return {
          totalExpenses: 0,
          averageMonthly: 0,
          highestMonth: { month: '', amount: 0 },
          lowestMonth: { month: '', amount: 0 },
          periodComparison: {
            current: 0,
            previous: 0,
            percentageChange: 0,
          },
        };
      }

      const totalExpenses = monthlyExpenses.reduce((sum, month) => sum + month.total, 0);
      const averageMonthly = totalExpenses / monthlyExpenses.length;

      const sortedByAmount = [...monthlyExpenses].sort((a, b) => b.total - a.total);
      const highestMonth = {
        month: sortedByAmount[0].month,
        amount: sortedByAmount[0].total,
      };
      const lowestMonth = {
        month: sortedByAmount[sortedByAmount.length - 1].month,
        amount: sortedByAmount[sortedByAmount.length - 1].total,
      };

      // Calculate period comparison (last 6 months vs previous 6 months)
      const currentPeriod = monthlyExpenses.slice(-6);
      const previousPeriod = monthlyExpenses.slice(-12, -6);

      const currentTotal = currentPeriod.reduce((sum, month) => sum + month.total, 0);
      const previousTotal = previousPeriod.reduce((sum, month) => sum + month.total, 0);

      const percentageChange =
        previousTotal > 0 ? ((currentTotal - previousTotal) / previousTotal) * 100 : 0;

      return {
        totalExpenses,
        averageMonthly,
        highestMonth,
        lowestMonth,
        periodComparison: {
          current: currentTotal,
          previous: previousTotal,
          percentageChange,
        },
      };
    } catch (error) {
      console.error('Error fetching expense metrics:', error);
      throw error;
    }
  }

  /**
   * Busca resumo consolidado de finanças
   */
  async getConsolidatedSummary() {
    try {
      // Fetch bills
      const billsResponse = await axiosInstance.get<PaginatedResponse<BillResponseDTO>>('/bills', { 
        params: { 
          size: 1000,
          page: 0,
        } 
      });

      // Fetch all credit cards
      const creditCards = await creditCardsService.getAll();

      // Fetch invoices for all credit cards in parallel
      const invoicePromises = creditCards.map(card => 
        invoicesService.getByCreditCard(card.id).catch(() => []) // Return empty array on error
      );
      
      const invoiceArrays = await Promise.all(invoicePromises);
      const allInvoices = invoiceArrays.flat();

      return {
        bills: billsResponse.data.content || [],
        invoices: allInvoices,
        creditCards: creditCards,
      };
    } catch (error) {
      console.error('Error fetching consolidated summary:', error);
      throw error;
    }
  }
}

export const reportsService = new ReportsService();
