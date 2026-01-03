import { useParams, useNavigate } from 'react-router-dom'
import { ArrowLeft, Receipt } from 'lucide-react'
import { AppShell } from '@/components/layout/AppShell'
import { useInvoices } from '@/hooks/useInvoices'
import { useCreditCard } from '@/hooks/useCreditCards'
import { Button } from '@/components/ui/Button'
import { LoadingSpinner } from '@/components/ui/LoadingSpinner'
import { EmptyState } from '@/components/ui/EmptyState'
import { InvoiceCard } from '@/components/invoices/InvoiceCard'
import { ExportButton } from '@/components/ui/ExportButton'
import { formatInvoicesForExport } from '@/utils/exportFormatters'

export function InvoicesList() {
  const navigate = useNavigate()
  const { creditCardId } = useParams<{ creditCardId: string }>()
  const cardId = creditCardId ? parseInt(creditCardId) : undefined

  const { data: creditCard, isLoading: isLoadingCard } = useCreditCard(cardId)
  const { data: invoices, isLoading: isLoadingInvoices, error } = useInvoices(cardId)

  const isLoading = isLoadingCard || isLoadingInvoices

  if (isLoading) {
    return (
      <AppShell title="Faturas">
        <LoadingSpinner fullScreen />
      </AppShell>
    )
  }

  if (error || !creditCard) {
    return (
      <AppShell title="Faturas">
        <div className="flex items-center justify-center min-h-[400px]">
          <div className="text-center">
            <p className="text-red-600 dark:text-red-400 mb-4">
              {error ? `Erro ao carregar faturas: ${(error as any).message}` : 'Cartão não encontrado'}
            </p>
            <Button onClick={() => navigate('/credit-cards')}>Voltar aos Cartões</Button>
          </div>
        </div>
      </AppShell>
    )
  }

  // Ordenar faturas por mês de referência (mais recente primeiro)
  // Não filtramos faturas futuras aqui, pois o backend já retorna apenas faturas válidas
  const sortedInvoices = invoices
    ? invoices.sort((a, b) => {
        // Ordenar por mês de referência (mais recente primeiro)
        const [yearA, monthA] = a.referenceMonth.split('-').map(Number)
        const [yearB, monthB] = b.referenceMonth.split('-').map(Number)
        
        if (yearA !== yearB) {
          return yearB - yearA // Ano mais recente primeiro
        }
        return monthB - monthA // Mês mais recente primeiro
      })
    : []

  const isEmpty = !sortedInvoices || sortedInvoices.length === 0

  return (
    <AppShell title={`Faturas - ${creditCard.name}`}>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center gap-4">
          <Button variant="ghost" onClick={() => navigate('/credit-cards')} aria-label="Voltar">
            <ArrowLeft className="w-5 h-5" />
          </Button>
          <div className="flex-1">
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
              Faturas - {creditCard.name}
            </h1>
            <p className="text-gray-600 dark:text-gray-400 mt-1">
              Acompanhe todas as faturas do cartão
            </p>
          </div>
          {!isEmpty && (
            <ExportButton
              data={sortedInvoices}
              filename={`faturas_${creditCard.name.replace(/\s+/g, '_')}`}
              formatData={formatInvoicesForExport}
            />
          )}
        </div>

        {/* Content */}
        {isEmpty ? (
          <EmptyState
            icon={<Receipt className="w-12 h-12" />}
            message="Nenhuma fatura encontrada"
            description="Não há faturas registradas para este cartão ainda."
            actionLabel="Voltar aos Cartões"
            onAction={() => navigate('/credit-cards')}
          />
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {sortedInvoices.map((invoice) => (
              <InvoiceCard
                key={invoice.id}
                invoice={invoice}
                creditCardName={creditCard.name}
              />
            ))}
          </div>
        )}
      </div>
    </AppShell>
  )
}
