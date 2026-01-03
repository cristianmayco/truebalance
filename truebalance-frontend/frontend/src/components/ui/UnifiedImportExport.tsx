import { useState, useRef } from 'react'
import { Upload, Download, Loader2 } from 'lucide-react'
import { unifiedService } from '@/services/unified.service'
import { useToast } from '@/contexts/ToastContext'
import { useQueryClient } from '@tanstack/react-query'
import { UnifiedImportModal } from './UnifiedImportModal'

interface UnifiedImportExportProps {
  onImportComplete?: () => void
  className?: string
  variant?: 'default' | 'outline' | 'ghost'
  size?: 'sm' | 'md' | 'lg'
}

export function UnifiedImportExport({
  onImportComplete,
  className = '',
  variant = 'outline',
  size = 'md',
}: UnifiedImportExportProps) {
  const [showImportModal, setShowImportModal] = useState(false)
  const [isExporting, setIsExporting] = useState(false)
  const fileInputRef = useRef<HTMLInputElement>(null)
  const { showToast } = useToast()
  const queryClient = useQueryClient()

  const handleExport = async () => {
    setIsExporting(true)
    try {
      const blob = await unifiedService.exportAll()
      
      // Criar link de download
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      
      // Nome do arquivo com timestamp
      const timestamp = new Date().toISOString().split('T')[0].replace(/-/g, '')
      link.download = `truebalance_export_${timestamp}.xlsx`
      
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)

      showToast('success', 'Exportação concluída com sucesso!')
    } catch (error: any) {
      showToast('error', 'Erro ao exportar: ' + (error.message || 'Erro desconhecido'))
    } finally {
      setIsExporting(false)
    }
  }

  const [selectedFile, setSelectedFile] = useState<File | null>(null)

  const handleFileSelect = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return

    // Validar extensão
    const extension = file.name.split('.').pop()?.toLowerCase()
    if (extension !== 'xlsx' && extension !== 'xls') {
      showToast('error', 'Formato de arquivo inválido. Use XLS ou XLSX')
      e.target.value = ''
      return
    }

    setSelectedFile(file)
    setShowImportModal(true)
    e.target.value = ''
  }

  const handleImportComplete = async (file: File, duplicateStrategy: 'SKIP' | 'CREATE_DUPLICATE') => {
    try {
      const result = await unifiedService.importAll(file, duplicateStrategy)

      // Invalidar todas as queries
      queryClient.invalidateQueries({ queryKey: ['bills'] })
      queryClient.invalidateQueries({ queryKey: ['creditCards'] })
      queryClient.invalidateQueries({ queryKey: ['invoices'] })

      // Mostrar resultado
      const { summary } = result
      if (summary.totalErrors === 0) {
        showToast(
          'success',
          `Importação concluída! ${summary.totalCreated} registro(s) criado(s), ${summary.totalSkipped} ignorado(s)`
        )
      } else {
        showToast(
          'warning',
          `Importação concluída com ${summary.totalErrors} erro(s). ${summary.totalCreated} criado(s), ${summary.totalSkipped} ignorado(s)`
        )
      }

      setShowImportModal(false)
      setSelectedFile(null)
      if (onImportComplete) {
        onImportComplete()
      }
    } catch (error: any) {
      showToast('error', 'Erro ao importar: ' + (error.message || 'Erro desconhecido'))
    }
  }

  const handleCloseModal = () => {
    setShowImportModal(false)
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
        accept=".xlsx,.xls"
        onChange={handleFileSelect}
        className="hidden"
      />

      <div className="flex items-center gap-2">
        <button
          onClick={handleExport}
          disabled={isExporting}
          className={baseClasses}
        >
          {isExporting ? (
            <Loader2 className="w-4 h-4 animate-spin" />
          ) : (
            <Download className="w-4 h-4" />
          )}
          {isExporting ? 'Exportando...' : 'Exportar Tudo'}
        </button>

        <button
          onClick={() => fileInputRef.current?.click()}
          className={baseClasses}
        >
          <Upload className="w-4 h-4" />
          Importar Tudo
        </button>
      </div>

      {showImportModal && selectedFile && (
        <UnifiedImportModal
          file={selectedFile}
          onClose={() => {
            handleCloseModal()
            setSelectedFile(null)
          }}
          onImport={handleImportComplete}
        />
      )}
    </>
  )
}
