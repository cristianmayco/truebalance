import { useState } from 'react'
import { X, Loader2, CheckCircle, XCircle, AlertCircle, ChevronDown, ChevronUp } from 'lucide-react'
import { useImport } from '@/hooks/useImport'
import { useToast } from '@/contexts/ToastContext'
import { format } from 'date-fns'
import { ptBR } from 'date-fns/locale'
import type { EntityType } from './ImportButton'
import type {
  BillImportItemDTO,
  InvoiceImportItemDTO,
  CreditCardImportItemDTO,
  DuplicateStrategy,
  BillImportResultDTO,
  InvoiceImportResultDTO,
  CreditCardImportResultDTO,
  DuplicateInfoDTO,
  InvoiceDuplicateInfoDTO,
  CreditCardDuplicateInfoDTO,
  ImportErrorDTO,
} from '@/types/dtos/import.dto'

type Step = 'preview' | 'processing' | 'result'
type ImportItemDTO = BillImportItemDTO | InvoiceImportItemDTO | CreditCardImportItemDTO
type ImportResultDTO = BillImportResultDTO | InvoiceImportResultDTO | CreditCardImportResultDTO

interface ImportModalProps {
  entityType: EntityType
  data: any[]
  transformedItems: ImportItemDTO[]
  onClose: () => void
  onComplete?: () => void
}

export function ImportModal({
  entityType,
  transformedItems,
  onClose,
  onComplete,
}: ImportModalProps) {
  const [step, setStep] = useState<Step>('preview')
  const [duplicateStrategy, setDuplicateStrategy] = useState<DuplicateStrategy>('SKIP')
  const [result, setResult] = useState<ImportResultDTO | null>(null)
  const { importMutation } = useImport(entityType)
  const { showToast } = useToast()

  const handleImport = async () => {
    setStep('processing')

    try {
      const importResult = await importMutation.mutateAsync({
        items: transformedItems,
        duplicateStrategy,
      })

      setResult(importResult)
      setStep('result')

      if (importResult.totalErrors === 0) {
        showToast(
          'success',
          `${importResult.totalCreated} registro(s) importado(s) com sucesso!`
        )
      } else {
        showToast(
          'warning',
          `Importação concluída com ${importResult.totalErrors} erro(s)`
        )
      }

      if (onComplete) onComplete()
    } catch (error: any) {
      showToast('error', 'Erro ao importar: ' + (error.message || 'Erro desconhecido'))
      setStep('preview')
    }
  }

  const handleClose = () => {
    if (step === 'processing') return // Não permitir fechar durante processamento
    onClose()
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow-xl w-full max-w-4xl max-h-[90vh] overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-200 dark:border-gray-700">
          <h2 className="text-xl font-semibold text-gray-900 dark:text-white">
            {step === 'preview' && 'Preview da Importação'}
            {step === 'processing' && 'Processando Importação'}
            {step === 'result' && 'Resultado da Importação'}
          </h2>
          <button
            onClick={handleClose}
            disabled={step === 'processing'}
            className="text-gray-400 hover:text-gray-600 dark:hover:text-gray-200 disabled:opacity-50"
          >
            <X className="w-6 h-6" />
          </button>
        </div>

        {/* Content */}
        <div className="px-6 py-4 overflow-y-auto max-h-[calc(90vh-140px)]">
          {step === 'preview' && (
            <PreviewStep
              entityType={entityType}
              transformedItems={transformedItems}
              duplicateStrategy={duplicateStrategy}
              onStrategyChange={setDuplicateStrategy}
              onImport={handleImport}
              onCancel={onClose}
            />
          )}

          {step === 'processing' && <ProcessingStep />}

          {step === 'result' && result && (
            <ResultStep entityType={entityType} result={result} onClose={handleClose} />
          )}
        </div>
      </div>
    </div>
  )
}

// Preview Step Component
function PreviewStep({
  entityType,
  transformedItems,
  duplicateStrategy,
  onStrategyChange,
  onImport,
  onCancel,
}: {
  entityType: EntityType
  transformedItems: ImportItemDTO[]
  duplicateStrategy: DuplicateStrategy
  onStrategyChange: (strategy: DuplicateStrategy) => void
  onImport: () => void
  onCancel: () => void
}) {
  const renderPreviewTable = () => {
    switch (entityType) {
      case 'bills':
        return (
          <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
            <thead className="bg-gray-50 dark:bg-gray-900">
              <tr>
                <th className="px-3 py-2 text-left text-xs font-medium text-gray-500 dark:text-gray-400">#</th>
                <th className="px-3 py-2 text-left text-xs font-medium text-gray-500 dark:text-gray-400">Nome</th>
                <th className="px-3 py-2 text-left text-xs font-medium text-gray-500 dark:text-gray-400">Data</th>
                <th className="px-3 py-2 text-left text-xs font-medium text-gray-500 dark:text-gray-400">Valor</th>
                <th className="px-3 py-2 text-left text-xs font-medium text-gray-500 dark:text-gray-400">Parcelas</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200 dark:divide-gray-700">
              {(transformedItems as BillImportItemDTO[]).slice(0, 5).map((item, index) => (
                <tr key={index}>
                  <td className="px-3 py-2 text-sm text-gray-900 dark:text-gray-100">{item.lineNumber}</td>
                  <td className="px-3 py-2 text-sm text-gray-900 dark:text-gray-100">{item.name}</td>
                  <td className="px-3 py-2 text-sm text-gray-900 dark:text-gray-100">
                    {format(new Date(item.executionDate), 'dd/MM/yyyy', { locale: ptBR })}
                  </td>
                  <td className="px-3 py-2 text-sm text-gray-900 dark:text-gray-100">
                    {item.totalAmount.toLocaleString('pt-BR', {
                      style: 'currency',
                      currency: 'BRL',
                    })}
                  </td>
                  <td className="px-3 py-2 text-sm text-gray-900 dark:text-gray-100">{item.numberOfInstallments}x</td>
                </tr>
              ))}
            </tbody>
          </table>
        )
      case 'invoices':
        return (
          <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
            <thead className="bg-gray-50 dark:bg-gray-900">
              <tr>
                <th className="px-3 py-2 text-left text-xs font-medium text-gray-500 dark:text-gray-400">#</th>
                <th className="px-3 py-2 text-left text-xs font-medium text-gray-500 dark:text-gray-400">Cartão</th>
                <th className="px-3 py-2 text-left text-xs font-medium text-gray-500 dark:text-gray-400">Mês</th>
                <th className="px-3 py-2 text-left text-xs font-medium text-gray-500 dark:text-gray-400">Valor</th>
                <th className="px-3 py-2 text-left text-xs font-medium text-gray-500 dark:text-gray-400">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200 dark:divide-gray-700">
              {(transformedItems as InvoiceImportItemDTO[]).slice(0, 5).map((item, index) => (
                <tr key={index}>
                  <td className="px-3 py-2 text-sm text-gray-900 dark:text-gray-100">{item.lineNumber}</td>
                  <td className="px-3 py-2 text-sm text-gray-900 dark:text-gray-100">{item.creditCardId}</td>
                  <td className="px-3 py-2 text-sm text-gray-900 dark:text-gray-100">
                    {format(new Date(item.referenceMonth), 'MM/yyyy', { locale: ptBR })}
                  </td>
                  <td className="px-3 py-2 text-sm text-gray-900 dark:text-gray-100">
                    {item.totalAmount.toLocaleString('pt-BR', {
                      style: 'currency',
                      currency: 'BRL',
                    })}
                  </td>
                  <td className="px-3 py-2 text-sm text-gray-900 dark:text-gray-100">
                    {item.closed ? 'Fechada' : 'Aberta'} {item.paid ? '| Paga' : ''}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )
      case 'creditCards':
        return (
          <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
            <thead className="bg-gray-50 dark:bg-gray-900">
              <tr>
                <th className="px-3 py-2 text-left text-xs font-medium text-gray-500 dark:text-gray-400">#</th>
                <th className="px-3 py-2 text-left text-xs font-medium text-gray-500 dark:text-gray-400">Nome</th>
                <th className="px-3 py-2 text-left text-xs font-medium text-gray-500 dark:text-gray-400">Limite</th>
                <th className="px-3 py-2 text-left text-xs font-medium text-gray-500 dark:text-gray-400">Fechamento</th>
                <th className="px-3 py-2 text-left text-xs font-medium text-gray-500 dark:text-gray-400">Vencimento</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200 dark:divide-gray-700">
              {(transformedItems as CreditCardImportItemDTO[]).slice(0, 5).map((item, index) => (
                <tr key={index}>
                  <td className="px-3 py-2 text-sm text-gray-900 dark:text-gray-100">{item.lineNumber}</td>
                  <td className="px-3 py-2 text-sm text-gray-900 dark:text-gray-100">{item.name}</td>
                  <td className="px-3 py-2 text-sm text-gray-900 dark:text-gray-100">
                    {item.creditLimit.toLocaleString('pt-BR', {
                      style: 'currency',
                      currency: 'BRL',
                    })}
                  </td>
                  <td className="px-3 py-2 text-sm text-gray-900 dark:text-gray-100">{item.closingDay}</td>
                  <td className="px-3 py-2 text-sm text-gray-900 dark:text-gray-100">{item.dueDay}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )
    }
  }

  return (
    <div className="space-y-6">
      {/* Resumo */}
      <div className="bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 rounded-lg p-4">
        <p className="text-blue-900 dark:text-blue-100 font-medium">
          {transformedItems.length} registro(s) encontrado(s) no arquivo
        </p>
      </div>

      {/* Preview dos dados */}
      <div>
        <h3 className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
          Preview dos Primeiros 5 Registros:
        </h3>
        <div className="overflow-x-auto">{renderPreviewTable()}</div>
        {transformedItems.length > 5 && (
          <p className="text-xs text-gray-500 dark:text-gray-400 mt-2">
            ... e mais {transformedItems.length - 5} registro(s)
          </p>
        )}
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
              onChange={() => onStrategyChange('SKIP')}
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
              onChange={() => onStrategyChange('CREATE_DUPLICATE')}
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

      {/* Botões */}
      <div className="flex justify-end gap-3 pt-4">
        <button
          onClick={onCancel}
          className="px-4 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-600"
        >
          Cancelar
        </button>
        <button
          onClick={onImport}
          className="px-4 py-2 text-sm font-medium text-white bg-primary-600 hover:bg-primary-700 rounded-lg"
        >
          Importar {transformedItems.length} registro(s)
        </button>
      </div>
    </div>
  )
}

// Processing Step Component
function ProcessingStep() {
  return (
    <div className="flex flex-col items-center justify-center py-12">
      <Loader2 className="w-16 h-16 text-primary-600 animate-spin mb-4" />
      <p className="text-lg font-medium text-gray-900 dark:text-white">
        Processando importação...
      </p>
      <p className="text-sm text-gray-500 dark:text-gray-400 mt-2">
        Isso pode levar alguns segundos
      </p>
    </div>
  )
}

// Result Step Component
function ResultStep({
  entityType,
  result,
  onClose,
}: {
  entityType: EntityType
  result: ImportResultDTO
  onClose: () => void
}) {
  const [showDuplicates, setShowDuplicates] = useState(false)
  const [showErrors, setShowErrors] = useState(false)

  const renderDuplicatesList = () => {
    switch (entityType) {
      case 'bills':
        return <DuplicatesList items={(result as BillImportResultDTO).duplicatesFound} />
      case 'invoices':
        return <InvoiceDuplicatesList items={(result as InvoiceImportResultDTO).duplicatesFound} />
      case 'creditCards':
        return <CreditCardDuplicatesList items={(result as CreditCardImportResultDTO).duplicatesFound} />
    }
  }

  return (
    <div className="space-y-6">
      {/* Resumo em Cards */}
      <div className="grid grid-cols-3 gap-4">
        <StatCard
          label="Criados"
          value={result.totalCreated}
          icon={<CheckCircle className="w-6 h-6" />}
          color="green"
        />
        <StatCard
          label="Ignorados"
          value={result.totalSkipped}
          icon={<AlertCircle className="w-6 h-6" />}
          color="yellow"
        />
        <StatCard
          label="Erros"
          value={result.totalErrors}
          icon={<XCircle className="w-6 h-6" />}
          color="red"
        />
      </div>

      {/* Lista de Duplicatas */}
      {result.duplicatesFound && result.duplicatesFound.length > 0 && (
        <Accordion
          title={`${result.duplicatesFound.length} Duplicata(s) Encontrada(s)`}
          isOpen={showDuplicates}
          onToggle={() => setShowDuplicates(!showDuplicates)}
        >
          {renderDuplicatesList()}
        </Accordion>
      )}

      {/* Lista de Erros */}
      {result.errors && result.errors.length > 0 && (
        <Accordion
          title={`${result.errors.length} Erro(s)`}
          isOpen={showErrors}
          onToggle={() => setShowErrors(!showErrors)}
        >
          <ErrorsList items={result.errors} />
        </Accordion>
      )}

      {/* Botão Fechar */}
      <div className="flex justify-end pt-4">
        <button
          onClick={onClose}
          className="px-6 py-2 text-sm font-medium text-white bg-primary-600 hover:bg-primary-700 rounded-lg"
        >
          Fechar
        </button>
      </div>
    </div>
  )
}

// Stat Card Component
function StatCard({
  label,
  value,
  icon,
  color,
}: {
  label: string
  value: number
  icon: React.ReactNode
  color: 'green' | 'yellow' | 'red'
}) {
  const colorClasses = {
    green: 'bg-green-50 dark:bg-green-900/20 border-green-200 dark:border-green-800 text-green-700 dark:text-green-300',
    yellow: 'bg-yellow-50 dark:bg-yellow-900/20 border-yellow-200 dark:border-yellow-800 text-yellow-700 dark:text-yellow-300',
    red: 'bg-red-50 dark:bg-red-900/20 border-red-200 dark:border-red-800 text-red-700 dark:text-red-300',
  }

  return (
    <div className={`border rounded-lg p-4 ${colorClasses[color]}`}>
      <div className="flex items-center gap-2 mb-2">{icon}</div>
      <div className="text-2xl font-bold">{value}</div>
      <div className="text-sm">{label}</div>
    </div>
  )
}

// Accordion Component
function Accordion({
  title,
  isOpen,
  onToggle,
  children,
}: {
  title: string
  isOpen: boolean
  onToggle: () => void
  children: React.ReactNode
}) {
  return (
    <div className="border border-gray-200 dark:border-gray-700 rounded-lg">
      <button
        onClick={onToggle}
        className="w-full flex items-center justify-between px-4 py-3 text-left hover:bg-gray-50 dark:hover:bg-gray-700 rounded-lg"
      >
        <span className="font-medium text-gray-900 dark:text-white">{title}</span>
        {isOpen ? (
          <ChevronUp className="w-5 h-5 text-gray-400" />
        ) : (
          <ChevronDown className="w-5 h-5 text-gray-400" />
        )}
      </button>
      {isOpen && <div className="px-4 pb-4">{children}</div>}
    </div>
  )
}

// Duplicates List Component (Bills)
function DuplicatesList({ items }: { items: DuplicateInfoDTO[] }) {
  return (
    <div className="space-y-2">
      {items.map((item, index) => (
        <div
          key={index}
          className="bg-yellow-50 dark:bg-yellow-900/10 border border-yellow-200 dark:border-yellow-800 rounded p-3 text-sm"
        >
          <div className="font-medium text-gray-900 dark:text-white">
            Linha {item.lineNumber}: {item.name}
          </div>
          <div className="text-gray-600 dark:text-gray-400 mt-1">
            Valor: {item.totalAmount.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}
            {' • '}
            Data: {format(new Date(item.executionDate), 'dd/MM/yyyy', { locale: ptBR })}
            {' • '}
            Parcelas: {item.numberOfInstallments}x
          </div>
          <div className="text-xs text-gray-500 dark:text-gray-400 mt-1">{item.reason}</div>
        </div>
      ))}
    </div>
  )
}

// Invoice Duplicates List Component
function InvoiceDuplicatesList({ items }: { items: InvoiceDuplicateInfoDTO[] }) {
  return (
    <div className="space-y-2">
      {items.map((item, index) => (
        <div
          key={index}
          className="bg-yellow-50 dark:bg-yellow-900/10 border border-yellow-200 dark:border-yellow-800 rounded p-3 text-sm"
        >
          <div className="font-medium text-gray-900 dark:text-white">
            Linha {item.lineNumber}: Cartão {item.creditCardId} - {format(new Date(item.referenceMonth), 'MM/yyyy', { locale: ptBR })}
          </div>
          <div className="text-gray-600 dark:text-gray-400 mt-1">
            Valor: {item.totalAmount.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}
          </div>
          <div className="text-xs text-gray-500 dark:text-gray-400 mt-1">{item.reason}</div>
        </div>
      ))}
    </div>
  )
}

// Credit Card Duplicates List Component
function CreditCardDuplicatesList({ items }: { items: CreditCardDuplicateInfoDTO[] }) {
  return (
    <div className="space-y-2">
      {items.map((item, index) => (
        <div
          key={index}
          className="bg-yellow-50 dark:bg-yellow-900/10 border border-yellow-200 dark:border-yellow-800 rounded p-3 text-sm"
        >
          <div className="font-medium text-gray-900 dark:text-white">
            Linha {item.lineNumber}: {item.name}
          </div>
          <div className="text-gray-600 dark:text-gray-400 mt-1">
            Limite: {item.creditLimit.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}
            {' • '}
            Fechamento: {item.closingDay}
            {' • '}
            Vencimento: {item.dueDay}
          </div>
          <div className="text-xs text-gray-500 dark:text-gray-400 mt-1">{item.reason}</div>
        </div>
      ))}
    </div>
  )
}

// Errors List Component
function ErrorsList({ items }: { items: ImportErrorDTO[] }) {
  return (
    <div className="space-y-2">
      {items.map((item, index) => (
        <div
          key={index}
          className="bg-red-50 dark:bg-red-900/10 border border-red-200 dark:border-red-800 rounded p-3 text-sm"
        >
          <div className="font-medium text-gray-900 dark:text-white">
            Linha {item.lineNumber}
            {item.field !== 'general' && ` - Campo: ${item.field}`}
          </div>
          <div className="text-red-600 dark:text-red-400 mt-1">{item.message}</div>
          {item.value && (
            <div className="text-xs text-gray-500 dark:text-gray-400 mt-1">Valor: {item.value}</div>
          )}
        </div>
      ))}
    </div>
  )
}
