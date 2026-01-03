export interface BillRequestDTO {
  name: string
  executionDate: string  // ISO 8601: "2025-01-15T14:30:00"
  totalAmount: number
  numberOfInstallments: number
  description?: string
  isRecurring?: boolean  // Se true, a conta é recorrente (ex: conta de internet mensal)
  creditCardId?: number  // Opcional, futuro
}

import type { InstallmentResponseDTO } from './installment.dto'

export interface BillResponseDTO {
  id: number
  name: string
  executionDate: string
  date?: string  // Alias para executionDate
  billDate?: string  // Alias para executionDate
  totalAmount: number
  numberOfInstallments: number
  installmentAmount: number
  description: string | null
  isRecurring?: boolean  // Se true, a conta é recorrente (ex: conta de internet mensal)
  creditCardId?: number | null  // ID do cartão de crédito associado (se houver)
  isPaid?: boolean
  installments?: InstallmentResponseDTO[]
  createdAt: string
  updatedAt: string
}

export interface BillFiltersDTO {
  page?: number
  size?: number
  sort?: string
  name?: string
  startDate?: string
  endDate?: string
}
