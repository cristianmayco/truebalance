/**
 * Formata um valor numérico para o formato de moeda brasileira (BRL)
 * @param value - Valor numérico a ser formatado
 * @returns String formatada no padrão BRL (ex: R$ 1.234,56)
 */
export function formatCurrency(value: number): string {
  return new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL',
  }).format(value)
}

/**
 * Converte uma string de moeda formatada para número
 * @param value - String no formato de moeda (ex: "R$ 1.234,56" ou "1.234,56")
 * @returns Valor numérico
 */
export function parseCurrency(value: string): number {
  // Remove símbolos de moeda e espaços
  const cleaned = value.replace(/[R$\s]/g, '')

  // Substitui pontos de milhar e vírgula decimal
  const normalized = cleaned.replace(/\./g, '').replace(',', '.')

  return parseFloat(normalized) || 0
}

/**
 * Formata um valor para input de moeda (sem símbolo R$)
 * @param value - Valor numérico
 * @returns String formatada (ex: 1.234,56)
 */
export function formatCurrencyInput(value: number): string {
  return value.toLocaleString('pt-BR', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })
}
