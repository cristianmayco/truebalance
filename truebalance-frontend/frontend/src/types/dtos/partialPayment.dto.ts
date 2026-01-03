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
}
