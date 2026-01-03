import { ButtonHTMLAttributes, ReactNode } from 'react'
import { Loader } from 'lucide-react'

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  children: ReactNode
  variant?: 'primary' | 'secondary' | 'ghost' | 'danger'
  size?: 'sm' | 'md' | 'lg'
  loading?: boolean
  iconLeft?: ReactNode
  iconRight?: ReactNode
}

export function Button({
  children,
  variant = 'primary',
  size = 'md',
  loading = false,
  iconLeft,
  iconRight,
  disabled,
  className = '',
  ...props
}: ButtonProps) {
  const baseStyles = 'inline-flex items-center justify-center gap-2 rounded-lg font-medium transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed'

  const sizeStyles = {
    sm: 'px-3 py-1.5 text-sm',
    md: 'px-4 py-2 text-base',
    lg: 'px-6 py-3 text-lg',
  }

  const variantStyles = {
    primary: 'bg-primary-600 hover:bg-primary-700 text-white shadow-sm hover:shadow-md',
    secondary: 'bg-gray-200 hover:bg-gray-300 dark:bg-slate-700 dark:hover:bg-slate-600 text-gray-800 dark:text-white',
    ghost: 'bg-transparent hover:bg-gray-100 dark:hover:bg-slate-800 text-gray-700 dark:text-slate-300',
    danger: 'bg-error hover:bg-error-dark text-white shadow-sm hover:shadow-md',
  }

  return (
    <button
      className={`${baseStyles} ${sizeStyles[size]} ${variantStyles[variant]} ${className}`}
      disabled={disabled || loading}
      aria-busy={loading}
      {...props}
    >
      {loading && <Loader className="w-4 h-4 animate-spin" aria-hidden="true" />}
      {!loading && iconLeft && iconLeft}
      {children}
      {!loading && iconRight && iconRight}
    </button>
  )
}

export default Button;
