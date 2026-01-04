import type { BillResponseDTO } from './bill.dto'
import type { InvoiceResponseDTO } from './invoice.dto'
import type { CreditCardResponseDTO } from './creditCard.dto'

export type DuplicateStrategy = 'SKIP' | 'CREATE_DUPLICATE'

export interface BillImportItemDTO {
  name: string
  description?: string
  category?: string
  executionDate: string // ISO
  totalAmount: number
  numberOfInstallments: number
  isRecurring?: boolean
  creditCardId?: number
  lineNumber: number
}

export interface BillBulkImportRequestDTO {
  items: BillImportItemDTO[]
  duplicateStrategy: DuplicateStrategy
}

export interface DuplicateInfoDTO {
  lineNumber: number
  name: string
  totalAmount: number
  executionDate: string
  numberOfInstallments: number
  existingBillId: number
  reason: string
}

export interface ImportErrorDTO {
  lineNumber: number
  field: string
  message: string
  value: string
}

export interface BillImportResultDTO {
  totalProcessed: number
  totalCreated: number
  totalSkipped: number
  totalErrors: number
  duplicatesFound: DuplicateInfoDTO[]
  errors: ImportErrorDTO[]
  createdBills: BillResponseDTO[]
}

// Invoice Import DTOs
export interface InvoiceImportItemDTO {
  creditCardId: number
  referenceMonth: string // YYYY-MM-DD format (primeiro dia do mês)
  totalAmount: number
  previousBalance?: number
  closed?: boolean
  paid?: boolean
  lineNumber: number
}

export interface InvoiceBulkImportRequestDTO {
  items: InvoiceImportItemDTO[]
  duplicateStrategy: DuplicateStrategy
}

export interface InvoiceDuplicateInfoDTO {
  lineNumber: number
  creditCardId: number
  referenceMonth: string
  totalAmount: number
  existingInvoiceId: number
  reason: string
}

export interface InvoiceImportResultDTO {
  totalProcessed: number
  totalCreated: number
  totalSkipped: number
  totalErrors: number
  duplicatesFound: InvoiceDuplicateInfoDTO[]
  errors: ImportErrorDTO[]
  createdInvoices: InvoiceResponseDTO[]
}

// Credit Card Import DTOs
export interface CreditCardImportItemDTO {
  name: string
  creditLimit: number
  closingDay: number
  dueDay: number
  allowsPartialPayment?: boolean
  lineNumber: number
}

export interface CreditCardBulkImportRequestDTO {
  items: CreditCardImportItemDTO[]
  duplicateStrategy: DuplicateStrategy
}

export interface CreditCardDuplicateInfoDTO {
  lineNumber: number
  name: string
  creditLimit: number
  closingDay: number
  dueDay: number
  existingCreditCardId: number
  reason: string
}

export interface CreditCardImportResultDTO {
  totalProcessed: number
  totalCreated: number
  totalSkipped: number
  totalErrors: number
  duplicatesFound: CreditCardDuplicateInfoDTO[]
  errors: ImportErrorDTO[]
  createdCreditCards: CreditCardResponseDTO[]
}
