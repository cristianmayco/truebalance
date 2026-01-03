export interface CreditCardRequestDTO {
  name: string
  creditLimit: number
  closingDay: number      // 1-31
  dueDay: number          // 1-31
  allowsPartialPayment: boolean
}

export interface CreditCardResponseDTO {
  id: number
  name: string
  creditLimit: number
  limit: number  // Alias para creditLimit
  availableLimit: number
  closingDay: number
  dueDay: number
  allowsPartialPayment: boolean
  createdAt: string
  updatedAt: string
}

export interface AvailableLimitResponseDTO {
  creditCardId: number
  creditLimit: number
  availableLimit: number
  usedLimit: number
}
