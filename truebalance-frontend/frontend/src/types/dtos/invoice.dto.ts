import type { PartialPaymentResponseDTO } from './partialPayment.dto'
import type { CreditCardResponseDTO } from './creditCard.dto'

export interface InvoiceResponseDTO {
  id: number
  creditCardId: number
  creditCard?: CreditCardResponseDTO
  referenceMonth: string  // "2025-01-01"
  totalAmount: number
  previousBalance: number
  closed: boolean
  paid: boolean
  partialPayments?: PartialPaymentResponseDTO[]
  createdAt: string
  updatedAt: string
}
