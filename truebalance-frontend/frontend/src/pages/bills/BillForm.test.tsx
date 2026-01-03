import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@/test/test-utils'
import userEvent from '@testing-library/user-event'
import '@testing-library/jest-dom/vitest'
import { BillForm } from './BillForm'
import { useBill, useCreateBill, useUpdateBill } from '@/hooks/useBills'
import { useCreditCards } from '@/hooks/useCreditCards'
import { useToast } from '@/contexts/ToastContext'
import * as reactRouterDom from 'react-router-dom'

// Mock hooks
vi.mock('@/hooks/useBills')
vi.mock('@/hooks/useCreditCards')
vi.mock('@/contexts/ToastContext')
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')
  return {
    ...actual,
    useNavigate: () => vi.fn(),
  }
})

const mockUseBill = vi.mocked(useBill)
const mockUseCreateBill = vi.mocked(useCreateBill)
const mockUseUpdateBill = vi.mocked(useUpdateBill)
const mockUseCreditCards = vi.mocked(useCreditCards)
const mockUseToast = vi.mocked(useToast)

describe('BillForm', () => {
  const mockShowToast = vi.fn()
  const mockCreateBill = vi.fn()
  const mockUpdateBill = vi.fn()

  beforeEach(() => {
    vi.clearAllMocks()
    
    mockUseToast.mockReturnValue({
      showToast: mockShowToast,
    } as any)

    mockUseCreditCards.mockReturnValue({
      data: [],
      isLoading: false,
    } as any)

    mockUseBill.mockReturnValue({
      data: undefined,
      isLoading: false,
    } as any)

    mockUseCreateBill.mockReturnValue({
      mutate: mockCreateBill,
      isPending: false,
    } as any)

    mockUseUpdateBill.mockReturnValue({
      mutate: mockUpdateBill,
      isPending: false,
    } as any)
  })

  describe('Creating new bill', () => {
    it('renders form fields', () => {
      render(<BillForm />)
      
      expect(screen.getByLabelText(/nome da conta/i)).toBeInTheDocument()
      expect(screen.getByLabelText(/data da conta/i)).toBeInTheDocument()
      expect(screen.getByLabelText(/valor total/i)).toBeInTheDocument()
      expect(screen.getByLabelText(/número de parcelas/i)).toBeInTheDocument()
      expect(screen.getByLabelText(/descrição/i)).toBeInTheDocument()
    })

    it('renders recurring checkbox', () => {
      render(<BillForm />)
      
      const checkbox = screen.getByLabelText(/conta recorrente/i)
      expect(checkbox).toBeInTheDocument()
      expect(checkbox).not.toBeChecked()
    })

    it('allows user to check recurring checkbox', async () => {
      const user = userEvent.setup()
      render(<BillForm />)
      
      const checkbox = screen.getByLabelText(/conta recorrente/i)
      await user.click(checkbox)
      
      expect(checkbox).toBeChecked()
    })

    it('shows help text for recurring checkbox', () => {
      render(<BillForm />)
      
      expect(screen.getByText(/marque esta opção se a conta se repete mensalmente/i)).toBeInTheDocument()
    })

    it('submits form with isRecurring false by default', async () => {
      const user = userEvent.setup()
      render(<BillForm />)
      
      await user.type(screen.getByLabelText(/nome da conta/i), 'Test Bill')
      await user.type(screen.getByLabelText(/valor total/i), '100')
      await user.type(screen.getByLabelText(/número de parcelas/i), '1')
      
      const submitButton = screen.getByRole('button', { name: /criar conta/i })
      await user.click(submitButton)
      
      await waitFor(() => {
        expect(mockCreateBill).toHaveBeenCalledWith(
          expect.objectContaining({
            isRecurring: false,
          }),
          expect.any(Object)
        )
      })
    })

    it('submits form with isRecurring true when checkbox is checked', async () => {
      const user = userEvent.setup()
      render(<BillForm />)
      
      await user.type(screen.getByLabelText(/nome da conta/i), 'Internet Bill')
      await user.type(screen.getByLabelText(/valor total/i), '100')
      await user.type(screen.getByLabelText(/número de parcelas/i), '1')
      
      const checkbox = screen.getByLabelText(/conta recorrente/i)
      await user.click(checkbox)
      
      const submitButton = screen.getByRole('button', { name: /criar conta/i })
      await user.click(submitButton)
      
      await waitFor(() => {
        expect(mockCreateBill).toHaveBeenCalledWith(
          expect.objectContaining({
            isRecurring: true,
          }),
          expect.any(Object)
        )
      })
    })
  })

  describe('Editing existing bill', () => {
    beforeEach(() => {
      vi.spyOn(reactRouterDom, 'useParams').mockReturnValue({ id: '1' })
    })

    it('loads recurring bill data when editing', () => {
      const recurringBill = {
        id: 1,
        name: 'Internet Bill',
        totalAmount: 100,
        numberOfInstallments: 1,
        installmentAmount: 100,
        description: 'Monthly subscription',
        isRecurring: true,
        executionDate: '2025-01-15T10:00:00',
        createdAt: '2025-01-15T10:00:00',
        updatedAt: '2025-01-15T10:00:00',
      }

      mockUseBill.mockReturnValue({
        data: recurringBill,
        isLoading: false,
      } as any)

      render(<BillForm />)
      
      const checkbox = screen.getByLabelText(/conta recorrente/i)
      expect(checkbox).toBeChecked()
    })

    it('loads non-recurring bill data when editing', () => {
      const nonRecurringBill = {
        id: 1,
        name: 'One-time Bill',
        totalAmount: 500,
        numberOfInstallments: 1,
        installmentAmount: 500,
        description: null,
        isRecurring: false,
        executionDate: '2025-01-15T10:00:00',
        createdAt: '2025-01-15T10:00:00',
        updatedAt: '2025-01-15T10:00:00',
      }

      mockUseBill.mockReturnValue({
        data: nonRecurringBill,
        isLoading: false,
      } as any)

      render(<BillForm />)
      
      const checkbox = screen.getByLabelText(/conta recorrente/i)
      expect(checkbox).not.toBeChecked()
    })

    it('updates bill with isRecurring field', async () => {
      const user = userEvent.setup()
      const existingBill = {
        id: 1,
        name: 'Test Bill',
        totalAmount: 100,
        numberOfInstallments: 1,
        installmentAmount: 100,
        description: null,
        isRecurring: false,
        executionDate: '2025-01-15T10:00:00',
        createdAt: '2025-01-15T10:00:00',
        updatedAt: '2025-01-15T10:00:00',
      }

      mockUseBill.mockReturnValue({
        data: existingBill,
        isLoading: false,
      } as any)

      render(<BillForm />)
      
      const checkbox = screen.getByLabelText(/conta recorrente/i)
      await user.click(checkbox)
      
      const submitButton = screen.getByRole('button', { name: /atualizar/i })
      await user.click(submitButton)
      
      await waitFor(() => {
        expect(mockUpdateBill).toHaveBeenCalledWith(
          expect.objectContaining({
            bill: expect.objectContaining({
              isRecurring: true,
            }),
          }),
          expect.any(Object)
        )
      })
    })
  })

  describe('Form validation', () => {
    it('allows form submission with recurring checkbox unchecked', async () => {
      const user = userEvent.setup()
      render(<BillForm />)
      
      await user.type(screen.getByLabelText(/nome da conta/i), 'Test Bill')
      await user.type(screen.getByLabelText(/valor total/i), '100')
      await user.type(screen.getByLabelText(/número de parcelas/i), '1')
      
      const submitButton = screen.getByRole('button', { name: /criar conta/i })
      await user.click(submitButton)
      
      await waitFor(() => {
        expect(mockCreateBill).toHaveBeenCalled()
      })
    })

    it('allows form submission with recurring checkbox checked', async () => {
      const user = userEvent.setup()
      render(<BillForm />)
      
      await user.type(screen.getByLabelText(/nome da conta/i), 'Internet Bill')
      await user.type(screen.getByLabelText(/valor total/i), '100')
      await user.type(screen.getByLabelText(/número de parcelas/i), '1')
      
      const checkbox = screen.getByLabelText(/conta recorrente/i)
      await user.click(checkbox)
      
      const submitButton = screen.getByRole('button', { name: /criar conta/i })
      await user.click(submitButton)
      
      await waitFor(() => {
        expect(mockCreateBill).toHaveBeenCalled()
      })
    })
  })
})
