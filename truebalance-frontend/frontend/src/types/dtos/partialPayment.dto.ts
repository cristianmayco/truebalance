export interface PartialPaymentRequestDTO {
  amount: number
  description?: string
}

export interface PartialPaymentResponseDTO {
  id: number
  invoiceId: number
  amount: number
  description: string | null
  paymentDate: string
  createdAt: string
  creditCardAvailableLimit?: number // BR-PP-006: Limite disponível atualizado após pagamento
}
