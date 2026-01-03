import { describe, it, expect, vi, beforeEach } from 'vitest'
import { importExportService } from './importExport.service'
import apiClient from '@/lib/axios'

// Mock axios
vi.mock('@/lib/axios', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
  },
}))

describe('importExportService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('exportData', () => {
    it('should export all data successfully', async () => {
      // Given
      const mockData = {
        bills: [],
        creditCards: [],
        invoices: [],
      }

      ;(apiClient.get as any).mockResolvedValue({ data: mockData })

      // When
      const result = await importExportService.exportData()

      // Then
      expect(result).toEqual(mockData)
      expect(apiClient.get).toHaveBeenCalledWith('/import-export/export')
    })

    it('should handle export errors', async () => {
      // Given
      const error = new Error('Export failed')
      ;(apiClient.get as any).mockRejectedValue(error)

      // When & Then
      await expect(importExportService.exportData()).rejects.toThrow('Export failed')
    })
  })

  describe('importData', () => {
    it('should import data successfully', async () => {
      // Given
      const importData = {
        bills: [],
        creditCards: [],
        invoices: [],
      }

      const mockResult = {
        totalProcessed: 0,
        totalCreated: 0,
        totalSkipped: 0,
        totalErrors: 0,
        errors: [],
      }

      ;(apiClient.post as any).mockResolvedValue({ data: mockResult })

      // When
      const result = await importExportService.importData(importData)

      // Then
      expect(result).toEqual(mockResult)
      expect(apiClient.post).toHaveBeenCalledWith('/import-export/import', importData)
    })

    it('should handle import errors', async () => {
      // Given
      const importData = {
        bills: [],
        creditCards: [],
        invoices: [],
      }

      const error = new Error('Import failed')
      ;(apiClient.post as any).mockRejectedValue(error)

      // When & Then
      await expect(importExportService.importData(importData)).rejects.toThrow('Import failed')
    })
  })

  describe('downloadExport', () => {
    beforeEach(() => {
      // Mock URL.createObjectURL and document methods
      global.URL.createObjectURL = vi.fn(() => 'blob:mock-url')
      global.URL.revokeObjectURL = vi.fn()
      document.createElement = vi.fn((tag) => {
        const element = {
          href: '',
          download: '',
          click: vi.fn(),
        } as any
        return element
      })
      document.body.appendChild = vi.fn()
      document.body.removeChild = vi.fn()
    })

    it('should download exported data as JSON file', async () => {
      // Given
      const mockData = {
        bills: [{ id: 1, name: 'Bill 1' }],
        creditCards: [{ id: 1, name: 'Card 1' }],
        invoices: [{ id: 1, creditCardId: 1 }],
      }

      ;(apiClient.get as any).mockResolvedValue({ data: mockData })

      // When
      await importExportService.downloadExport()

      // Then
      expect(apiClient.get).toHaveBeenCalledWith('/import-export/export')
      expect(global.URL.createObjectURL).toHaveBeenCalled()
      expect(document.createElement).toHaveBeenCalledWith('a')
    })

    it('should handle download errors', async () => {
      // Given
      const error = new Error('Export failed')
      ;(apiClient.get as any).mockRejectedValue(error)

      // When & Then
      await expect(importExportService.downloadExport()).rejects.toThrow('Export failed')
    })
  })

  describe('readJsonFile', () => {
    it('should read and parse JSON file successfully', async () => {
      // Given
      const jsonData = {
        bills: [{ id: 1, name: 'Bill 1' }],
        creditCards: [],
        invoices: [],
      }

      const file = new File([JSON.stringify(jsonData)], 'test.json', { type: 'application/json' })

      // When
      const result = await importExportService.readJsonFile(file)

      // Then
      expect(result).toEqual(jsonData)
    })

    it('should handle invalid JSON file', async () => {
      // Given
      const invalidFile = new File(['invalid json'], 'test.json', { type: 'application/json' })

      // When & Then
      await expect(importExportService.readJsonFile(invalidFile)).rejects.toThrow(
        'Arquivo JSON inválido'
      )
    })

    it('should handle file read errors', async () => {
      // Given
      const file = new File(['test'], 'test.json', { type: 'application/json' })
      // Mock FileReader to simulate error
      const originalFileReader = global.FileReader
      global.FileReader = class MockFileReader {
        onerror: ((event: any) => void) | null = null
        readAsText() {
          // Simulate error after a short delay
          setTimeout(() => {
            if (this.onerror) {
              this.onerror(new Error('Read error') as any)
            }
          }, 0)
        }
      } as any

      // When & Then
      await expect(importExportService.readJsonFile(file)).rejects.toThrow('Erro ao ler arquivo')

      // Cleanup
      global.FileReader = originalFileReader
    })
  })
})
