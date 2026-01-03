import { describe, it, expect } from 'vitest'
import { render, screen } from '@/test/test-utils'
import { LoadingSpinner } from './LoadingSpinner'

describe('LoadingSpinner', () => {
  it('renders spinner', () => {
    render(<LoadingSpinner />)
    expect(screen.getByRole('status')).toBeInTheDocument()
  })

  it('has default label "Carregando..."', () => {
    render(<LoadingSpinner />)
    expect(screen.getByText('Carregando...')).toBeInTheDocument()
  })

  it('accepts custom label', () => {
    render(<LoadingSpinner label="Processing..." />)
    expect(screen.getByText('Processing...')).toBeInTheDocument()
    expect(screen.queryByText('Carregando...')).not.toBeInTheDocument()
  })

  it('has aria-live polite', () => {
    render(<LoadingSpinner />)
    const status = screen.getByRole('status')
    expect(status).toHaveAttribute('aria-live', 'polite')
  })

  it('has aria-label', () => {
    render(<LoadingSpinner label="Loading data" />)
    const status = screen.getByRole('status')
    expect(status).toHaveAttribute('aria-label', 'Loading data')
  })

  it('renders medium size by default', () => {
    render(<LoadingSpinner />)
    const icon = screen.getByRole('status').querySelector('svg')
    expect(icon).toHaveClass('w-8', 'h-8')
  })

  it('renders small size', () => {
    render(<LoadingSpinner size="sm" />)
    const icon = screen.getByRole('status').querySelector('svg')
    expect(icon).toHaveClass('w-4', 'h-4')
  })

  it('renders large size', () => {
    render(<LoadingSpinner size="lg" />)
    const icon = screen.getByRole('status').querySelector('svg')
    expect(icon).toHaveClass('w-12', 'h-12')
  })

  it('spinner has animation', () => {
    render(<LoadingSpinner />)
    const icon = screen.getByRole('status').querySelector('svg')
    expect(icon).toHaveClass('animate-spin')
  })

  it('spinner is hidden from screen readers', () => {
    render(<LoadingSpinner />)
    const icon = screen.getByRole('status').querySelector('svg')
    expect(icon).toHaveAttribute('aria-hidden', 'true')
  })

  it('renders in fullScreen mode', () => {
    render(<LoadingSpinner fullScreen />)
    const container = screen.getByRole('status')
    expect(container).toHaveClass('fixed', 'inset-0', 'z-50')
  })

  it('fullScreen has backdrop blur', () => {
    render(<LoadingSpinner fullScreen />)
    const container = screen.getByRole('status')
    expect(container).toHaveClass('backdrop-blur-sm')
  })

  it('screen reader text is hidden visually but accessible', () => {
    render(<LoadingSpinner label="Loading" />)
    const srText = screen.getByText('Loading')
    expect(srText).toHaveClass('sr-only')
  })
})
