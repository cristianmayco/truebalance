import { useState } from 'react'
import { Edit2, Trash2, Repeat } from 'lucide-react'
import { Button } from '@/components/ui/Button'
import { Badge } from '@/components/ui/Badge'
import { DeleteConfirmation } from '@/components/ui/DeleteConfirmation'
import { useDeleteBill } from '@/hooks/useBills'
import { useToast } from '@/contexts/ToastContext'
import { formatCurrency } from '@/utils/currency'
import { formatDate } from '@/utils/date'
import type { BillResponseDTO } from '@/types/dtos/bill.dto'

interface BillsTableProps {
  bills: BillResponseDTO[]
  onEdit?: (bill: BillResponseDTO) => void
}

export function BillsTable({ bills, onEdit }: BillsTableProps) {
  const [billToDelete, setBillToDelete] = useState<BillResponseDTO | null>(null)
  const { mutate: deleteBill, isPending } = useDeleteBill()
  const { showToast } = useToast()

  const handleDelete = () => {
    if (!billToDelete) return

    deleteBill(billToDelete.id, {
      onSuccess: () => {
        showToast('success', 'Conta excluída com sucesso!')
        setBillToDelete(null)
      },
      onError: (error: any) => {
        showToast('error', error.response?.data?.message || 'Erro ao excluir conta')
      },
    })
  }

  return (
    <>
      <div className="hidden lg:block overflow-x-auto">
        <div className="w-full overflow-x-auto">
          <table className="w-full">
            <thead className="bg-gray-50 dark:bg-slate-800 border-b border-gray-200 dark:border-slate-700">
              <tr>
                <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700 dark:text-slate-300">Nome</th>
                <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700 dark:text-slate-300">Data</th>
                <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700 dark:text-slate-300">Valor Total</th>
                <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700 dark:text-slate-300">Parcelas</th>
                <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700 dark:text-slate-300">Valor da Parcela</th>
                <th className="px-4 py-3 text-right text-sm font-semibold text-gray-700 dark:text-slate-300">Ações</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200 dark:divide-slate-700">
              {bills.map((bill) => (
                <tr key={bill.id} className="bg-white dark:bg-slate-800 transition-colors">
                  <td className="px-4 py-3 text-sm text-gray-900 dark:text-white">
                    <div>
                      <div className="font-medium text-gray-900 dark:text-white flex items-center gap-2">
                        {bill.name}
                        {bill.isRecurring && (
                          <Badge variant="info" size="sm" icon={<Repeat className="w-3 h-3" />}>
                            Recorrente
                          </Badge>
                        )}
                      </div>
                      {bill.description && (
                        <div className="text-sm text-gray-500 dark:text-gray-400">
                          {bill.description}
                        </div>
                      )}
                    </div>
                  </td>
                  <td className="px-4 py-3 text-sm text-gray-900 dark:text-white">{formatDate(bill.date || bill.executionDate)}</td>
                  <td className="px-4 py-3 text-sm text-gray-900 dark:text-white font-medium">{formatCurrency(bill.totalAmount)}</td>
                  <td className="px-4 py-3 text-sm text-gray-900 dark:text-white">{bill.numberOfInstallments}x</td>
                  <td className="px-4 py-3 text-sm text-gray-900 dark:text-white font-medium">{formatCurrency(bill.installmentAmount)}</td>
                  <td className="px-4 py-3 text-sm text-gray-900 dark:text-white">
                    <div className="flex justify-end gap-2">
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
                        onClick={() => setBillToDelete(bill)}
                        aria-label="Excluir conta"
                        className="text-red-600 hover:text-red-700 dark:text-red-400"
                      >
                        <Trash2 className="w-4 h-4" />
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      <DeleteConfirmation
        isOpen={!!billToDelete}
        onClose={() => setBillToDelete(null)}
        onConfirm={handleDelete}
        isLoading={isPending}
        message={
          billToDelete
            ? `Tem certeza que deseja excluir a conta "${billToDelete.name}"? Esta ação não pode ser desfeita.`
            : ''
        }
      />
    </>
  )
}
