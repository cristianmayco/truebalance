import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@/test/test-utils'
import '@testing-library/jest-dom/vitest'
import { InstallmentsTable } from './InstallmentsTable'
import { billsService } from '@/services/bills.service'
import type { InstallmentResponseDTO } from '@/types/dtos/installment.dto'
import type { BillResponseDTO } from '@/types/dtos/bill.dto'

// Mock bills service
vi.mock('@/services/bills.service', () => ({
  billsService: {
    getAll: vi.fn(),
  },
}))

// Mock useBills hook
vi.mock('@/hooks/useBills', () => ({
  useBills: vi.fn(),
}))

const mockBillsService = vi.mocked(billsService)

describe('InstallmentsTable', () => {
  const mockBills: BillResponseDTO[] = [
    {
      id: 1,
      name: 'Conta de Luz',
      description: 'Conta de energia elétrica',
      date: '2025-01-15T00:00:00',
      totalAmount: 150.0,
      numberOfInstallments: 1,
      installmentAmount: 150.0,
      isPaid: false,
      isRecurring: false,
      category: 'Utilidades',
      creditCardId: null,
    },
    {
      id: 2,
      name: 'Internet',
      description: 'Plano de internet',
      date: '2025-01-20T00:00:00',
      totalAmount: 100.0,
      numberOfInstallments: 1,
      installmentAmount: 100.0,
      isPaid: false,
      isRecurring: true,
      category: 'Serviços',
      creditCardId: null,
    },
    {
      id: 3,
      name: 'Compra no Cartão',
      description: 'Compra parcelada',
      date: '2025-01-10T00:00:00',
      totalAmount: 300.0,
      numberOfInstallments: 3,
      installmentAmount: 100.0,
      isPaid: false,
      isRecurring: false,
      category: 'Compras',
      creditCardId: 1,
    },
  ]

  const mockInstallments: InstallmentResponseDTO[] = [
    {
      id: 1,
      billId: 1,
      invoiceId: 1,
      installmentNumber: 1,
      amount: 150.0,
      dueDate: '2025-01-15',
      createdAt: '2025-01-01T00:00:00',
    },
    {
      id: 2,
      billId: 2,
      invoiceId: 1,
      installmentNumber: 1,
      amount: 100.0,
      dueDate: '2025-01-20',
      createdAt: '2025-01-01T00:00:00',
    },
    {
      id: 3,
      billId: 3,
      invoiceId: 1,
      installmentNumber: 1,
      amount: 100.0,
      dueDate: '2025-01-10',
      createdAt: '2025-01-01T00:00:00',
    },
    {
      id: 4,
      billId: 3,
      invoiceId: 2,
      installmentNumber: 2,
      amount: 100.0,
      dueDate: '2025-02-10',
      createdAt: '2025-01-01T00:00:00',
    },
  ]

  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('Rendering', () => {
    it('should render installments table with bill names', async () => {
      const { useBills } = await import('@/hooks/useBills')
      vi.mocked(useBills).mockReturnValue({
        data: {
          content: mockBills,
          totalElements: 3,
          totalPages: 1,
          size: 10,
          page: 0,
        },
        isLoading: false,
        isError: false,
        error: null,
        refetch: vi.fn(),
      } as any)

      render(<InstallmentsTable installments={mockInstallments} />)

      await waitFor(() => {
        expect(screen.getAllByText('Conta de Luz').length).toBeGreaterThan(0)
        expect(screen.getAllByText('Internet').length).toBeGreaterThan(0)
        expect(screen.getAllByText('Compra no Cartão').length).toBeGreaterThan(0)
      })
    })

    it('should display fallback "Conta #ID" when bill is not found', async () => {
      const { useBills } = await import('@/hooks/useBills')
      vi.mocked(useBills).mockReturnValue({
        data: {
          content: [mockBills[0]], // Only first bill
          totalElements: 1,
          totalPages: 1,
          size: 10,
          page: 0,
        },
        isLoading: false,
        isError: false,
        error: null,
        refetch: vi.fn(),
      } as any)

      render(<InstallmentsTable installments={mockInstallments} />)

      await waitFor(() => {
        expect(screen.getAllByText('Conta de Luz').length).toBeGreaterThan(0)
        expect(screen.getAllByText('Conta #2').length).toBeGreaterThan(0)
        expect(screen.getAllByText('Conta #3').length).toBeGreaterThan(0)
      })
    })

    it('should display installment numbers correctly', async () => {
      const { useBills } = await import('@/hooks/useBills')
      vi.mocked(useBills).mockReturnValue({
        data: {
          content: mockBills,
          totalElements: 3,
          totalPages: 1,
          size: 10,
          page: 0,
        },
        isLoading: false,
        isError: false,
        error: null,
        refetch: vi.fn(),
      } as any)

      render(<InstallmentsTable installments={mockInstallments} />)

      await waitFor(() => {
        // Check desktop table
        const installmentNumbers = screen.getAllByText('1')
        expect(installmentNumbers.length).toBeGreaterThan(0)
      })
    })

    it('should calculate and display total amount correctly', async () => {
      const { useBills } = await import('@/hooks/useBills')
      vi.mocked(useBills).mockReturnValue({
        data: {
          content: mockBills,
          totalElements: 3,
          totalPages: 1,
          size: 10,
          page: 0,
        },
        isLoading: false,
        isError: false,
        error: null,
        refetch: vi.fn(),
      } as any)

      render(<InstallmentsTable installments={mockInstallments} />)

      await waitFor(() => {
        // Total should be 450.0 (150 + 100 + 100 + 100)
        // Format can be "R$ 450,00" or "R$450,00"
        const totalElements = screen.getAllByText(/R\$\s*450[,.]00/i)
        expect(totalElements.length).toBeGreaterThan(0)
      })
    })
  })

  describe('Mobile View', () => {
    it('should render mobile cards with bill names', async () => {
      // Mock window.innerWidth to simulate mobile
      Object.defineProperty(window, 'innerWidth', {
        writable: true,
        configurable: true,
        value: 500,
      })

      const { useBills } = await import('@/hooks/useBills')
      vi.mocked(useBills).mockReturnValue({
        data: {
          content: mockBills,
          totalElements: 3,
          totalPages: 1,
          size: 10,
          page: 0,
        },
        isLoading: false,
        isError: false,
        error: null,
        refetch: vi.fn(),
      } as any)

      render(<InstallmentsTable installments={mockInstallments} />)

      await waitFor(() => {
        expect(screen.getAllByText('Conta de Luz').length).toBeGreaterThan(0)
        expect(screen.getAllByText('Internet').length).toBeGreaterThan(0)
      })
    })
  })

  describe('Loading State', () => {
    it('should handle loading state gracefully', async () => {
      const { useBills } = await import('@/hooks/useBills')
      vi.mocked(useBills).mockReturnValue({
        data: undefined,
        isLoading: true,
        isError: false,
        error: null,
        refetch: vi.fn(),
      } as any)

      render(<InstallmentsTable installments={mockInstallments} />)

      // Should still render installments even if bills are loading
      await waitFor(() => {
        expect(screen.getAllByText('Conta #1').length).toBeGreaterThan(0)
      })
    })
  })

  describe('Empty State', () => {
    it('should handle empty installments array', async () => {
      const { useBills } = await import('@/hooks/useBills')
      vi.mocked(useBills).mockReturnValue({
        data: {
          content: mockBills,
          totalElements: 3,
          totalPages: 1,
          size: 10,
          page: 0,
        },
        isLoading: false,
        isError: false,
        error: null,
        refetch: vi.fn(),
      } as any)

      render(<InstallmentsTable installments={[]} />)

      await waitFor(() => {
        // Should render total even with empty array
        const totalElements = screen.getAllByText('Total')
        expect(totalElements.length).toBeGreaterThan(0)
        // Total should be R$ 0,00 (format can vary)
        expect(screen.getAllByText(/R\$\s*0[,.]00/i).length).toBeGreaterThan(0)
      })
    })
  })

  describe('Bill Name Mapping', () => {
    it('should correctly map bill IDs to names', async () => {
      const { useBills } = await import('@/hooks/useBills')
      vi.mocked(useBills).mockReturnValue({
        data: {
          content: mockBills,
          totalElements: 3,
          totalPages: 1,
          size: 10,
          page: 0,
        },
        isLoading: false,
        isError: false,
        error: null,
        refetch: vi.fn(),
      } as any)

      render(<InstallmentsTable installments={mockInstallments} />)

      await waitFor(() => {
        // Verify all bill names are displayed correctly
        expect(screen.getAllByText('Conta de Luz').length).toBeGreaterThan(0)
        expect(screen.getAllByText('Internet').length).toBeGreaterThan(0)
        expect(screen.getAllByText('Compra no Cartão').length).toBeGreaterThan(0)
      })
    })

    it('should handle multiple installments from same bill', async () => {
      const { useBills } = await import('@/hooks/useBills')
      vi.mocked(useBills).mockReturnValue({
        data: {
          content: mockBills,
          totalElements: 3,
          totalPages: 1,
          size: 10,
          page: 0,
        },
        isLoading: false,
        isError: false,
        error: null,
        refetch: vi.fn(),
      } as any)

      // Installments 3 and 4 are from the same bill (billId: 3)
      render(<InstallmentsTable installments={mockInstallments} />)

      await waitFor(() => {
        const billNames = screen.getAllByText('Compra no Cartão')
        expect(billNames.length).toBeGreaterThanOrEqual(1)
      })
    })
  })
})
