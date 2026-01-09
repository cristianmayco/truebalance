import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { ReactNode } from 'react'
import {
  useAddPartialPayment,
  useDeletePartialPayment,
} from './useInvoices'
import { invoicesService } from '@/services/invoices.service'
import type { InvoiceResponseDTO } from '@/types/dtos/invoice.dto'
import type { PartialPaymentRequestDTO } from '@/types/dtos/partialPayment.dto'

// Mock invoices service
vi.mock('@/services/invoices.service', () => ({
  invoicesService: {
    addPartialPayment: vi.fn(),
    deletePartialPayment: vi.fn(),
  },
}))

const mockInvoicesService = vi.mocked(invoicesService)

// Helper to create a test query client
const createTestQueryClient = () =>
  new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
        gcTime: 0,
      },
      mutations: {
        retry: false,
      },
    },
  })

// Wrapper component for hooks
function wrapper({ children }: { children: ReactNode }) {
  const queryClient = createTestQueryClient()
  return (
    <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
  )
}

describe('useInvoices hooks', () => {
  let queryClient: QueryClient

  beforeEach(() => {
    queryClient = createTestQueryClient()
    vi.clearAllMocks()
  })

  describe('useAddPartialPayment', () => {
    const mockInvoice: InvoiceResponseDTO = {
      id: 1,
      creditCardId: 10,
      referenceMonth: '2025-01-01',
      totalAmount: 1000.0,
      previousBalance: 0,
      closed: false,
      paid: false,
      createdAt: '2025-01-01T00:00:00',
      updatedAt: '2025-01-01T00:00:00',
    }

    const mockPayment: PartialPaymentRequestDTO = {
      amount: 500.0,
      description: 'Pagamento parcial',
    }

    it('should invalidate credit card limit query when creditCardId is found in cache', async () => {
      // Set invoice in cache
      queryClient.setQueryData(['invoices', 1], mockInvoice)
      // Set credit card limit query to test invalidation
      queryClient.setQueryData(['creditCards', 10, 'limit'], {
        availableLimit: 5000,
        usedLimit: 5000,
      })

      const { result } = renderHook(() => useAddPartialPayment(), {
        wrapper: ({ children }) => (
          <QueryClientProvider client={queryClient}>
            {children}
          </QueryClientProvider>
        ),
      })

      mockInvoicesService.addPartialPayment.mockResolvedValue({
        invoiceId: 1,
        id: 1,
        amount: 500.0,
        description: 'Pagamento parcial',
        paymentDate: '2025-01-15',
        createdAt: '2025-01-15T00:00:00',
      } as any)

      await result.current.mutateAsync({
        invoiceId: 1,
        payment: mockPayment,
      })

      await waitFor(() => {
        // Check if credit card limit query was invalidated (status should be pending or data should be stale)
        const limitQuery = queryClient.getQueryState([
          'creditCards',
          10,
          'limit',
        ])
        // After invalidation, query should be marked as stale or pending
        expect(limitQuery?.status === 'pending' || limitQuery?.isInvalidated).toBe(true)
      })
    })

    it('should invalidate all credit cards queries as fallback', async () => {
      // Don't set invoice in cache - should use fallback
      // Set credit cards query to test invalidation
      queryClient.setQueryData(['creditCards'], [])

      const { result } = renderHook(() => useAddPartialPayment(), {
        wrapper: ({ children }) => (
          <QueryClientProvider client={queryClient}>
            {children}
          </QueryClientProvider>
        ),
      })

      mockInvoicesService.addPartialPayment.mockResolvedValue({
        invoiceId: 1,
        id: 1,
        amount: 500.0,
        description: 'Pagamento parcial',
        paymentDate: '2025-01-15',
        createdAt: '2025-01-15T00:00:00',
      } as any)

      await result.current.mutateAsync({
        invoiceId: 1,
        payment: mockPayment,
      })

      await waitFor(() => {
        // Should invalidate all credit cards queries
        const creditCardsQuery = queryClient.getQueryState(['creditCards'])
        // After invalidation, query should be marked as stale or pending
        expect(creditCardsQuery?.status === 'pending' || creditCardsQuery?.isInvalidated).toBe(true)
      })
    })

    it('should find creditCardId from invoice list query when not in specific query', async () => {
      // Set invoice in a list query instead of specific query
      queryClient.setQueryData(['invoices', 'creditCard', 10], [mockInvoice])
      // Set credit card limit query to test invalidation
      queryClient.setQueryData(['creditCards', 10, 'limit'], {
        availableLimit: 5000,
        usedLimit: 5000,
      })

      const { result } = renderHook(() => useAddPartialPayment(), {
        wrapper: ({ children }) => (
          <QueryClientProvider client={queryClient}>
            {children}
          </QueryClientProvider>
        ),
      })

      mockInvoicesService.addPartialPayment.mockResolvedValue({
        invoiceId: 1,
        id: 1,
        amount: 500.0,
        description: 'Pagamento parcial',
        paymentDate: '2025-01-15',
        createdAt: '2025-01-15T00:00:00',
      } as any)

      await result.current.mutateAsync({
        invoiceId: 1,
        payment: mockPayment,
      })

      await waitFor(() => {
        // Should find creditCardId from list and invalidate limit query
        const limitQuery = queryClient.getQueryState([
          'creditCards',
          10,
          'limit',
        ])
        // After invalidation, query should be marked as stale or pending
        expect(limitQuery?.status === 'pending' || limitQuery?.isInvalidated).toBe(true)
      })
    })

    it('should invalidate invoice queries correctly', async () => {
      queryClient.setQueryData(['invoices', 1], mockInvoice)
      // Set queries to test invalidation
      queryClient.setQueryData(['invoices', 1, 'partialPayments'], [])

      const { result } = renderHook(() => useAddPartialPayment(), {
        wrapper: ({ children }) => (
          <QueryClientProvider client={queryClient}>
            {children}
          </QueryClientProvider>
        ),
      })

      mockInvoicesService.addPartialPayment.mockResolvedValue({
        invoiceId: 1,
        id: 1,
        amount: 500.0,
        description: 'Pagamento parcial',
        paymentDate: '2025-01-15',
        createdAt: '2025-01-15T00:00:00',
      } as any)

      await result.current.mutateAsync({
        invoiceId: 1,
        payment: mockPayment,
      })

      await waitFor(() => {
        // Should invalidate invoice queries
        const invoiceQuery = queryClient.getQueryState(['invoices', 1])
        expect(invoiceQuery?.status === 'pending' || invoiceQuery?.isInvalidated).toBe(true)
        const partialPaymentsQuery = queryClient.getQueryState([
          'invoices',
          1,
          'partialPayments',
        ])
        expect(partialPaymentsQuery?.status === 'pending' || partialPaymentsQuery?.isInvalidated).toBe(true)
      })
    })
  })

  describe('useDeletePartialPayment', () => {
    const mockInvoice: InvoiceResponseDTO = {
      id: 1,
      creditCardId: 10,
      referenceMonth: '2025-01-01',
      totalAmount: 1000.0,
      previousBalance: 0,
      closed: false,
      paid: false,
      partialPayments: [
        {
          id: 100,
          amount: 500.0,
          paymentDate: '2025-01-15',
          createdAt: '2025-01-15T00:00:00',
        },
      ],
      createdAt: '2025-01-01T00:00:00',
      updatedAt: '2025-01-01T00:00:00',
    }

    it('should find invoice and creditCardId from partial payments in cache', async () => {
      // Set invoice with partial payments in cache
      queryClient.setQueryData(['invoices', 1], mockInvoice)
      // Set credit card limit query to test invalidation
      queryClient.setQueryData(['creditCards', 10, 'limit'], {
        availableLimit: 5000,
        usedLimit: 5000,
      })

      const { result } = renderHook(() => useDeletePartialPayment(), {
        wrapper: ({ children }) => (
          <QueryClientProvider client={queryClient}>
            {children}
          </QueryClientProvider>
        ),
      })

      mockInvoicesService.deletePartialPayment.mockResolvedValue(undefined)

      await result.current.mutateAsync(100)

      await waitFor(() => {
        // Should find creditCardId and invalidate limit query
        const limitQuery = queryClient.getQueryState([
          'creditCards',
          10,
          'limit',
        ])
        // After invalidation, query should be marked as stale or pending
        expect(limitQuery?.status === 'pending' || limitQuery?.isInvalidated).toBe(true)
      })
    })

    it('should find invoice from list query when searching for partial payment', async () => {
      // Set invoice in a list query
      queryClient.setQueryData(['invoices', 'creditCard', 10], [mockInvoice])
      // Set credit card limit query to test invalidation
      queryClient.setQueryData(['creditCards', 10, 'limit'], {
        availableLimit: 5000,
        usedLimit: 5000,
      })

      const { result } = renderHook(() => useDeletePartialPayment(), {
        wrapper: ({ children }) => (
          <QueryClientProvider client={queryClient}>
            {children}
          </QueryClientProvider>
        ),
      })

      mockInvoicesService.deletePartialPayment.mockResolvedValue(undefined)

      await result.current.mutateAsync(100)

      await waitFor(() => {
        // Should find invoice from list, then find creditCardId and invalidate limit query
        const limitQuery = queryClient.getQueryState([
          'creditCards',
          10,
          'limit',
        ])
        // After invalidation, query should be marked as stale or pending
        expect(limitQuery?.status === 'pending' || limitQuery?.isInvalidated).toBe(true)
      })
    })

    it('should invalidate all credit cards queries as fallback when invoice not found', async () => {
      // Don't set invoice in cache
      // Set credit cards query to test invalidation
      queryClient.setQueryData(['creditCards'], [])

      const { result } = renderHook(() => useDeletePartialPayment(), {
        wrapper: ({ children }) => (
          <QueryClientProvider client={queryClient}>
            {children}
          </QueryClientProvider>
        ),
      })

      mockInvoicesService.deletePartialPayment.mockResolvedValue(undefined)

      await result.current.mutateAsync(100)

      await waitFor(() => {
        // Should invalidate all credit cards queries as fallback
        const creditCardsQuery = queryClient.getQueryState(['creditCards'])
        // After invalidation, query should be marked as stale or pending
        expect(creditCardsQuery?.status === 'pending' || creditCardsQuery?.isInvalidated).toBe(true)
      })
    })

    it('should invalidate all invoice queries', async () => {
      queryClient.setQueryData(['invoices', 1], mockInvoice)
      // Set invoices query to test invalidation
      queryClient.setQueryData(['invoices'], [])

      const { result } = renderHook(() => useDeletePartialPayment(), {
        wrapper: ({ children }) => (
          <QueryClientProvider client={queryClient}>
            {children}
          </QueryClientProvider>
        ),
      })

      mockInvoicesService.deletePartialPayment.mockResolvedValue(undefined)

      await result.current.mutateAsync(100)

      await waitFor(() => {
        // Should invalidate all invoice queries
        const invoicesQuery = queryClient.getQueryState(['invoices'])
        // After invalidation, query should be marked as stale or pending
        expect(invoicesQuery?.status === 'pending' || invoicesQuery?.isInvalidated).toBe(true)
      })
    })

    it('should handle multiple invoices in list when searching for partial payment', async () => {
      const mockInvoice2: InvoiceResponseDTO = {
        id: 2,
        creditCardId: 10,
        referenceMonth: '2025-02-01',
        totalAmount: 800.0,
        previousBalance: 0,
        closed: false,
        paid: false,
        partialPayments: [
          {
            id: 200,
            amount: 300.0,
            paymentDate: '2025-02-15',
            createdAt: '2025-02-15T00:00:00',
          },
        ],
        createdAt: '2025-02-01T00:00:00',
        updatedAt: '2025-02-01T00:00:00',
      }

      // Set multiple invoices in list
      queryClient.setQueryData(['invoices', 'creditCard', 10], [
        mockInvoice,
        mockInvoice2,
      ])
      // Set credit card limit query to test invalidation
      queryClient.setQueryData(['creditCards', 10, 'limit'], {
        availableLimit: 5000,
        usedLimit: 5000,
      })

      const { result } = renderHook(() => useDeletePartialPayment(), {
        wrapper: ({ children }) => (
          <QueryClientProvider client={queryClient}>
            {children}
          </QueryClientProvider>
        ),
      })

      mockInvoicesService.deletePartialPayment.mockResolvedValue(undefined)

      // Delete partial payment from second invoice
      await result.current.mutateAsync(200)

      await waitFor(() => {
        // Should find correct invoice and invalidate limit query
        const limitQuery = queryClient.getQueryState([
          'creditCards',
          10,
          'limit',
        ])
        // After invalidation, query should be marked as stale or pending
        expect(limitQuery?.status === 'pending' || limitQuery?.isInvalidated).toBe(true)
      })
    })
  })
})
