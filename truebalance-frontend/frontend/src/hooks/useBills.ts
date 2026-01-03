import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { billsService } from '@/services/bills.service'
import type { BillRequestDTO, BillFiltersDTO } from '@/types/dtos/bill.dto'

const BILLS_QUERY_KEY = 'bills'

/**
 * Hook para buscar lista de bills com paginação e filtros
 */
export function useBills(params?: BillFiltersDTO) {
  return useQuery({
    queryKey: [BILLS_QUERY_KEY, params],
    queryFn: () => billsService.getAll(params),
    refetchOnMount: 'always', // Always refetch when component mounts, even if data exists
    staleTime: 0, // Data is immediately stale, ensuring fresh data on mount
  })
}

/**
 * Hook para buscar uma bill específica por ID
 */
export function useBill(id: number | undefined) {
  return useQuery({
    queryKey: [BILLS_QUERY_KEY, id],
    queryFn: () => billsService.getById(id!),
    enabled: !!id,
  })
}

/**
 * Hook para criar uma nova bill
 */
export function useCreateBill() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (bill: BillRequestDTO) => billsService.create(bill),
    onSuccess: () => {
      // Invalidate all bills queries - this will mark them as stale
      // and trigger a refetch when the queries are used again
      queryClient.invalidateQueries({ 
        queryKey: [BILLS_QUERY_KEY]
      })
      // Force refetch active queries immediately
      queryClient.refetchQueries({ 
        queryKey: [BILLS_QUERY_KEY],
        type: 'active'
      })
    },
  })
}

/**
 * Hook para atualizar uma bill existente
 */
export function useUpdateBill() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, bill }: { id: number; bill: BillRequestDTO }) =>
      billsService.update(id, bill),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: [BILLS_QUERY_KEY] })
      queryClient.invalidateQueries({ queryKey: [BILLS_QUERY_KEY, variables.id] })
    },
  })
}

/**
 * Hook para deletar uma bill
 */
export function useDeleteBill() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (id: number) => billsService.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [BILLS_QUERY_KEY] })
    },
  })
}
