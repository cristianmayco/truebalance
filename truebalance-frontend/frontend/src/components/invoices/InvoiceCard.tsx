import { useNavigate } from 'react-router-dom'
import { Eye, CheckCircle2, XCircle, Calendar, X } from 'lucide-react'
import { Card } from '@/components/ui/Card'
import { Button } from '@/components/ui/Button'
import { Badge } from '@/components/ui/Badge'
import { formatCurrency } from '@/utils/currency'
import { useMarkInvoiceAsPaid, useMarkInvoiceAsUnpaid } from '@/hooks/useInvoices'
import { useToast } from '@/contexts/ToastContext'
import type { InvoiceResponseDTO } from '@/types/dtos/invoice.dto'

interface InvoiceCardProps {
  invoice: InvoiceResponseDTO
  creditCardName?: string
}

export function InvoiceCard({ invoice, creditCardName }: InvoiceCardProps) {
  const navigate = useNavigate()
  const { showToast } = useToast()
  const markAsPaid = useMarkInvoiceAsPaid()
  const markAsUnpaid = useMarkInvoiceAsUnpaid()

  // Format reference month (2025-01-01 -> Janeiro/2025)
  // Parse date string as local date to avoid timezone issues
  const [year, month, day] = invoice.referenceMonth.split('-').map(Number)
  const referenceDate = new Date(year, month - 1, day) // month is 0-indexed
  const monthName = referenceDate.toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' })
  const formattedMonth = monthName.charAt(0).toUpperCase() + monthName.slice(1)

  const getStatusBadge = () => {
    if (invoice.paid) {
      return (
        <Badge variant="success" className="bg-green-100 dark:bg-green-900/40 text-green-800 dark:text-green-200 border-green-400 dark:border-green-600">
          <CheckCircle2 className="w-3 h-3" />
          Paga
        </Badge>
      )
    }
    if (invoice.closed) {
      return (
        <Badge variant="warning">
          <XCircle className="w-3 h-3" />
          Fechada
        </Badge>
      )
    }
    return (
      <Badge variant="info">
        <Calendar className="w-3 h-3" />
        Aberta
      </Badge>
    )
  }

  const handleViewDetails = () => {
    navigate(`/invoices/${invoice.id}`)
  }

  const handleTogglePaid = () => {
    if (invoice.paid) {
      markAsUnpaid.mutate(invoice.id, {
        onSuccess: () => {
          showToast('success', 'Fatura marcada como não paga')
        },
        onError: (error: any) => {
          showToast('error', error.response?.data?.message || 'Erro ao marcar fatura como não paga')
        },
      })
    } else {
      markAsPaid.mutate(invoice.id, {
        onSuccess: () => {
          showToast('success', 'Fatura marcada como paga')
        },
        onError: (error: any) => {
          showToast('error', error.response?.data?.message || 'Erro ao marcar fatura como paga')
        },
      })
    }
  }

  const isProcessing = markAsPaid.isPending || markAsUnpaid.isPending

  return (
    <Card className="hover:shadow-lg transition-shadow">
      <div className="space-y-4">
        {/* Header */}
        <div className="flex items-start justify-between">
          <div>
            <h3 className="text-lg font-semibold text-gray-900 dark:text-white">
              {formattedMonth}
            </h3>
            {creditCardName && (
              <p className="text-sm text-gray-600 dark:text-gray-400">{creditCardName}</p>
            )}
          </div>
          {getStatusBadge()}
        </div>

        {/* Amounts */}
        <div className="space-y-2">
          <div className="flex justify-between items-baseline">
            <span className="text-sm text-gray-600 dark:text-gray-400">Valor total</span>
            <span className="text-2xl font-bold text-gray-900 dark:text-white">
              {formatCurrency(invoice.totalAmount)}
            </span>
          </div>

          {invoice.previousBalance > 0 && (
            <div className="flex justify-between items-baseline">
              <span className="text-sm text-gray-600 dark:text-gray-400">Saldo anterior</span>
              <span className="text-sm font-medium text-orange-600 dark:text-orange-400">
                {formatCurrency(invoice.previousBalance)}
              </span>
            </div>
          )}
        </div>

        {/* Actions */}
        <div className="flex gap-2 pt-2">
          <Button variant="primary" size="sm" onClick={handleViewDetails} className="flex-1">
            <Eye className="w-4 h-4" />
            Ver Detalhes
          </Button>
          <Button
            variant={invoice.paid ? "secondary" : "primary"}
            size="sm"
            onClick={handleTogglePaid}
            loading={isProcessing}
            disabled={isProcessing}
          >
            {invoice.paid ? (
              <>
                <X className="w-4 h-4" />
                Marcar como Não Pago
              </>
            ) : (
              <>
                <CheckCircle2 className="w-4 h-4" />
                Marcar como Pago
              </>
            )}
          </Button>
        </div>
      </div>
    </Card>
  )
}
