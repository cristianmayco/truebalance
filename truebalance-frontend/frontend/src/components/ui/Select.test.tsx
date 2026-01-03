import { describe, it, expect } from 'vitest'
import { render, screen } from '@/test/test-utils'
import userEvent from '@testing-library/user-event'
import { Select } from './Select'

const mockOptions = [
  { value: '1', label: 'Option 1' },
  { value: '2', label: 'Option 2' },
  { value: '3', label: 'Option 3' },
]

describe('Select', () => {
  it('renders select element', () => {
    render(<Select options={mockOptions} />)
    expect(screen.getByRole('combobox')).toBeInTheDocument()
  })

  it('renders with label', () => {
    render(<Select label="Choose option" options={mockOptions} />)
    expect(screen.getByLabelText('Choose option')).toBeInTheDocument()
  })

  it('renders all options', () => {
    render(<Select options={mockOptions} />)

    expect(screen.getByRole('option', { name: 'Option 1' })).toBeInTheDocument()
    expect(screen.getByRole('option', { name: 'Option 2' })).toBeInTheDocument()
    expect(screen.getByRole('option', { name: 'Option 3' })).toBeInTheDocument()
  })

  it('shows required indicator when required', () => {
    render(<Select label="Country" options={mockOptions} required />)
    expect(screen.getByText('*')).toBeInTheDocument()
    expect(screen.getByLabelText(/country/i)).toBeRequired()
  })

  it('allows selecting an option', async () => {
    const user = userEvent.setup()
    render(<Select label="Pick" options={mockOptions} />)

    const select = screen.getByLabelText('Pick')
    await user.selectOptions(select, '2')

    expect(select).toHaveValue('2')
  })

  it('displays error message', () => {
    render(<Select label="Status" options={mockOptions} error="Required field" />)

    expect(screen.getByText('Required field')).toBeInTheDocument()
    expect(screen.getByRole('alert')).toBeInTheDocument()
  })

  it('sets aria-invalid when error exists', () => {
    render(<Select label="Type" options={mockOptions} error="Invalid selection" />)

    const select = screen.getByLabelText('Type')
    expect(select).toHaveAttribute('aria-invalid', 'true')
  })

  it('does not set aria-invalid when no error', () => {
    render(<Select label="Type" options={mockOptions} />)

    const select = screen.getByLabelText('Type')
    expect(select).toHaveAttribute('aria-invalid', 'false')
  })

  it('links error message via aria-describedby', () => {
    render(<Select label="Category" options={mockOptions} error="Please select" />)

    const select = screen.getByLabelText('Category')
    const errorId = select.getAttribute('aria-describedby')
    const errorElement = screen.getByText('Please select')

    expect(errorId).toBeTruthy()
    expect(errorElement).toHaveAttribute('id', errorId!)
  })

  it('generates unique id when not provided', () => {
    render(
      <>
        <Select label="First" options={mockOptions} />
        <Select label="Second" options={mockOptions} />
      </>
    )

    const firstSelect = screen.getByLabelText('First')
    const secondSelect = screen.getByLabelText('Second')
    const firstId = firstSelect.getAttribute('id')
    const secondId = secondSelect.getAttribute('id')

    expect(firstId).toBeTruthy()
    expect(secondId).toBeTruthy()
    expect(firstId).not.toBe(secondId)
  })

  it('uses provided id', () => {
    render(<Select label="Custom" options={mockOptions} id="custom-select-id" />)

    const select = screen.getByLabelText('Custom')
    expect(select).toHaveAttribute('id', 'custom-select-id')
  })

  it('associates label with select via htmlFor', () => {
    render(<Select label="Test Select" options={mockOptions} />)

    const label = screen.getByText('Test Select')
    const select = screen.getByLabelText('Test Select')
    const selectId = select.getAttribute('id')

    expect(label).toHaveAttribute('for', selectId!)
  })

  it('applies error styles when error exists', () => {
    render(<Select label="Status" options={mockOptions} error="Invalid" />)

    const select = screen.getByLabelText('Status')
    expect(select).toHaveClass('border-error')
  })

  it('applies custom className', () => {
    render(<Select label="Test" options={mockOptions} className="custom-class" />)

    const select = screen.getByLabelText('Test')
    expect(select).toHaveClass('custom-class')
  })

  it('forwards ref correctly', () => {
    const ref = { current: null as HTMLSelectElement | null }
    render(<Select label="Test" options={mockOptions} ref={ref} />)

    expect(ref.current).toBeInstanceOf(HTMLSelectElement)
  })

  it('supports disabled state', () => {
    render(<Select label="Disabled" options={mockOptions} disabled />)

    const select = screen.getByLabelText('Disabled')
    expect(select).toBeDisabled()
    expect(select).toHaveClass('disabled:opacity-50', 'disabled:cursor-not-allowed')
  })

  it('handles numeric values', () => {
    const numericOptions = [
      { value: 1, label: 'One' },
      { value: 2, label: 'Two' },
    ]

    render(<Select label="Number" options={numericOptions} />)

    expect(screen.getByRole('option', { name: 'One' })).toHaveValue('1')
    expect(screen.getByRole('option', { name: 'Two' })).toHaveValue('2')
  })

  it('handles empty options array', () => {
    render(<Select label="Empty" options={[]} />)

    const select = screen.getByLabelText('Empty')
    const options = screen.queryAllByRole('option')

    expect(select).toBeInTheDocument()
    expect(options).toHaveLength(0)
  })
})
