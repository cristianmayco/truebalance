import { describe, it, expect, beforeAll, vi } from 'vitest'
import {
  formatDate,
  formatRelative,
  isToday,
  isTomorrow,
  isYesterday,
  formatDateTime,
  toISOString,
  formatDateInput,
} from './date'

describe('date utils', () => {
  beforeAll(() => {
    // Set a fixed date for consistent testing
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2024-01-15T12:00:00Z'))
  })

  describe('formatDate', () => {
    it('formats ISO string to dd/MM/yyyy by default', () => {
      const result = formatDate('2024-01-15')
      expect(result).toBe('15/01/2024')
    })

    it('formats Date object to dd/MM/yyyy by default', () => {
      const date = new Date(2024, 11, 31) // Month is 0-indexed
      const result = formatDate(date)
      expect(result).toBe('31/12/2024')
    })

    it('supports custom format string', () => {
      const result = formatDate('2024-01-15', 'dd-MM-yyyy')
      expect(result).toBe('15-01-2024')
    })

    it('formats with month names', () => {
      const result = formatDate('2024-01-15', 'dd MMM yyyy')
      expect(result).toContain('jan')
    })
  })

  describe('formatRelative', () => {
    it('formats dates relative to now with suffix', () => {
      // Date in the past
      const pastDate = '2024-01-14T12:00:00Z'
      const result = formatRelative(pastDate)
      expect(result).toContain('1 dia')
    })

    it('works with Date objects', () => {
      const date = new Date('2024-01-14T12:00:00Z')
      const result = formatRelative(date)
      expect(result).toContain('dia')
    })
  })

  describe('isToday', () => {
    it('returns true for today\'s date', () => {
      const today = new Date(2024, 0, 15, 12, 0, 0) // Month is 0-indexed
      expect(isToday(today)).toBe(true)
    })

    it('returns false for yesterday', () => {
      const yesterday = new Date(2024, 0, 14, 12, 0, 0)
      expect(isToday(yesterday)).toBe(false)
    })

    it('works with ISO string', () => {
      const todayISO = '2024-01-15'
      expect(isToday(todayISO)).toBe(true)
    })
  })

  describe('isTomorrow', () => {
    it('returns true for tomorrow\'s date', () => {
      const tomorrow = new Date(2024, 0, 16, 12, 0, 0)
      expect(isTomorrow(tomorrow)).toBe(true)
    })

    it('returns false for today', () => {
      const today = new Date(2024, 0, 15, 12, 0, 0)
      expect(isTomorrow(today)).toBe(false)
    })

    it('works with ISO string', () => {
      const tomorrowISO = '2024-01-16'
      expect(isTomorrow(tomorrowISO)).toBe(true)
    })
  })

  describe('isYesterday', () => {
    it('returns true for yesterday\'s date', () => {
      const yesterday = new Date(2024, 0, 14, 12, 0, 0)
      expect(isYesterday(yesterday)).toBe(true)
    })

    it('returns false for today', () => {
      const today = new Date(2024, 0, 15, 12, 0, 0)
      expect(isYesterday(today)).toBe(false)
    })

    it('works with ISO string', () => {
      const yesterdayISO = '2024-01-14'
      expect(isYesterday(yesterdayISO)).toBe(true)
    })
  })

  describe('formatDateTime', () => {
    it('formats date and time correctly', () => {
      const result = formatDateTime('2024-01-15T14:30:00Z')
      expect(result).toContain('às')
      expect(result).toMatch(/\d{2}\/\d{2}\/\d{4}/)
    })

    it('works with Date objects', () => {
      const date = new Date('2024-01-15T14:30:00')
      const result = formatDateTime(date)
      expect(result).toContain('às')
    })
  })

  describe('toISOString', () => {
    it('converts Date to ISO string', () => {
      const date = new Date('2024-01-15T12:00:00Z')
      const result = toISOString(date)
      expect(result).toBe('2024-01-15T12:00:00.000Z')
    })

    it('preserves time information', () => {
      const date = new Date('2024-12-31T23:59:59Z')
      const result = toISOString(date)
      expect(result).toContain('T23:59:59')
    })
  })

  describe('formatDateInput', () => {
    it('formats for HTML date input (yyyy-MM-dd)', () => {
      const result = formatDateInput('2024-01-15')
      expect(result).toBe('2024-01-15')
    })

    it('works with Date objects', () => {
      const date = new Date(2024, 11, 31) // Month is 0-indexed
      const result = formatDateInput(date)
      expect(result).toBe('2024-12-31')
    })

    it('pads single digit months and days', () => {
      const result = formatDateInput('2024-01-05')
      expect(result).toBe('2024-01-05')
    })
  })
})
