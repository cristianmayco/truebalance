export interface CategoryRequestDTO {
  name: string
  description?: string
  color?: string  // Formato hex: #RRGGBB
}

export interface CategoryResponseDTO {
  id: number
  name: string
  description?: string | null
  color?: string | null
  createdAt: string
  updatedAt: string
}

export interface CategoryExpenseDTO {
  period: string  // Data do período (formato ISO: YYYY-MM-DD)
  totalAmount: number
  billCount: number
}
