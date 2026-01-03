import { useState } from 'react'
import { X, Loader2 } from 'lucide-react'
import { useToast } from '@/contexts/ToastContext'

interface UnifiedImportModalProps {
  file: File | null
  onClose: () => void
  onImport: (file: File, duplicateStrategy: 'SKIP' | 'CREATE_DUPLICATE') => void
}

export function UnifiedImportModal({
  file,
  onClose,
  onImport,
}: UnifiedImportModalProps) {
  const [duplicateStrategy, setDuplicateStrategy] = useState<'SKIP' | 'CREATE_DUPLICATE'>('SKIP')
  const [isImporting, setIsImporting] = useState(false)
  const { showToast } = useToast()

  const handleImport = async () => {
    if (!file) {
      showToast('error', 'Nenhum arquivo selecionado')
      return
    }

    setIsImporting(true)
    try {
      await onImport(file, duplicateStrategy)
    } catch (error: any) {
      showToast('error', 'Erro ao importar: ' + (error.message || 'Erro desconhecido'))
    } finally {
      setIsImporting(false)
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow-xl w-full max-w-2xl max-h-[90vh] overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-200 dark:border-gray-700">
          <h2 className="text-xl font-semibold text-gray-900 dark:text-white">
            Importar Todas as Entidades
          </h2>
          <button
            onClick={onClose}
            disabled={isImporting}
            className="text-gray-400 hover:text-gray-600 dark:hover:text-gray-200 disabled:opacity-50"
          >
            <X className="w-6 h-6" />
          </button>
        </div>

        {/* Content */}
        <div className="px-6 py-4 overflow-y-auto max-h-[calc(90vh-140px)]">
          <div className="space-y-6">
            {/* Informações do arquivo */}
            <div className="bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 rounded-lg p-4">
              <p className="text-blue-900 dark:text-blue-100 font-medium mb-2">
                Arquivo selecionado:
              </p>
              <p className="text-blue-800 dark:text-blue-200 text-sm">
                {file?.name || 'Nenhum arquivo'}
              </p>
              <p className="text-blue-700 dark:text-blue-300 text-xs mt-2">
                O arquivo deve conter as abas: <strong>Contas</strong>, <strong>Cartões de Crédito</strong> e <strong>Faturas</strong>
              </p>
            </div>

            {/* Estratégia de Duplicatas */}
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">
                Estratégia para Duplicatas:
              </label>
              <div className="space-y-3">
                <label className="flex items-start cursor-pointer">
                  <input
                    type="radio"
                    name="strategy"
                    value="SKIP"
                    checked={duplicateStrategy === 'SKIP'}
                    onChange={() => setDuplicateStrategy('SKIP')}
                    disabled={isImporting}
                    className="mt-1 mr-3"
                  />
                  <div>
                    <div className="font-medium text-gray-900 dark:text-white">
                      Ignorar Duplicatas (Recomendado)
                    </div>
                    <div className="text-sm text-gray-500 dark:text-gray-400">
                      Registros duplicados serão pulados e não serão importados
                    </div>
                  </div>
                </label>

                <label className="flex items-start cursor-pointer">
                  <input
                    type="radio"
                    name="strategy"
                    value="CREATE_DUPLICATE"
                    checked={duplicateStrategy === 'CREATE_DUPLICATE'}
                    onChange={() => setDuplicateStrategy('CREATE_DUPLICATE')}
                    disabled={isImporting}
                    className="mt-1 mr-3"
                  />
                  <div>
                    <div className="font-medium text-gray-900 dark:text-white">Criar Duplicatas</div>
                    <div className="text-sm text-gray-500 dark:text-gray-400">
                      Todos os registros serão criados, mesmo se duplicados
                    </div>
                  </div>
                </label>
              </div>
            </div>

            {/* Instruções */}
            <div className="bg-gray-50 dark:bg-gray-900/50 border border-gray-200 dark:border-gray-700 rounded-lg p-4">
              <h3 className="text-sm font-medium text-gray-900 dark:text-white mb-2">
                Formato esperado do arquivo:
              </h3>
              <ul className="text-xs text-gray-600 dark:text-gray-400 space-y-1 list-disc list-inside">
                <li><strong>Contas:</strong> ID, Nome, Descrição, Data, Valor Total, Número de Parcelas, Valor da Parcela, ID Cartão</li>
                <li><strong>Cartões de Crédito:</strong> ID, Nome, Limite de Crédito, Limite Disponível, Dia de Fechamento, Dia de Vencimento, Permite Pagamento Parcial</li>
                <li><strong>Faturas:</strong> ID, ID Cartão, Mês de Referência, Valor Total, Saldo Anterior, Fechada, Paga</li>
              </ul>
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="flex justify-end gap-3 px-6 py-4 border-t border-gray-200 dark:border-gray-700">
          <button
            onClick={onClose}
            disabled={isImporting}
            className="px-4 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-600 disabled:opacity-50"
          >
            Cancelar
          </button>
          <button
            onClick={handleImport}
            disabled={isImporting || !file}
            className="px-4 py-2 text-sm font-medium text-white bg-primary-600 hover:bg-primary-700 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
          >
            {isImporting ? (
              <>
                <Loader2 className="w-4 h-4 animate-spin" />
                Importando...
              </>
            ) : (
              'Importar'
            )}
          </button>
        </div>
      </div>
    </div>
  )
}
