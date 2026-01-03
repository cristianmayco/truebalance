import { describe, it, expect, vi, beforeEach } from 'vitest'
import { reportsService } from './reports.service'
import { axiosInstance } from '@/lib/axios'
import type { BillResponseDTO } from '@/types/dtos/bill.dto'
import type { InvoiceResponseDTO } from '@/types/dtos/invoice.dto'
import type { PaginatedResponse } from '@/types/dtos/common.dto'

// Mock axios
const mockGet = vi.fn()
vi.mock('@/lib/axios', () => ({
  axiosInstance: {
    get: mockGet,
  },
}))

const mockAxiosInstance = {
  get: mockGet,
} as any

describe('ReportsService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getMonthlyExpenses', () => {
    it('should fetch and group monthly expenses correctly', async () => {
      const mockBills: PaginatedResponse<BillResponseDTO> = {
        content: [
          {
            id: 1,
            name: 'Conta 1',
            executionDate: '2024-01-15T10:00:00',
            totalAmount: 1000,
            numberOfInstallments: 2,
            installmentAmount: 500,
            description: null,
            createdAt: '2024-01-01T00:00:00',
            updatedAt: '2024-01-01T00:00:00',
          },
          {
            id: 2,
            name: 'Conta 2',
            executionDate: '2024-01-20T10:00:00',
            totalAmount: 500,
            numberOfInstallments: 1,
            installmentAmount: 500,
            description: null,
            createdAt: '2024-01-01T00:00:00',
            updatedAt: '2024-01-01T00:00:00',
          },
        ],
        page: 0,
        size: 1000,
        totalElements: 2,
        totalPages: 1,
      }

      const mockInvoices: PaginatedResponse<InvoiceResponseDTO> = {
        content: [
          {
            id: 1,
            creditCardId: 1,
            referenceMonth: '2024-01-01',
            totalAmount: 800,
            previousBalance: 0,
            closed: true,
            paid: false,
            createdAt: '2024-01-01T00:00:00',
            updatedAt: '2024-01-01T00:00:00',
          },
        ],
        page: 0,
        size: 1000,
        totalElements: 1,
        totalPages: 1,
      }

      mockGet
        .mockResolvedValueOnce({ data: mockBills })
        .mockResolvedValueOnce({ data: mockInvoices })

      const result = await reportsService.getMonthlyExpenses(2024)

      expect(mockGet).toHaveBeenCalledTimes(2)
      expect(result).toHaveLength(1)
      expect(result[0].month).toBe('janeiro')
      expect(result[0].year).toBe(2024)
      expect(result[0].bills).toBe(1000) // (1000/2) + (500/1) = 500 + 500 = 1000
      expect(result[0].creditCards).toBe(800)
      expect(result[0].total).toBe(1800)
    })

    it('should handle bills with different date formats', async () => {
      const mockBills: PaginatedResponse<BillResponseDTO> = {
        content: [
          {
            id: 1,
            name: 'Conta 1',
            executionDate: '2024-02-15T10:00:00',
            date: '2024-02-15T10:00:00',
            billDate: '2024-02-15T10:00:00',
            totalAmount: 1000,
            numberOfInstallments: 1,
            installmentAmount: 1000,
            description: null,
            createdAt: '2024-01-01T00:00:00',
            updatedAt: '2024-01-01T00:00:00',
          },
        ],
        page: 0,
        size: 1000,
        totalElements: 1,
        totalPages: 1,
      }

      const mockInvoices: PaginatedResponse<InvoiceResponseDTO> = {
        content: [],
        page: 0,
        size: 1000,
        totalElements: 0,
        totalPages: 0,
      }

      mockGet
        .mockResolvedValueOnce({ data: mockBills })
        .mockResolvedValueOnce({ data: mockInvoices })

      const result = await reportsService.getMonthlyExpenses(2024)

      expect(result).toHaveLength(1)
      expect(result[0].bills).toBe(1000)
    })

    it('should handle empty data', async () => {
      const mockBills: PaginatedResponse<BillResponseDTO> = {
        content: [],
        page: 0,
        size: 1000,
        totalElements: 0,
        totalPages: 0,
      }

      const mockInvoices: PaginatedResponse<InvoiceResponseDTO> = {
        content: [],
        page: 0,
        size: 1000,
        totalElements: 0,
        totalPages: 0,
      }

      mockGet
        .mockResolvedValueOnce({ data: mockBills })
        .mockResolvedValueOnce({ data: mockInvoices })

      const result = await reportsService.getMonthlyExpenses(2024)

      expect(result).toHaveLength(0)
    })

    it('should use custom date range when provided', async () => {
      const startDate = new Date(2024, 5, 1) // June 2024
      const endDate = new Date(2024, 6, 30) // July 2024

      mockGet
        .mockResolvedValueOnce({ data: { content: [], page: 0, size: 1000, totalElements: 0, totalPages: 0 } })
        .mockResolvedValueOnce({ data: { content: [], page: 0, size: 1000, totalElements: 0, totalPages: 0 } })

      await reportsService.getMonthlyExpenses(2024, startDate, endDate)

      expect(mockGet).toHaveBeenCalledWith('/bills', {
        params: {
          startDate: startDate.toISOString(),
          endDate: endDate.toISOString(),
          size: 1000,
          page: 0,
        },
      })
    })
  })

  describe('getMonthlyExpensesByPeriod', () => {
    it('should fetch expenses for the last N months', async () => {
      const mockBills: PaginatedResponse<BillResponseDTO> = {
        content: [
          {
            id: 1,
            name: 'Conta 1',
            executionDate: new Date().toISOString(),
            totalAmount: 1000,
            numberOfInstallments: 1,
            installmentAmount: 1000,
            description: null,
            createdAt: '2024-01-01T00:00:00',
            updatedAt: '2024-01-01T00:00:00',
          },
        ],
        page: 0,
        size: 1000,
        totalElements: 1,
        totalPages: 1,
      }

      const mockInvoices: PaginatedResponse<InvoiceResponseDTO> = {
        content: [],
        page: 0,
        size: 1000,
        totalElements: 0,
        totalPages: 0,
      }

      mockGet
        .mockResolvedValueOnce({ data: mockBills })
        .mockResolvedValueOnce({ data: mockInvoices })

      const result = await reportsService.getMonthlyExpensesByPeriod(12)

      expect(mockGet).toHaveBeenCalledTimes(2)
      expect(result).toBeDefined()
    })

    it('should calculate correct date range for 12 months', async () => {
      mockGet
        .mockResolvedValueOnce({ data: { content: [], page: 0, size: 1000, totalElements: 0, totalPages: 0 } })
        .mockResolvedValueOnce({ data: { content: [], page: 0, size: 1000, totalElements: 0, totalPages: 0 } })

      await reportsService.getMonthlyExpensesByPeriod(12)

      const calls = mockGet.mock.calls as any[]
      const billsCall = calls.find((call: any[]) => call[0] === '/bills')
      
      expect(billsCall).toBeDefined()
      if (billsCall) {
        const params = billsCall[1]?.params
        expect(params?.size).toBe(1000)
        expect(params?.page).toBe(0)
        expect(params?.startDate).toBeDefined()
        expect(params?.endDate).toBeDefined()
      }
    })
  })

  describe('getCategoryBreakdown', () => {
    it('should group expenses by category', async () => {
      const mockBills: PaginatedResponse<BillResponseDTO> = {
        content: [
          {
            id: 1,
            name: 'Internet',
            executionDate: '2024-01-15T10:00:00',
            totalAmount: 1000,
            numberOfInstallments: 1,
            installmentAmount: 1000,
            description: null,
            createdAt: '2024-01-01T00:00:00',
            updatedAt: '2024-01-01T00:00:00',
          } as any,
        ],
        page: 0,
        size: 1000,
        totalElements: 1,
        totalPages: 1,
      }

      const mockInvoices: PaginatedResponse<InvoiceResponseDTO> = {
        content: [
          {
            id: 1,
            creditCardId: 1,
            referenceMonth: '2024-01-01',
            totalAmount: 500,
            previousBalance: 0,
            closed: true,
            paid: false,
            createdAt: '2024-01-01T00:00:00',
            updatedAt: '2024-01-01T00:00:00',
          },
        ],
        page: 0,
        size: 1000,
        totalElements: 1,
        totalPages: 1,
      }

      mockGet
        .mockResolvedValueOnce({ data: mockBills })
        .mockResolvedValueOnce({ data: mockInvoices })

      const result = await reportsService.getCategoryBreakdown()

      expect(result).toBeDefined()
      expect(Array.isArray(result)).toBe(true)
      
      // Should have at least 'Contas' and 'Cartão de Crédito' categories
      const categories = result.map(r => r.category)
      expect(categories).toContain('Cartão de Crédito')
    })

    it('should calculate percentages correctly', async () => {
      const mockBills: PaginatedResponse<BillResponseDTO> = {
        content: [
          {
            id: 1,
            name: 'Conta',
            executionDate: '2024-01-15T10:00:00',
            totalAmount: 1000,
            numberOfInstallments: 1,
            installmentAmount: 1000,
            description: null,
            createdAt: '2024-01-01T00:00:00',
            updatedAt: '2024-01-01T00:00:00',
          } as any,
        ],
        page: 0,
        size: 1000,
        totalElements: 1,
        totalPages: 1,
      }

      const mockInvoices: PaginatedResponse<InvoiceResponseDTO> = {
        content: [
          {
            id: 1,
            creditCardId: 1,
            referenceMonth: '2024-01-01',
            totalAmount: 1000,
            previousBalance: 0,
            closed: true,
            paid: false,
            createdAt: '2024-01-01T00:00:00',
            updatedAt: '2024-01-01T00:00:00',
          },
        ],
        page: 0,
        size: 1000,
        totalElements: 1,
        totalPages: 1,
      }

      mockGet
        .mockResolvedValueOnce({ data: mockBills })
        .mockResolvedValueOnce({ data: mockInvoices })

      const result = await reportsService.getCategoryBreakdown()

      const totalPercentage = result.reduce((sum, cat) => sum + cat.percentage, 0)
      expect(totalPercentage).toBeCloseTo(100, 1)
    })

    it('should filter by date range when provided', async () => {
      const startDate = new Date(2024, 0, 1)
      const endDate = new Date(2024, 11, 31)

      mockGet
        .mockResolvedValueOnce({ data: { content: [], page: 0, size: 1000, totalElements: 0, totalPages: 0 } })
        .mockResolvedValueOnce({ data: { content: [], page: 0, size: 1000, totalElements: 0, totalPages: 0 } })

      await reportsService.getCategoryBreakdown(startDate, endDate)

      const calls = mockGet.mock.calls as any[]
      const billsCall = calls.find((call: any[]) => call[0] === '/bills')
      
      expect(billsCall?.[1]?.params?.startDate).toBe(startDate.toISOString())
      expect(billsCall?.[1]?.params?.endDate).toBe(endDate.toISOString())
    })
  })

  describe('getExpenseMetrics', () => {
    it('should calculate metrics correctly', async () => {
      // Mock getMonthlyExpenses to return test data
      const mockMonthlyExpenses = [
        {
          month: 'janeiro',
          year: 2024,
          bills: 1000,
          creditCards: 500,
          total: 1500,
        },
        {
          month: 'fevereiro',
          year: 2024,
          bills: 800,
          creditCards: 700,
          total: 1500,
        },
        {
          month: 'março',
          year: 2024,
          bills: 1200,
          creditCards: 300,
          total: 1500,
        },
      ]

      vi.spyOn(reportsService, 'getMonthlyExpenses').mockResolvedValue(mockMonthlyExpenses)

      const result = await reportsService.getExpenseMetrics(2024)

      expect(result.totalExpenses).toBe(4500)
      expect(result.averageMonthly).toBe(1500)
      expect(result.highestMonth.amount).toBe(1500)
      expect(result.lowestMonth.amount).toBe(1500)
    })

    it('should handle empty data', async () => {
      vi.spyOn(reportsService, 'getMonthlyExpenses').mockResolvedValue([])

      const result = await reportsService.getExpenseMetrics(2024)

      expect(result.totalExpenses).toBe(0)
      expect(result.averageMonthly).toBe(0)
      expect(result.periodComparison.percentageChange).toBe(0)
    })
  })

  describe('getConsolidatedSummary', () => {
    it('should fetch all consolidated data', async () => {
      const mockBills: PaginatedResponse<BillResponseDTO> = {
        content: [
          {
            id: 1,
            name: 'Conta 1',
            executionDate: '2024-01-15T10:00:00',
            totalAmount: 1000,
            numberOfInstallments: 1,
            installmentAmount: 1000,
            description: null,
            createdAt: '2024-01-01T00:00:00',
            updatedAt: '2024-01-01T00:00:00',
          },
        ],
        page: 0,
        size: 1000,
        totalElements: 1,
        totalPages: 1,
      }

      const mockInvoices: PaginatedResponse<InvoiceResponseDTO> = {
        content: [],
        page: 0,
        size: 1000,
        totalElements: 0,
        totalPages: 0,
      }

      const mockCreditCards = [
        {
          id: 1,
          name: 'Cartão 1',
          limit: 5000,
          closingDay: 10,
          dueDay: 20,
          allowsPartialPayment: true,
          createdAt: '2024-01-01T00:00:00',
          updatedAt: '2024-01-01T00:00:00',
        },
      ]

      mockGet
        .mockResolvedValueOnce({ data: mockBills })
        .mockResolvedValueOnce({ data: mockInvoices })
        .mockResolvedValueOnce({ data: mockCreditCards })

      const result = await reportsService.getConsolidatedSummary()

      expect(result.bills).toHaveLength(1)
      expect(result.invoices).toHaveLength(0)
      expect(result.creditCards).toHaveLength(1)
      expect(mockGet).toHaveBeenCalledTimes(3)
    })

    it('should handle empty responses', async () => {
      mockGet
        .mockResolvedValueOnce({ data: { content: [], page: 0, size: 1000, totalElements: 0, totalPages: 0 } })
        .mockResolvedValueOnce({ data: { content: [], page: 0, size: 1000, totalElements: 0, totalPages: 0 } })
        .mockResolvedValueOnce({ data: [] })

      const result = await reportsService.getConsolidatedSummary()

      expect(result.bills).toHaveLength(0)
      expect(result.invoices).toHaveLength(0)
      expect(result.creditCards).toHaveLength(0)
    })
  })
})
