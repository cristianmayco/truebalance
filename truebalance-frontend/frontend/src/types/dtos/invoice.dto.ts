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
  useAbsoluteValue?: boolean // Se true, totalAmount não é recalculado pela soma das parcelas
  registerAvailableLimit?: boolean // Se true, esta fatura é o ponto de partida para cálculos de limite
  registeredAvailableLimit?: number // Valor do limite disponível registrado
  partialPayments?: PartialPaymentResponseDTO[]
  createdAt: string
  updatedAt: string
}
