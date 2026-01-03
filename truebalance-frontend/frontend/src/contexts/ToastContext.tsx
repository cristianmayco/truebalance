import { createContext, useContext, useState, useCallback, ReactNode } from 'react';
import { AnimatePresence } from 'framer-motion';
import { Toast } from '../components/ui/Toast';

type ToastType = 'success' | 'error' | 'warning' | 'info';

interface ToastMessage {
  id: string;
  type: ToastType;
  title?: string;
  message: string;
  duration?: number;
}

interface ToastContextType {
  showToast: (type: ToastType, message: string, title?: string, duration?: number) => void;
  success: (message: string, title?: string, duration?: number) => void;
  error: (message: string, title?: string, duration?: number) => void;
  warning: (message: string, title?: string, duration?: number) => void;
  info: (message: string, title?: string, duration?: number) => void;
  removeToast: (id: string) => void;
}

const ToastContext = createContext<ToastContextType | undefined>(undefined);

interface ToastProviderProps {
  children: ReactNode;
  maxToasts?: number;
}

export function ToastProvider({ children, maxToasts = 5 }: ToastProviderProps) {
  const [toasts, setToasts] = useState<ToastMessage[]>([]);

  const removeToast = useCallback((id: string) => {
    setToasts((prev) => prev.filter((toast) => toast.id !== id));
  }, []);

  const showToast = useCallback(
    (type: ToastType, message: string, title?: string, duration: number = 5000) => {
      const id = `toast-${Date.now()}-${Math.random()}`;

      const newToast: ToastMessage = {
        id,
        type,
        title,
        message,
        duration,
      };

      setToasts((prev) => {
        const updated = [...prev, newToast];
        if (updated.length > maxToasts) {
          return updated.slice(updated.length - maxToasts);
        }
        return updated;
      });

      if (duration > 0) {
        setTimeout(() => {
          removeToast(id);
        }, duration);
      }
    },
    [maxToasts, removeToast]
  );

  const success = useCallback(
    (message: string, title?: string, duration?: number) => {
      showToast('success', message, title, duration);
    },
    [showToast]
  );

  const error = useCallback(
    (message: string, title?: string, duration?: number) => {
      showToast('error', message, title, duration);
    },
    [showToast]
  );

  const warning = useCallback(
    (message: string, title?: string, duration?: number) => {
      showToast('warning', message, title, duration);
    },
    [showToast]
  );

  const info = useCallback(
    (message: string, title?: string, duration?: number) => {
      showToast('info', message, title, duration);
    },
    [showToast]
  );

  const value = {
    showToast,
    success,
    error,
    warning,
    info,
    removeToast,
  };

  return (
    <ToastContext.Provider value={value}>
      {children}
      <div
        className="fixed top-4 right-4 z-50 flex flex-col gap-2 pointer-events-none"
        aria-live="polite"
        aria-atomic="false"
      >
        <AnimatePresence>
          {toasts.map((toast) => (
            <Toast
              key={toast.id}
              type={toast.type}
              title={toast.title}
              message={toast.message}
              onClose={() => removeToast(toast.id)}
            />
          ))}
        </AnimatePresence>
      </div>
    </ToastContext.Provider>
  );
}

export function useToast() {
  const context = useContext(ToastContext);

  if (context === undefined) {
    throw new Error('useToast must be used within a ToastProvider');
  }

  return context;
}
