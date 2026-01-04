import apiClient from '@/lib/axios'
import type { InvoiceResponseDTO } from '@/types/dtos/invoice.dto'
import type { InstallmentResponseDTO } from '@/types/dtos/installment.dto'
import type { PartialPaymentRequestDTO, PartialPaymentResponseDTO } from '@/types/dtos/partialPayment.dto'
import type { InvoiceBulkImportRequestDTO, InvoiceImportResultDTO } from '@/types/dtos/import.dto'

const INVOICES_ENDPOINT = '/invoices'

export const invoicesService = {
  /**
   * Get all invoices for a credit card
   */
  async getByCreditCard(creditCardId: number): Promise<InvoiceResponseDTO[]> {
    const response = await apiClient.get<InvoiceResponseDTO[]>(`${INVOICES_ENDPOINT}`, {
      params: { creditCardId },
    })
    return response.data
  },

  /**
   * Get a single invoice by ID
   */
  async getById(id: number): Promise<InvoiceResponseDTO> {
    const response = await apiClient.get<InvoiceResponseDTO>(`${INVOICES_ENDPOINT}/${id}`)
    return response.data
  },

  /**
   * Get all installments for an invoice
   */
  async getInstallments(invoiceId: number): Promise<InstallmentResponseDTO[]> {
    const response = await apiClient.get<InstallmentResponseDTO[]>(
      `${INVOICES_ENDPOINT}/${invoiceId}/installments`
    )
    return response.data
  },

  /**
   * Get all partial payments for an invoice
   */
  async getPartialPayments(invoiceId: number): Promise<PartialPaymentResponseDTO[]> {
    const response = await apiClient.get<PartialPaymentResponseDTO[]>(
      `${INVOICES_ENDPOINT}/${invoiceId}/partial-payments`
    )
    return response.data
  },

  /**
   * Close an invoice
   */
  async closeInvoice(invoiceId: number): Promise<InvoiceResponseDTO> {
    const response = await apiClient.post<InvoiceResponseDTO>(
      `${INVOICES_ENDPOINT}/${invoiceId}/close`
    )
    return response.data
  },

  /**
   * Mark an invoice as paid
   */
  async markAsPaid(invoiceId: number): Promise<InvoiceResponseDTO> {
    const response = await apiClient.patch<InvoiceResponseDTO>(
      `${INVOICES_ENDPOINT}/${invoiceId}/mark-as-paid`
    )
    return response.data
  },

  /**
   * Mark an invoice as unpaid
   */
  async markAsUnpaid(invoiceId: number): Promise<InvoiceResponseDTO> {
    const response = await apiClient.patch<InvoiceResponseDTO>(
      `${INVOICES_ENDPOINT}/${invoiceId}/mark-as-unpaid`
    )
    return response.data
  },

  /**
   * Add a partial payment to an invoice
   */
  async addPartialPayment(
    invoiceId: number,
    payment: PartialPaymentRequestDTO
  ): Promise<PartialPaymentResponseDTO> {
    const response = await apiClient.post<PartialPaymentResponseDTO>(
      `${INVOICES_ENDPOINT}/${invoiceId}/partial-payments`,
      payment
    )
    return response.data
  },

  /**
   * Delete a partial payment
   */
  async deletePartialPayment(partialPaymentId: number): Promise<void> {
    await apiClient.delete(`${INVOICES_ENDPOINT}/partial-payments/${partialPaymentId}`)
  },

  /**
   * Update useAbsoluteValue flag for an invoice
   */
  async updateUseAbsoluteValue(invoiceId: number, useAbsoluteValue: boolean): Promise<InvoiceResponseDTO> {
    const response = await apiClient.patch<InvoiceResponseDTO>(
      `${INVOICES_ENDPOINT}/${invoiceId}/use-absolute-value?useAbsoluteValue=${useAbsoluteValue}`
    )
    return response.data
  },

  /**
   * Update total amount of an invoice (only allowed when useAbsoluteValue = true)
   */
  async updateTotalAmount(invoiceId: number, totalAmount: number): Promise<InvoiceResponseDTO> {
    const response = await apiClient.patch<InvoiceResponseDTO>(
      `${INVOICES_ENDPOINT}/${invoiceId}/total-amount`,
      { totalAmount }
    )
    return response.data
  },

  /**
   * Update registered available limit for an invoice (only allowed for closed invoices)
   */
  async updateRegisteredLimit(
    invoiceId: number,
    registerAvailableLimit: boolean,
    registeredAvailableLimit?: number
  ): Promise<InvoiceResponseDTO> {
    const response = await apiClient.patch<InvoiceResponseDTO>(
      `${INVOICES_ENDPOINT}/${invoiceId}/registered-limit`,
      { registerAvailableLimit, registeredAvailableLimit }
    )
    return response.data
  },

  /**
   * Bulk import invoices from CSV/XLS
   */
  async bulkImport(request: InvoiceBulkImportRequestDTO): Promise<InvoiceImportResultDTO> {
    const response = await apiClient.post<InvoiceImportResultDTO>(
      `${INVOICES_ENDPOINT}/bulk-import`,
      request
    )
    return response.data
  },
}
