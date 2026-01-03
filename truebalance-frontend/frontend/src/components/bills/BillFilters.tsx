import { useState, useEffect } from 'react'
import { Search, X, Filter } from 'lucide-react'
import { Input } from '@/components/ui/Input'
import { Button } from '@/components/ui/Button'
import { useDebounce } from '@/hooks/useDebounce'
import type { BillFiltersDTO } from '@/types/dtos/bill.dto'

interface BillFiltersProps {
  onFilterChange: (filters: BillFiltersDTO) => void
  initialFilters?: BillFiltersDTO
}

export function BillFilters({ onFilterChange, initialFilters }: BillFiltersProps) {
  const [isExpanded, setIsExpanded] = useState(false)
  const [filters, setFilters] = useState<BillFiltersDTO>(initialFilters || {})
  const [searchTerm, setSearchTerm] = useState(filters.name || '')

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

  const handleFilterChange = (key: keyof BillFiltersDTO, value: string) => {
    const newFilters = { ...filters, [key]: value || undefined }
    setFilters(newFilters)
    onFilterChange(newFilters)
  }

  const handleClear = () => {
    const clearedFilters: BillFiltersDTO = {}
    setFilters(clearedFilters)
    setSearchTerm('')
    onFilterChange(clearedFilters)
  }

  const hasActiveFilters = Boolean(filters.name || filters.startDate || filters.endDate)

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
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
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
