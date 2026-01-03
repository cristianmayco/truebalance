import { ReactNode } from 'react'

interface CardProps {
  children: ReactNode
  variant?: 'default' | 'gradient' | 'outlined'
  hover?: boolean
  className?: string
  onClick?: () => void
}

export function Card({
  children,
  variant = 'default',
  hover = false,
  className = '',
  onClick
}: CardProps) {
  const baseStyles = 'rounded-lg p-4 transition-all duration-200'

  const variantStyles = {
    default: 'bg-white dark:bg-slate-800 shadow-sm',
    gradient: 'bg-gradient-to-br from-primary-500 to-primary-700 text-white shadow-lg',
    outlined: 'bg-transparent border-2 border-gray-200 dark:border-slate-700',
  }

  const hoverStyles = hover ? 'hover:shadow-md hover:scale-[1.02] cursor-pointer' : ''

  return (
    <div
      className={`${baseStyles} ${variantStyles[variant]} ${hoverStyles} ${className}`}
      onClick={onClick}
    >
      {children}
    </div>
  )
}
