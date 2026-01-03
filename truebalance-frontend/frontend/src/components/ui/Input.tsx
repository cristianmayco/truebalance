import { forwardRef, InputHTMLAttributes, useId, ReactNode } from 'react'

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label?: string
  error?: string
  icon?: ReactNode
  helpText?: string
}

export const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ label, error, icon, helpText, className = '', id: providedId, ...props }, ref) => {
    const generatedId = useId()
    const inputId = providedId || generatedId
    const errorId = `${inputId}-error`
    const helpTextId = `${inputId}-help`

    return (
      <div className="w-full">
        {label && (
          <label
            htmlFor={inputId}
            className="block text-sm font-medium text-gray-700 dark:text-slate-300 mb-1"
          >
            {label}
            {props.required && <span className="text-error ml-1">*</span>}
          </label>
        )}
        <div className="relative">
          {icon && (
            <div className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 dark:text-slate-500 flex items-center">
              {icon}
            </div>
          )}
          <input
            ref={ref}
            id={inputId}
            aria-invalid={error ? 'true' : 'false'}
            aria-describedby={error ? errorId : helpText ? helpTextId : undefined}
            className={`
              w-full ${icon ? 'pl-10' : 'px-3'} py-2 rounded-lg border
              bg-white dark:bg-slate-800
              text-gray-900 dark:text-white
              placeholder-gray-400 dark:placeholder-slate-500
              focus:outline-none focus:ring-2 focus:ring-primary-500
              disabled:opacity-50 disabled:cursor-not-allowed
              ${
                error
                  ? 'border-error dark:border-error focus:ring-error'
                  : 'border-gray-300 dark:border-slate-600'
              }
              ${className}
            `}
            {...props}
          />
        </div>
        {helpText && !error && (
          <p id={helpTextId} className="mt-1 text-sm text-gray-500 dark:text-slate-400">
            {helpText}
          </p>
        )}
        {error && (
          <p id={errorId} className="mt-1 text-sm text-error" role="alert">
            {error}
          </p>
        )}
      </div>
    )
  }
)

Input.displayName = 'Input';

export default Input;
