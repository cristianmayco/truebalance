import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { TrendingUp, TrendingDown, ChevronDown, ChevronUp, Calendar } from 'lucide-react';
import { reportsService } from '@/services/reports.service';
import Card from '@/components/ui/Card';
import LoadingSpinner from '@/components/ui/LoadingSpinner';

export default function ExpensesTimeline() {
  const currentYear = new Date().getFullYear();
  const [expandedMonths, setExpandedMonths] = useState<Set<string>>(new Set());

  const { data: monthlyExpenses, isLoading } = useQuery({
    queryKey: ['monthlyExpenses', currentYear],
    queryFn: () => reportsService.getMonthlyExpenses(currentYear),
  });

  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL',
    }).format(value);
  };

  const toggleMonth = (monthKey: string) => {
    const newExpanded = new Set(expandedMonths);
    if (newExpanded.has(monthKey)) {
      newExpanded.delete(monthKey);
    } else {
      newExpanded.add(monthKey);
    }
    setExpandedMonths(newExpanded);
  };

  const getTrendIcon = (current: number, previous: number | null) => {
    if (previous === null) return null;
    if (current > previous) return <TrendingUp className="w-4 h-4 text-red-500" />;
    if (current < previous) return <TrendingDown className="w-4 h-4 text-green-500" />;
    return null;
  };

  const getTrendPercentage = (current: number, previous: number | null) => {
    if (previous === null || previous === 0) return null;
    const change = ((current - previous) / previous) * 100;
    return change.toFixed(1);
  };

  if (isLoading) {
    return (
      <Card className="p-6">
        <div className="flex items-center justify-center min-h-[200px]">
          <LoadingSpinner size="md" />
        </div>
      </Card>
    );
  }

  if (!monthlyExpenses || monthlyExpenses.length === 0) {
    return (
      <Card className="p-6">
        <div className="flex items-center gap-3 mb-6">
          <div className="p-2 bg-blue-100 dark:bg-blue-900/30 rounded-lg">
            <Calendar className="w-5 h-5 text-blue-600 dark:text-blue-400" />
          </div>
          <h2 className="text-lg font-semibold text-gray-900 dark:text-white">
            Timeline de Gastos
          </h2>
        </div>
        <p className="text-center text-gray-500 dark:text-gray-400">
          Nenhum dado disponível para o ano atual
        </p>
      </Card>
    );
  }

  // Reverse to show most recent first
  const sortedExpenses = [...monthlyExpenses].reverse();

  return (
    <Card className="p-6">
      {/* Header */}
      <div className="flex items-center gap-3 mb-6">
        <div className="p-2 bg-blue-100 dark:bg-blue-900/30 rounded-lg">
          <Calendar className="w-5 h-5 text-blue-600 dark:text-blue-400" />
        </div>
        <h2 className="text-lg font-semibold text-gray-900 dark:text-white">Timeline de Gastos</h2>
      </div>

      {/* Timeline */}
      <div className="space-y-3">
        {sortedExpenses.map((expense, index) => {
          const monthKey = `${expense.year}-${expense.month}`;
          const isExpanded = expandedMonths.has(monthKey);
          const previousExpense = sortedExpenses[index + 1] || null;
          const trendIcon = previousExpense ? getTrendIcon(expense.total, previousExpense.total) : null;
          const trendPercentage = previousExpense ? getTrendPercentage(expense.total, previousExpense.total) : null;

          return (
            <div
              key={monthKey}
              className="border border-gray-200 dark:border-gray-700 rounded-lg overflow-hidden hover:border-violet-300 dark:hover:border-violet-600 transition-colors"
            >
              {/* Month Header */}
              <button
                onClick={() => toggleMonth(monthKey)}
                className="w-full px-4 py-3 flex items-center justify-between bg-gray-50 dark:bg-gray-800/50 hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
              >
                <div className="flex items-center gap-3">
                  <span className="text-sm font-semibold text-gray-900 dark:text-white capitalize">
                    {expense.month} {expense.year}
                  </span>

                  {trendIcon && (
                    <div className="flex items-center gap-1">
                      {trendIcon}
                      {trendPercentage && (
                        <span
                          className={`text-xs font-medium ${
                            parseFloat(trendPercentage) > 0
                              ? 'text-red-600 dark:text-red-400'
                              : 'text-green-600 dark:text-green-400'
                          }`}
                        >
                          {parseFloat(trendPercentage) > 0 ? '+' : ''}
                          {trendPercentage}%
                        </span>
                      )}
                    </div>
                  )}
                </div>

                <div className="flex items-center gap-3">
                  <span className="text-lg font-bold text-violet-600 dark:text-violet-400">
                    {formatCurrency(expense.total)}
                  </span>
                  {isExpanded ? (
                    <ChevronUp className="w-5 h-5 text-gray-400" />
                  ) : (
                    <ChevronDown className="w-5 h-5 text-gray-400" />
                  )}
                </div>
              </button>

              {/* Expanded Details */}
              {isExpanded && (
                <div className="px-4 py-3 space-y-2 bg-white dark:bg-gray-900">
                  <div className="flex justify-between items-center py-2">
                    <span className="text-sm text-gray-600 dark:text-gray-400">Contas</span>
                    <span className="text-sm font-semibold text-gray-900 dark:text-white">
                      {formatCurrency(expense.bills)}
                    </span>
                  </div>

                  <div className="flex justify-between items-center py-2 border-t border-gray-100 dark:border-gray-800">
                    <span className="text-sm text-gray-600 dark:text-gray-400">
                      Cartões de Crédito
                    </span>
                    <span className="text-sm font-semibold text-gray-900 dark:text-white">
                      {formatCurrency(expense.creditCards)}
                    </span>
                  </div>

                  <div className="flex justify-between items-center py-2 border-t-2 border-violet-200 dark:border-violet-800">
                    <span className="text-sm font-medium text-gray-900 dark:text-white">Total</span>
                    <span className="text-base font-bold text-violet-600 dark:text-violet-400">
                      {formatCurrency(expense.total)}
                    </span>
                  </div>

                  {/* Percentage breakdown */}
                  <div className="pt-2 mt-2 border-t border-gray-100 dark:border-gray-800">
                    <div className="grid grid-cols-2 gap-3">
                      <div className="text-center p-2 bg-violet-50 dark:bg-violet-900/20 rounded">
                        <p className="text-xs text-gray-600 dark:text-gray-400 mb-1">Contas</p>
                        <p className="text-sm font-semibold text-violet-600 dark:text-violet-400">
                          {expense.total > 0 ? ((expense.bills / expense.total) * 100).toFixed(1) : 0}%
                        </p>
                      </div>
                      <div className="text-center p-2 bg-purple-50 dark:bg-purple-900/20 rounded">
                        <p className="text-xs text-gray-600 dark:text-gray-400 mb-1">Cartões</p>
                        <p className="text-sm font-semibold text-purple-600 dark:text-purple-400">
                          {expense.total > 0 ? ((expense.creditCards / expense.total) * 100).toFixed(1) : 0}%
                        </p>
                      </div>
                    </div>
                  </div>
                </div>
              )}
            </div>
          );
        })}
      </div>

      {/* Summary */}
      <div className="mt-6 pt-4 border-t border-gray-200 dark:border-gray-700">
        <div className="grid grid-cols-2 gap-4">
          <div className="text-center p-3 bg-gray-50 dark:bg-gray-800 rounded-lg">
            <p className="text-xs text-gray-600 dark:text-gray-400 mb-1">Total no Ano</p>
            <p className="text-lg font-bold text-gray-900 dark:text-white">
              {formatCurrency(monthlyExpenses.reduce((sum, exp) => sum + exp.total, 0))}
            </p>
          </div>
          <div className="text-center p-3 bg-violet-50 dark:bg-violet-900/20 rounded-lg">
            <p className="text-xs text-gray-600 dark:text-gray-400 mb-1">Média Mensal</p>
            <p className="text-lg font-bold text-violet-600 dark:text-violet-400">
              {formatCurrency(
                monthlyExpenses.reduce((sum, exp) => sum + exp.total, 0) / monthlyExpenses.length
              )}
            </p>
          </div>
        </div>
      </div>
    </Card>
  );
}
