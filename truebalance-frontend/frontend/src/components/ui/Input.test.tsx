import { describe, it, expect } from 'vitest'
import { render, screen } from '@/test/test-utils'
import userEvent from '@testing-library/user-event'
import '@testing-library/jest-dom/vitest'
import { Input } from './Input'

describe('Input', () => {
  it('renders input field', () => {
    render(<Input />)
    expect(screen.getByRole('textbox')).toBeInTheDocument()
  })

  it('renders with label', () => {
    render(<Input label="Username" />)
    expect(screen.getByLabelText('Username')).toBeInTheDocument()
  })

  it('shows required indicator when required', () => {
    render(<Input label="Email" required />)
    expect(screen.getByText('*')).toBeInTheDocument()
    expect(screen.getByLabelText(/email/i)).toBeRequired()
  })

  it('accepts user input', async () => {
    const user = userEvent.setup()
    render(<Input label="Name" />)

    const input = screen.getByLabelText('Name')
    await user.type(input, 'John Doe')

    expect(input).toHaveValue('John Doe')
  })

  it('displays error message', () => {
    render(<Input label="Email" error="Invalid email" />)

    expect(screen.getByText('Invalid email')).toBeInTheDocument()
    expect(screen.getByRole('alert')).toBeInTheDocument()
  })

  it('sets aria-invalid when error exists', () => {
    render(<Input label="Email" error="Invalid email" />)

    const input = screen.getByLabelText('Email')
    expect(input).toHaveAttribute('aria-invalid', 'true')
  })

  it('does not set aria-invalid when no error', () => {
    render(<Input label="Email" />)

    const input = screen.getByLabelText('Email')
    expect(input).toHaveAttribute('aria-invalid', 'false')
  })

  it('links error message via aria-describedby', () => {
    render(<Input label="Email" error="Invalid email" />)

    const input = screen.getByLabelText('Email')
    const errorId = input.getAttribute('aria-describedby')
    const errorElement = screen.getByText('Invalid email')

    expect(errorId).toBeTruthy()
    expect(errorElement).toHaveAttribute('id', errorId!)
  })

  it('generates unique id when not provided', () => {
    render(
      <>
        <Input label="First" />
        <Input label="Second" />
      </>
    )

    const firstInput = screen.getByLabelText('First')
    const secondInput = screen.getByLabelText('Second')
    const firstId = firstInput.getAttribute('id')
    const secondId = secondInput.getAttribute('id')

    expect(firstId).toBeTruthy()
    expect(secondId).toBeTruthy()
    expect(firstId).not.toBe(secondId)
  })

  it('uses provided id', () => {
    render(<Input label="Custom" id="custom-id" />)

    const input = screen.getByLabelText('Custom')
    expect(input).toHaveAttribute('id', 'custom-id')
  })

  it('associates label with input via htmlFor', () => {
    render(<Input label="Test Label" />)

    const label = screen.getByText('Test Label')
    const input = screen.getByLabelText('Test Label')
    const inputId = input.getAttribute('id')

    expect(label).toHaveAttribute('for', inputId!)
  })

  it('applies error styles when error exists', () => {
    render(<Input label="Email" error="Invalid" />)

    const input = screen.getByLabelText('Email')
    expect(input).toHaveClass('border-error')
  })

  it('applies custom className', () => {
    render(<Input label="Test" className="custom-class" />)

    const input = screen.getByLabelText('Test')
    expect(input).toHaveClass('custom-class')
  })

  it('forwards ref correctly', () => {
    const ref = { current: null as HTMLInputElement | null }
    render(<Input label="Test" ref={ref} />)

    expect(ref.current).toBeInstanceOf(HTMLInputElement)
  })

  it('supports disabled state', () => {
    render(<Input label="Disabled" disabled />)

    const input = screen.getByLabelText('Disabled')
    expect(input).toBeDisabled()
    expect(input).toHaveClass('disabled:opacity-50', 'disabled:cursor-not-allowed')
  })

  it('supports placeholder', () => {
    render(<Input label="Email" placeholder="Enter your email" />)

    const input = screen.getByPlaceholderText('Enter your email')
    expect(input).toBeInTheDocument()
  })

  it('supports different input types', () => {
    render(<Input label="Password" type="password" />)

    const input = screen.getByLabelText('Password')
    expect(input).toHaveAttribute('type', 'password')
  })
})
