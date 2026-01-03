import React from 'react'

interface ButtonProps {
  onClick: () => void
  children: React.ReactNode
  variant?: 'primary' | 'secondary'
}

const Button: React.FC<ButtonProps> = ({
  onClick,
  children,
  variant = 'primary'
}) => {
  const baseStyles = 'px-4 py-2 rounded-lg font-semibold transition-colors duration-200'
  const variantStyles = {
    primary: 'bg-indigo-600 hover:bg-indigo-700 text-white',
    secondary: 'bg-gray-200 hover:bg-gray-300 text-gray-800'
  }

  return (
    <button
      onClick={onClick}
      className={`${baseStyles} ${variantStyles[variant]}`}
    >
      {children}
    </button>
  )
}

export default Button
