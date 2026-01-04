import apiClient from '@/lib/axios'
import type { CategoryRequestDTO, CategoryResponseDTO, CategoryExpenseDTO } from '@/types/dtos/category.dto'

const CATEGORIES_ENDPOINT = '/categories'

export const categoriesService = {
  /**
   * Get all categories
   */
  async getAll(): Promise<CategoryResponseDTO[]> {
    const response = await apiClient.get<CategoryResponseDTO[]>(CATEGORIES_ENDPOINT)
    return response.data
  },

  /**
   * Get a single category by ID
   */
  async getById(id: number): Promise<CategoryResponseDTO> {
    const response = await apiClient.get<CategoryResponseDTO>(`${CATEGORIES_ENDPOINT}/${id}`)
    return response.data
  },

  /**
   * Create a new category
   */
  async create(category: CategoryRequestDTO): Promise<CategoryResponseDTO> {
    const response = await apiClient.post<CategoryResponseDTO>(CATEGORIES_ENDPOINT, category)
    return response.data
  },

  /**
   * Update an existing category
   */
  async update(id: number, category: CategoryRequestDTO): Promise<CategoryResponseDTO> {
    const response = await apiClient.put<CategoryResponseDTO>(`${CATEGORIES_ENDPOINT}/${id}`, category)
    return response.data
  },

  /**
   * Delete a category
   */
  async delete(id: number): Promise<void> {
    await apiClient.delete(`${CATEGORIES_ENDPOINT}/${id}`)
  },

  /**
   * Get expenses for a category
   */
  async getExpenses(id: number, period: 'monthly' | 'yearly' = 'monthly'): Promise<CategoryExpenseDTO[]> {
    const response = await apiClient.get<CategoryExpenseDTO[]>(`${CATEGORIES_ENDPOINT}/${id}/expenses`, {
      params: { period },
    })
    return response.data
  },
}
