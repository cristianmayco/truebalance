export interface PaginatedResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface APIError {
  timestamp: string
  status: number
  error: string
  message: string
  path: string
}
