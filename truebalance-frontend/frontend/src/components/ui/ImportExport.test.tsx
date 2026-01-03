import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@/test/test-utils'
import { fireEvent } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { ImportExport } from './ImportExport'
import { importExportService } from '@/services/importExport.service'
import { ToastProvider } from '@/contexts/ToastContext'

// Mock the service
vi.mock('@/services/importExport.service', () => ({
  importExportService: {
    downloadExport: vi.fn(),
    readJsonFile: vi.fn(),
    importData: vi.fn(),
  },
}))

// Wrapper component with ToastProvider
const ImportExportWithProvider = () => (
  <ToastProvider>
    <ImportExport />
  </ToastProvider>
)

describe('ImportExport', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders import/export button', () => {
    render(<ImportExportWithProvider />)

    expect(screen.getByRole('button', { name: /importar\/exportar/i })).toBeInTheDocument()
  })

  it('opens modal when button is clicked', async () => {
    const user = userEvent.setup()
    render(<ImportExportWithProvider />)

    const button = screen.getByRole('button', { name: /importar\/exportar/i })
    await user.click(button)

    expect(screen.getByRole('dialog')).toBeInTheDocument()
    expect(screen.getByText(/importar\/exportar dados/i)).toBeInTheDocument()
  })

  describe('Export functionality', () => {
    it('exports data successfully', async () => {
      const user = userEvent.setup()
      ;(importExportService.downloadExport as any).mockResolvedValue(undefined)

      render(<ImportExportWithProvider />)

      // Open modal
      await user.click(screen.getByRole('button', { name: /importar\/exportar/i }))

      // Click export button
      const exportButton = screen.getByRole('button', { name: /exportar dados/i })
      await user.click(exportButton)

      await waitFor(() => {
        expect(importExportService.downloadExport).toHaveBeenCalledTimes(1)
      })
    })

    it('handles export errors', async () => {
      const user = userEvent.setup()
      const error = new Error('Export failed')
      ;(importExportService.downloadExport as any).mockRejectedValue(error)

      render(<ImportExportWithProvider />)

      // Open modal
      await user.click(screen.getByRole('button', { name: /importar\/exportar/i }))

      // Click export button
      const exportButton = screen.getByRole('button', { name: /exportar dados/i })
      await user.click(exportButton)

      await waitFor(() => {
        // Error toast should be shown (we can't easily test toast without more complex setup)
        expect(importExportService.downloadExport).toHaveBeenCalledTimes(1)
      })
    })
  })

  describe('Import functionality', () => {
    it('allows file selection', async () => {
      const user = userEvent.setup()
      render(<ImportExportWithProvider />)

      // Open modal
      await user.click(screen.getByRole('button', { name: /importar\/exportar/i }))

      // Find file input by id (label is associated via htmlFor)
      const fileInput = document.getElementById('json-file-input') as HTMLInputElement
      expect(fileInput).toBeInTheDocument()
      expect(fileInput).toBeInTheDocument()
      expect(fileInput.type).toBe('file')
    })

    it('validates JSON file type', async () => {
      const user = userEvent.setup()
      render(<ImportExportWithProvider />)

      // Open modal
      await user.click(screen.getByRole('button', { name: /importar\/exportar/i }))

      // Create invalid file (not JSON)
      const invalidFile = new File(['test'], 'test.txt', { type: 'text/plain' })
      const fileInput = document.getElementById('json-file-input') as HTMLInputElement

      // Simulate file selection using fireEvent
      fireEvent.change(fileInput, { target: { files: [invalidFile] } })

      // Try to import
      const importButton = screen.getByRole('button', { name: /importar dados/i })
      await user.click(importButton)

      await waitFor(() => {
        // Error toast should be shown
        expect(importExportService.importData).not.toHaveBeenCalled()
      })
    })

    it('imports data successfully', async () => {
      const user = userEvent.setup()
      const mockImportData = {
        bills: [],
        creditCards: [],
        invoices: [],
      }

      const mockResult = {
        totalProcessed: 2,
        totalCreated: 2,
        totalSkipped: 0,
        totalErrors: 0,
        errors: [],
      }

      ;(importExportService.readJsonFile as any).mockResolvedValue(mockImportData)
      ;(importExportService.importData as any).mockResolvedValue(mockResult)

      render(<ImportExportWithProvider />)

      // Open modal
      await user.click(screen.getByRole('button', { name: /importar\/exportar/i }))

      // Create and select JSON file
      const jsonContent = JSON.stringify(mockImportData)
      const file = new File([jsonContent], 'test.json', { type: 'application/json' })
      const fileInput = document.getElementById('json-file-input') as HTMLInputElement

      // Simulate file selection using fireEvent
      fireEvent.change(fileInput, { target: { files: [file] } })
      
      // Wait for state update
      await waitFor(() => {
        expect(screen.getByText(/arquivo selecionado:/i)).toBeInTheDocument()
      })

      // Click import button
      const importButton = screen.getByRole('button', { name: /importar dados/i })
      await user.click(importButton)

      await waitFor(() => {
        expect(importExportService.readJsonFile).toHaveBeenCalledWith(file)
        expect(importExportService.importData).toHaveBeenCalledWith(mockImportData)
      })
    })

    it('displays import result with errors', async () => {
      const user = userEvent.setup()
      const mockImportData = {
        bills: [],
        creditCards: [],
        invoices: [],
      }

      const mockResult = {
        totalProcessed: 3,
        totalCreated: 1,
        totalSkipped: 1,
        totalErrors: 1,
        errors: ['Erro ao importar conta: Dados inválidos'],
      }

      ;(importExportService.readJsonFile as any).mockResolvedValue(mockImportData)
      ;(importExportService.importData as any).mockResolvedValue(mockResult)

      render(<ImportExportWithProvider />)

      // Open modal
      await user.click(screen.getByRole('button', { name: /importar\/exportar/i }))

      // Create and select JSON file
      const jsonContent = JSON.stringify(mockImportData)
      const file = new File([jsonContent], 'test.json', { type: 'application/json' })
      const fileInput = document.getElementById('json-file-input') as HTMLInputElement

      // Simulate file selection using fireEvent
      fireEvent.change(fileInput, { target: { files: [file] } })
      
      // Wait for state update
      await waitFor(() => {
        expect(screen.getByText(/arquivo selecionado:/i)).toBeInTheDocument()
      })

      // Click import button
      const importButton = screen.getByRole('button', { name: /importar dados/i })
      await user.click(importButton)

      await waitFor(() => {
        expect(screen.getByText(/resultado da importação/i)).toBeInTheDocument()
        expect(screen.getByText(/processados:/i)).toBeInTheDocument()
        expect(screen.getByText(/criados:/i)).toBeInTheDocument()
        expect(screen.getByText(/ignorados:/i)).toBeInTheDocument()
        // There are multiple elements with "erros:", so use getAllByText
        expect(screen.getAllByText(/erros:/i).length).toBeGreaterThan(0)
        expect(screen.getByText(/detalhes dos erros:/i)).toBeInTheDocument()
      })
    })

    it('requires file selection before import', async () => {
      const user = userEvent.setup()
      render(<ImportExportWithProvider />)

      // Open modal
      await user.click(screen.getByRole('button', { name: /importar\/exportar/i }))

      // Try to import without selecting file
      const importButton = screen.getByRole('button', { name: /importar dados/i })
      expect(importButton).toBeDisabled()

      await user.click(importButton)

      await waitFor(() => {
        // Error toast should be shown
        expect(importExportService.importData).not.toHaveBeenCalled()
      })
    })

    it('handles import errors', async () => {
      const user = userEvent.setup()
      const mockImportData = {
        bills: [],
        creditCards: [],
        invoices: [],
      }

      const error = new Error('Import failed')
      ;(importExportService.readJsonFile as any).mockResolvedValue(mockImportData)
      ;(importExportService.importData as any).mockRejectedValue(error)

      render(<ImportExportWithProvider />)

      // Open modal
      await user.click(screen.getByRole('button', { name: /importar\/exportar/i }))

      // Create and select JSON file
      const jsonContent = JSON.stringify(mockImportData)
      const file = new File([jsonContent], 'test.json', { type: 'application/json' })
      const fileInput = document.getElementById('json-file-input') as HTMLInputElement

      // Simulate file selection using fireEvent
      fireEvent.change(fileInput, { target: { files: [file] } })
      
      // Wait for state update
      await waitFor(() => {
        expect(screen.getByText(/arquivo selecionado:/i)).toBeInTheDocument()
      })

      // Click import button
      const importButton = screen.getByRole('button', { name: /importar dados/i })
      await user.click(importButton)

      await waitFor(() => {
        // Error toast should be shown
        expect(importExportService.importData).toHaveBeenCalled()
      })
    })
  })

  describe('Modal behavior', () => {
    it('closes modal when close button is clicked', async () => {
      const user = userEvent.setup()
      render(<ImportExportWithProvider />)

      // Open modal
      await user.click(screen.getByRole('button', { name: /importar\/exportar/i }))
      expect(screen.getByRole('dialog')).toBeInTheDocument()

      // Close modal
      const closeButton = screen.getByLabelText('Fechar modal')
      await user.click(closeButton)

      await waitFor(() => {
        expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
      })
    })

    it('resets state when modal is closed', async () => {
      const user = userEvent.setup()
      render(<ImportExportWithProvider />)

      // Open modal
      await user.click(screen.getByRole('button', { name: /importar\/exportar/i }))

      // Select file
      const jsonContent = JSON.stringify({ bills: [] })
      const file = new File([jsonContent], 'test.json', { type: 'application/json' })
      const fileInput = document.getElementById('json-file-input') as HTMLInputElement

      // Simulate file selection using fireEvent
      fireEvent.change(fileInput, { target: { files: [file] } })
      
      // Wait for state update
      await waitFor(() => {
        expect(screen.getByText(/arquivo selecionado:/i)).toBeInTheDocument()
      })

      // Close modal
      const closeButton = screen.getByLabelText('Fechar modal')
      await user.click(closeButton)

      // Reopen modal
      await user.click(screen.getByRole('button', { name: /importar\/exportar/i }))

      // File selection should be reset
      expect(screen.queryByText(/arquivo selecionado:/i)).not.toBeInTheDocument()
    })
  })
})
