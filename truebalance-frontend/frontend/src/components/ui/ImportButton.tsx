import { useState, useRef } from 'react'
import { Upload, Loader2 } from 'lucide-react'
import { useImport } from '@/hooks/useImport'
import { ImportModal } from './ImportModal'
import { useToast } from '@/contexts/ToastContext'

export type EntityType = 'bills' | 'invoices' | 'creditCards'

interface ImportButtonProps {
  entityType: EntityType
  onImportComplete?: () => void
  className?: string
  variant?: 'default' | 'outline' | 'ghost'
  size?: 'sm' | 'md' | 'lg'
}

export function ImportButton({
  entityType,
  onImportComplete,
  className = '',
  variant = 'outline',
  size = 'md',
}: ImportButtonProps) {
  const [showModal, setShowModal] = useState(false)
  const fileInputRef = useRef<HTMLInputElement>(null)
  const { validateFile, state, reset } = useImport(entityType)
  const { showToast } = useToast()

  const handleFileSelect = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return

    const result = await validateFile(file)

    if (result.success) {
      setShowModal(true)
    } else {
      // Mostrar toast de erro
      showToast('error', result.error || 'Erro ao validar arquivo')

      // Se houver múltiplos erros de validação, mostrar em lista
      if (result.errors && result.errors.length > 0) {
        console.error('Erros de validação:', result.errors)
        // Opcional: Mostrar modal com lista de erros
      }
    }

    // Reset input para permitir selecionar o mesmo arquivo novamente
    e.target.value = ''
  }

  const handleCloseModal = () => {
    setShowModal(false)
    reset()
  }

  const handleImportComplete = () => {
    setShowModal(false)
    reset()
    if (onImportComplete) {
      onImportComplete()
    }
  }

  const variantClasses = {
    default: 'bg-primary-600 hover:bg-primary-700 text-white',
    outline:
      'border border-gray-300 dark:border-gray-600 hover:bg-gray-50 dark:hover:bg-gray-800 text-gray-700 dark:text-gray-300',
    ghost: 'hover:bg-gray-100 dark:hover:bg-gray-800 text-gray-700 dark:text-gray-300',
  }

  const sizeClasses = {
    sm: 'px-3 py-1.5 text-sm',
    md: 'px-4 py-2 text-sm',
    lg: 'px-6 py-3 text-base',
  }

  const baseClasses = `
    inline-flex items-center gap-2 font-medium rounded-lg
    transition-colors disabled:opacity-50 disabled:cursor-not-allowed
    ${variantClasses[variant]}
    ${sizeClasses[size]}
    ${className}
  `

  return (
    <>
      <input
        ref={fileInputRef}
        type="file"
        accept=".csv,.xlsx,.xls"
        onChange={handleFileSelect}
        className="hidden"
      />

      <button
        onClick={() => fileInputRef.current?.click()}
        disabled={state.isValidating}
        className={baseClasses}
      >
        {state.isValidating ? (
          <Loader2 className="w-4 h-4 animate-spin" />
        ) : (
          <Upload className="w-4 h-4" />
        )}
        {state.isValidating ? 'Validando...' : 'Importar'}
      </button>

      {showModal && (
        <ImportModal
          entityType={entityType}
          data={state.parsedData}
          transformedItems={state.transformedItems}
          onClose={handleCloseModal}
          onComplete={handleImportComplete}
        />
      )}
    </>
  )
}
