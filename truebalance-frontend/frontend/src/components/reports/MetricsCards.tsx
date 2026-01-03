import { memo } from 'react';
import { DollarSign, TrendingUp, TrendingDown, Calendar, BarChart3 } from 'lucide-react';
import Card from '@/components/ui/Card';
import type { ExpenseMetrics } from '@/services/reports.service';

interface MetricsCardsProps {
  metrics: ExpenseMetrics;
}

// Move formatting functions outside component to avoid recreation
const formatCurrency = (value: number) => {
  return new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL',
  }).format(value);
};

const formatPercentage = (value: number) => {
  const sign = value >= 0 ? '+' : '';
  return `${sign}${value.toFixed(1)}%`;
};

const getPercentageColor = (value: number) => {
  if (value > 0) return 'text-red-600 dark:text-red-400';
  if (value < 0) return 'text-green-600 dark:text-green-400';
  return 'text-gray-600 dark:text-gray-400';
};

const getPercentageIcon = (value: number) => {
  if (value > 0) return TrendingUp;
  if (value < 0) return TrendingDown;
  return BarChart3;
};

function MetricsCards({ metrics }: MetricsCardsProps) {
  const PercentageIcon = getPercentageIcon(metrics.periodComparison.percentageChange);

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
      {/* Total Expenses */}
      <Card className="p-6 hover:shadow-lg transition-shadow">
        <div className="flex items-center justify-between mb-4">
          <div className="p-3 bg-violet-100 dark:bg-violet-900/30 rounded-lg">
            <DollarSign className="w-6 h-6 text-violet-600 dark:text-violet-400" />
          </div>
        </div>
        <h3 className="text-sm font-medium text-gray-600 dark:text-gray-400 mb-1">
          Total de Gastos
        </h3>
        <p className="text-2xl font-bold text-gray-900 dark:text-white">
          {formatCurrency(metrics.totalExpenses)}
        </p>
        <p className="text-xs text-gray-500 dark:text-gray-500 mt-2">No período selecionado</p>
      </Card>

      {/* Average Monthly */}
      <Card className="p-6 hover:shadow-lg transition-shadow">
        <div className="flex items-center justify-between mb-4">
          <div className="p-3 bg-blue-100 dark:bg-blue-900/30 rounded-lg">
            <Calendar className="w-6 h-6 text-blue-600 dark:text-blue-400" />
          </div>
        </div>
        <h3 className="text-sm font-medium text-gray-600 dark:text-gray-400 mb-1">Média Mensal</h3>
        <p className="text-2xl font-bold text-gray-900 dark:text-white">
          {formatCurrency(metrics.averageMonthly)}
        </p>
        <p className="text-xs text-gray-500 dark:text-gray-500 mt-2">Gasto médio por mês</p>
      </Card>

      {/* Period Comparison */}
      <Card className="p-6 hover:shadow-lg transition-shadow">
        <div className="flex items-center justify-between mb-4">
          <div
            className={`p-3 rounded-lg ${
              metrics.periodComparison.percentageChange > 0
                ? 'bg-red-100 dark:bg-red-900/30'
                : 'bg-green-100 dark:bg-green-900/30'
            }`}
          >
            <PercentageIcon
              className={`w-6 h-6 ${
                metrics.periodComparison.percentageChange > 0
                  ? 'text-red-600 dark:text-red-400'
                  : 'text-green-600 dark:text-green-400'
              }`}
            />
          </div>
        </div>
        <h3 className="text-sm font-medium text-gray-600 dark:text-gray-400 mb-1">
          Variação (6 meses)
        </h3>
        <div className="flex items-baseline gap-2">
          <p className="text-2xl font-bold text-gray-900 dark:text-white">
            {formatCurrency(metrics.periodComparison.current)}
          </p>
          <span className={`text-sm font-semibold ${getPercentageColor(metrics.periodComparison.percentageChange)}`}>
            {formatPercentage(metrics.periodComparison.percentageChange)}
          </span>
        </div>
        <p className="text-xs text-gray-500 dark:text-gray-500 mt-2">
          Comparado com os 6 meses anteriores
        </p>
      </Card>

      {/* Highest Month */}
      <Card className="p-6 hover:shadow-lg transition-shadow">
        <div className="flex items-center justify-between mb-4">
          <div className="p-3 bg-orange-100 dark:bg-orange-900/30 rounded-lg">
            <TrendingUp className="w-6 h-6 text-orange-600 dark:text-orange-400" />
          </div>
        </div>
        <h3 className="text-sm font-medium text-gray-600 dark:text-gray-400 mb-1">Mês Maior</h3>
        <p className="text-2xl font-bold text-gray-900 dark:text-white">
          {formatCurrency(metrics.highestMonth.amount)}
        </p>
        <p className="text-xs text-gray-500 dark:text-gray-500 mt-2 capitalize">
          {metrics.highestMonth.month}
        </p>
      </Card>

      {/* Additional Metrics Row */}
      <Card className="p-6 hover:shadow-lg transition-shadow md:col-span-2 lg:col-span-2">
        <div className="flex items-center justify-between mb-4">
          <div className="p-3 bg-green-100 dark:bg-green-900/30 rounded-lg">
            <TrendingDown className="w-6 h-6 text-green-600 dark:text-green-400" />
          </div>
        </div>
        <h3 className="text-sm font-medium text-gray-600 dark:text-gray-400 mb-1">Mês Menor</h3>
        <p className="text-2xl font-bold text-gray-900 dark:text-white">
          {formatCurrency(metrics.lowestMonth.amount)}
        </p>
        <p className="text-xs text-gray-500 dark:text-gray-500 mt-2 capitalize">
          {metrics.lowestMonth.month}
        </p>
      </Card>

      {/* Comparison Summary */}
      <Card className="p-6 hover:shadow-lg transition-shadow md:col-span-2 lg:col-span-2">
        <h3 className="text-sm font-medium text-gray-600 dark:text-gray-400 mb-4">
          Comparação de Períodos
        </h3>
        <div className="space-y-3">
          <div className="flex justify-between items-center p-3 bg-violet-50 dark:bg-violet-900/20 rounded-lg">
            <span className="text-sm text-gray-700 dark:text-gray-300">Últimos 6 meses</span>
            <span className="text-lg font-semibold text-violet-600 dark:text-violet-400">
              {formatCurrency(metrics.periodComparison.current)}
            </span>
          </div>
          <div className="flex justify-between items-center p-3 bg-gray-50 dark:bg-gray-800 rounded-lg">
            <span className="text-sm text-gray-700 dark:text-gray-300">6 meses anteriores</span>
            <span className="text-lg font-semibold text-gray-600 dark:text-gray-400">
              {formatCurrency(metrics.periodComparison.previous)}
            </span>
          </div>
          <div className="flex justify-between items-center p-3 bg-gray-50 dark:bg-gray-800 rounded-lg border-t-2 border-violet-600">
            <span className="text-sm font-medium text-gray-700 dark:text-gray-300">Diferença</span>
            <div className="flex items-center gap-2">
              <span className="text-lg font-bold text-gray-900 dark:text-white">
                {formatCurrency(metrics.periodComparison.current - metrics.periodComparison.previous)}
              </span>
              <span className={`text-sm font-semibold ${getPercentageColor(metrics.periodComparison.percentageChange)}`}>
                ({formatPercentage(metrics.periodComparison.percentageChange)})
              </span>
            </div>
          </div>
        </div>
      </Card>
    </div>
  );
}

// Export memoized version to prevent unnecessary re-renders
export default memo(MetricsCards);
