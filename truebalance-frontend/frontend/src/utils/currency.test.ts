import { describe, it, expect } from 'vitest'
import { formatCurrency, parseCurrency, formatCurrencyInput } from './currency'

describe('currency utils', () => {
  describe('formatCurrency', () => {
    it('formats positive number to BRL currency', () => {
      const result = formatCurrency(1234.56)
      expect(result).toMatch(/R\$\s?1\.234,56/)
      expect(result).toContain('1.234,56')
    })

    it('formats zero correctly', () => {
      const result = formatCurrency(0)
      expect(result).toMatch(/R\$\s?0,00/)
    })

    it('formats negative number correctly', () => {
      const result = formatCurrency(-1234.56)
      expect(result).toContain('1.234,56')
      expect(result).toContain('-')
    })

    it('formats large numbers correctly', () => {
      const result = formatCurrency(1000000)
      expect(result).toMatch(/R\$\s?1\.000\.000,00/)
    })

    it('formats decimal numbers with proper precision', () => {
      expect(formatCurrency(99.99)).toMatch(/R\$\s?99,99/)
      expect(formatCurrency(100.5)).toMatch(/R\$\s?100,50/)
    })

    it('rounds to 2 decimal places', () => {
      expect(formatCurrency(10.999)).toMatch(/R\$\s?11,00/)
      expect(formatCurrency(10.994)).toMatch(/R\$\s?10,99/)
    })
  })

  describe('parseCurrency', () => {
    it('parses formatted currency string to number', () => {
      expect(parseCurrency('R$ 1.234,56')).toBe(1234.56)
    })

    it('parses string without R$ symbol', () => {
      expect(parseCurrency('1.234,56')).toBe(1234.56)
    })

    it('parses string with spaces', () => {
      expect(parseCurrency('R$ 1 234,56')).toBe(1234.56)
    })

    it('returns 0 for empty string', () => {
      expect(parseCurrency('')).toBe(0)
    })

    it('returns 0 for invalid input', () => {
      expect(parseCurrency('invalid')).toBe(0)
    })

    it('parses negative values', () => {
      expect(parseCurrency('-R$ 1.234,56')).toBe(-1234.56)
    })

    it('parses large numbers correctly', () => {
      expect(parseCurrency('R$ 1.000.000,00')).toBe(1000000)
    })

    it('handles decimal only values', () => {
      expect(parseCurrency('0,50')).toBe(0.5)
      expect(parseCurrency('R$ 0,99')).toBe(0.99)
    })
  })

  describe('formatCurrencyInput', () => {
    it('formats number for input without R$ symbol', () => {
      expect(formatCurrencyInput(1234.56)).toBe('1.234,56')
    })

    it('formats zero correctly', () => {
      expect(formatCurrencyInput(0)).toBe('0,00')
    })

    it('formats with exactly 2 decimal places', () => {
      expect(formatCurrencyInput(10)).toBe('10,00')
      expect(formatCurrencyInput(10.5)).toBe('10,50')
    })

    it('formats negative numbers', () => {
      expect(formatCurrencyInput(-1234.56)).toBe('-1.234,56')
    })

    it('uses Brazilian locale thousands separator', () => {
      expect(formatCurrencyInput(1000)).toBe('1.000,00')
      expect(formatCurrencyInput(1000000)).toBe('1.000.000,00')
    })
  })

  describe('round trip conversion', () => {
    it('formatCurrency -> parseCurrency returns original value', () => {
      const original = 1234.56
      const formatted = formatCurrency(original)
      const parsed = parseCurrency(formatted)
      expect(parsed).toBe(original)
    })

    it('formatCurrencyInput -> parseCurrency returns original value', () => {
      const original = 999.99
      const formatted = formatCurrencyInput(original)
      const parsed = parseCurrency(formatted)
      expect(parsed).toBe(original)
    })
  })
})
