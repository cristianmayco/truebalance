import * as XLSX from 'xlsx'
import { parse } from 'date-fns'
import { ptBR } from 'date-fns/locale'
import type {
  BillImportItemDTO,
  InvoiceImportItemDTO,
  CreditCardImportItemDTO,
} from '@/types/dtos/import.dto'

// Mapeamento de cabeçalhos (português → campos da API)
export const BILL_HEADERS_MAP: Record<string, string> = {
  ID: 'id', // Ignorar
  Nome: 'name',
  Descrição: 'description',
  Data: 'executionDate',
  'Valor Total': 'totalAmount',
  'Número de Parcelas': 'numberOfInstallments',
  'Valor da Parcela': 'installmentAmount', // Ignorar (calculado)
  'Criado em': 'createdAt', // Ignorar
  'Atualizado em': 'updatedAt', // Ignorar
}

export const INVOICE_HEADERS_MAP: Record<string, string> = {
  'ID Cartão': 'creditCardId',
  'Cartão de Crédito': 'creditCardId',
  'Mês de Referência': 'referenceMonth',
  'Valor Total': 'totalAmount',
  'Saldo Anterior': 'previousBalance',
  'Fechada': 'closed',
  'Paga': 'paid',
}

export const CREDIT_CARD_HEADERS_MAP: Record<string, string> = {
  Nome: 'name',
  'Limite de Crédito': 'creditLimit',
  'Limite': 'creditLimit',
  'Dia de Fechamento': 'closingDay',
  'Dia Fechamento': 'closingDay',
  'Dia de Vencimento': 'dueDay',
  'Dia Vencimento': 'dueDay',
  'Permite Pagamento Parcial': 'allowsPartialPayment',
  'Pagamento Parcial': 'allowsPartialPayment',
}

/**
 * Parse arquivo CSV/XLS usando biblioteca xlsx
 */
export const parseImportFile = async (file: File): Promise<any[]> => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()

    reader.onload = (e) => {
      try {
        const data = e.target?.result
        const workbook = XLSX.read(data, { type: 'binary' })
        const firstSheet = workbook.Sheets[workbook.SheetNames[0]]
        const jsonData = XLSX.utils.sheet_to_json(firstSheet, {
          raw: false,
          defval: '',
        })

        resolve(jsonData)
      } catch (error) {
        reject(new Error('Erro ao fazer parse do arquivo'))
      }
    }

    reader.onerror = () => {
      reject(new Error('Erro ao ler o arquivo'))
    }

    reader.readAsBinaryString(file)
  })
}

/**
 * Parse de datas em formato brasileiro (dd/MM/yyyy) ou ISO
 */
export const parseBrazilianDate = (dateStr: string): string | null => {
  if (!dateStr || dateStr.trim() === '') return null

  try {
    // Tentar formato brasileiro dd/MM/yyyy
    const brFormatRegex = /^(\d{1,2})\/(\d{1,2})\/(\d{4})$/
    if (brFormatRegex.test(dateStr)) {
      const parsed = parse(dateStr, 'dd/MM/yyyy', new Date(), { locale: ptBR })
      if (isNaN(parsed.getTime())) return null
      return parsed.toISOString()
    }

    // Tentar formato ISO
    const isoDate = new Date(dateStr)
    if (!isNaN(isoDate.getTime())) {
      return isoDate.toISOString()
    }

    return null
  } catch (error) {
    return null
  }
}

/**
 * Parse de valores monetários brasileiros (R$ 1.234,56)
 */
export const parseCurrencyValue = (value: string | number): number => {
  if (typeof value === 'number') return value

  if (!value || typeof value !== 'string') return NaN

  try {
    // Remove R$, espaços e pontos de milhar
    let cleanValue = value.replace(/R\$\s?/g, '').replace(/\s/g, '')

    // Troca vírgula por ponto
    cleanValue = cleanValue.replace(',', '.')

    // Remove pontos de milhar (assume que o último ponto é decimal)
    const parts = cleanValue.split('.')
    if (parts.length > 2) {
      // Múltiplos pontos - remover todos exceto o último
      const integerPart = parts.slice(0, -1).join('')
      const decimalPart = parts[parts.length - 1]
      cleanValue = `${integerPart}.${decimalPart}`
    }

    const parsed = parseFloat(cleanValue)
    return isNaN(parsed) ? NaN : parsed
  } catch (error) {
    return NaN
  }
}

/**
 * Validar e transformar linha do CSV para BillImportItem
 */
export const transformRowToBillImport = (
  row: any,
  lineNumber: number
): BillImportItemDTO => {
  // Mapear campos do CSV para o formato da API
  const name = row['Nome'] || row.name || ''
  const description = row['Descrição'] || row.description || undefined
  const dateStr = row['Data'] || row.date || row.executionDate || ''
  const totalAmountStr = row['Valor Total'] || row.totalAmount || '0'
  const numberOfInstallmentsStr =
    row['Número de Parcelas'] || row.numberOfInstallments || '1'

  // Parse e validação
  const executionDate = parseBrazilianDate(dateStr)
  const totalAmount = parseCurrencyValue(totalAmountStr)
  const numberOfInstallments = parseInt(numberOfInstallmentsStr)

  if (!executionDate) {
    throw new Error(
      `Linha ${lineNumber}: Data inválida. Use formato dd/MM/yyyy`
    )
  }

  if (isNaN(totalAmount) || totalAmount <= 0) {
    throw new Error(
      `Linha ${lineNumber}: Valor total inválido. Deve ser um número positivo`
    )
  }

  if (
    isNaN(numberOfInstallments) ||
    numberOfInstallments < 1 ||
    numberOfInstallments > 120
  ) {
    throw new Error(
      `Linha ${lineNumber}: Número de parcelas inválido. Deve ser entre 1 e 120`
    )
  }

  if (!name || name.trim().length < 3 || name.trim().length > 100) {
    throw new Error(
      `Linha ${lineNumber}: Nome inválido. Deve ter entre 3 e 100 caracteres`
    )
  }

  if (description && description.length > 500) {
    throw new Error(
      `Linha ${lineNumber}: Descrição muito longa. Máximo de 500 caracteres`
    )
  }

  return {
    name: name.trim(),
    description: description?.trim(),
    executionDate,
    totalAmount,
    numberOfInstallments,
    lineNumber,
  }
}

/**
 * Validar estrutura do arquivo importado para contas
 */
export const validateBillImportFileStructure = (
  data: any[]
): { valid: boolean; error?: string } => {
  if (!data || data.length === 0) {
    return { valid: false, error: 'Arquivo vazio ou sem dados válidos' }
  }

  const firstRow = data[0]
  const requiredHeaders = ['Nome', 'Data', 'Valor Total', 'Número de Parcelas']
  const availableHeaders = Object.keys(firstRow)

  const missingHeaders = requiredHeaders.filter(
    (header) => !availableHeaders.includes(header)
  )

  if (missingHeaders.length > 0) {
    return {
      valid: false,
      error: `Cabeçalhos obrigatórios faltando: ${missingHeaders.join(', ')}`,
    }
  }

  if (data.length > 1000) {
    return {
      valid: false,
      error: `Limite de 1000 registros excedido. Arquivo contém ${data.length} registros`,
    }
  }

  return { valid: true }
}

/**
 * Validar estrutura do arquivo importado para faturas
 */
export const validateInvoiceImportFileStructure = (
  data: any[]
): { valid: boolean; error?: string } => {
  if (!data || data.length === 0) {
    return { valid: false, error: 'Arquivo vazio ou sem dados válidos' }
  }

  const firstRow = data[0]
  const requiredHeaders = ['ID Cartão', 'Mês de Referência', 'Valor Total']
  const availableHeaders = Object.keys(firstRow)

  const missingHeaders = requiredHeaders.filter(
    (header) => !availableHeaders.includes(header)
  )

  if (missingHeaders.length > 0) {
    return {
      valid: false,
      error: `Cabeçalhos obrigatórios faltando: ${missingHeaders.join(', ')}`,
    }
  }

  if (data.length > 1000) {
    return {
      valid: false,
      error: `Limite de 1000 registros excedido. Arquivo contém ${data.length} registros`,
    }
  }

  return { valid: true }
}

/**
 * Validar estrutura do arquivo importado para cartões de crédito
 */
export const validateCreditCardImportFileStructure = (
  data: any[]
): { valid: boolean; error?: string } => {
  if (!data || data.length === 0) {
    return { valid: false, error: 'Arquivo vazio ou sem dados válidos' }
  }

  const firstRow = data[0]
  const requiredHeaders = ['Nome', 'Limite de Crédito', 'Dia de Fechamento', 'Dia de Vencimento']
  const availableHeaders = Object.keys(firstRow)

  const missingHeaders = requiredHeaders.filter(
    (header) => !availableHeaders.includes(header)
  )

  if (missingHeaders.length > 0) {
    return {
      valid: false,
      error: `Cabeçalhos obrigatórios faltando: ${missingHeaders.join(', ')}`,
    }
  }

  if (data.length > 1000) {
    return {
      valid: false,
      error: `Limite de 1000 registros excedido. Arquivo contém ${data.length} registros`,
    }
  }

  return { valid: true }
}

/**
 * Validar estrutura do arquivo importado (mantido para compatibilidade)
 */
export const validateImportFileStructure = validateBillImportFileStructure

/**
 * Validar tipo de arquivo
 */
export const validateFileType = (
  file: File
): { valid: boolean; error?: string } => {
  const extension = file.name.split('.').pop()?.toLowerCase()

  if (!extension || !['csv', 'xlsx', 'xls'].includes(extension)) {
    return {
      valid: false,
      error: 'Formato de arquivo inválido. Use CSV, XLS ou XLSX',
    }
  }

  // Validar tamanho (5MB max)
  const maxSize = 5 * 1024 * 1024 // 5MB
  if (file.size > maxSize) {
    return {
      valid: false,
      error: `Arquivo muito grande. Tamanho máximo: 5MB. Tamanho do arquivo: ${(file.size / 1024 / 1024).toFixed(2)}MB`,
    }
  }

  return { valid: true }
}

/**
 * Parse de mês de referência (MM/yyyy ou yyyy-MM)
 */
export const parseReferenceMonth = (monthStr: string): string | null => {
  if (!monthStr || monthStr.trim() === '') return null

  try {
    // Tentar formato MM/yyyy
    const brFormatRegex = /^(\d{1,2})\/(\d{4})$/
    if (brFormatRegex.test(monthStr)) {
      const [month, year] = monthStr.split('/')
      const date = new Date(parseInt(year), parseInt(month) - 1, 1)
      if (!isNaN(date.getTime())) {
        return date.toISOString().split('T')[0]
      }
    }

    // Tentar formato yyyy-MM
    const isoFormatRegex = /^(\d{4})-(\d{1,2})$/
    if (isoFormatRegex.test(monthStr)) {
      const [year, month] = monthStr.split('-')
      const date = new Date(parseInt(year), parseInt(month) - 1, 1)
      if (!isNaN(date.getTime())) {
        return date.toISOString().split('T')[0]
      }
    }

    return null
  } catch (error) {
    return null
  }
}

/**
 * Parse de valores booleanos
 */
export const parseBoolean = (value: string | boolean | number): boolean => {
  if (typeof value === 'boolean') return value
  if (typeof value === 'number') return value !== 0
  if (typeof value !== 'string') return false

  const lower = value.toLowerCase().trim()
  return lower === 'true' || lower === 'sim' || lower === 's' || lower === '1' || lower === 'yes' || lower === 'y'
}

/**
 * Validar e transformar linha do CSV para InvoiceImportItem
 */
export const transformRowToInvoiceImport = (
  row: any,
  lineNumber: number
): InvoiceImportItemDTO => {
  const creditCardIdStr = row['ID Cartão'] || row['Cartão de Crédito'] || row.creditCardId || ''
  const referenceMonthStr = row['Mês de Referência'] || row.referenceMonth || ''
  const totalAmountStr = row['Valor Total'] || row.totalAmount || '0'
  const previousBalanceStr = row['Saldo Anterior'] || row.previousBalance || '0'
  const closedStr = row['Fechada'] || row.closed || 'false'
  const paidStr = row['Paga'] || row.paid || 'false'

  const creditCardId = parseInt(creditCardIdStr)
  const referenceMonth = parseReferenceMonth(referenceMonthStr)
  const totalAmount = parseCurrencyValue(totalAmountStr)
  const previousBalance = parseCurrencyValue(previousBalanceStr)
  const closed = parseBoolean(closedStr)
  const paid = parseBoolean(paidStr)

  if (isNaN(creditCardId) || creditCardId <= 0) {
    throw new Error(`Linha ${lineNumber}: ID do cartão de crédito inválido`)
  }

  if (!referenceMonth) {
    throw new Error(`Linha ${lineNumber}: Mês de referência inválido. Use formato MM/yyyy ou yyyy-MM`)
  }

  if (isNaN(totalAmount) || totalAmount < 0) {
    throw new Error(`Linha ${lineNumber}: Valor total inválido. Deve ser um número positivo ou zero`)
  }

  if (isNaN(previousBalance) || previousBalance < 0) {
    throw new Error(`Linha ${lineNumber}: Saldo anterior inválido. Deve ser um número positivo ou zero`)
  }

  return {
    creditCardId,
    referenceMonth,
    totalAmount,
    previousBalance: previousBalance > 0 ? previousBalance : undefined,
    closed,
    paid,
    lineNumber,
  }
}

/**
 * Validar e transformar linha do CSV para CreditCardImportItem
 */
export const transformRowToCreditCardImport = (
  row: any,
  lineNumber: number
): CreditCardImportItemDTO => {
  const name = row['Nome'] || row.name || ''
  const creditLimitStr = row['Limite de Crédito'] || row['Limite'] || row.creditLimit || '0'
  const closingDayStr = row['Dia de Fechamento'] || row['Dia Fechamento'] || row.closingDay || ''
  const dueDayStr = row['Dia de Vencimento'] || row['Dia Vencimento'] || row.dueDay || ''
  const allowsPartialPaymentStr = row['Permite Pagamento Parcial'] || row['Pagamento Parcial'] || row.allowsPartialPayment || 'true'

  const creditLimit = parseCurrencyValue(creditLimitStr)
  const closingDay = parseInt(closingDayStr)
  const dueDay = parseInt(dueDayStr)
  const allowsPartialPayment = parseBoolean(allowsPartialPaymentStr)

  if (!name || name.trim().length < 3 || name.trim().length > 100) {
    throw new Error(`Linha ${lineNumber}: Nome inválido. Deve ter entre 3 e 100 caracteres`)
  }

  if (isNaN(creditLimit) || creditLimit <= 0) {
    throw new Error(`Linha ${lineNumber}: Limite de crédito inválido. Deve ser um número positivo`)
  }

  if (isNaN(closingDay) || closingDay < 1 || closingDay > 31) {
    throw new Error(`Linha ${lineNumber}: Dia de fechamento inválido. Deve ser entre 1 e 31`)
  }

  if (isNaN(dueDay) || dueDay < 1 || dueDay > 31) {
    throw new Error(`Linha ${lineNumber}: Dia de vencimento inválido. Deve ser entre 1 e 31`)
  }

  return {
    name: name.trim(),
    creditLimit,
    closingDay,
    dueDay,
    allowsPartialPayment,
    lineNumber,
  }
}
