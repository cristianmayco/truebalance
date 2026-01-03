import { axiosInstance } from '../lib/axios';
import type { BillResponseDTO } from '../types/dtos/bill.dto';
import type { InvoiceResponseDTO } from '../types/dtos/invoice.dto';

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
   * Calcula gastos mensais com base em contas e faturas
   */
  async getMonthlyExpenses(year: number): Promise<MonthlyExpense[]> {
    try {
      // Fetch bills and invoices for the year
      const [billsResponse, invoicesResponse] = await Promise.all([
        axiosInstance.get<BillResponseDTO[]>('/bills', {
          params: {
            year,
            pageSize: 1000, // Get all for the year
          },
        }),
        axiosInstance.get<InvoiceResponseDTO[]>('/invoices', {
          params: {
            year,
            pageSize: 1000,
          },
        }),
      ]);

      const bills = billsResponse.data;
      const invoices = invoicesResponse.data;

      // Group by month
      const monthlyData = new Map<string, MonthlyExpense>();

      // Process bills
      bills.forEach((bill) => {
        const date = new Date(bill.billDate);
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
        const installmentValue = bill.totalAmount / bill.installments;
        data.bills += installmentValue;
        data.total += installmentValue;
      });

      // Process invoices
      invoices.forEach((invoice) => {
        const date = new Date(invoice.referenceMonth);
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
        data.creditCards += invoice.totalAmount;
        data.total += invoice.totalAmount;
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
      if (startDate) params.startDate = startDate.toISOString();
      if (endDate) params.endDate = endDate.toISOString();

      const [billsResponse, invoicesResponse] = await Promise.all([
        axiosInstance.get<BillResponseDTO[]>('/bills', { params: { ...params, pageSize: 1000 } }),
        axiosInstance.get<InvoiceResponseDTO[]>('/invoices', { params: { ...params, pageSize: 1000 } }),
      ]);

      const bills = billsResponse.data;
      const invoices = invoicesResponse.data;

      const categoryMap = new Map<string, { amount: number; count: number }>();
      let totalAmount = 0;

      // Process bills (assuming they have a category or description we can use)
      bills.forEach((bill) => {
        const category = (bill as any).category || 'Contas';
        const installmentValue = bill.totalAmount / bill.installments;

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
      const [bills, invoices, creditCards] = await Promise.all([
        axiosInstance.get('/bills', { params: { pageSize: 1000 } }),
        axiosInstance.get('/invoices', { params: { pageSize: 1000 } }),
        axiosInstance.get('/credit-cards'),
      ]);

      return {
        bills: bills.data,
        invoices: invoices.data,
        creditCards: creditCards.data,
      };
    } catch (error) {
      console.error('Error fetching consolidated summary:', error);
      throw error;
    }
  }
}

export const reportsService = new ReportsService();
