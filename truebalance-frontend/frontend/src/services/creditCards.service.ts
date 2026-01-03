import apiClient from '@/lib/axios'
import type {
  CreditCardRequestDTO,
  CreditCardResponseDTO,
  AvailableLimitResponseDTO,
} from '@/types/dtos/creditCard.dto'
import type {
  CreditCardBulkImportRequestDTO,
  CreditCardImportResultDTO,
} from '@/types/dtos/import.dto'

const CREDIT_CARDS_ENDPOINT = '/credit-cards'

export const creditCardsService = {
  /**
   * Get all credit cards
   */
  async getAll(): Promise<CreditCardResponseDTO[]> {
    const response = await apiClient.get<CreditCardResponseDTO[]>(CREDIT_CARDS_ENDPOINT)
    return response.data
  },

  /**
   * Get a single credit card by ID
   */
  async getById(id: number): Promise<CreditCardResponseDTO> {
    const response = await apiClient.get<CreditCardResponseDTO>(
      `${CREDIT_CARDS_ENDPOINT}/${id}`
    )
    return response.data
  },

  /**
   * Get available limit for a credit card
   */
  async getAvailableLimit(id: number): Promise<AvailableLimitResponseDTO> {
    const response = await apiClient.get<AvailableLimitResponseDTO>(
      `${CREDIT_CARDS_ENDPOINT}/${id}/available-limit`
    )
    return response.data
  },

  /**
   * Create a new credit card
   */
  async create(creditCard: CreditCardRequestDTO): Promise<CreditCardResponseDTO> {
    const response = await apiClient.post<CreditCardResponseDTO>(
      CREDIT_CARDS_ENDPOINT,
      creditCard
    )
    return response.data
  },

  /**
   * Update an existing credit card
   */
  async update(
    id: number,
    creditCard: CreditCardRequestDTO
  ): Promise<CreditCardResponseDTO> {
    const response = await apiClient.put<CreditCardResponseDTO>(
      `${CREDIT_CARDS_ENDPOINT}/${id}`,
      creditCard
    )
    return response.data
  },

  /**
   * Delete a credit card
   */
  async delete(id: number): Promise<void> {
    await apiClient.delete(`${CREDIT_CARDS_ENDPOINT}/${id}`)
  },

  /**
   * Bulk import credit cards from CSV/XLS
   */
  async bulkImport(
    request: CreditCardBulkImportRequestDTO
  ): Promise<CreditCardImportResultDTO> {
    const response = await apiClient.post<CreditCardImportResultDTO>(
      `${CREDIT_CARDS_ENDPOINT}/bulk-import`,
      request
    )
    return response.data
  },
}
