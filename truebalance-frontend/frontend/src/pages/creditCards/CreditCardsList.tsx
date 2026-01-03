import { useNavigate } from 'react-router-dom'
import { Plus, CreditCard } from 'lucide-react'
import { AppShell } from '@/components/layout/AppShell'
import { useCreditCards } from '@/hooks/useCreditCards'
import { Button } from '@/components/ui/Button'
import { LoadingSpinner } from '@/components/ui/LoadingSpinner'
import { EmptyState } from '@/components/ui/EmptyState'
import { CreditCardCard } from '@/components/creditCards/CreditCardCard'
import type { CreditCardResponseDTO } from '@/types/dtos/creditCard.dto'

export function CreditCardsList() {
  const navigate = useNavigate()
  const { data: creditCards, isLoading, error } = useCreditCards()

  const handleEdit = (creditCard: CreditCardResponseDTO) => {
    navigate(`/credit-cards/${creditCard.id}/edit`)
  }

  const handleNew = () => {
    navigate('/credit-cards/new')
  }

  if (isLoading) {
    return (
      <AppShell title="Cartões de Crédito">
        <LoadingSpinner fullScreen />
      </AppShell>
    )
  }

  if (error) {
    return (
      <AppShell title="Cartões de Crédito">
        <div className="flex items-center justify-center min-h-[400px]">
          <div className="text-center">
            <p className="text-red-600 dark:text-red-400 mb-4">
              Erro ao carregar cartões: {(error as any).message}
            </p>
            <Button onClick={() => window.location.reload()}>Tentar novamente</Button>
          </div>
        </div>
      </AppShell>
    )
  }

  const isEmpty = !creditCards || creditCards.length === 0

  return (
    <AppShell title="Cartões de Crédito">
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
              Cartões de Crédito
            </h1>
            <p className="text-gray-600 dark:text-gray-400 mt-1">
              Gerencie seus cartões e acompanhe os limites
            </p>
          </div>
          <Button onClick={handleNew}>
            <Plus className="w-4 h-4" />
            Novo Cartão
          </Button>
        </div>

        {/* Content */}
        {isEmpty ? (
          <EmptyState
            icon={<CreditCard className="w-12 h-12" />}
            message="Nenhum cartão cadastrado"
            description="Comece adicionando seu primeiro cartão de crédito para gerenciar suas faturas."
            actionLabel="Novo Cartão"
            onAction={handleNew}
          />
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {creditCards.map((creditCard) => (
              <CreditCardCard key={creditCard.id} creditCard={creditCard} onEdit={handleEdit} />
            ))}
          </div>
        )}
      </div>
    </AppShell>
  )
}
