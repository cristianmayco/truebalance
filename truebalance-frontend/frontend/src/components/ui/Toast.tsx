import { motion } from 'framer-motion';
import { CheckCircle, XCircle, AlertCircle, Info, X } from 'lucide-react';

export type ToastType = 'success' | 'error' | 'warning' | 'info';

interface ToastProps {
  type: ToastType;
  title?: string;
  message: string;
  onClose: () => void;
}

const icons = {
  success: CheckCircle,
  error: XCircle,
  warning: AlertCircle,
  info: Info,
};

const styles = {
  success: 'bg-green-50 border-green-500 text-green-800 dark:bg-green-900/20 dark:border-green-600 dark:text-green-200',
  error: 'bg-red-50 border-red-500 text-red-800 dark:bg-red-900/20 dark:border-red-600 dark:text-red-200',
  warning: 'bg-yellow-50 border-yellow-500 text-yellow-800 dark:bg-yellow-900/20 dark:border-yellow-600 dark:text-yellow-200',
  info: 'bg-blue-50 border-blue-500 text-blue-800 dark:bg-blue-900/20 dark:border-blue-600 dark:text-blue-200',
};

const iconStyles = {
  success: 'text-green-600 dark:text-green-400',
  error: 'text-red-600 dark:text-red-400',
  warning: 'text-yellow-600 dark:text-yellow-400',
  info: 'text-blue-600 dark:text-blue-400',
};

export function Toast({ type, title, message, onClose }: ToastProps) {
  const Icon = icons[type];
  const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  const variants = {
    initial: {
      opacity: prefersReducedMotion ? 1 : 0,
      y: prefersReducedMotion ? 0 : -50,
      scale: prefersReducedMotion ? 1 : 0.95,
    },
    animate: {
      opacity: 1,
      y: 0,
      scale: 1,
      transition: {
        duration: prefersReducedMotion ? 0.01 : 0.3,
        ease: [0.25, 0.1, 0.25, 1] as [number, number, number, number],
      },
    },
    exit: {
      opacity: prefersReducedMotion ? 1 : 0,
      x: prefersReducedMotion ? 0 : 300,
      transition: {
        duration: prefersReducedMotion ? 0.01 : 0.2,
        ease: 'easeIn' as const,
      },
    },
  };

  return (
    <motion.div
      variants={variants}
      initial="initial"
      animate="animate"
      exit="exit"
      className={`
        flex items-start gap-3 p-4 rounded-lg border-l-4 shadow-lg backdrop-blur-sm
        pointer-events-auto min-w-[320px] max-w-md
        ${styles[type]}
      `}
      role="alert"
      aria-live="polite"
    >
      <Icon className={`w-5 h-5 flex-shrink-0 mt-0.5 ${iconStyles[type]}`} aria-hidden="true" />

      <div className="flex-1 min-w-0">
        {title && typeof title === 'string' && <p className="text-sm font-semibold mb-1">{title}</p>}
        <p className="text-sm">{typeof message === 'string' ? message : String(message || '')}</p>
      </div>

      <button
        onClick={onClose}
        className="flex-shrink-0 hover:opacity-70 transition-opacity p-1 -mr-1 -mt-1"
        aria-label="Fechar notificação"
      >
        <X className="w-4 h-4" />
      </button>
    </motion.div>
  );
}
