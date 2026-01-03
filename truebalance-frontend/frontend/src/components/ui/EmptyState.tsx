import { ReactNode } from 'react'
import { Button } from './Button'

interface EmptyStateProps {
  icon?: ReactNode
  message: string
  description?: string
  actionLabel?: string
  onAction?: () => void
}

export function EmptyState({
  icon,
  message,
  description,
  actionLabel,
  onAction
}: EmptyStateProps) {
  return (
    <div className="flex flex-col items-center justify-center py-12 px-4 text-center">
      {icon && (
        <div className="mb-4 text-gray-400 dark:text-slate-500">
          {icon}
        </div>
      )}
      <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-2">
        {message}
      </h3>
      {description && (
        <p className="text-sm text-gray-600 dark:text-slate-400 mb-6 max-w-md">
          {description}
        </p>
      )}
      {actionLabel && onAction && (
        <Button onClick={onAction}>
          {actionLabel}
        </Button>
      )}
    </div>
  )
}

export default EmptyState;
