import { forwardRef, SelectHTMLAttributes, useId } from 'react'

interface SelectOption {
  value: string | number
  label: string
}

interface SelectProps extends SelectHTMLAttributes<HTMLSelectElement> {
  label?: string
  error?: string
  options: SelectOption[]
}

export const Select = forwardRef<HTMLSelectElement, SelectProps>(
  ({ label, error, options, className = '', id: providedId, ...props }, ref) => {
    const generatedId = useId()
    const selectId = providedId || generatedId
    const errorId = `${selectId}-error`

    return (
      <div className="w-full">
        {label && (
          <label
            htmlFor={selectId}
            className="block text-sm font-medium text-gray-700 dark:text-slate-300 mb-1"
          >
            {label}
            {props.required && <span className="text-error ml-1">*</span>}
          </label>
        )}
        <select
          ref={ref}
          id={selectId}
          aria-invalid={error ? 'true' : 'false'}
          aria-describedby={error ? errorId : undefined}
          className={`
            w-full px-3 py-2 rounded-lg border
            bg-white dark:bg-slate-800
            text-gray-900 dark:text-white
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
        >
          {options.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
        {error && (
          <p id={errorId} className="mt-1 text-sm text-error" role="alert">
            {error}
          </p>
        )}
      </div>
    )
  }
)

Select.displayName = 'Select';

export default Select;
