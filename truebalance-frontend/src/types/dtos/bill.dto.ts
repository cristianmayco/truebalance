export interface BillRequestDTO {
  name: string
  date: string // ISO date string
  totalAmount: number
  numberOfInstallments: number
  description?: string
}

export interface BillResponseDTO {
  id: number
  name: string
  date: string // ISO date string
  totalAmount: number
  numberOfInstallments: number
  installmentAmount: number
  description?: string
  createdAt: string
  updatedAt: string
}

export interface BillFiltersDTO {
  name?: string
  startDate?: string
  endDate?: string
  page?: number
  size?: number
  sort?: string
}
