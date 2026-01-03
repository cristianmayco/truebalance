import { CheckCircle2 } from 'lucide-react'
import { Card } from '@/components/ui/Card'
import { formatCurrency } from '@/utils/currency'
import { formatDateTime } from '@/utils/date'
import type { PartialPaymentResponseDTO } from '@/types/dtos/partialPayment.dto'

interface PaymentsHistoryProps {
  payments: PartialPaymentResponseDTO[]
  totalAmount?: number
}

export function PaymentsHistory({ payments, totalAmount = 0 }: PaymentsHistoryProps) {
  const totalPaid = payments.reduce((sum, payment) => sum + payment.amount, 0)
  const remainingBalance = totalAmount - totalPaid

  if (payments.length === 0) {
    return (
      <Card className="bg-gray-50 dark:bg-gray-800">
        <div className="text-center py-8">
          <p className="text-gray-600 dark:text-gray-400">
            Nenhum pagamento parcial registrado
          </p>
        </div>
      </Card>
    )
  }

  return (
    <div className="space-y-4">
      {/* Payments List */}
      <div className="space-y-3">
        {payments.map((payment) => (
          <Card
            key={payment.id}
            className="bg-green-50 dark:bg-green-900/10 border-green-200 dark:border-green-800"
          >
            <div className="flex items-start gap-3">
              <div className="flex-shrink-0 w-10 h-10 rounded-full bg-green-100 dark:bg-green-900/20 flex items-center justify-center">
                <CheckCircle2 className="w-5 h-5 text-green-600 dark:text-green-400" />
              </div>
              <div className="flex-1 min-w-0">
                <div className="flex justify-between items-start gap-2">
                  <div className="flex-1">
                    <div className="font-medium text-gray-900 dark:text-white">
                      Pagamento Parcial
                    </div>
                    {payment.description && (
                      <div className="text-sm text-gray-600 dark:text-gray-400 mt-1">
                        {payment.description}
                      </div>
                    )}
                    <div className="text-xs text-gray-500 dark:text-gray-400 mt-1">
                      {formatDateTime(payment.paymentDate)}
                    </div>
                  </div>
                  <div className="text-right flex-shrink-0">
                    <div className="font-bold text-green-700 dark:text-green-400">
                      {formatCurrency(payment.amount)}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </Card>
        ))}
      </div>

      {/* Summary */}
      <Card className="bg-violet-50 dark:bg-violet-900/20 border-violet-200 dark:border-violet-800">
        <div className="space-y-3">
          <div className="flex justify-between items-center">
            <span className="text-sm text-gray-700 dark:text-gray-300">Total pago</span>
            <span className="font-bold text-green-700 dark:text-green-400">
              {formatCurrency(totalPaid)}
            </span>
          </div>

          {totalAmount > 0 && (
            <>
              <div className="flex justify-between items-center">
                <span className="text-sm text-gray-700 dark:text-gray-300">Valor total</span>
                <span className="font-medium text-gray-900 dark:text-white">
                  {formatCurrency(totalAmount)}
                </span>
              </div>

              <div className="pt-3 border-t border-violet-200 dark:border-violet-700">
                <div className="flex justify-between items-center">
                  <span className="font-semibold text-gray-900 dark:text-white">
                    Saldo restante
                  </span>
                  <span className="text-xl font-bold text-violet-900 dark:text-violet-100">
                    {formatCurrency(remainingBalance)}
                  </span>
                </div>
              </div>
            </>
          )}
        </div>
      </Card>
    </div>
  )
}
