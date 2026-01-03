import {
  format as dateFnsFormat,
  formatDistanceToNow,
  isToday as dateFnsIsToday,
  isTomorrow as dateFnsIsTomorrow,
  isYesterday as dateFnsIsYesterday,
  parseISO,
} from 'date-fns'
import { ptBR } from 'date-fns/locale'

/**
 * Formata uma data conforme o formato especificado
 * @param date - Data a ser formatada (string ISO ou Date)
 * @param formatString - Formato desejado (padrão: dd/MM/yyyy)
 * @returns String formatada
 */
export function formatDate(date: string | Date, formatString: string = 'dd/MM/yyyy'): string {
  const dateObj = typeof date === 'string' ? parseISO(date) : date
  return dateFnsFormat(dateObj, formatString, { locale: ptBR })
}

/**
 * Formata uma data de forma relativa (ex: "há 2 dias", "em 3 horas")
 * @param date - Data a ser formatada (string ISO ou Date)
 * @returns String formatada
 */
export function formatRelative(date: string | Date): string {
  const dateObj = typeof date === 'string' ? parseISO(date) : date
  return formatDistanceToNow(dateObj, { addSuffix: true, locale: ptBR })
}

/**
 * Verifica se a data é hoje
 * @param date - Data a ser verificada (string ISO ou Date)
 * @returns Boolean
 */
export function isToday(date: string | Date): boolean {
  const dateObj = typeof date === 'string' ? parseISO(date) : date
  return dateFnsIsToday(dateObj)
}

/**
 * Verifica se a data é amanhã
 * @param date - Data a ser verificada (string ISO ou Date)
 * @returns Boolean
 */
export function isTomorrow(date: string | Date): boolean {
  const dateObj = typeof date === 'string' ? parseISO(date) : date
  return dateFnsIsTomorrow(dateObj)
}

/**
 * Verifica se a data é ontem
 * @param date - Data a ser verificada (string ISO ou Date)
 * @returns Boolean
 */
export function isYesterday(date: string | Date): boolean {
  const dateObj = typeof date === 'string' ? parseISO(date) : date
  return dateFnsIsYesterday(dateObj)
}

/**
 * Formata data e hora completa
 * @param date - Data a ser formatada (string ISO ou Date)
 * @returns String formatada (ex: 31/12/2023 às 14:30)
 */
export function formatDateTime(date: string | Date): string {
  const dateObj = typeof date === 'string' ? parseISO(date) : date
  return dateFnsFormat(dateObj, "dd/MM/yyyy 'às' HH:mm", { locale: ptBR })
}

/**
 * Converte uma data para formato ISO string para envio à API
 * @param date - Data a ser convertida
 * @returns String no formato ISO
 */
export function toISOString(date: Date): string {
  return date.toISOString()
}

/**
 * Formata data para input do tipo date (yyyy-MM-dd)
 * @param date - Data a ser formatada (string ISO ou Date)
 * @returns String formatada (ex: 2023-12-31)
 */
export function formatDateInput(date: string | Date): string {
  const dateObj = typeof date === 'string' ? parseISO(date) : date
  return dateFnsFormat(dateObj, 'yyyy-MM-dd')
}
