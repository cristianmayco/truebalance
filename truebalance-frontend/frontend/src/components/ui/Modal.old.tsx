import { ReactNode, useEffect, useRef, useId } from 'react'
import { X } from 'lucide-react'

interface ModalProps {
  isOpen: boolean
  onClose: () => void
  title?: string
  children: ReactNode
  footer?: ReactNode
}

export function Modal({ isOpen, onClose, title, children, footer }: ModalProps) {
  const modalRef = useRef<HTMLDivElement>(null)
  const titleId = useId()

  useEffect(() => {
    const handleEscape = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose()
    }

    if (isOpen) {
      document.addEventListener('keydown', handleEscape)
      document.body.style.overflow = 'hidden'

      // Focus the modal when it opens
      const timeout = setTimeout(() => {
        modalRef.current?.focus()
      }, 100)

      return () => {
        clearTimeout(timeout)
        document.removeEventListener('keydown', handleEscape)
        document.body.style.overflow = 'unset'
      }
    }
  }, [isOpen, onClose])

  // Focus trap
  useEffect(() => {
    if (!isOpen || !modalRef.current) return

    const modal = modalRef.current
    const focusableElements = modal.querySelectorAll<HTMLElement>(
      'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
    )
    const firstElement = focusableElements[0]
    const lastElement = focusableElements[focusableElements.length - 1]

    const handleTabKey = (e: KeyboardEvent) => {
      if (e.key !== 'Tab') return

      if (e.shiftKey) {
        // Shift + Tab
        if (document.activeElement === firstElement) {
          e.preventDefault()
          lastElement?.focus()
        }
      } else {
        // Tab
        if (document.activeElement === lastElement) {
          e.preventDefault()
          firstElement?.focus()
        }
      }
    }

    modal.addEventListener('keydown', handleTabKey as any)
    return () => {
      modal.removeEventListener('keydown', handleTabKey as any)
    }
  }, [isOpen])

  if (!isOpen) return null

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      {/* Backdrop */}
      <div
        className="absolute inset-0 bg-black/50 backdrop-blur-sm"
        onClick={onClose}
        aria-hidden="true"
      />

      {/* Modal */}
      <div
        ref={modalRef}
        role="dialog"
        aria-modal="true"
        aria-labelledby={title ? titleId : undefined}
        aria-label={!title ? 'Modal' : undefined}
        tabIndex={-1}
        className="relative w-full max-w-lg bg-white dark:bg-slate-800 rounded-xl shadow-2xl max-h-[90vh] flex flex-col animate-in fade-in zoom-in duration-200 focus:outline-none"
      >
        {/* Header */}
        {title && (
          <div className="flex items-center justify-between p-6 border-b border-gray-200 dark:border-slate-700">
            <h3 id={titleId} className="text-xl font-semibold text-gray-900 dark:text-white">
              {title}
            </h3>
            <button
              onClick={onClose}
              aria-label="Fechar modal"
              className="p-1 rounded-lg hover:bg-gray-100 dark:hover:bg-slate-700 transition-colors"
            >
              <X className="w-5 h-5 text-gray-500 dark:text-slate-400" aria-hidden="true" />
            </button>
          </div>
        )}

        {/* Body */}
        <div className="flex-1 overflow-y-auto p-6">
          {children}
        </div>

        {/* Footer */}
        {footer && (
          <div className="p-6 border-t border-gray-200 dark:border-slate-700">
            {footer}
          </div>
        )}
      </div>
    </div>
  )
}
