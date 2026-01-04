import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { invoicesService } from '@/services/invoices.service'
import type { PartialPaymentRequestDTO } from '@/types/dtos/partialPayment.dto'

const INVOICES_QUERY_KEY = 'invoices'

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
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: [INVOICES_QUERY_KEY] })
      queryClient.invalidateQueries({ queryKey: [INVOICES_QUERY_KEY, data.invoiceId] })
      queryClient.invalidateQueries({ queryKey: [INVOICES_QUERY_KEY, data.invoiceId, 'partialPayments'] })
      // Invalidate credit card limit queries to update available limit
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
    onSuccess: () => {
      // Invalidate all invoice queries to refresh data
      queryClient.invalidateQueries({ queryKey: [INVOICES_QUERY_KEY] })
      // Invalidate credit card limit queries to update available limit
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
