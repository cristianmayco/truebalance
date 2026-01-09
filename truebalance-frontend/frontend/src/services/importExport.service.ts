import apiClient from '@/lib/axios'

export interface ExportDataDTO {
  bills: any[]
  creditCards: any[]
  invoices: any[]
  categories: any[]
}

export interface ImportDataDTO {
  bills?: any[]
  creditCards?: any[]
  invoices?: any[]
  categories?: any[]
}

export interface ImportResultDTO {
  totalProcessed: number
  totalCreated: number
  totalSkipped: number
  totalErrors: number
  errors: string[]
}

const IMPORT_EXPORT_ENDPOINT = '/import-export'

export const importExportService = {
  /**
   * Export all data (bills, credit cards, invoices)
   */
  async exportData(): Promise<ExportDataDTO> {
    const response = await apiClient.get<ExportDataDTO>(
      `${IMPORT_EXPORT_ENDPOINT}/export`
    )
    return response.data
  },

  /**
   * Import data (bills, credit cards, invoices)
   */
  async importData(data: ImportDataDTO): Promise<ImportResultDTO> {
    const response = await apiClient.post<ImportResultDTO>(
      `${IMPORT_EXPORT_ENDPOINT}/import`,
      data
    )
    return response.data
  },

  /**
   * Download exported data as JSON file
   */
  async downloadExport(): Promise<void> {
    try {
      const data = await this.exportData()
      const jsonString = JSON.stringify(data, null, 2)
      const blob = new Blob([jsonString], { type: 'application/json' })
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `truebalance-export-${new Date().toISOString().split('T')[0]}.json`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      URL.revokeObjectURL(url)
    } catch (error) {
      console.error('Erro ao exportar dados:', error)
      throw error
    }
  },

  /**
   * Read JSON file and return parsed data
   */
  async readJsonFile(file: File): Promise<ImportDataDTO> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader()
      reader.onload = (event) => {
        try {
          const json = JSON.parse(event.target?.result as string)
          resolve(json)
        } catch (error) {
          reject(new Error('Arquivo JSON inválido'))
        }
      }
      reader.onerror = () => reject(new Error('Erro ao ler arquivo'))
      reader.readAsText(file)
    })
  },
}
