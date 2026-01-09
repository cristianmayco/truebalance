import { useQuery, useMutation, useQueryClient, QueryClient } from '@tanstack/react-query'
import { invoicesService } from '@/services/invoices.service'
import type { PartialPaymentRequestDTO } from '@/types/dtos/partialPayment.dto'
import type { InvoiceResponseDTO } from '@/types/dtos/invoice.dto'

const INVOICES_QUERY_KEY = 'invoices'

/**
 * Função auxiliar para buscar creditCardId de uma fatura no cache
 * Procura em todas as queries possíveis onde a fatura pode estar armazenada
 */
function findCreditCardIdFromCache(
  queryClient: QueryClient,
  invoiceId: number
): number | undefined {
  // 1. Tentar buscar da query específica da fatura
  const invoice = queryClient.getQueryData<InvoiceResponseDTO>([
    INVOICES_QUERY_KEY,
    invoiceId,
  ])
  if (invoice?.creditCardId) {
    return invoice.creditCardId
  }

  // 2. Buscar em todas as queries de listas por cartão
  const queryCache = queryClient.getQueryCache()
  const invoiceQueries = queryCache.findAll({
    queryKey: [INVOICES_QUERY_KEY],
  })

  for (const query of invoiceQueries) {
    const data = query.state.data
    if (Array.isArray(data)) {
      // É uma lista de faturas
      const foundInvoice = data.find(
        (inv: InvoiceResponseDTO) => inv.id === invoiceId
      )
      if (foundInvoice?.creditCardId) {
        return foundInvoice.creditCardId
      }
    } else if (data && typeof data === 'object' && 'id' in data) {
      // É uma fatura individual
      const inv = data as InvoiceResponseDTO
      if (inv.id === invoiceId && inv.creditCardId) {
        return inv.creditCardId
      }
    }
  }

  return undefined
}

/**
 * Hook para buscar faturas de um cartão de crédito
 */
export function useInvoices(creditCardId: number | undefined) {
  return useQuery({
    queryKey: [INVOICES_QUERY_KEY, 'creditCard', creditCardId],
    queryFn: () => invoicesService.getByCreditCard(creditCardId!),
    enabled: !!creditCardId,
  })
}

/**
 * Hook para buscar uma fatura específica por ID
 */
export function useInvoice(id: number | undefined) {
  return useQuery({
    queryKey: [INVOICES_QUERY_KEY, id],
    queryFn: () => invoicesService.getById(id!),
    enabled: !!id,
  })
}

/**
 * Hook para buscar parcelas de uma fatura
 */
export function useInvoiceInstallments(invoiceId: number | undefined) {
  return useQuery({
    queryKey: [INVOICES_QUERY_KEY, invoiceId, 'installments'],
    queryFn: () => invoicesService.getInstallments(invoiceId!),
    enabled: !!invoiceId,
  })
}

/**
 * Hook para buscar pagamentos parciais de uma fatura
 */
export function useInvoicePartialPayments(invoiceId: number | undefined) {
  return useQuery({
    queryKey: [INVOICES_QUERY_KEY, invoiceId, 'partialPayments'],
    queryFn: () => invoicesService.getPartialPayments(invoiceId!),
    enabled: !!invoiceId,
  })
}

/**
 * Hook para fechar fatura
 */
export function useCloseInvoice() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (invoiceId: number) => invoicesService.closeInvoice(invoiceId),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: [INVOICES_QUERY_KEY] })
      queryClient.invalidateQueries({ queryKey: [INVOICES_QUERY_KEY, data.id] })
      queryClient.invalidateQueries({ queryKey: ['creditCards', data.creditCardId, 'limit'] })
    },
  })
}

/**
 * Hook para marcar fatura como paga
 */
export function useMarkInvoiceAsPaid() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (invoiceId: number) => invoicesService.markAsPaid(invoiceId),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: [INVOICES_QUERY_KEY] })
      queryClient.invalidateQueries({ queryKey: [INVOICES_QUERY_KEY, data.id] })
      queryClient.invalidateQueries({ queryKey: ['creditCards', data.creditCardId, 'limit'] })
    },
  })
}

/**
 * Hook para marcar fatura como não paga
 */
export function useMarkInvoiceAsUnpaid() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (invoiceId: number) => invoicesService.markAsUnpaid(invoiceId),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: [INVOICES_QUERY_KEY] })
      queryClient.invalidateQueries({ queryKey: [INVOICES_QUERY_KEY, data.id] })
      queryClient.invalidateQueries({ queryKey: ['creditCards', data.creditCardId, 'limit'] })
    },
  })
}

/**
 * Hook para adicionar pagamento parcial
 */
export function useAddPartialPayment() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ invoiceId, payment }: { invoiceId: number; payment: PartialPaymentRequestDTO }) =>
      invoicesService.addPartialPayment(invoiceId, payment),
    onSuccess: (data, variables) => {
      queryClient.invalidateQueries({ queryKey: [INVOICES_QUERY_KEY] })
      queryClient.invalidateQueries({ queryKey: [INVOICES_QUERY_KEY, data.invoiceId] })
      queryClient.invalidateQueries({ queryKey: [INVOICES_QUERY_KEY, data.invoiceId, 'partialPayments'] })
      
      // Buscar creditCardId usando a função auxiliar que procura em todas as queries possíveis
      const creditCardId = findCreditCardIdFromCache(queryClient, variables.invoiceId)
      
      // Invalidate credit card limit queries to update available limit
      if (creditCardId) {
        queryClient.invalidateQueries({ queryKey: ['creditCards', creditCardId, 'limit'] })
      }
      // Also invalidate all credit cards queries as fallback
      queryClient.invalidateQueries({ queryKey: ['creditCards'] })
    },
  })
}

/**
 * Hook para deletar pagamento parcial
 */
export function useDeletePartialPayment() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (partialPaymentId: number) => invoicesService.deletePartialPayment(partialPaymentId),
    onSuccess: (_, partialPaymentId) => {
      // Invalidate all invoice queries to refresh data
      queryClient.invalidateQueries({ queryKey: [INVOICES_QUERY_KEY] })
      
      // Try to find the invoice from cache by searching partial payments
      // This is a best-effort approach to invalidate the specific credit card limit
      const queryCache = queryClient.getQueryCache()
      let invoiceId: number | undefined
      
      // Search through all invoice queries to find the one containing this partial payment
      queryCache.findAll({ queryKey: [INVOICES_QUERY_KEY] }).forEach((query) => {
        const data = query.state.data
        if (Array.isArray(data)) {
          // É uma lista de faturas
          const foundInvoice = data.find((inv: InvoiceResponseDTO) =>
            inv.partialPayments?.some(p => p.id === partialPaymentId)
          )
          if (foundInvoice) {
            invoiceId = foundInvoice.id
          }
        } else if (data && typeof data === 'object' && 'id' in data) {
          // É uma fatura individual
          const inv = data as InvoiceResponseDTO
          if (inv.partialPayments?.some(p => p.id === partialPaymentId)) {
            invoiceId = inv.id
          }
        }
      })
      
      // Se encontrou o invoiceId, usar a função auxiliar para buscar o creditCardId
      let creditCardId: number | undefined
      if (invoiceId) {
        creditCardId = findCreditCardIdFromCache(queryClient, invoiceId)
      }
      
      // Invalidate credit card limit queries to update available limit
      if (creditCardId) {
        queryClient.invalidateQueries({ queryKey: ['creditCards', creditCardId, 'limit'] })
      }
      // Also invalidate all credit cards queries as fallback
      queryClient.invalidateQueries({ queryKey: ['creditCards'] })
    },
  })
}

/**
 * Hook para atualizar flag useAbsoluteValue de uma fatura
 */
export function useUpdateInvoiceUseAbsoluteValue() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ invoiceId, useAbsoluteValue }: { invoiceId: number; useAbsoluteValue: boolean }) =>
      invoicesService.updateUseAbsoluteValue(invoiceId, useAbsoluteValue),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: [INVOICES_QUERY_KEY] })
      queryClient.invalidateQueries({ queryKey: [INVOICES_QUERY_KEY, data.id] })
    },
  })
}

/**
 * Hook para atualizar o valor total de uma fatura (apenas quando useAbsoluteValue = true)
 */
export function useUpdateInvoiceTotalAmount() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ invoiceId, totalAmount }: { invoiceId: number; totalAmount: number }) =>
      invoicesService.updateTotalAmount(invoiceId, totalAmount),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: [INVOICES_QUERY_KEY] })
      queryClient.invalidateQueries({ queryKey: [INVOICES_QUERY_KEY, data.id] })
      queryClient.invalidateQueries({ queryKey: ['creditCards', data.creditCardId, 'limit'] })
    },
  })
}

/**
 * Hook para cadastrar limite disponível em uma fatura fechada
 */
export function useUpdateInvoiceRegisteredLimit() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({
      invoiceId,
      registerAvailableLimit,
      registeredAvailableLimit,
    }: {
      invoiceId: number
      registerAvailableLimit: boolean
      registeredAvailableLimit?: number
    }) => invoicesService.updateRegisteredLimit(invoiceId, registerAvailableLimit, registeredAvailableLimit),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: [INVOICES_QUERY_KEY] })
      queryClient.invalidateQueries({ queryKey: [INVOICES_QUERY_KEY, data.id] })
      queryClient.invalidateQueries({ queryKey: ['creditCards', data.creditCardId, 'limit'] })
    },
  })
}
