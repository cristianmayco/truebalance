import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { ArrowLeft, TrendingUp, Calendar, DollarSign } from 'lucide-react'
import { AppShell } from '@/components/layout/AppShell'
import { useCategory } from '@/hooks/useCategories'
import { categoriesService } from '@/services/categories.service'
import { useQuery } from '@tanstack/react-query'
import { Button } from '@/components/ui/Button'
import { LoadingSpinner } from '@/components/ui/LoadingSpinner'
import { Card } from '@/components/ui/Card'
import { formatCurrency } from '@/utils/currency'
import { CategoryExpenseChart } from '@/components/categories/CategoryExpenseChart'

type PeriodType = 'monthly' | 'yearly'

export function CategoryDashboard() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const categoryId = id ? parseInt(id) : undefined
  const [period, setPeriod] = useState<PeriodType>('monthly')

  const { data: category, isLoading: isLoadingCategory } = useCategory(categoryId)
  const { data: expenses = [], isLoading: isLoadingExpenses } = useQuery({
    queryKey: ['categoryExpenses', categoryId, period],
    queryFn: () => categoriesService.getExpenses(categoryId!, period),
    enabled: !!categoryId,
  })

  if (isLoadingCategory || !category) {
    return (
      <AppShell title="Dashboard da Categoria">
        <LoadingSpinner fullScreen />
      </AppShell>
    )
  }

  // Calcular totais
  const totalAmount = expenses.reduce((sum, expense) => sum + expense.totalAmount, 0)
  const totalBills = expenses.reduce((sum, expense) => sum + expense.billCount, 0)
  const averageAmount = expenses.length > 0 ? totalAmount / expenses.length : 0

  // Formatar período para exibição
  const formatPeriod = (dateString: string) => {
    const date = new Date(dateString)
    if (period === 'monthly') {
      return date.toLocaleDateString('pt-BR', { month: 'short', year: 'numeric' })
    } else {
      return date.getFullYear().toString()
    }
  }

  return (
    <AppShell title={`Dashboard - ${category.name}`}>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center gap-4">
          <Button variant="ghost" onClick={() => navigate('/categories')} aria-label="Voltar">
            <ArrowLeft className="w-5 h-5" />
          </Button>
          <div className="flex-1">
            <div className="flex items-center gap-3">
              {category.color && (
                <div
                  className="w-6 h-6 rounded-full"
                  style={{ backgroundColor: category.color }}
                />
              )}
              <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
                {category.name}
              </h1>
            </div>
            {category.description && (
              <p className="text-gray-600 dark:text-gray-400 mt-1">{category.description}</p>
            )}
          </div>
        </div>

        {/* Filtros de período */}
        <Card className="p-4">
          <div className="flex flex-col sm:flex-row sm:items-center gap-4">
            <span className="text-sm font-medium text-gray-700 dark:text-slate-300">
              Visualização:
            </span>
            <div className="flex gap-2">
              <Button
                variant={period === 'monthly' ? 'primary' : 'secondary'}
                size="sm"
                onClick={() => setPeriod('monthly')}
                className="flex-1 sm:flex-none"
              >
                <Calendar className="w-4 h-4" />
                <span className="hidden sm:inline">Mensal (12 meses)</span>
                <span className="sm:hidden">Mensal</span>
              </Button>
              <Button
                variant={period === 'yearly' ? 'primary' : 'secondary'}
                size="sm"
                onClick={() => setPeriod('yearly')}
                className="flex-1 sm:flex-none"
              >
                <Calendar className="w-4 h-4" />
                <span className="hidden sm:inline">Anual (12 anos)</span>
                <span className="sm:hidden">Anual</span>
              </Button>
            </div>
          </div>
        </Card>

        {/* Cards de métricas */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <Card className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600 dark:text-gray-400">Total Gasto</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white mt-1">
                  {formatCurrency(totalAmount)}
                </p>
              </div>
              <div className="p-3 bg-primary-100 dark:bg-primary-900/30 rounded-lg">
                <DollarSign className="w-6 h-6 text-primary-600 dark:text-primary-400" />
              </div>
            </div>
          </Card>

          <Card className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600 dark:text-gray-400">Média por Período</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white mt-1">
                  {formatCurrency(averageAmount)}
                </p>
              </div>
              <div className="p-3 bg-green-100 dark:bg-green-900/30 rounded-lg">
                <TrendingUp className="w-6 h-6 text-green-600 dark:text-green-400" />
              </div>
            </div>
          </Card>

          <Card className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600 dark:text-gray-400">Total de Contas</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white mt-1">
                  {totalBills}
                </p>
              </div>
              <div className="p-3 bg-blue-100 dark:bg-blue-900/30 rounded-lg">
                <Calendar className="w-6 h-6 text-blue-600 dark:text-blue-400" />
              </div>
            </div>
          </Card>
        </div>

        {/* Gráfico */}
        {isLoadingExpenses ? (
          <Card className="p-12">
            <LoadingSpinner />
          </Card>
        ) : expenses.length > 0 ? (
          <Card className="p-6">
            <h2 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
              Evolução dos Gastos
            </h2>
            <CategoryExpenseChart expenses={expenses} period={period} />
          </Card>
        ) : (
          <Card className="p-12 text-center">
            <p className="text-gray-600 dark:text-gray-400">
              Nenhum gasto encontrado para esta categoria no período selecionado.
            </p>
          </Card>
        )}

        {/* Tabela de detalhes */}
        {expenses.length > 0 && (
          <Card className="p-4 sm:p-6">
            <h2 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
              Detalhamento por Período
            </h2>
            <div className="overflow-x-auto">
              <table className="w-full min-w-[400px]">
                <thead>
                  <tr className="border-b border-gray-200 dark:border-slate-700">
                    <th className="text-left py-3 px-4 text-sm font-medium text-gray-700 dark:text-slate-300">
                      Período
                    </th>
                    <th className="text-right py-3 px-4 text-sm font-medium text-gray-700 dark:text-slate-300">
                      Valor Total
                    </th>
                    <th className="text-right py-3 px-4 text-sm font-medium text-gray-700 dark:text-slate-300">
                      Quantidade
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {expenses.map((expense, index) => (
                    <tr
                      key={index}
                      className="border-b border-gray-100 dark:border-slate-800 hover:bg-gray-50 dark:hover:bg-slate-800 transition-colors"
                    >
                      <td className="py-3 px-4 text-sm text-gray-900 dark:text-white font-medium">
                        {formatPeriod(expense.period)}
                      </td>
                      <td className="py-3 px-4 text-sm text-right font-semibold text-gray-900 dark:text-white">
                        {formatCurrency(expense.totalAmount)}
                      </td>
                      <td className="py-3 px-4 text-sm text-right text-gray-600 dark:text-gray-400">
                        {expense.billCount} {expense.billCount === 1 ? 'conta' : 'contas'}
                      </td>
                    </tr>
                  ))}
                </tbody>
                <tfoot>
                  <tr className="border-t-2 border-gray-300 dark:border-slate-600 bg-gray-50 dark:bg-slate-800/50">
                    <td className="py-3 px-4 text-sm font-bold text-gray-900 dark:text-white">
                      Total
                    </td>
                    <td className="py-3 px-4 text-sm text-right font-bold text-gray-900 dark:text-white">
                      {formatCurrency(totalAmount)}
                    </td>
                    <td className="py-3 px-4 text-sm text-right font-bold text-gray-900 dark:text-white">
                      {totalBills} {totalBills === 1 ? 'conta' : 'contas'}
                    </td>
                  </tr>
                </tfoot>
              </table>
            </div>
          </Card>
        )}
      </div>
    </AppShell>
  )
}
