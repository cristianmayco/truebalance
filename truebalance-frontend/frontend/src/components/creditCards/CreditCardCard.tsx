import { useState, memo } from 'react'
import { useNavigate } from 'react-router-dom'
import { Edit2, Trash2, Receipt, MoreVertical, CreditCard as CreditCardIcon } from 'lucide-react'
import { Card } from '@/components/ui/Card'
import { Button } from '@/components/ui/Button'
import { ProgressBar } from '@/components/ui/ProgressBar'
import { DeleteConfirmation } from '@/components/ui/DeleteConfirmation'
import { useDeleteCreditCard, useCreditCardLimit } from '@/hooks/useCreditCards'
import { useToast } from '@/contexts/ToastContext'
import { formatCurrency } from '@/utils/currency'
import type { CreditCardResponseDTO } from '@/types/dtos/creditCard.dto'

interface CreditCardCardProps {
  creditCard: CreditCardResponseDTO
  onEdit?: (creditCard: CreditCardResponseDTO) => void
}

function CreditCardCardComponent({ creditCard, onEdit }: CreditCardCardProps) {
  const navigate = useNavigate()
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false)
  const [showMenu, setShowMenu] = useState(false)

  const { data: limitData } = useCreditCardLimit(creditCard.id)
  const { mutate: deleteCreditCard, isPending } = useDeleteCreditCard()
  const { showToast } = useToast()

  const availableLimit = limitData?.availableLimit ?? creditCard.creditLimit
  const usedLimit = limitData?.usedLimit ?? 0
  const usagePercentage = (usedLimit / creditCard.creditLimit) * 100

  const getProgressVariant = () => {
    if (usagePercentage >= 90) return 'danger'
    if (usagePercentage >= 70) return 'warning'
    return 'success'
  }

  const handleDelete = () => {
    deleteCreditCard(creditCard.id, {
      onSuccess: () => {
        showToast('success', 'Cartão excluído com sucesso!')
        setShowDeleteConfirm(false)
      },
      onError: (error: any) => {
        showToast('error', error.response?.data?.message || 'Erro ao excluir cartão')
      },
    })
  }

  const handleViewInvoices = () => {
    navigate(`/credit-cards/${creditCard.id}/invoices`)
  }

  return (
    <>
      <Card
        className="relative overflow-hidden cursor-pointer hover:shadow-xl transition-all group"
        onClick={() => onEdit?.(creditCard)}
      >
        {/* Gradient Background */}
        <div className="absolute inset-0 bg-gradient-to-br from-violet-600 via-violet-700 to-purple-800 dark:from-violet-800 dark:via-violet-900 dark:to-purple-950" />

        {/* Pattern Overlay */}
        <div className="absolute inset-0 opacity-10">
          <div className="absolute top-0 right-0 w-64 h-64 bg-white rounded-full -mr-32 -mt-32" />
          <div className="absolute bottom-0 left-0 w-48 h-48 bg-white rounded-full -ml-24 -mb-24" />
        </div>

        {/* Content */}
        <div className="relative z-10 space-y-6 text-white">
          {/* Header */}
          <div className="flex items-start justify-between">
            <div className="flex items-center gap-3">
              <div className="w-12 h-12 rounded-full bg-white/20 backdrop-blur-sm flex items-center justify-center">
                <CreditCardIcon className="w-6 h-6" />
              </div>
              <div>
                <h3 className="text-xl font-bold">{creditCard.name}</h3>
                <p className="text-sm text-white/80">
                  Limite: {formatCurrency(creditCard.creditLimit)}
                </p>
              </div>
            </div>

            {/* Menu Button */}
            <div className="relative">
              <Button
                variant="ghost"
                size="sm"
                onClick={(e) => {
                  e.stopPropagation()
                  setShowMenu(!showMenu)
                }}
                className="text-white hover:bg-white/20"
              >
                <MoreVertical className="w-5 h-5" />
              </Button>

              {showMenu && (
                <div className="absolute right-0 top-12 w-48 bg-white dark:bg-gray-800 rounded-lg shadow-lg py-2 z-20">
                  <button
                    onClick={(e) => {
                      e.stopPropagation()
                      onEdit?.(creditCard)
                      setShowMenu(false)
                    }}
                    className="w-full px-4 py-2 text-left text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 flex items-center gap-2"
                  >
                    <Edit2 className="w-4 h-4" />
                    Editar
                  </button>
                  <button
                    onClick={(e) => {
                      e.stopPropagation()
                      handleViewInvoices()
                      setShowMenu(false)
                    }}
                    className="w-full px-4 py-2 text-left text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 flex items-center gap-2"
                  >
                    <Receipt className="w-4 h-4" />
                    Ver Faturas
                  </button>
                  <button
                    onClick={(e) => {
                      e.stopPropagation()
                      setShowDeleteConfirm(true)
                      setShowMenu(false)
                    }}
                    className="w-full px-4 py-2 text-left text-red-600 dark:text-red-400 hover:bg-gray-100 dark:hover:bg-gray-700 flex items-center gap-2"
                  >
                    <Trash2 className="w-4 h-4" />
                    Excluir
                  </button>
                </div>
              )}
            </div>
          </div>

          {/* Limit Info */}
          <div>
            <div className="flex justify-between items-baseline mb-2">
              <span className="text-sm text-white/80">Disponível</span>
              <span className="text-2xl font-bold">{formatCurrency(availableLimit)}</span>
            </div>
            <ProgressBar
              value={usagePercentage}
              variant={getProgressVariant()}
              size="lg"
              className="opacity-90"
            />
            <div className="flex justify-between items-center mt-2 text-xs text-white/70">
              <span>Usado: {formatCurrency(usedLimit)}</span>
              <span>{usagePercentage.toFixed(1)}% utilizado</span>
            </div>
          </div>

          {/* Footer Info */}
          <div className="flex justify-between items-center text-sm text-white/80">
            <div>
              <div className="text-xs">Fechamento</div>
              <div className="font-medium">Dia {creditCard.closingDay}</div>
            </div>
            <div>
              <div className="text-xs">Vencimento</div>
              <div className="font-medium">Dia {creditCard.dueDay}</div>
            </div>
            {creditCard.allowsPartialPayment && (
              <div className="px-2 py-1 bg-white/20 rounded text-xs">Parcial OK</div>
            )}
          </div>
        </div>
      </Card>

      <DeleteConfirmation
        isOpen={showDeleteConfirm}
        onClose={() => setShowDeleteConfirm(false)}
        onConfirm={handleDelete}
        isLoading={isPending}
        message={`Tem certeza que deseja excluir o cartão "${creditCard.name}"? Todas as faturas associadas também serão excluídas.`}
      />
    </>
  )
}

// Export memoized version for better list performance
export const CreditCardCard = memo(CreditCardCardComponent);
