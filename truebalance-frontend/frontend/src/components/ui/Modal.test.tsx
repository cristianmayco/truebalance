import { describe, it, expect, vi } from 'vitest'
import { render, screen, waitFor } from '@/test/test-utils'
import userEvent from '@testing-library/user-event'
import { Modal } from './Modal'

describe('Modal', () => {
  it('does not render when isOpen is false', () => {
    render(
      <Modal isOpen={false} onClose={vi.fn()}>
        <p>Modal content</p>
      </Modal>
    )

    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
  })

  it('renders when isOpen is true', () => {
    render(
      <Modal isOpen={true} onClose={vi.fn()}>
        <p>Modal content</p>
      </Modal>
    )

    expect(screen.getByRole('dialog')).toBeInTheDocument()
  })

  it('renders children', () => {
    render(
      <Modal isOpen={true} onClose={vi.fn()}>
        <p>Test content</p>
      </Modal>
    )

    expect(screen.getByText('Test content')).toBeInTheDocument()
  })

  it('renders title when provided', () => {
    render(
      <Modal isOpen={true} onClose={vi.fn()} title="Test Title">
        <p>Content</p>
      </Modal>
    )

    expect(screen.getByText('Test Title')).toBeInTheDocument()
  })

  it('renders footer when provided', () => {
    render(
      <Modal isOpen={true} onClose={vi.fn()} footer={<button>Save</button>}>
        <p>Content</p>
      </Modal>
    )

    expect(screen.getByRole('button', { name: /save/i })).toBeInTheDocument()
  })

  it('has role="dialog"', () => {
    render(
      <Modal isOpen={true} onClose={vi.fn()}>
        <p>Content</p>
      </Modal>
    )

    expect(screen.getByRole('dialog')).toBeInTheDocument()
  })

  it('has aria-modal="true"', () => {
    render(
      <Modal isOpen={true} onClose={vi.fn()}>
        <p>Content</p>
      </Modal>
    )

    const dialog = screen.getByRole('dialog')
    expect(dialog).toHaveAttribute('aria-modal', 'true')
  })

  it('links title via aria-labelledby when title provided', () => {
    render(
      <Modal isOpen={true} onClose={vi.fn()} title="Modal Title">
        <p>Content</p>
      </Modal>
    )

    const dialog = screen.getByRole('dialog')
    const labelId = dialog.getAttribute('aria-labelledby')
    const title = screen.getByText('Modal Title')

    expect(labelId).toBeTruthy()
    expect(title).toHaveAttribute('id', labelId!)
  })

  it('has aria-label when no title provided', () => {
    render(
      <Modal isOpen={true} onClose={vi.fn()}>
        <p>Content</p>
      </Modal>
    )

    const dialog = screen.getByRole('dialog')
    expect(dialog).toHaveAttribute('aria-label', 'Modal')
  })

  it('renders close button with aria-label', () => {
    render(
      <Modal isOpen={true} onClose={vi.fn()} title="Test">
        <p>Content</p>
      </Modal>
    )

    const closeButton = screen.getByLabelText('Fechar modal')
    expect(closeButton).toBeInTheDocument()
  })

  it('calls onClose when close button is clicked', async () => {
    const handleClose = vi.fn()
    const user = userEvent.setup()

    render(
      <Modal isOpen={true} onClose={handleClose} title="Test">
        <p>Content</p>
      </Modal>
    )

    await user.click(screen.getByLabelText('Fechar modal'))
    expect(handleClose).toHaveBeenCalledTimes(1)
  })

  it('calls onClose when backdrop is clicked', async () => {
    const handleClose = vi.fn()
    const user = userEvent.setup()

    render(
      <Modal isOpen={true} onClose={handleClose}>
        <p>Content</p>
      </Modal>
    )

    const backdrop = screen.getByRole('dialog').previousSibling as HTMLElement
    await user.click(backdrop)
    expect(handleClose).toHaveBeenCalledTimes(1)
  })

  it('calls onClose when Escape key is pressed', async () => {
    const handleClose = vi.fn()
    const user = userEvent.setup()

    render(
      <Modal isOpen={true} onClose={handleClose}>
        <p>Content</p>
      </Modal>
    )

    await user.keyboard('{Escape}')
    expect(handleClose).toHaveBeenCalledTimes(1)
  })

  it('sets overflow hidden on body when open', () => {
    const { unmount } = render(
      <Modal isOpen={true} onClose={vi.fn()}>
        <p>Content</p>
      </Modal>
    )

    expect(document.body.style.overflow).toBe('hidden')

    unmount()
    expect(document.body.style.overflow).toBe('unset')
  })

  it('focuses modal when opened', async () => {
    render(
      <Modal isOpen={true} onClose={vi.fn()} title="Test">
        <p>Content</p>
      </Modal>
    )

    await waitFor(() => {
      const dialog = screen.getByRole('dialog')
      expect(document.activeElement).toBe(dialog)
    })
  })

  it('close button icon is hidden from screen readers', () => {
    render(
      <Modal isOpen={true} onClose={vi.fn()} title="Test">
        <p>Content</p>
      </Modal>
    )

    const closeButton = screen.getByLabelText('Fechar modal')
    const icon = closeButton.querySelector('svg')
    expect(icon).toHaveAttribute('aria-hidden', 'true')
  })

  it('backdrop is hidden from screen readers', () => {
    render(
      <Modal isOpen={true} onClose={vi.fn()}>
        <p>Content</p>
      </Modal>
    )

    const backdrop = screen.getByRole('dialog').previousSibling as HTMLElement
    expect(backdrop).toHaveAttribute('aria-hidden', 'true')
  })
})
