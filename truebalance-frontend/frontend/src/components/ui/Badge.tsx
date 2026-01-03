import { ReactNode } from 'react'

interface BadgeProps {
  children: ReactNode
  variant?: 'success' | 'warning' | 'error' | 'info' | 'default'
  size?: 'sm' | 'md' | 'lg'
  icon?: ReactNode
  className?: string
}

export function Badge({
  children,
  variant = 'default',
  size = 'md',
  icon,
  className = ''
}: BadgeProps) {
  const sizeStyles = {
    sm: 'text-xs px-2 py-0.5',
    md: 'text-sm px-2.5 py-1',
    lg: 'text-base px-3 py-1.5',
  }

  const variantStyles = {
    success: 'bg-success-light/20 text-success-dark dark:bg-success/20 dark:text-success-light border border-success',
    warning: 'bg-warning-light/20 text-warning-dark dark:bg-warning/20 dark:text-warning-light border border-warning',
    error: 'bg-error-light/20 text-error-dark dark:bg-error/20 dark:text-error-light border border-error',
    info: 'bg-info-light/20 text-info-dark dark:bg-info/20 dark:text-info-light border border-info',
    default: 'bg-gray-100 text-gray-700 dark:bg-slate-700 dark:text-slate-300 border border-gray-300 dark:border-slate-600',
  }

  return (
    <span className={`inline-flex items-center gap-1 rounded-full font-medium ${sizeStyles[size]} ${variantStyles[variant]} ${className}`}>
      {icon && icon}
      {children}
    </span>
  )
}

export default Badge;
