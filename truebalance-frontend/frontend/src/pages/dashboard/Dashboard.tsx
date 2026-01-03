import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { Plus, DollarSign, Calendar, TrendingUp, AlertCircle } from 'lucide-react'
import { AppShell } from '@/components/layout/AppShell'
import { Button } from '@/components/ui/Button'
import { Card } from '@/components/ui/Card'
import { LoadingSpinner } from '@/components/ui/LoadingSpinner'
import { EmptyState } from '@/components/ui/EmptyState'
import { Select } from '@/components/ui/Select'
import { UnifiedImportExport } from '@/components/ui/UnifiedImportExport'
import { formatCurrency } from '@/utils/currency'
import { reportsService } from '@/services/reports.service'

type PeriodFilter = '12' | '24' | '60' | '120'

// Helper para normalizar nomes de meses (remove acentos e converte para minúsculas)
function normalizeMonthName(month: string): string {
  return month
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
}

export function Dashboard() {
  const navigate = useNavigate()
  const [periodFilter, setPeriodFilter] = useState<PeriodFilter>('12')

  // Fetch monthly expenses based on selected period
  const {
    data: monthlyExpenses,
    isLoading: isLoadingExpenses,
    isError: isErrorExpenses,
    error: expensesError,
  } = useQuery({
    queryKey: ['monthlyExpenses', periodFilter],
    queryFn: () => reportsService.getMonthlyExpensesByPeriod(Number(periodFilter)),
    retry: 1,
  })

  const expenses = monthlyExpenses || []

  // Calculate summary metrics
  const totalAmount = expenses.reduce((sum, exp) => sum + exp.total, 0)
  const averageMonthly = expenses.length > 0 ? totalAmount / expenses.length : 0
  
  // Get current month expenses - usando comparação normalizada
  const currentDate = new Date()
  const currentMonth = normalizeMonthName(
    currentDate.toLocaleDateString('pt-BR', { month: 'long' })
  )
  const currentYear = currentDate.getFullYear()
  const currentMonthExpense = expenses.find(
    (exp) => normalizeMonthName(exp.month) === currentMonth && exp.year === currentYear
  )
  const currentMonthTotal = currentMonthExpense?.total || 0

  const handleNewBill = () => {
    navigate('/bills/new')
  }

  const handleViewAll = () => {
    navigate('/bills')
  }

  const periodOptions = [
    { value: '12', label: 'Últimos 12 meses' },
    { value: '24', label: 'Últimos 2 anos' },
    { value: '60', label: 'Últimos 5 anos' },
    { value: '120', label: 'Últimos 10 anos' },
  ]

  const isLoading = isLoadingExpenses

  if (isLoading) {
    return (
      <AppShell title="Dashboard">
        <LoadingSpinner fullScreen />
      </AppShell>
    )
  }

  // Tratamento de erro
  if (isErrorExpenses) {
    return (
      <AppShell title="Dashboard">
        <div className="space-y-6">
          <div>
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Dashboard</h1>
            <p className="text-gray-600 dark:text-gray-400 mt-1">
              Visão geral das suas finanças
            </p>
          </div>
          <Card>
            <div className="p-6 text-center">
              <AlertCircle className="w-12 h-12 text-red-500 mx-auto mb-4" />
              <h2 className="text-xl font-semibold text-gray-900 dark:text-white mb-2">
                Erro ao carregar dados
              </h2>
              <p className="text-gray-600 dark:text-gray-400 mb-4">
                Não foi possível carregar os dados do dashboard. Por favor, tente novamente.
              </p>
              {expensesError instanceof Error && (
                <p className="text-sm text-gray-500 dark:text-gray-500 mb-4">
                  {expensesError.message}
                </p>
              )}
              <Button
                onClick={() => window.location.reload()}
                variant="secondary"
              >
                Recarregar página
              </Button>
            </div>
          </Card>
        </div>
      </AppShell>
    )
  }

  return (
    <AppShell title="Dashboard">
      <div className="space-y-6">
        {/* Header */}
        <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
          <div>
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Dashboard</h1>
            <p className="text-gray-600 dark:text-gray-400 mt-1">
              Visão geral das suas finanças
            </p>
          </div>
          <div className="flex items-center gap-3 flex-wrap">
            <div className="w-48">
              <Select
                value={periodFilter}
                onChange={(e) => {
                  const value = e.target.value as PeriodFilter
                  setPeriodFilter(value)
                }}
                options={periodOptions}
              />
            </div>
            <UnifiedImportExport />
            <Button onClick={handleNewBill}>
              <Plus className="w-4 h-4" />
              Nova Conta
            </Button>
          </div>
        </div>

        {/* Summary Cards */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <Card>
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 rounded-full bg-violet-100 dark:bg-violet-900/20 flex items-center justify-center">
                <DollarSign className="w-6 h-6 text-violet-600 dark:text-violet-400" />
              </div>
              <div>
                <div className="text-sm text-gray-600 dark:text-gray-400">Total no Período</div>
                <div className="text-2xl font-bold text-gray-900 dark:text-white">
                  {formatCurrency(totalAmount)}
                </div>
              </div>
            </div>
          </Card>

          <Card>
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 rounded-full bg-blue-100 dark:bg-blue-900/20 flex items-center justify-center">
                <Calendar className="w-6 h-6 text-blue-600 dark:text-blue-400" />
              </div>
              <div>
                <div className="text-sm text-gray-600 dark:text-gray-400">Gastos do Mês</div>
                <div className="text-2xl font-bold text-gray-900 dark:text-white">
                  {formatCurrency(currentMonthTotal)}
                </div>
              </div>
            </div>
          </Card>

          <Card>
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 rounded-full bg-green-100 dark:bg-green-900/20 flex items-center justify-center">
                <TrendingUp className="w-6 h-6 text-green-600 dark:text-green-400" />
              </div>
              <div>
                <div className="text-sm text-gray-600 dark:text-gray-400">Média Mensal</div>
                <div className="text-2xl font-bold text-gray-900 dark:text-white">
                  {formatCurrency(averageMonthly)}
                </div>
              </div>
            </div>
          </Card>
        </div>

        {/* Monthly Cards */}
        <div>
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-xl font-semibold text-gray-900 dark:text-white">
              Gastos por Mês
            </h2>
            <Button variant="ghost" size="sm" onClick={handleViewAll}>
              Ver todas as contas
            </Button>
          </div>

          {expenses.length === 0 ? (
            <Card>
              <EmptyState
                icon={<Calendar className="w-12 h-12" />}
                message="Nenhum gasto encontrado"
                description={`Não há gastos registrados no período selecionado.`}
                actionLabel="Nova Conta"
                onAction={handleNewBill}
              />
            </Card>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {expenses
                .slice()
                .reverse()
                .map((expense) => {
                  const monthKey = `${expense.year}-${expense.month}`
                  const billsPercentage = expense.total > 0 
                    ? ((expense.bills / expense.total) * 100).toFixed(1) 
                    : '0'
                  const creditCardsPercentage = expense.total > 0 
                    ? ((expense.creditCards / expense.total) * 100).toFixed(1) 
                    : '0'

                  return (
                    <Card key={monthKey} className="hover:shadow-lg transition-shadow">
                      <div className="p-4">
                        {/* Month Header */}
                        <div className="flex items-center justify-between mb-4">
                          <div>
                            <h3 className="text-lg font-semibold text-gray-900 dark:text-white capitalize">
                              {expense.month}
                            </h3>
                            <p className="text-sm text-gray-600 dark:text-gray-400">
                              {expense.year}
                            </p>
                          </div>
                          <div className="w-10 h-10 rounded-full bg-violet-100 dark:bg-violet-900/20 flex items-center justify-center">
                            <Calendar className="w-5 h-5 text-violet-600 dark:text-violet-400" />
                          </div>
                        </div>

                        {/* Total Amount */}
                        <div className="mb-4">
                          <p className="text-2xl font-bold text-violet-600 dark:text-violet-400">
                            {formatCurrency(expense.total)}
                          </p>
                        </div>

                        {/* Breakdown */}
                        <div className="space-y-2 mb-4">
                          <div className="flex items-center justify-between text-sm">
                            <span className="text-gray-600 dark:text-gray-400">Contas</span>
                            <div className="flex items-center gap-2">
                              <span className="font-medium text-gray-900 dark:text-white">
                                {formatCurrency(expense.bills)}
                              </span>
                              <span className="text-xs text-gray-500 dark:text-gray-400">
                                ({billsPercentage}%)
                              </span>
                            </div>
                          </div>
                          <div className="flex items-center justify-between text-sm">
                            <span className="text-gray-600 dark:text-gray-400">Cartões</span>
                            <div className="flex items-center gap-2">
                              <span className="font-medium text-gray-900 dark:text-white">
                                {formatCurrency(expense.creditCards)}
                              </span>
                              <span className="text-xs text-gray-500 dark:text-gray-400">
                                ({creditCardsPercentage}%)
                              </span>
                            </div>
                          </div>
                        </div>

                        {/* Progress Bar */}
                        <div className="space-y-1">
                          <div className="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2">
                            <div
                              className="bg-violet-600 h-2 rounded-full"
                              style={{ width: `${billsPercentage}%` }}
                            />
                          </div>
                          <div className="flex justify-between text-xs text-gray-500 dark:text-gray-400">
                            <span>Contas</span>
                            <span>Cartões</span>
                          </div>
                        </div>
                      </div>
                    </Card>
                  )
                })}
            </div>
          )}
        </div>
      </div>
    </AppShell>
  )
}
