import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@/test/test-utils'
import userEvent from '@testing-library/user-event'
import '@testing-library/jest-dom/vitest'
import { Button } from './Button'
import { Plus } from 'lucide-react'

describe('Button', () => {
  it('renders with children', () => {
    render(<Button>Click me</Button>)
    expect(screen.getByRole('button', { name: /click me/i })).toBeInTheDocument()
  })

  it('calls onClick when clicked', async () => {
    const handleClick = vi.fn()
    const user = userEvent.setup()

    render(<Button onClick={handleClick}>Click me</Button>)

    await user.click(screen.getByRole('button'))
    expect(handleClick).toHaveBeenCalledTimes(1)
  })

  it('renders with primary variant by default', () => {
    render(<Button>Primary</Button>)
    const button = screen.getByRole('button')
    expect(button).toHaveClass('bg-primary-600')
  })

  it('renders with secondary variant', () => {
    render(<Button variant="secondary">Secondary</Button>)
    const button = screen.getByRole('button')
    expect(button).toHaveClass('bg-gray-200')
  })

  it('renders with ghost variant', () => {
    render(<Button variant="ghost">Ghost</Button>)
    const button = screen.getByRole('button')
    expect(button).toHaveClass('bg-transparent')
  })

  it('renders with danger variant', () => {
    render(<Button variant="danger">Danger</Button>)
    const button = screen.getByRole('button')
    expect(button).toHaveClass('bg-error')
  })

  it('renders with different sizes', () => {
    const { rerender } = render(<Button size="sm">Small</Button>)
    expect(screen.getByRole('button')).toHaveClass('px-3', 'py-1.5', 'text-sm')

    rerender(<Button size="md">Medium</Button>)
    expect(screen.getByRole('button')).toHaveClass('px-4', 'py-2', 'text-base')

    rerender(<Button size="lg">Large</Button>)
    expect(screen.getByRole('button')).toHaveClass('px-6', 'py-3', 'text-lg')
  })

  it('shows loading state', () => {
    render(<Button loading>Loading</Button>)
    const button = screen.getByRole('button')

    expect(button).toBeDisabled()
    expect(button).toHaveAttribute('aria-busy', 'true')
  })

  it('does not call onClick when disabled', async () => {
    const handleClick = vi.fn()
    const user = userEvent.setup()

    render(<Button disabled onClick={handleClick}>Disabled</Button>)

    await user.click(screen.getByRole('button'))
    expect(handleClick).not.toHaveBeenCalled()
  })

  it('does not call onClick when loading', async () => {
    const handleClick = vi.fn()
    const user = userEvent.setup()

    render(<Button loading onClick={handleClick}>Loading</Button>)

    await user.click(screen.getByRole('button'))
    expect(handleClick).not.toHaveBeenCalled()
  })

  it('renders with left icon', () => {
    render(
      <Button iconLeft={<Plus data-testid="plus-icon" />}>
        Add Item
      </Button>
    )

    expect(screen.getByTestId('plus-icon')).toBeInTheDocument()
    expect(screen.getByText('Add Item')).toBeInTheDocument()
  })

  it('renders with right icon', () => {
    render(
      <Button iconRight={<Plus data-testid="plus-icon" />}>
        Add Item
      </Button>
    )

    expect(screen.getByTestId('plus-icon')).toBeInTheDocument()
    expect(screen.getByText('Add Item')).toBeInTheDocument()
  })

  it('hides icons when loading', () => {
    render(
      <Button loading iconLeft={<Plus data-testid="plus-icon" />}>
        Loading
      </Button>
    )

    expect(screen.queryByTestId('plus-icon')).not.toBeInTheDocument()
  })

  it('applies custom className', () => {
    render(<Button className="custom-class">Custom</Button>)
    expect(screen.getByRole('button')).toHaveClass('custom-class')
  })

  it('forwards other props to button element', () => {
    render(
      <Button data-testid="custom-button" type="submit">
        Submit
      </Button>
    )

    const button = screen.getByTestId('custom-button')
    expect(button).toHaveAttribute('type', 'submit')
  })
})
