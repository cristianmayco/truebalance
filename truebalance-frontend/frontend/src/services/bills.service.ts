import apiClient from '@/lib/axios'
import type { BillRequestDTO, BillResponseDTO, BillFiltersDTO } from '@/types/dtos/bill.dto'
import type { PaginatedResponse } from '@/types/dtos/common.dto'

const BILLS_ENDPOINT = '/bills'

export const billsService = {
  /**
   * Get all bills with pagination and filters
   */
  async getAll(params?: BillFiltersDTO): Promise<PaginatedResponse<BillResponseDTO>> {
    const response = await apiClient.get<PaginatedResponse<BillResponseDTO>>(BILLS_ENDPOINT, {
      params: {
        page: params?.page ?? 0,
        size: params?.size ?? 10,
        sort: params?.sort ?? 'executionDate,desc',
        name: params?.name,
        startDate: params?.startDate,
        endDate: params?.endDate,
      },
    })
    return response.data
  },

  /**
   * Get a single bill by ID
   */
  async getById(id: number): Promise<BillResponseDTO> {
    const response = await apiClient.get<BillResponseDTO>(`${BILLS_ENDPOINT}/${id}`)
    return response.data
  },

  /**
   * Create a new bill
   */
  async create(bill: BillRequestDTO): Promise<BillResponseDTO> {
    const response = await apiClient.post<BillResponseDTO>(BILLS_ENDPOINT, bill)
    return response.data
  },

  /**
   * Update an existing bill
   */
  async update(id: number, bill: BillRequestDTO): Promise<BillResponseDTO> {
    const response = await apiClient.put<BillResponseDTO>(`${BILLS_ENDPOINT}/${id}`, bill)
    return response.data
  },

  /**
   * Delete a bill
   */
  async delete(id: number): Promise<void> {
    await apiClient.delete(`${BILLS_ENDPOINT}/${id}`)
  },
}
