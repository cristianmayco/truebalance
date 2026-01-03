import { cn } from '@/lib/utils'

interface ProgressBarProps {
  value: number // 0-100
  variant?: 'default' | 'success' | 'warning' | 'danger'
  label?: string
  showPercentage?: boolean
  size?: 'sm' | 'md' | 'lg'
  className?: string
}

const variantStyles = {
  default: 'bg-violet-600 dark:bg-violet-500',
  success: 'bg-green-600 dark:bg-green-500',
  warning: 'bg-yellow-600 dark:bg-yellow-500',
  danger: 'bg-red-600 dark:bg-red-500',
}

const sizeStyles = {
  sm: 'h-1',
  md: 'h-2',
  lg: 'h-3',
}

export function ProgressBar({
  value,
  variant = 'default',
  label,
  showPercentage = false,
  size = 'md',
  className,
}: ProgressBarProps) {
  const clampedValue = Math.min(Math.max(value, 0), 100)

  return (
    <div className={cn('w-full', className)}>
      {(label || showPercentage) && (
        <div className="flex items-center justify-between mb-2">
          {label && <span className="text-sm text-gray-700 dark:text-gray-300">{label}</span>}
          {showPercentage && (
            <span className="text-sm font-medium text-gray-900 dark:text-white">
              {clampedValue.toFixed(0)}%
            </span>
          )}
        </div>
      )}
      <div className="w-full bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden">
        <div
          className={cn(
            'transition-all duration-300 ease-in-out rounded-full',
            sizeStyles[size],
            variantStyles[variant]
          )}
          style={{ width: `${clampedValue}%` }}
          role="progressbar"
          aria-valuenow={clampedValue}
          aria-valuemin={0}
          aria-valuemax={100}
        />
      </div>
    </div>
  )
}

export default ProgressBar;
