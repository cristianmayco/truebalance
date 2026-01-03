import { useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import {
  parseImportFile,
  validateFileType,
  validateBillImportFileStructure,
  validateInvoiceImportFileStructure,
  validateCreditCardImportFileStructure,
  transformRowToBillImport,
  transformRowToInvoiceImport,
  transformRowToCreditCardImport,
} from '@/utils/importParsers'
import { billsService } from '@/services/bills.service'
import { invoicesService } from '@/services/invoices.service'
import { creditCardsService } from '@/services/creditCards.service'
import type {
  BillImportItemDTO,
  BillImportResultDTO,
  InvoiceImportItemDTO,
  InvoiceImportResultDTO,
  CreditCardImportItemDTO,
  CreditCardImportResultDTO,
  DuplicateStrategy,
} from '@/types/dtos/import.dto'
import type { EntityType } from '@/components/ui/ImportButton'

type ImportItemDTO = BillImportItemDTO | InvoiceImportItemDTO | CreditCardImportItemDTO
type ImportResultDTO = BillImportResultDTO | InvoiceImportResultDTO | CreditCardImportResultDTO

interface ImportState {
  file: File | null
  parsedData: any[]
  transformedItems: ImportItemDTO[]
  isValidating: boolean
  validationErrors: string[]
}

export function useImport(entityType: EntityType) {
  const queryClient = useQueryClient()

  const [state, setState] = useState<ImportState>({
    file: null,
    parsedData: [],
    transformedItems: [],
    isValidating: false,
    validationErrors: [],
  })

  /**
   * Validar arquivo e fazer parse
   */
  const validateFile = async (file: File) => {
    setState((prev) => ({ ...prev, isValidating: true, file }))

    try {
      // 1. Validar tipo e tamanho
      const fileValidation = validateFileType(file)
      if (!fileValidation.valid) {
        throw new Error(fileValidation.error)
      }

      // 2. Parse do arquivo
      const data = await parseImportFile(file)

      // 3. Validar estrutura baseado no tipo de entidade
      let structureValidation: { valid: boolean; error?: string }
      switch (entityType) {
        case 'bills':
          structureValidation = validateBillImportFileStructure(data)
          break
        case 'invoices':
          structureValidation = validateInvoiceImportFileStructure(data)
          break
        case 'creditCards':
          structureValidation = validateCreditCardImportFileStructure(data)
          break
        default:
          throw new Error('Tipo de entidade não suportado')
      }

      if (!structureValidation.valid) {
        throw new Error(structureValidation.error)
      }

      // 4. Transformar e validar cada linha
      const transformedItems: ImportItemDTO[] = []
      const errors: string[] = []

      for (let i = 0; i < data.length; i++) {
        try {
          let item: ImportItemDTO
          switch (entityType) {
            case 'bills':
              item = transformRowToBillImport(data[i], i + 1)
              break
            case 'invoices':
              item = transformRowToInvoiceImport(data[i], i + 1)
              break
            case 'creditCards':
              item = transformRowToCreditCardImport(data[i], i + 1)
              break
            default:
              throw new Error('Tipo de entidade não suportado')
          }
          transformedItems.push(item)
        } catch (error: any) {
          errors.push(error.message)
          // Continuar processando outras linhas
        }
      }

      // Se houver erros de validação, retornar erro
      if (errors.length > 0) {
        setState((prev) => ({
          ...prev,
          isValidating: false,
          validationErrors: errors,
        }))
        return {
          success: false,
          error: `${errors.length} erro(s) encontrado(s) no arquivo. Corrija os erros e tente novamente.`,
          errors,
        }
      }

      setState((prev) => ({
        ...prev,
        parsedData: data,
        transformedItems,
        isValidating: false,
        validationErrors: [],
      }))

      return { success: true, data, transformedItems }
    } catch (error: any) {
      setState((prev) => ({
        ...prev,
        isValidating: false,
        validationErrors: [error.message],
      }))
      return { success: false, error: error.message }
    }
  }

  /**
   * Mutation para enviar ao backend
   */
  const importMutation = useMutation({
    mutationFn: async ({
      items,
      duplicateStrategy,
    }: {
      items: ImportItemDTO[]
      duplicateStrategy: DuplicateStrategy
    }): Promise<ImportResultDTO> => {
      switch (entityType) {
        case 'bills':
          return billsService.bulkImport({
            items: items as BillImportItemDTO[],
            duplicateStrategy,
          })
        case 'invoices':
          return invoicesService.bulkImport({
            items: items as InvoiceImportItemDTO[],
            duplicateStrategy,
          })
        case 'creditCards':
          return creditCardsService.bulkImport({
            items: items as CreditCardImportItemDTO[],
            duplicateStrategy,
          })
        default:
          throw new Error('Tipo de entidade não suportado')
      }
    },
    onSuccess: () => {
      // Invalidar queries relevantes
      switch (entityType) {
        case 'bills':
          queryClient.invalidateQueries({ queryKey: ['bills'] })
          break
        case 'invoices':
          queryClient.invalidateQueries({ queryKey: ['invoices'] })
          break
        case 'creditCards':
          queryClient.invalidateQueries({ queryKey: ['creditCards'] })
          break
      }
    },
  })

  /**
   * Reset state
   */
  const reset = () => {
    setState({
      file: null,
      parsedData: [],
      transformedItems: [],
      isValidating: false,
      validationErrors: [],
    })
  }

  return {
    state,
    validateFile,
    importMutation,
    reset,
  }
}
