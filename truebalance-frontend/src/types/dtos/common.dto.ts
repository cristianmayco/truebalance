export interface PaginatedResponse<T> {
  content: T[]
  pageable: {
    pageNumber: number
    pageSize: number
    sort: {
      empty: boolean
      sorted: boolean
      unsorted: boolean
    }
    offset: number
    paged: boolean
    unpaged: boolean
  }
  totalElements: number
  totalPages: number
  last: boolean
  size: number
  number: number
  sort: {
    empty: boolean
    sorted: boolean
    unsorted: boolean
  }
  numberOfElements: number
  first: boolean
  empty: boolean
}

export interface ApiError {
  message: string
  status: number
  timestamp: string
  path?: string
}

export interface QueryParams {
  page?: number
  size?: number
  sort?: string
  [key: string]: any
}
