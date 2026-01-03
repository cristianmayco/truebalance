import { useParams, useNavigate } from 'react-router-dom'
import { ArrowLeft, CheckCircle, DollarSign } from 'lucide-react'
import { AppShell } from '@/components/layout/AppShell'
import {
  useInvoice,
  useInvoiceInstallments,
  useInvoicePartialPayments,
  useMarkInvoiceAsPaid,
} from '@/hooks/useInvoices'
import { useCreditCard } from '@/hooks/useCreditCards'
import { useToast } from '@/contexts/ToastContext'
import { Button } from '@/components/ui/Button'
import { Card } from '@/components/ui/Card'
import { Badge } from '@/components/ui/Badge'
import { LoadingSpinner } from '@/components/ui/LoadingSpinner'
import { InstallmentsTable } from '@/components/invoices/InstallmentsTable'
import { PaymentsHistory } from '@/components/invoices/PaymentsHistory'
import { formatCurrency } from '@/utils/currency'

export function InvoiceDetails() {
  const navigate = useNavigate()
  const { id } = useParams<{ id: string }>()
  const invoiceId = id ? parseInt(id) : undefined
  const { showToast } = useToast()

  const { data: invoice, isLoading: isLoadingInvoice } = useInvoice(invoiceId)
  const { data: creditCard, isLoading: isLoadingCard } = useCreditCard(invoice?.creditCardId)
  const { data: installments, isLoading: isLoadingInstallments } =
    useInvoiceInstallments(invoiceId)
  const { data: partialPayments, isLoading: isLoadingPayments } =
    useInvoicePartialPayments(invoiceId)

  const markAsPaidMutation = useMarkInvoiceAsPaid()
  const isMarkingAsPaid = markAsPaidMutation.isPending

  const isLoading =
    isLoadingInvoice || isLoadingCard || isLoadingInstallments || isLoadingPayments

  const handleMarkAsPaid = async () => {
    if (!invoiceId) return

    try {
      await markAsPaidMutation.mutateAsync(invoiceId)
      showToast('success', 'Fatura marcada como paga!')
    } catch (error: any) {
      showToast('error', error.response?.data?.message || 'Erro ao marcar fatura como paga')
    }
  }

  if (isLoading) {
    return (
      <AppShell title="Detalhes da Fatura">
        <LoadingSpinner fullScreen />
      </AppShell>
    )
  }

  if (!invoice || !creditCard) {
    return (
      <AppShell title="Detalhes da Fatura">
        <div className="flex items-center justify-center min-h-[400px]">
          <div className="text-center">
            <p className="text-red-600 dark:text-red-400 mb-4">Fatura não encontrada</p>
            <Button onClick={() => navigate('/credit-cards')}>Voltar aos Cartões</Button>
          </div>
        </div>
      </AppShell>
    )
  }

  // Format reference month
  // Parse date string as local date to avoid timezone issues
  const [year, month, day] = invoice.referenceMonth.split('-').map(Number)
  const referenceDate = new Date(year, month - 1, day) // month is 0-indexed
  const monthName = referenceDate.toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' })
  const formattedMonth = monthName.charAt(0).toUpperCase() + monthName.slice(1)

  // Calculate totals
  const totalPaid = partialPayments?.reduce((sum, p) => sum + p.amount, 0) || 0
  const remainingBalance = invoice.totalAmount - totalPaid

  return (
    <AppShell title={`Fatura ${formattedMonth}`}>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center gap-4">
          <Button
            variant="ghost"
            onClick={() => navigate(`/credit-cards/${invoice.creditCardId}/invoices`)}
            aria-label="Voltar"
          >
            <ArrowLeft className="w-5 h-5" />
          </Button>
          <div className="flex-1">
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
              Fatura {formattedMonth}
            </h1>
            <p className="text-gray-600 dark:text-gray-400 mt-1">{creditCard.name}</p>
          </div>
          <div className="flex gap-2">
            {invoice.paid ? (
              <Badge variant="success">
                <CheckCircle className="w-3 h-3" />
                Paga
              </Badge>
            ) : invoice.closed ? (
              <Badge variant="warning">Fechada</Badge>
            ) : (
              <Badge variant="info">Aberta</Badge>
            )}
          </div>
        </div>

        {/* Summary Cards */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <Card>
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 rounded-full bg-violet-100 dark:bg-violet-900/20 flex items-center justify-center">
                <DollarSign className="w-6 h-6 text-violet-600 dark:text-violet-400" />
              </div>
              <div>
                <div className="text-sm text-gray-600 dark:text-gray-400">Valor Total</div>
                <div className="text-2xl font-bold text-gray-900 dark:text-white">
                  {formatCurrency(invoice.totalAmount)}
                </div>
              </div>
            </div>
          </Card>

          {invoice.previousBalance > 0 && (
            <Card>
              <div className="flex items-center gap-4">
                <div className="w-12 h-12 rounded-full bg-orange-100 dark:bg-orange-900/20 flex items-center justify-center">
                  <DollarSign className="w-6 h-6 text-orange-600 dark:text-orange-400" />
                </div>
                <div>
                  <div className="text-sm text-gray-600 dark:text-gray-400">Saldo Anterior</div>
                  <div className="text-2xl font-bold text-gray-900 dark:text-white">
                    {formatCurrency(invoice.previousBalance)}
                  </div>
                </div>
              </div>
            </Card>
          )}

          {!invoice.paid && totalPaid > 0 && (
            <Card>
              <div className="flex items-center gap-4">
                <div className="w-12 h-12 rounded-full bg-green-100 dark:bg-green-900/20 flex items-center justify-center">
                  <CheckCircle className="w-6 h-6 text-green-600 dark:text-green-400" />
                </div>
                <div>
                  <div className="text-sm text-gray-600 dark:text-gray-400">Saldo Restante</div>
                  <div className="text-2xl font-bold text-gray-900 dark:text-white">
                    {formatCurrency(remainingBalance)}
                  </div>
                </div>
              </div>
            </Card>
          )}
        </div>

        {/* Actions */}
        {!invoice.paid && invoice.closed && (
          <div className="flex gap-3">
            <Button variant="primary" onClick={handleMarkAsPaid} loading={isMarkingAsPaid}>
              <CheckCircle className="w-4 h-4" />
              Marcar como Paga
            </Button>
            {creditCard.allowsPartialPayment && (
              <Button
                variant="secondary"
                onClick={() => navigate(`/invoices/${invoice.id}/payment`)}
              >
                <DollarSign className="w-4 h-4" />
                Pagamento Parcial
              </Button>
            )}
          </div>
        )}

        {/* Installments */}
        <Card>
          <h2 className="text-xl font-semibold text-gray-900 dark:text-white mb-4">
            Parcelas da Fatura
          </h2>
          {installments && installments.length > 0 ? (
            <InstallmentsTable installments={installments} />
          ) : (
            <div className="text-center py-8 text-gray-600 dark:text-gray-400">
              Nenhuma parcela encontrada
            </div>
          )}
        </Card>

        {/* Partial Payments */}
        {partialPayments && partialPayments.length > 0 && (
          <Card>
            <h2 className="text-xl font-semibold text-gray-900 dark:text-white mb-4">
              Histórico de Pagamentos
            </h2>
            <PaymentsHistory payments={partialPayments} totalAmount={invoice.totalAmount} />
          </Card>
        )}
      </div>
    </AppShell>
  )
}
