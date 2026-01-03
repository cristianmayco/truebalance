import apiClient from '@/lib/axios'

export interface UnifiedImportResult {
  bills?: {
    totalProcessed: number
    totalCreated: number
    totalSkipped: number
    totalErrors: number
    duplicatesFound: any[]
    errors: any[]
    createdBills: any[]
  }
  creditCards?: {
    totalProcessed: number
    totalCreated: number
    totalSkipped: number
    totalErrors: number
    duplicatesFound: any[]
    errors: any[]
    createdCreditCards: any[]
  }
  invoices?: {
    totalProcessed: number
    totalCreated: number
    totalSkipped: number
    totalErrors: number
    duplicatesFound: any[]
    errors: any[]
    createdInvoices: any[]
  }
  summary: {
    totalCreated: number
    totalSkipped: number
    totalErrors: number
  }
}

export const unifiedService = {
  /**
   * Exportar todas as entidades para um único arquivo Excel
   */
  async exportAll(): Promise<Blob> {
    const response = await apiClient.get('/unified/export', {
      responseType: 'blob',
    })
    return response.data
  },

  /**
   * Importar todas as entidades de um arquivo Excel
   */
  async importAll(
    file: File,
    duplicateStrategy: 'SKIP' | 'CREATE_DUPLICATE'
  ): Promise<UnifiedImportResult> {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('duplicateStrategy', duplicateStrategy)

    const response = await apiClient.post<UnifiedImportResult>(
      '/unified/import',
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      }
    )
    return response.data
  },
}
