import { useState } from 'react'
import { Download, Upload, FileJson, AlertCircle, CheckCircle2 } from 'lucide-react'
import { Button } from './Button'
import { Modal } from './Modal'
import { importExportService, type ImportResultDTO } from '@/services/importExport.service'
import { useToast } from '@/contexts/ToastContext'

export function ImportExport() {
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [isExporting, setIsExporting] = useState(false)
  const [isImporting, setIsImporting] = useState(false)
  const [importResult, setImportResult] = useState<ImportResultDTO | null>(null)
  const [selectedFile, setSelectedFile] = useState<File | null>(null)
  const { success, error: showError } = useToast()

  const handleExport = async () => {
    setIsExporting(true)
    try {
      await importExportService.downloadExport()
      success('Dados exportados com sucesso!')
      setIsModalOpen(false)
    } catch (err) {
      showError('Erro ao exportar dados. Tente novamente.')
      console.error('Erro ao exportar:', err)
    } finally {
      setIsExporting(false)
    }
  }

  const handleFileSelect = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0]
    if (file) {
      if (file.type !== 'application/json' && !file.name.endsWith('.json')) {
        showError('Por favor, selecione um arquivo JSON válido.')
        return
      }
      setSelectedFile(file)
      setImportResult(null)
    }
  }

  const handleImport = async () => {
    if (!selectedFile) {
      showError('Por favor, selecione um arquivo para importar.')
      return
    }

    setIsImporting(true)
    setImportResult(null)

    try {
      const data = await importExportService.readJsonFile(selectedFile)
      const result = await importExportService.importData(data)
      setImportResult(result)

      if (result.totalErrors === 0) {
        success(
          `Importação concluída! ${result.totalCreated} registros criados, ${result.totalSkipped} ignorados.`
        )
      } else {
        showError(
          `Importação concluída com erros. ${result.totalCreated} criados, ${result.totalSkipped} ignorados, ${result.totalErrors} erros.`
        )
      }
    } catch (err: any) {
      showError(err.message || 'Erro ao importar dados. Verifique o formato do arquivo.')
      console.error('Erro ao importar:', err)
    } finally {
      setIsImporting(false)
    }
  }

  return (
    <>
      <Button
        onClick={() => setIsModalOpen(true)}
        variant="secondary"
        iconLeft={<FileJson className="w-4 h-4" />}
      >
        Importar/Exportar
      </Button>

      <Modal
        isOpen={isModalOpen}
        onClose={() => {
          setIsModalOpen(false)
          setSelectedFile(null)
          setImportResult(null)
        }}
        title="Importar/Exportar Dados"
        size="lg"
      >
        <div className="space-y-6">
          {/* Export Section */}
          <div className="space-y-4">
            <div className="flex items-start gap-3">
              <Download className="w-5 h-5 text-primary-600 mt-0.5" />
              <div className="flex-1">
                <h3 className="font-semibold text-gray-900 dark:text-gray-100 mb-1">
                  Exportar Dados
                </h3>
                <p className="text-sm text-gray-600 dark:text-gray-400 mb-3">
                  Exporte todos os seus dados (contas, cartões de crédito e faturas) em formato
                  JSON.
                </p>
                <Button
                  onClick={handleExport}
                  loading={isExporting}
                  disabled={isExporting || isImporting}
                  iconLeft={<Download className="w-4 h-4" />}
                >
                  Exportar Dados
                </Button>
              </div>
            </div>
          </div>

          <div className="border-t border-gray-200 dark:border-gray-700 pt-6">
            {/* Import Section */}
            <div className="space-y-4">
              <div className="flex items-start gap-3">
                <Upload className="w-5 h-5 text-primary-600 mt-0.5" />
                <div className="flex-1">
                  <h3 className="font-semibold text-gray-900 dark:text-gray-100 mb-1">
                    Importar Dados
                  </h3>
                  <p className="text-sm text-gray-600 dark:text-gray-400 mb-3">
                    Importe dados de um arquivo JSON. Duplicatas serão ignoradas automaticamente.
                    <br />
                    <strong className="text-yellow-600 dark:text-yellow-400">Importante:</strong> Certifique-se de que os cartões de crédito sejam importados antes das faturas, pois as faturas dependem dos cartões.
                  </p>

                  <div className="space-y-3">
                    <div>
                      <label htmlFor="json-file-input" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                        Selecionar Arquivo JSON
                      </label>
                      <input
                        id="json-file-input"
                        type="file"
                        accept=".json,application/json"
                        onChange={handleFileSelect}
                        disabled={isImporting || isExporting}
                        className="block w-full text-sm text-gray-500 dark:text-gray-400
                          file:mr-4 file:py-2 file:px-4
                          file:rounded-lg file:border-0
                          file:text-sm file:font-semibold
                          file:bg-primary-50 file:text-primary-700
                          dark:file:bg-primary-900/20 dark:file:text-primary-400
                          hover:file:bg-primary-100
                          dark:hover:file:bg-primary-900/30
                          cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
                      />
                      {selectedFile && (
                        <p className="mt-2 text-sm text-gray-600 dark:text-gray-400">
                          Arquivo selecionado: <strong>{selectedFile.name}</strong>
                        </p>
                      )}
                    </div>

                    <Button
                      onClick={handleImport}
                      loading={isImporting}
                      disabled={!selectedFile || isImporting || isExporting}
                      iconLeft={<Upload className="w-4 h-4" />}
                    >
                      Importar Dados
                    </Button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Import Result */}
          {importResult && (
            <div className="border-t border-gray-200 dark:border-gray-700 pt-6">
              <h3 className="font-semibold text-gray-900 dark:text-gray-100 mb-3">
                Resultado da Importação
              </h3>
              <div className="space-y-2">
                <div className="flex items-center gap-2 text-sm">
                  <CheckCircle2 className="w-4 h-4 text-green-600" />
                  <span className="text-gray-700 dark:text-gray-300">
                    Processados: <strong>{importResult.totalProcessed}</strong>
                  </span>
                </div>
                <div className="flex items-center gap-2 text-sm">
                  <CheckCircle2 className="w-4 h-4 text-green-600" />
                  <span className="text-gray-700 dark:text-gray-300">
                    Criados: <strong>{importResult.totalCreated}</strong>
                  </span>
                </div>
                <div className="flex items-center gap-2 text-sm">
                  <AlertCircle className="w-4 h-4 text-yellow-600" />
                  <span className="text-gray-700 dark:text-gray-300">
                    Ignorados: <strong>{importResult.totalSkipped}</strong>
                  </span>
                </div>
                {importResult.totalErrors > 0 && (
                  <div className="flex items-center gap-2 text-sm">
                    <AlertCircle className="w-4 h-4 text-red-600" />
                    <span className="text-gray-700 dark:text-gray-300">
                      Erros: <strong>{importResult.totalErrors}</strong>
                    </span>
                  </div>
                )}
                {importResult.errors && importResult.errors.length > 0 && (
                  <div className="mt-3 p-3 bg-red-50 dark:bg-red-900/20 rounded-lg">
                    <p className="text-sm font-medium text-red-800 dark:text-red-300 mb-2">
                      Detalhes dos erros:
                    </p>
                    <ul className="list-disc list-inside space-y-1 text-xs text-red-700 dark:text-red-400">
                      {importResult.errors.map((err, index) => (
                        <li key={index}>{err}</li>
                      ))}
                    </ul>
                  </div>
                )}
              </div>
            </div>
          )}
        </div>
      </Modal>
    </>
  )
}
