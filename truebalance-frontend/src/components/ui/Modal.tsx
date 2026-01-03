import { ReactNode, useEffect, useRef } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X } from 'lucide-react';
import { Button } from './Button';

interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title?: string;
  children: ReactNode;
  footer?: ReactNode;
  size?: 'sm' | 'md' | 'lg' | 'xl' | 'full';
  closeOnOverlayClick?: boolean;
  closeOnEsc?: boolean;
  showCloseButton?: boolean;
}

/**
 * Modal Component
 *
 * Accessible modal dialog with smooth animations, focus trap, and keyboard support.
 * Respects user's reduced motion preferences.
 *
 * Features:
 * - Smooth slide-in animation from bottom
 * - Backdrop with fade animation
 * - Focus trap (Tab/Shift+Tab cycles within modal)
 * - Auto-focus on open
 * - Close on ESC key (optional)
 * - Close on overlay click (optional)
 * - Fullscreen on mobile
 * - ARIA attributes for accessibility
 *
 * @example
 * ```tsx
 * <Modal
 *   isOpen={isOpen}
 *   onClose={() => setIsOpen(false)}
 *   title="Confirm Action"
 *   footer={<Button onClick={handleConfirm}>Confirm</Button>}
 * >
 *   <p>Are you sure?</p>
 * </Modal>
 * ```
 */
export function Modal({
  isOpen,
  onClose,
  title,
  children,
  footer,
  size = 'md',
  closeOnOverlayClick = true,
  closeOnEsc = true,
  showCloseButton = true,
}: ModalProps) {
  const modalRef = useRef<HTMLDivElement>(null);
  const previousActiveElement = useRef<HTMLElement | null>(null);

  const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  // Store the element that had focus before modal opened
  useEffect(() => {
    if (isOpen) {
      previousActiveElement.current = document.activeElement as HTMLElement;
    }
  }, [isOpen]);

  // Focus trap and ESC key handler
  useEffect(() => {
    if (!isOpen) return;

    const handleKeyDown = (e: KeyboardEvent) => {
      // ESC key
      if (closeOnEsc && e.key === 'Escape') {
        onClose();
        return;
      }

      // Focus trap (Tab key)
      if (e.key === 'Tab' && modalRef.current) {
        const focusableElements = modalRef.current.querySelectorAll<HTMLElement>(
          'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
        );

        const firstElement = focusableElements[0];
        const lastElement = focusableElements[focusableElements.length - 1];

        if (e.shiftKey) {
          // Shift + Tab
          if (document.activeElement === firstElement) {
            e.preventDefault();
            lastElement?.focus();
          }
        } else {
          // Tab
          if (document.activeElement === lastElement) {
            e.preventDefault();
            firstElement?.focus();
          }
        }
      }
    };

    document.addEventListener('keydown', handleKeyDown);

    // Auto-focus first focusable element
    const focusableElements = modalRef.current?.querySelectorAll<HTMLElement>(
      'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
    );
    if (focusableElements && focusableElements.length > 0) {
      focusableElements[0]?.focus();
    }

    return () => {
      document.removeEventListener('keydown', handleKeyDown);
      // Restore focus to previous element
      previousActiveElement.current?.focus();
    };
  }, [isOpen, closeOnEsc, onClose]);

  // Prevent body scroll when modal is open
  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = '';
    }

    return () => {
      document.body.style.overflow = '';
    };
  }, [isOpen]);

  const sizeClasses = {
    sm: 'max-w-sm',
    md: 'max-w-md',
    lg: 'max-w-lg',
    xl: 'max-w-xl',
    full: 'max-w-full mx-4',
  };

  const backdropVariants = {
    hidden: { opacity: 0 },
    visible: {
      opacity: 1,
      transition: { duration: prefersReducedMotion ? 0.01 : 0.3 },
    },
    exit: {
      opacity: 0,
      transition: { duration: prefersReducedMotion ? 0.01 : 0.2 },
    },
  };

  const modalVariants = {
    hidden: {
      opacity: prefersReducedMotion ? 1 : 0,
      y: prefersReducedMotion ? 0 : 50,
      scale: prefersReducedMotion ? 1 : 0.95,
    },
    visible: {
      opacity: 1,
      y: 0,
      scale: 1,
      transition: {
        duration: prefersReducedMotion ? 0.01 : 0.4,
        ease: [0.25, 0.1, 0.25, 1],
      },
    },
    exit: {
      opacity: prefersReducedMotion ? 1 : 0,
      y: prefersReducedMotion ? 0 : 30,
      scale: prefersReducedMotion ? 1 : 0.95,
      transition: {
        duration: prefersReducedMotion ? 0.01 : 0.3,
        ease: 'easeIn',
      },
    },
  };

  return (
    <AnimatePresence>
      {isOpen && (
        <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center">
          {/* Backdrop */}
          <motion.div
            variants={backdropVariants}
            initial="hidden"
            animate="visible"
            exit="exit"
            className="absolute inset-0 bg-black/50 dark:bg-black/70"
            onClick={closeOnOverlayClick ? onClose : undefined}
            aria-hidden="true"
          />

          {/* Modal */}
          <motion.div
            ref={modalRef}
            variants={modalVariants}
            initial="hidden"
            animate="visible"
            exit="exit"
            role="dialog"
            aria-modal="true"
            aria-labelledby={title ? 'modal-title' : undefined}
            aria-label={title ? undefined : 'Modal'}
            className={`
              relative w-full ${sizeClasses[size]}
              bg-white dark:bg-gray-800
              rounded-t-2xl sm:rounded-2xl
              shadow-2xl
              max-h-[90vh] sm:max-h-[85vh]
              flex flex-col
              overflow-hidden
            `}
          >
            {/* Header */}
            {(title || showCloseButton) && (
              <div className="flex items-center justify-between px-6 py-4 border-b border-gray-200 dark:border-gray-700">
                {title && (
                  <h2
                    id="modal-title"
                    className="text-xl font-semibold text-gray-900 dark:text-gray-100"
                  >
                    {title}
                  </h2>
                )}
                {showCloseButton && (
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={onClose}
                    className="ml-auto -mr-2"
                    aria-label="Fechar modal"
                  >
                    <X className="h-5 w-5" />
                  </Button>
                )}
              </div>
            )}

            {/* Body */}
            <div className="flex-1 overflow-y-auto px-6 py-4">
              {children}
            </div>

            {/* Footer */}
            {footer && (
              <div className="px-6 py-4 border-t border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800/50">
                {footer}
              </div>
            )}
          </motion.div>
        </div>
      )}
    </AnimatePresence>
  );
}
