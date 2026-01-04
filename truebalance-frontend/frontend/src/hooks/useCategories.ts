import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { categoriesService } from '@/services/categories.service'
import type { CategoryRequestDTO } from '@/types/dtos/category.dto'

const CATEGORIES_QUERY_KEY = 'categories'

/**
 * Hook para buscar lista de categorias
 */
export function useCategories() {
  return useQuery({
    queryKey: [CATEGORIES_QUERY_KEY],
    queryFn: () => categoriesService.getAll(),
  })
}

/**
 * Hook para buscar uma categoria específica por ID
 */
export function useCategory(id: number | undefined) {
  return useQuery({
    queryKey: [CATEGORIES_QUERY_KEY, id],
    queryFn: () => categoriesService.getById(id!),
    enabled: !!id,
  })
}

/**
 * Hook para criar uma nova categoria
 */
export function useCreateCategory() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (category: CategoryRequestDTO) => categoriesService.create(category),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [CATEGORIES_QUERY_KEY] })
    },
  })
}

/**
 * Hook para atualizar uma categoria existente
 */
export function useUpdateCategory() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, category }: { id: number; category: CategoryRequestDTO }) =>
      categoriesService.update(id, category),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [CATEGORIES_QUERY_KEY] })
    },
  })
}

/**
 * Hook para deletar uma categoria
 */
export function useDeleteCategory() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (id: number) => categoriesService.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [CATEGORIES_QUERY_KEY] })
      queryClient.invalidateQueries({ queryKey: ['bills'] })
    },
  })
}
