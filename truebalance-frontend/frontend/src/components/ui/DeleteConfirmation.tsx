import { AlertTriangle } from 'lucide-react'
import { Modal } from './Modal'
import { Button } from './Button'

interface DeleteConfirmationProps {
  isOpen: boolean
  onClose: () => void
  onConfirm: () => void
  title?: string
  message?: string
  isLoading?: boolean
}

export function DeleteConfirmation({
  isOpen,
  onClose,
  onConfirm,
  title = 'Confirmar exclusão',
  message = 'Tem certeza que deseja excluir este item? Esta ação não pode ser desfeita.',
  isLoading = false,
}: DeleteConfirmationProps) {
  return (
    <Modal isOpen={isOpen} onClose={onClose} title={title}>
      <div className="space-y-6">
        <div className="flex items-start gap-4">
          <div className="flex-shrink-0 w-12 h-12 rounded-full bg-red-100 dark:bg-red-900/20 flex items-center justify-center">
            <AlertTriangle className="w-6 h-6 text-red-600 dark:text-red-400" />
          </div>
          <div className="flex-1">
            <p className="text-gray-700 dark:text-gray-300">{message}</p>
          </div>
        </div>

        <div className="flex justify-end gap-3">
          <Button variant="secondary" onClick={onClose} disabled={isLoading}>
            Cancelar
          </Button>
          <Button variant="danger" onClick={onConfirm} loading={isLoading}>
            {isLoading ? 'Excluindo...' : 'Excluir'}
          </Button>
        </div>
      </div>
    </Modal>
  )
}
