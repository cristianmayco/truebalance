import { useMemo } from 'react'
import { formatCurrency } from '@/utils/currency'
import type { CategoryExpenseDTO } from '@/types/dtos/category.dto'

interface CategoryExpenseChartProps {
  expenses: CategoryExpenseDTO[]
  period: 'monthly' | 'yearly'
}

export function CategoryExpenseChart({ expenses, period }: CategoryExpenseChartProps) {
  const maxAmount = useMemo(() => {
    return Math.max(...expenses.map((e) => e.totalAmount), 0)
  }, [expenses])

  const formatPeriod = (dateString: string) => {
    const date = new Date(dateString)
    if (period === 'monthly') {
      return date.toLocaleDateString('pt-BR', { month: 'short' })
    } else {
      return date.getFullYear().toString()
    }
  }

  if (expenses.length === 0) {
    return (
      <div className="flex items-center justify-center h-64 text-gray-500 dark:text-gray-400">
        Nenhum dado disponível
      </div>
    )
  }

  return (
    <div className="space-y-4">
      {/* Gráfico de barras */}
      <div className="flex items-end gap-1 sm:gap-2 h-64 overflow-x-auto pb-4">
        {expenses.map((expense, index) => {
          const height = maxAmount > 0 ? (expense.totalAmount / maxAmount) * 100 : 0
          return (
            <div key={index} className="flex-1 min-w-[40px] sm:min-w-0 flex flex-col items-center group">
              <div className="w-full flex flex-col items-center justify-end h-full relative">
                <div
                  className="w-full bg-primary-500 dark:bg-primary-400 rounded-t transition-all hover:bg-primary-600 dark:hover:bg-primary-300 relative group cursor-pointer"
                  style={{ height: `${height}%`, minHeight: height > 0 ? '4px' : '0' }}
                >
                  {/* Tooltip */}
                  <div className="absolute bottom-full left-1/2 -translate-x-1/2 mb-2 opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none z-10">
                    <div className="bg-gray-900 dark:bg-gray-700 text-white text-xs rounded-lg py-2 px-3 whitespace-nowrap shadow-lg">
                      <div className="font-semibold mb-1">{formatPeriod(expense.period)}</div>
                      <div className="mb-1">{formatCurrency(expense.totalAmount)}</div>
                      <div className="text-gray-400 text-xs">
                        {expense.billCount} {expense.billCount === 1 ? 'conta' : 'contas'}
                      </div>
                      {/* Seta */}
                      <div className="absolute top-full left-1/2 -translate-x-1/2 -mt-1">
                        <div className="border-4 border-transparent border-t-gray-900 dark:border-t-gray-700" />
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <div className="mt-2 text-xs text-gray-600 dark:text-gray-400 text-center whitespace-nowrap">
                <span className="hidden sm:inline">{formatPeriod(expense.period)}</span>
                <span className="sm:hidden">{formatPeriod(expense.period).substring(0, 3)}</span>
              </div>
            </div>
          )
        })}
      </div>

      {/* Legenda */}
      <div className="flex items-center justify-center gap-4 text-sm text-gray-600 dark:text-gray-400">
        <div className="flex items-center gap-2">
          <div className="w-4 h-4 bg-primary-500 dark:bg-primary-400 rounded" />
          <span>Valor gasto no período</span>
        </div>
      </div>
    </div>
  )
}
