import { Loader } from 'lucide-react'

interface LoadingSpinnerProps {
  size?: 'sm' | 'md' | 'lg'
  fullScreen?: boolean
  label?: string
}

function LoadingSpinner({
  size = 'md',
  fullScreen = false,
  label = 'Carregando...'
}: LoadingSpinnerProps) {
  const sizeStyles = {
    sm: 'w-4 h-4',
    md: 'w-8 h-8',
    lg: 'w-12 h-12',
  }

  const spinner = (
    <Loader
      className={`${sizeStyles[size]} animate-spin text-primary-600 dark:text-primary-400`}
      aria-hidden="true"
    />
  )

  if (fullScreen) {
    return (
      <div
        className="fixed inset-0 z-50 flex items-center justify-center bg-white/80 dark:bg-slate-900/80 backdrop-blur-sm"
        role="status"
        aria-live="polite"
        aria-label={label}
      >
        {spinner}
        <span className="sr-only">{label}</span>
      </div>
    )
  }

  return (
    <div className="flex items-center justify-center" role="status" aria-live="polite" aria-label={label}>
      {spinner}
      <span className="sr-only">{label}</span>
    </div>
  )
}

export default LoadingSpinner;
export { LoadingSpinner };
