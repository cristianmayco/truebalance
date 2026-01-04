import { useState, useEffect } from 'react'
import { Search, X, Filter } from 'lucide-react'
import { Input } from '@/components/ui/Input'
import { Button } from '@/components/ui/Button'
import { Select } from '@/components/ui/Select'
import { useDebounce } from '@/hooks/useDebounce'
import { useCreditCards } from '@/hooks/useCreditCards'
import { useCategories } from '@/hooks/useCategories'
import type { BillFiltersDTO } from '@/types/dtos/bill.dto'

interface BillFiltersProps {
  onFilterChange: (filters: BillFiltersDTO) => void
  initialFilters?: BillFiltersDTO
}

export function BillFilters({ onFilterChange, initialFilters }: BillFiltersProps) {
  const [isExpanded, setIsExpanded] = useState(false)
  const [filters, setFilters] = useState<BillFiltersDTO>(initialFilters || {})
  const [searchTerm, setSearchTerm] = useState(filters.name || '')
  const { data: creditCards = [] } = useCreditCards()
  const { data: categories = [] } = useCategories()

  // Debounce search term to avoid excessive API calls
  const debouncedSearchTerm = useDebounce(searchTerm, 500)

  // Update filters when debounced search term changes
  useEffect(() => {
    if (debouncedSearchTerm !== filters.name) {
      const newFilters = { ...filters, name: debouncedSearchTerm || undefined }
      setFilters(newFilters)
      onFilterChange(newFilters)
    }
  }, [debouncedSearchTerm])

  const handleFilterChange = (key: keyof BillFiltersDTO, value: string | number | boolean | undefined) => {
    const newFilters = { ...filters, [key]: value === '' || value === null ? undefined : value }
    setFilters(newFilters)
    onFilterChange(newFilters)
  }

  const handleNumberFilterChange = (key: keyof BillFiltersDTO, value: string) => {
    const numValue = value === '' ? undefined : parseFloat(value)
    handleFilterChange(key, numValue)
  }

  const handleClear = () => {
    const clearedFilters: BillFiltersDTO = {}
    setFilters(clearedFilters)
    setSearchTerm('')
    onFilterChange(clearedFilters)
  }

  const hasActiveFilters = Boolean(
    filters.name || filters.startDate || filters.endDate || 
    filters.minAmount || filters.maxAmount || filters.numberOfInstallments ||
    filters.category || filters.creditCardId !== undefined || filters.hasCreditCard !== undefined
  )

  return (
    <div className="space-y-4">
      {/* Search bar - always visible */}
      <div className="flex gap-2">
        <div className="flex-1">
          <Input
            type="text"
            placeholder="Buscar por nome..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            icon={<Search className="w-4 h-4" />}
          />
        </div>
        <Button
          variant="secondary"
          onClick={() => setIsExpanded(!isExpanded)}
          aria-label={isExpanded ? 'Ocultar filtros' : 'Mostrar filtros'}
        >
          <Filter className="w-4 h-4" />
          {isExpanded ? 'Ocultar' : 'Filtros'}
        </Button>
      </div>

      {/* Advanced filters - collapsible */}
      {isExpanded && (
        <div className="p-4 bg-gray-50 dark:bg-gray-800 rounded-lg space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {/* Datas */}
            <Input
              type="date"
              label="Data início"
              value={filters.startDate || ''}
              onChange={(e) => handleFilterChange('startDate', e.target.value)}
            />
            <Input
              type="date"
              label="Data fim"
              value={filters.endDate || ''}
              onChange={(e) => handleFilterChange('endDate', e.target.value)}
            />

            {/* Valores */}
            <Input
              type="number"
              label="Valor mínimo"
              placeholder="0.00"
              step="0.01"
              min="0"
              value={filters.minAmount || ''}
              onChange={(e) => handleNumberFilterChange('minAmount', e.target.value)}
            />
            <Input
              type="number"
              label="Valor máximo"
              placeholder="0.00"
              step="0.01"
              min="0"
              value={filters.maxAmount || ''}
              onChange={(e) => handleNumberFilterChange('maxAmount', e.target.value)}
            />

            {/* Parcelas */}
            <Input
              type="number"
              label="Quantidade de parcelas"
              placeholder="Ex: 3"
              min="1"
              value={filters.numberOfInstallments || ''}
              onChange={(e) => handleFilterChange('numberOfInstallments', e.target.value === '' ? undefined : parseInt(e.target.value))}
            />

            {/* Categoria */}
            <Select
              label="Categoria"
              value={filters.category || ''}
              onChange={(e) => handleFilterChange('category', e.target.value)}
              options={[
                { value: '', label: 'Todas as categorias' },
                { value: '__NO_CATEGORY__', label: 'Sem categoria' },
                ...categories.map(cat => ({
                  value: cat.name,
                  label: cat.name
                }))
              ]}
            />

            {/* Filtro de cartão */}
            <Select
              label="Cartão de crédito"
              value={filters.hasCreditCard === undefined 
                ? (filters.creditCardId ? `card-${filters.creditCardId}` : 'all')
                : (filters.hasCreditCard ? 'with' : 'without')
              }
              onChange={(e) => {
                const value = e.target.value
                if (value === 'all') {
                  handleFilterChange('hasCreditCard', undefined)
                  handleFilterChange('creditCardId', undefined)
                } else if (value === 'with') {
                  handleFilterChange('hasCreditCard', true)
                  handleFilterChange('creditCardId', undefined)
                } else if (value === 'without') {
                  handleFilterChange('hasCreditCard', false)
                  handleFilterChange('creditCardId', undefined)
                } else if (value.startsWith('card-')) {
                  const cardId = parseInt(value.replace('card-', ''))
                  handleFilterChange('creditCardId', cardId)
                  handleFilterChange('hasCreditCard', undefined)
                }
              }}
              options={[
                { value: 'all', label: 'Todos' },
                { value: 'with', label: 'Com cartão' },
                { value: 'without', label: 'Sem cartão' },
                ...creditCards.map(card => ({
                  value: `card-${card.id}`,
                  label: card.name
                }))
              ]}
            />
          </div>

          {hasActiveFilters && (
            <div className="flex justify-end">
              <Button variant="ghost" size="sm" onClick={handleClear}>
                <X className="w-4 h-4" />
                Limpar filtros
              </Button>
            </div>
          )}
        </div>
      )}
    </div>
  )
}
