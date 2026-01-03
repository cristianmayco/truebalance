import { ChevronLeft, ChevronRight } from 'lucide-react'
import { Button } from './Button'

interface PaginationProps {
  currentPage: number
  totalPages: number
  onPageChange: (page: number) => void
  disabled?: boolean
}

export function Pagination({ currentPage, totalPages, onPageChange, disabled }: PaginationProps) {
  const canGoPrevious = currentPage > 0
  const canGoNext = currentPage < totalPages - 1

  return (
    <div className="flex items-center justify-between gap-4 flex-wrap">
      <div className="text-sm text-gray-600 dark:text-gray-400">
        Página {currentPage + 1} de {totalPages}
      </div>

      <div className="flex items-center gap-2">
        <Button
          variant="secondary"
          size="sm"
          onClick={() => onPageChange(currentPage - 1)}
          disabled={!canGoPrevious || disabled}
          aria-label="Página anterior"
        >
          <ChevronLeft className="w-4 h-4" />
          Anterior
        </Button>

        <Button
          variant="secondary"
          size="sm"
          onClick={() => onPageChange(currentPage + 1)}
          disabled={!canGoNext || disabled}
          aria-label="Próxima página"
        >
          Próximo
          <ChevronRight className="w-4 h-4" />
        </Button>
      </div>
    </div>
  )
}
