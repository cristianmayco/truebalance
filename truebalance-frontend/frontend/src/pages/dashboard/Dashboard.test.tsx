import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@/test/test-utils'
import userEvent from '@testing-library/user-event'
import '@testing-library/jest-dom/vitest'
import { Dashboard } from './Dashboard'
import { reportsService } from '@/services/reports.service'
import * as reactRouterDom from 'react-router-dom'

// Mock reports service
vi.mock('@/services/reports.service', () => ({
  reportsService: {
    getMonthlyExpensesByPeriod: vi.fn(),
  },
}))

// Mock router
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')
  return {
    ...actual,
    useNavigate: () => vi.fn(),
  }
})

const mockReportsService = vi.mocked(reportsService)

describe('Dashboard', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('Rendering', () => {
    it('should render dashboard title and description', async () => {
      mockReportsService.getMonthlyExpensesByPeriod.mockResolvedValue([])

      render(<Dashboard />)

      expect(screen.getByText('Dashboard')).toBeInTheDocument()
      expect(screen.getByText(/Visão geral das suas finanças/i)).toBeInTheDocument()
    })

    it('should render period filter select', async () => {
      mockReportsService.getMonthlyExpensesByPeriod.mockResolvedValue([])

      render(<Dashboard />)

      const select = screen.getByRole('combobox')
      expect(select).toBeInTheDocument()
      expect(select).toHaveValue('12')
    })

    it('should render "Nova Conta" button', async () => {
      mockReportsService.getMonthlyExpensesByPeriod.mockResolvedValue([])

      render(<Dashboard />)

      const button = screen.getByRole('button', { name: /nova conta/i })
      expect(button).toBeInTheDocument()
    })

    it('should render summary cards', async () => {
      mockReportsService.getMonthlyExpensesByPeriod.mockResolvedValue([])

      render(<Dashboard />)

      await waitFor(() => {
        expect(screen.getByText(/Total no Período/i)).toBeInTheDocument()
        expect(screen.getByText(/Gastos do Mês/i)).toBeInTheDocument()
        expect(screen.getByText(/Média Mensal/i)).toBeInTheDocument()
      })
    })
  })

  describe('Period Filter', () => {
    it('should have correct period options', async () => {
      mockReportsService.getMonthlyExpensesByPeriod.mockResolvedValue([])

      render(<Dashboard />)

      const select = screen.getByRole('combobox')
      const options = Array.from(select.querySelectorAll('option')).map(opt => opt.textContent)

      expect(options).toContain('Últimos 12 meses')
      expect(options).toContain('Últimos 2 anos')
      expect(options).toContain('Últimos 5 anos')
      expect(options).toContain('Últimos 10 anos')
    })

    it('should change period when selecting different option', async () => {
      const user = userEvent.setup()
      mockReportsService.getMonthlyExpensesByPeriod.mockResolvedValue([])

      render(<Dashboard />)

      const select = screen.getByRole('combobox')
      await user.selectOptions(select, '24')

      expect(mockReportsService.getMonthlyExpensesByPeriod).toHaveBeenCalledWith(24)
    })

    it('should fetch data with correct period', async () => {
      mockReportsService.getMonthlyExpensesByPeriod.mockResolvedValue([])

      render(<Dashboard />)

      await waitFor(() => {
        expect(mockReportsService.getMonthlyExpensesByPeriod).toHaveBeenCalledWith(12)
      })
    })
  })

  describe('Monthly Cards', () => {
    it('should display monthly expense cards', async () => {
      const mockExpenses = [
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
      ]

      mockReportsService.getMonthlyExpensesByPeriod.mockResolvedValue(mockExpenses)

      render(<Dashboard />)

      await waitFor(() => {
        expect(screen.getByText(/Gastos por Mês/i)).toBeInTheDocument()
      })

      // Check if month names are displayed
      expect(screen.getByText(/fevereiro/i)).toBeInTheDocument()
      expect(screen.getByText(/janeiro/i)).toBeInTheDocument()
    })

    it('should display empty state when no expenses', async () => {
      mockReportsService.getMonthlyExpensesByPeriod.mockResolvedValue([])

      render(<Dashboard />)

      await waitFor(() => {
        expect(screen.getByText(/Nenhum gasto encontrado/i)).toBeInTheDocument()
      })
    })

    it('should display breakdown of bills and credit cards', async () => {
      const mockExpenses = [
        {
          month: 'janeiro',
          year: 2024,
          bills: 1000,
          creditCards: 500,
          total: 1500,
        },
      ]

      mockReportsService.getMonthlyExpensesByPeriod.mockResolvedValue(mockExpenses)

      render(<Dashboard />)

      await waitFor(() => {
        expect(screen.getByText(/Contas/i)).toBeInTheDocument()
        expect(screen.getByText(/Cartões/i)).toBeInTheDocument()
      })
    })

    it('should display percentages correctly', async () => {
      const mockExpenses = [
        {
          month: 'janeiro',
          year: 2024,
          bills: 1000,
          creditCards: 500,
          total: 1500,
        },
      ]

      mockReportsService.getMonthlyExpensesByPeriod.mockResolvedValue(mockExpenses)

      render(<Dashboard />)

      await waitFor(() => {
        // Should show percentages (66.7% for bills, 33.3% for credit cards)
        const billsPercentage = (1000 / 1500 * 100).toFixed(1)
        expect(screen.getByText(new RegExp(`${billsPercentage}%`))).toBeInTheDocument()
      })
    })
  })

  describe('Summary Metrics', () => {
    it('should calculate total amount correctly', async () => {
      const mockExpenses = [
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
      ]

      mockReportsService.getMonthlyExpensesByPeriod.mockResolvedValue(mockExpenses)

      render(<Dashboard />)

      await waitFor(() => {
        // Total should be 3000 (1500 + 1500)
        expect(screen.getByText(/R\$\s*3\.000,00/i)).toBeInTheDocument()
      })
    })

    it('should calculate average monthly correctly', async () => {
      const mockExpenses = [
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
      ]

      mockReportsService.getMonthlyExpensesByPeriod.mockResolvedValue(mockExpenses)

      render(<Dashboard />)

      await waitFor(() => {
        // Average should be 1500 ((1500 + 1500) / 2)
        expect(screen.getByText(/R\$\s*1\.500,00/i)).toBeInTheDocument()
      })
    })

    it('should display current month total', async () => {
      const currentDate = new Date()
      const currentMonth = currentDate.toLocaleDateString('pt-BR', { month: 'long' })
      const currentYear = currentDate.getFullYear()

      const mockExpenses = [
        {
          month: currentMonth,
          year: currentYear,
          bills: 1000,
          creditCards: 500,
          total: 1500,
        },
      ]

      mockReportsService.getMonthlyExpensesByPeriod.mockResolvedValue(mockExpenses)

      render(<Dashboard />)

      await waitFor(() => {
        expect(screen.getByText(/R\$\s*1\.500,00/i)).toBeInTheDocument()
      })
    })
  })

  describe('Loading State', () => {
    it('should show loading spinner while fetching data', async () => {
      mockReportsService.getMonthlyExpensesByPeriod.mockImplementation(
        () => new Promise(() => {}) // Never resolves
      )

      render(<Dashboard />)

      // Should show loading spinner
      const spinner = screen.getByRole('status', { hidden: true })
      expect(spinner).toBeInTheDocument()
    })
  })

  describe('Navigation', () => {
    it('should navigate to new bill page when clicking "Nova Conta"', async () => {
      const user = userEvent.setup()
      const mockNavigate = vi.fn()
      vi.spyOn(reactRouterDom, 'useNavigate').mockReturnValue(mockNavigate)

      mockReportsService.getMonthlyExpensesByPeriod.mockResolvedValue([])

      render(<Dashboard />)

      const button = screen.getByRole('button', { name: /nova conta/i })
      await user.click(button)

      expect(mockNavigate).toHaveBeenCalledWith('/bills/new')
    })

    it('should navigate to bills list when clicking "Ver todas as contas"', async () => {
      const user = userEvent.setup()
      const mockNavigate = vi.fn()
      vi.spyOn(reactRouterDom, 'useNavigate').mockReturnValue(mockNavigate)

      const mockExpenses = [
        {
          month: 'janeiro',
          year: 2024,
          bills: 1000,
          creditCards: 500,
          total: 1500,
        },
      ]

      mockReportsService.getMonthlyExpensesByPeriod.mockResolvedValue(mockExpenses)

      render(<Dashboard />)

      await waitFor(() => {
        const button = screen.getByRole('button', { name: /ver todas as contas/i })
        expect(button).toBeInTheDocument()
      })

      const button = screen.getByRole('button', { name: /ver todas as contas/i })
      await user.click(button)

      expect(mockNavigate).toHaveBeenCalledWith('/bills')
    })
  })
})
