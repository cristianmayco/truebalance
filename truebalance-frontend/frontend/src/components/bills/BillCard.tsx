import { useState, memo } from 'react'
import { Edit2, Trash2, Calendar, DollarSign, Hash, Repeat } from 'lucide-react'
import { Card } from '@/components/ui/Card'
import { Badge } from '@/components/ui/Badge'
import { Button } from '@/components/ui/Button'
import { DeleteConfirmation } from '@/components/ui/DeleteConfirmation'
import { useDeleteBill } from '@/hooks/useBills'
import { useToast } from '@/contexts/ToastContext'
import { formatCurrency } from '@/utils/currency'
import { formatDate } from '@/utils/date'
import type { BillResponseDTO } from '@/types/dtos/bill.dto'

interface BillCardProps {
  bill: BillResponseDTO
  onEdit?: (bill: BillResponseDTO) => void
}

function BillCardComponent({ bill, onEdit }: BillCardProps) {
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false)
  const { mutate: deleteBill, isPending } = useDeleteBill()
  const { showToast } = useToast()

  const handleDelete = () => {
    deleteBill(bill.id, {
      onSuccess: () => {
        showToast('success', 'Conta excluída com sucesso!')
        setShowDeleteConfirm(false)
      },
      onError: (error: any) => {
        showToast('error', error.response?.data?.message || 'Erro ao excluir conta')
      },
    })
  }

  return (
    <>
      <Card className="hover:shadow-lg transition-shadow">
        <div className="space-y-4">
          {/* Header */}
          <div className="flex items-start justify-between">
            <div className="flex-1">
              <div className="flex items-center gap-2 flex-wrap">
                <h3 className="text-lg font-semibold text-gray-900 dark:text-white">
                  {bill.name}
                </h3>
                {bill.isRecurring && (
                  <Badge variant="info" size="sm" icon={<Repeat className="w-3 h-3" />}>
                    Recorrente
                  </Badge>
                )}
              </div>
              {bill.description && (
                <p className="text-sm text-gray-600 dark:text-gray-400 mt-1">
                  {bill.description}
                </p>
              )}
            </div>
            <div className="flex gap-2">
              <Button
                variant="ghost"
                size="sm"
                onClick={() => onEdit?.(bill)}
                aria-label="Editar conta"
              >
                <Edit2 className="w-4 h-4" />
              </Button>
              <Button
                variant="ghost"
                size="sm"
                onClick={() => setShowDeleteConfirm(true)}
                aria-label="Excluir conta"
                className="text-red-600 hover:text-red-700 dark:text-red-400"
              >
                <Trash2 className="w-4 h-4" />
              </Button>
            </div>
          </div>

          {/* Info Grid */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div className="flex items-center gap-2 text-sm">
              <Calendar className="w-4 h-4 text-gray-400" />
              <div>
                <div className="text-gray-500 dark:text-gray-400">Data</div>
                <div className="font-medium text-gray-900 dark:text-white">
                  {formatDate(bill.date || bill.executionDate)}
                </div>
              </div>
            </div>

            <div className="flex items-center gap-2 text-sm">
              <DollarSign className="w-4 h-4 text-gray-400" />
              <div>
                <div className="text-gray-500 dark:text-gray-400">Valor Total</div>
                <div className="font-medium text-gray-900 dark:text-white">
                  {formatCurrency(bill.totalAmount)}
                </div>
              </div>
            </div>

            <div className="flex items-center gap-2 text-sm">
              <Hash className="w-4 h-4 text-gray-400" />
              <div>
                <div className="text-gray-500 dark:text-gray-400">Parcelas</div>
                <div className="font-medium text-gray-900 dark:text-white">
                  {bill.numberOfInstallments}x de {formatCurrency(bill.installmentAmount)}
                </div>
              </div>
            </div>
          </div>
        </div>
      </Card>

      <DeleteConfirmation
        isOpen={showDeleteConfirm}
        onClose={() => setShowDeleteConfirm(false)}
        onConfirm={handleDelete}
        isLoading={isPending}
        message={`Tem certeza que deseja excluir a conta "${bill.name}"? Esta ação não pode ser desfeita.`}
      />
    </>
  )
}

// Export memoized version for better list performance
export const BillCard = memo(BillCardComponent);
