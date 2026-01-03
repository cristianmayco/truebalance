import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { creditCardsService } from '@/services/creditCards.service'
import type { CreditCardRequestDTO } from '@/types/dtos/creditCard.dto'

const CREDIT_CARDS_QUERY_KEY = 'creditCards'

/**
 * Hook para buscar lista de cartões de crédito
 */
export function useCreditCards() {
  return useQuery({
    queryKey: [CREDIT_CARDS_QUERY_KEY],
    queryFn: () => creditCardsService.getAll(),
  })
}

/**
 * Hook para buscar um cartão de crédito específico por ID
 */
export function useCreditCard(id: number | undefined) {
  return useQuery({
    queryKey: [CREDIT_CARDS_QUERY_KEY, id],
    queryFn: () => creditCardsService.getById(id!),
    enabled: !!id,
  })
}

/**
 * Hook para buscar limite disponível de um cartão
 */
export function useCreditCardLimit(id: number | undefined) {
  return useQuery({
    queryKey: [CREDIT_CARDS_QUERY_KEY, id, 'limit'],
    queryFn: () => creditCardsService.getAvailableLimit(id!),
    enabled: !!id,
  })
}

/**
 * Hook para criar um novo cartão de crédito
 */
export function useCreateCreditCard() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (creditCard: CreditCardRequestDTO) => creditCardsService.create(creditCard),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [CREDIT_CARDS_QUERY_KEY] })
    },
  })
}

/**
 * Hook para atualizar um cartão de crédito existente
 */
export function useUpdateCreditCard() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, creditCard }: { id: number; creditCard: CreditCardRequestDTO }) =>
      creditCardsService.update(id, creditCard),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: [CREDIT_CARDS_QUERY_KEY] })
      queryClient.invalidateQueries({ queryKey: [CREDIT_CARDS_QUERY_KEY, variables.id] })
      queryClient.invalidateQueries({ queryKey: [CREDIT_CARDS_QUERY_KEY, variables.id, 'limit'] })
    },
  })
}

/**
 * Hook para deletar um cartão de crédito
 */
export function useDeleteCreditCard() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (id: number) => creditCardsService.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [CREDIT_CARDS_QUERY_KEY] })
    },
  })
}
