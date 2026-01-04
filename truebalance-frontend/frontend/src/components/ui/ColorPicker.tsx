import { useState, useEffect } from 'react'
import { Palette } from 'lucide-react'

// 30 cores pré-definidas em formato hex
const PRESET_COLORS = [
  '#FF6B6B', '#4ECDC4', '#45B7D1', '#FFA07A', '#98D8C8',
  '#F7DC6F', '#BB8FCE', '#85C1E2', '#F8B739', '#52BE80',
  '#E74C3C', '#3498DB', '#2ECC71', '#F39C12', '#9B59B6',
  '#1ABC9C', '#E67E22', '#34495E', '#16A085', '#27AE60',
  '#2980B9', '#8E44AD', '#C0392B', '#D35400', '#7F8C8D',
  '#95A5A6', '#BDC3C7', '#ECF0F1', '#34495E', '#2C3E50',
]

interface ColorPickerProps {
  value?: string | null
  onChange: (color: string | undefined) => void
  error?: string
}

export function ColorPicker({ value, onChange, error }: ColorPickerProps) {
  const [showCustom, setShowCustom] = useState(false)
  const [customColor, setCustomColor] = useState(value || '#000000')

  // Atualizar customColor quando value mudar externamente
  useEffect(() => {
    if (value && !PRESET_COLORS.includes(value)) {
      setCustomColor(value)
    }
  }, [value])

  const handlePresetColorClick = (color: string) => {
    onChange(color)
    setShowCustom(false)
  }

  const handleCustomColorChange = (color: string) => {
    setCustomColor(color)
    if (/^#[0-9A-Fa-f]{6}$/.test(color)) {
      onChange(color)
    }
  }

  const handleRemoveColor = () => {
    onChange(undefined)
    setShowCustom(false)
    setCustomColor('#000000')
  }

  const selectedColor = value || null

  return (
    <div className="space-y-3">
      <label className="block text-sm font-medium text-gray-700 dark:text-slate-300">
        Cor (opcional)
      </label>

      {/* Cores pré-definidas */}
      <div className="grid grid-cols-10 gap-2 p-2 bg-gray-50 dark:bg-slate-800 rounded-lg border border-gray-200 dark:border-slate-700">
        {PRESET_COLORS.map((color) => (
          <button
            key={color}
            type="button"
            onClick={() => handlePresetColorClick(color)}
            className={`
              w-10 h-10 rounded-lg border-2 transition-all relative
              hover:scale-110 hover:shadow-lg hover:z-10
              ${
                selectedColor === color
                  ? 'border-primary-600 dark:border-primary-400 ring-2 ring-primary-200 dark:ring-primary-800 shadow-md scale-110 z-10'
                  : 'border-gray-300 dark:border-slate-600 hover:border-gray-400 dark:hover:border-slate-500'
              }
            `}
            style={{ backgroundColor: color }}
            aria-label={`Selecionar cor ${color}`}
            title={color}
          >
            {selectedColor === color && (
              <div className="absolute inset-0 flex items-center justify-center">
                <svg
                  className="w-5 h-5 text-white drop-shadow-lg"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={3}
                    d="M5 13l4 4L19 7"
                  />
                </svg>
              </div>
            )}
          </button>
        ))}
      </div>

      {/* Opções adicionais */}
      <div className="flex items-center gap-2">
        {/* Botão para cor customizada */}
        <button
          type="button"
          onClick={() => setShowCustom(!showCustom)}
          className={`
            flex items-center gap-2 px-3 py-2 rounded-lg border-2 transition-all
            ${
              showCustom
                ? 'border-primary-600 dark:border-primary-400 bg-primary-50 dark:bg-primary-900/20'
                : 'border-gray-300 dark:border-slate-600 hover:border-gray-400 dark:hover:border-slate-500'
            }
          `}
        >
          <Palette className="w-4 h-4" />
          <span className="text-sm text-gray-700 dark:text-slate-300">
            Cor personalizada
          </span>
        </button>

        {/* Botão para remover cor */}
        {selectedColor && (
          <button
            type="button"
            onClick={handleRemoveColor}
            className="px-3 py-2 text-sm text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-900/20 rounded-lg transition-colors"
          >
            Remover cor
          </button>
        )}
      </div>

      {/* Seletor de cor customizada */}
      {showCustom && (
        <div className="flex items-center gap-3 p-3 bg-gray-50 dark:bg-slate-800 rounded-lg border border-gray-200 dark:border-slate-700">
          <input
            type="color"
            value={customColor}
            onChange={(e) => handleCustomColorChange(e.target.value)}
            className="w-12 h-12 rounded border border-gray-300 dark:border-slate-600 cursor-pointer"
          />
          <div className="flex-1">
            <input
              type="text"
              value={customColor}
              onChange={(e) => {
                const newColor = e.target.value.trim()
                setCustomColor(newColor)
                if (/^#[0-9A-Fa-f]{6}$/.test(newColor)) {
                  onChange(newColor)
                } else if (newColor === '') {
                  onChange(undefined)
                }
              }}
              placeholder="#000000"
              className="w-full px-3 py-2 border border-gray-300 dark:border-slate-600 rounded-lg bg-white dark:bg-slate-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-primary-500 focus:border-transparent"
            />
          </div>
          <div
            className="w-12 h-12 rounded border border-gray-300 dark:border-slate-600"
            style={{ backgroundColor: customColor }}
          />
        </div>
      )}

      {/* Cor selecionada (preview) */}
      {selectedColor && !showCustom && (
        <div className="flex items-center gap-2 p-2 bg-gray-50 dark:bg-slate-800 rounded-lg">
          <div
            className="w-8 h-8 rounded border border-gray-300 dark:border-slate-600"
            style={{ backgroundColor: selectedColor }}
          />
          <span className="text-sm text-gray-700 dark:text-slate-300 font-mono">
            {selectedColor}
          </span>
        </div>
      )}

      {error && (
        <p className="text-sm text-red-600 dark:text-red-400" role="alert">
          {error}
        </p>
      )}
    </div>
  )
}
