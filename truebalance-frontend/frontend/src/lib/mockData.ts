/**
 * Mock Data for Demo Mode
 *
 * Realistic sample data for demonstrating the application without backend.
 * Used when demo mode is enabled.
 */

import type { BillResponseDTO } from '../types/dtos/bill.dto';
import type { CreditCardResponseDTO } from '../types/dtos/creditCard.dto';
import type { InvoiceResponseDTO } from '../types/dtos/invoice.dto';

// Mock Bills
export const mockBills: BillResponseDTO[] = [
  {
    id: 1,
    name: 'Aluguel',
    description: 'Aluguel mensal do apartamento',
    executionDate: '2025-01-05',
    totalAmount: 2500.00,
    numberOfInstallments: 12,
    installmentAmount: 2500.00,
    isPaid: false,
    createdAt: '2025-01-01T10:00:00Z',
    updatedAt: '2025-01-01T10:00:00Z',
  },
  {
    id: 2,
    name: 'Notebook Dell',
    description: 'Notebook para trabalho remoto',
    executionDate: '2024-12-15',
    totalAmount: 4800.00,
    numberOfInstallments: 12,
    installmentAmount: 400.00,
    isPaid: false,
    createdAt: '2024-12-01T14:30:00Z',
    updatedAt: '2024-12-01T14:30:00Z',
  },
  {
    id: 3,
    name: 'Academia',
    description: 'Mensalidade Smart Fit',
    executionDate: '2025-01-10',
    totalAmount: 99.90,
    numberOfInstallments: 1,
    installmentAmount: 99.90,
    isPaid: false,
    createdAt: '2025-01-01T09:00:00Z',
    updatedAt: '2025-01-01T09:00:00Z',
  },
  {
    id: 4,
    name: 'Curso de React',
    description: 'Udemy - React Avançado',
    executionDate: '2024-11-20',
    totalAmount: 599.90,
    numberOfInstallments: 6,
    installmentAmount: 99.98,
    isPaid: false,
    createdAt: '2024-11-15T16:00:00Z',
    updatedAt: '2024-11-15T16:00:00Z',
  },
  {
    id: 5,
    name: 'Seguro Saúde',
    description: 'Plano de saúde Unimed',
    executionDate: '2025-01-15',
    totalAmount: 650.00,
    numberOfInstallments: 12,
    installmentAmount: 650.00,
    isPaid: false,
    createdAt: '2025-01-01T11:00:00Z',
    updatedAt: '2025-01-01T11:00:00Z',
  },
  {
    id: 6,
    name: 'Netflix Premium',
    description: 'Assinatura mensal',
    executionDate: '2025-01-08',
    totalAmount: 55.90,
    numberOfInstallments: 1,
    installmentAmount: 55.90,
    isPaid: true,
    createdAt: '2025-01-01T08:00:00Z',
    updatedAt: '2025-01-08T20:00:00Z',
  },
];

// Mock Credit Cards
export const mockCreditCards: CreditCardResponseDTO[] = [
  {
    id: 1,
    name: 'Nubank Ultravioleta',
    creditLimit: 10000.00,
    limit: 10000.00,
    availableLimit: 6150.00,
    closingDay: 10,
    dueDay: 17,
    allowsPartialPayment: true,
    createdAt: '2024-01-01T10:00:00Z',
    updatedAt: '2024-01-01T10:00:00Z',
  },
  {
    id: 2,
    name: 'Inter Gold',
    creditLimit: 5000.00,
    limit: 5000.00,
    availableLimit: 3800.00,
    closingDay: 5,
    dueDay: 12,
    allowsPartialPayment: true,
    createdAt: '2024-03-15T14:00:00Z',
    updatedAt: '2024-03-15T14:00:00Z',
  },
  {
    id: 3,
    name: 'C6 Bank Carbon',
    creditLimit: 8000.00,
    limit: 8000.00,
    availableLimit: 7020.00,
    closingDay: 15,
    dueDay: 22,
    allowsPartialPayment: false,
    createdAt: '2024-06-10T09:00:00Z',
    updatedAt: '2024-06-10T09:00:00Z',
  },
];

// Mock Invoices
export const mockInvoices: InvoiceResponseDTO[] = [
  {
    id: 1,
    creditCardId: 1,
    referenceMonth: '2025-01-01',
    totalAmount: 3850.00,
    previousBalance: 0,
    closed: true,
    paid: false,
    createdAt: '2025-01-10T23:59:00Z',
    updatedAt: '2025-01-10T23:59:00Z',
  },
  {
    id: 2,
    creditCardId: 1,
    referenceMonth: '2024-12-01',
    totalAmount: 2150.00,
    previousBalance: 0,
    closed: true,
    paid: true,
    createdAt: '2024-12-10T23:59:00Z',
    updatedAt: '2024-12-16T10:00:00Z',
  },
  {
    id: 3,
    creditCardId: 2,
    referenceMonth: '2025-01-01',
    totalAmount: 1200.00,
    previousBalance: 0,
    closed: true,
    paid: false,
    createdAt: '2025-01-05T23:59:00Z',
    updatedAt: '2025-01-05T23:59:00Z',
  },
  {
    id: 4,
    creditCardId: 3,
    referenceMonth: '2025-01-01',
    totalAmount: 980.00,
    previousBalance: 0,
    closed: false,
    paid: false,
    createdAt: '2025-01-15T23:59:00Z',
    updatedAt: '2025-01-15T23:59:00Z',
  },
];

// Helper to get available limit
export function getMockAvailableLimit(cardId: number): number {
  const card = mockCreditCards.find(c => c.id === cardId);
  if (!card) return 0;

  const invoice = mockInvoices.find(
    i => i.creditCardId === cardId && !i.paid && i.closed
  );

  const used = invoice?.totalAmount || 0;
  return card.limit - used;
}

// Helper to get current month invoice
export function getMockCurrentInvoice(cardId: number): InvoiceResponseDTO | null {
  return mockInvoices.find(
    i => i.creditCardId === cardId && !i.closed
  ) || null;
}

// Helper to filter bills by query params
export function filterMockBills(params?: {
  search?: string;
  isPaid?: boolean;
  page?: number;
  limit?: number;
}): BillResponseDTO[] {
  let filtered = [...mockBills];

  if (params?.search) {
    const search = params.search.toLowerCase();
    filtered = filtered.filter(
      b => b.name.toLowerCase().includes(search) ||
           b.description?.toLowerCase().includes(search)
    );
  }

  if (params?.isPaid !== undefined) {
    filtered = filtered.filter(b => b.isPaid === params.isPaid);
  }

  // Pagination
  const page = params?.page || 1;
  const limit = params?.limit || 10;
  const start = (page - 1) * limit;
  const end = start + limit;

  return filtered.slice(start, end);
}

// Helper to generate expenses data for reports
export function getMockMonthlyExpenses(year: number = 2025) {
  const months = [
    'janeiro', 'fevereiro', 'março', 'abril', 'maio', 'junho',
    'julho', 'agosto', 'setembro', 'outubro', 'novembro', 'dezembro'
  ];

  return months.map((month, index) => ({
    month,
    monthNumber: index + 1,
    year,
    totalExpenses: Math.random() * 5000 + 2000, // 2000-7000
    billsCount: Math.floor(Math.random() * 10) + 5, // 5-15
  }));
}

// Helper to generate category breakdown
export function getMockCategoryBreakdown() {
  return [
    { category: 'Moradia', amount: 2500, percentage: 35, count: 1 },
    { category: 'Saúde', amount: 650, percentage: 9, count: 1 },
    { category: 'Educação', amount: 600, percentage: 8, count: 1 },
    { category: 'Tecnologia', amount: 400, percentage: 6, count: 1 },
    { category: 'Lazer', amount: 156, percentage: 2, count: 2 },
    { category: 'Compras', amount: 3850, percentage: 40, count: 15 },
  ];
}

/**
 * Demo mode flag (stored in localStorage)
 */
export const DEMO_MODE_KEY = 'truebalance_demo_mode';

export function isDemoMode(): boolean {
  return localStorage.getItem(DEMO_MODE_KEY) === 'true';
}

export function setDemoMode(enabled: boolean): void {
  localStorage.setItem(DEMO_MODE_KEY, enabled ? 'true' : 'false');
}

export function toggleDemoMode(): boolean {
  const newValue = !isDemoMode();
  setDemoMode(newValue);
  return newValue;
}
